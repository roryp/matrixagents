package com.matrixagents.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.annotation.PreDestroy;

import com.matrixagents.agents.ConditionalAgents;
import com.matrixagents.agents.DebateAgents;
import com.matrixagents.agents.GOAPAgents.AttractionFinder;
import com.matrixagents.agents.GOAPAgents.CityParser;
import com.matrixagents.agents.GOAPAgents.DistanceCalculator;
import com.matrixagents.agents.GOAPAgents.ItineraryPlanner;
import com.matrixagents.agents.GOAPAgents.RouteOptimizer;
import com.matrixagents.agents.HumanInLoopAgents.HoroscopeAgent;
import com.matrixagents.agents.HumanInLoopAgents.ZodiacExtractor;
import com.matrixagents.agents.LoopAgents.StyleScorer;
import com.matrixagents.agents.P2PAgents.CriticAgent;
import com.matrixagents.agents.P2PAgents.HypothesisAgent;
import com.matrixagents.agents.P2PAgents.LiteratureAgent;
import com.matrixagents.agents.P2PAgents.ScorerAgent;
import com.matrixagents.agents.P2PAgents.ValidationAgent;
import com.matrixagents.agents.ParallelAgents.EveningPlan;
import com.matrixagents.agents.ParallelAgents.EveningPlannerAgent;
import com.matrixagents.agents.ParallelMapperAgents;
import com.matrixagents.agents.SequenceAgents;
import com.matrixagents.agents.SequenceAgents.AudienceEditor;
import com.matrixagents.agents.SupervisorAgents.BankTool;
import com.matrixagents.agents.SupervisorAgents.CreditAgent;
import com.matrixagents.agents.SupervisorAgents.ExchangeAgent;
import com.matrixagents.agents.SupervisorAgents.ExchangeTool;
import com.matrixagents.agents.SupervisorAgents.WithdrawAgent;
import com.matrixagents.agents.VotingAgents;
import com.matrixagents.model.AgentEvent;
import com.matrixagents.model.ExecutionResult;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.agentic.patterns.debate.ConvergenceStrategy;
import dev.langchain4j.agentic.patterns.debate.DebatePlanner;
import dev.langchain4j.agentic.patterns.goap.GoalOrientedPlanner;
import dev.langchain4j.agentic.patterns.p2p.P2PPlanner;
import dev.langchain4j.agentic.patterns.voting.VotingPlanner;
import dev.langchain4j.agentic.patterns.voting.VotingStrategy;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Service that executes the 11 LangChain4j agentic patterns.
 * Uses the langchain4j-agentic module with proper AgenticServices.
 * Each pattern demonstrates a different workflow orchestration strategy.
 */
@ApplicationScoped
public class PatternExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PatternExecutionService.class);

    private final ChatModel chatModel;
    private final ChatModel plannerModel;
    private final EventPublisher eventPublisher;
    private final HumanInputService humanInputService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public PatternExecutionService(
            @Named("defaultChatModel") ChatModel chatModel,
            @Named("plannerModel") ChatModel plannerModel,
            EventPublisher eventPublisher,
            HumanInputService humanInputService) {
        this.chatModel = chatModel;
        this.plannerModel = plannerModel;
        this.eventPublisher = eventPublisher;
        this.humanInputService = humanInputService;
    }

    /**
     * Cleanup executor service on application shutdown.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down PatternExecutionService executor...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate gracefully, forcing shutdown...");
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            log.warn("Shutdown interrupted, forcing immediate shutdown");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public CompletableFuture<ExecutionResult> executePattern(String patternId, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            return switch (patternId) {
                case "sequence" -> executeSequence(prompt);
                case "parallel" -> executeParallel(prompt);
                case "parallel-mapper" -> executeParallelMapper(prompt);
                case "loop" -> executeLoop(prompt);
                case "conditional" -> executeConditional(prompt);
                case "supervisor" -> executeSupervisor(prompt);
                case "human-in-loop" -> executeHumanInLoop(prompt);
                case "goap" -> executeGOAP(prompt);
                case "p2p" -> executeP2P(prompt);
                case "debate" -> executeDebate(prompt);
                case "voting" -> executeVoting(prompt);
                default -> throw new IllegalArgumentException("Unknown pattern: " + patternId);
            };
        }, executor);
    }

    /**
     * SEQUENCE PATTERN: CreativeWriter -> AudienceEditor -> StyleEditor
     * Uses AgenticServices.sequenceBuilder() with AgentListener for proper 
     * chaining where each agent's output feeds into the next via AgenticScope.
     */
    private ExecutionResult executeSequence(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());

        try {
            events.add(publishEvent(AgentEvent.started("sequence", "Starting sequential workflow using AgenticServices.sequenceBuilder(): Writer → Audience Editor → Style Editor")));

            // Parse input: "topic" or "topic|audience|style"
            String topic = prompt;
            String audience = "young adults";
            String style = "fantasy adventure";

            if (prompt.contains("|")) {
                String[] parts = prompt.split("\\|");
                topic = parts[0].trim();
                if (parts.length > 1) audience = parts[1].trim();
                if (parts.length > 2) style = parts[2].trim();
            }

            // Create listener for real-time WebSocket events
            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "sequence", events);

            // Build agents using AgenticServices.agentBuilder() - proper LangChain4j way
            SequenceAgents.CreativeWriter writer = AgenticServices.agentBuilder(SequenceAgents.CreativeWriter.class)
                    .chatModel(chatModel)
                    .outputKey("story")
                    .build();

            AudienceEditor audienceEditor = AgenticServices.agentBuilder(AudienceEditor.class)
                    .chatModel(chatModel)
                    .outputKey("story")
                    .build();

            SequenceAgents.StyleEditor styleEditor = AgenticServices.agentBuilder(SequenceAgents.StyleEditor.class)
                    .chatModel(chatModel)
                    .outputKey("story")
                    .build();

            // Build sequence using AgenticServices.sequenceBuilder() with listener
            UntypedAgent novelCreator = AgenticServices.sequenceBuilder()
                    .name("novelCreator")
                    .subAgents(writer, audienceEditor, styleEditor)
                    .listener(listener)
                    .outputKey("story")
                    .build();

            // Execute the sequence - AgenticScope handles state passing automatically
            ResultWithAgenticScope<String> result = novelCreator.invokeWithAgenticScope(
                    Map.of("topic", topic, "audience", audience, "style", style));

            String finalStory = String.valueOf(result.result());
            Map<String, Object> scope = new ConcurrentHashMap<>(listener.getScopeSnapshot());
            scope.put("topic", topic);
            scope.put("audience", audience);
            scope.put("style", style);

            events.add(publishEvent(AgentEvent.completed("sequence", finalStory)));
            return ExecutionResult.success(executionId, "sequence", finalStory, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("sequence", null, e.getMessage())));
            return ExecutionResult.error(executionId, "sequence", e.getMessage(), events, startTime);
        }
    }

    /**
     * PARALLEL PATTERN: FoodExpert + MovieExpert run concurrently
     * Uses AgenticServices.parallelBuilder() for concurrent agent execution with result combination.
     */
    private ExecutionResult executeParallel(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("parallel", "Starting parallel workflow using AgenticServices: Food + Movie experts running concurrently")));

            String mood = prompt.isEmpty() ? "romantic" : prompt;
            scope.put("mood", mood);
            events.add(publishEvent(AgentEvent.stateUpdated("parallel", "mood", mood)));

            // Build parallel agents using createAgenticSystem for declarative API
            // EveningPlannerAgent uses @ParallelAgent annotation with subAgents declared
            EveningPlannerAgent planner = AgenticServices.createAgenticSystem(EveningPlannerAgent.class, chatModel);

            // Execute parallel agents - both FoodExpert and MovieExpert run concurrently
            events.add(publishEvent(AgentEvent.agentInvoked("parallel", "foodExpert", "Suggesting meals for " + mood + " mood...")));
            events.add(publishEvent(AgentEvent.agentInvoked("parallel", "movieExpert", "Recommending movies for " + mood + " mood...")));

            List<EveningPlan> plans = planner.plan(mood);

            // Log actual results from each expert
            StringBuilder meals = new StringBuilder();
            StringBuilder movies = new StringBuilder();
            for (int i = 0; i < plans.size(); i++) {
                EveningPlan plan = plans.get(i);
                if (i > 0) { meals.append(", "); movies.append(", "); }
                meals.append(plan.meal());
                movies.append(plan.movie());
            }
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "foodExpert", "Meals: " + meals)));
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "movieExpert", "Movies: " + movies)));

            // Format results
            StringBuilder result = new StringBuilder("## Evening Plans for " + mood + " mood:\n\n");
            for (int i = 0; i < plans.size(); i++) {
                EveningPlan plan = plans.get(i);
                result.append("**Plan ").append(i + 1).append(":**\n");
                result.append("- 🎬 Movie: ").append(plan.movie()).append("\n");
                result.append("- 🍽️ Meal: ").append(plan.meal()).append("\n\n");
            }
            
            String finalResult = result.toString();
            scope.put("plans", plans);
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "planCombiner", truncate(finalResult))));

            events.add(publishEvent(AgentEvent.completed("parallel", finalResult)));
            return ExecutionResult.success(executionId, "parallel", finalResult, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("parallel", null, e.getMessage())));
            return ExecutionResult.error(executionId, "parallel", e.getMessage(), events, startTime);
        }
    }

    /**
     * PARALLEL MAPPER PATTERN: fan a single agent out over a collection.
     */
    private ExecutionResult executeParallelMapper(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());

        try {
            events.add(publishEvent(AgentEvent.started("parallel-mapper", "Starting map-reduce workflow using AgenticServices.parallelMapperBuilder(): fanning one analyzer out over a batch of reviews")));

            List<String> reviews = new ArrayList<>();
            for (String part : prompt.split("\\r?\\n|;|\\|")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    reviews.add(trimmed);
                }
            }
            if (reviews.isEmpty()) {
                reviews.add(prompt.trim());
            }

            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "parallel-mapper", events);
            events.add(publishEvent(AgentEvent.stateUpdated("parallel-mapper", "reviewCount", String.valueOf(reviews.size()))));

            ParallelMapperAgents.ReviewAnalyzer analyzer = AgenticServices
                    .agentBuilder(ParallelMapperAgents.ReviewAnalyzer.class)
                    .chatModel(chatModel)
                    .outputKey("analysis")
                    .build();

            UntypedAgent batchAnalyzer = AgenticServices.parallelMapperBuilder()
                    .subAgents(analyzer)
                    .itemsProvider("reviews")
                    .outputKey("analyses")
                    .executor(executor)
                    .listener(listener)
                    .build();

            events.add(publishEvent(AgentEvent.agentInvoked("parallel-mapper", "parallelMapper", "Mapping ReviewAnalyzer over " + reviews.size() + " reviews concurrently...")));

            Object raw = batchAnalyzer.invoke(Map.of("reviews", reviews));
            List<Object> analyses = (raw instanceof List<?> list)
                    ? new ArrayList<>(list)
                    : new ArrayList<>(List.of(String.valueOf(raw)));

            StringBuilder report = new StringBuilder("## Batch Review Analysis (" + reviews.size() + " reviews)\n\n");
            for (int i = 0; i < reviews.size(); i++) {
                String analysis = i < analyses.size() ? String.valueOf(analyses.get(i)) : "(no analysis)";
                report.append("**").append(i + 1).append(".** _\"").append(truncate(reviews.get(i))).append("\"_\n\n");
                report.append("-> ").append(analysis.trim()).append("\n\n");
            }
            String finalReport = report.toString();

            Map<String, Object> scope = new ConcurrentHashMap<>(listener.getScopeSnapshot());
            scope.put("reviews", reviews);
            scope.put("analyses", analyses);

            events.add(publishEvent(AgentEvent.agentCompleted("parallel-mapper", "parallelMapper", analyses.size() + " analyses aggregated")));
            events.add(publishEvent(AgentEvent.completed("parallel-mapper", finalReport)));
            return ExecutionResult.success(executionId, "parallel-mapper", finalReport, events, scope, startTime);

        } catch (Exception e) {
            log.error("Parallel mapper execution failed", e);
            events.add(publishEvent(AgentEvent.error("parallel-mapper", null, e.getMessage())));
            return ExecutionResult.error(executionId, "parallel-mapper", e.getMessage(), events, startTime);
        }
    }

    /**
     * LOOP PATTERN: Generate -> Score -> Refine (repeat until threshold)
     * Uses AgenticServices.loopBuilder() with AgentListener for iterative 
     * refinement with exit conditions based on AgenticScope state.
     */
    private ExecutionResult executeLoop(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());

        try {
            events.add(publishEvent(AgentEvent.started("loop", "Starting loop workflow using AgenticServices.loopBuilder(): Generate → Score → Refine (until score ≥ 0.8)")));

            // Parse input
            String topic = prompt;
            String style = "comedy";
            if (prompt.contains("|")) {
                String[] parts = prompt.split("\\|");
                topic = parts[0].trim();
                if (parts.length > 1) style = parts[1].trim();
            }

            // Create listener for real-time WebSocket events
            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "loop", events);

            // Build agents using AgenticServices.agentBuilder() - proper LangChain4j way
            com.matrixagents.agents.LoopAgents.CreativeWriter generator = AgenticServices
                    .agentBuilder(com.matrixagents.agents.LoopAgents.CreativeWriter.class)
                    .chatModel(chatModel)
                    .outputKey("story")
                    .build();

            StyleScorer scorer = AgenticServices.agentBuilder(StyleScorer.class)
                    .chatModel(chatModel)
                    .outputKey("score")
                    .build();

            com.matrixagents.agents.LoopAgents.StyleEditor refiner = AgenticServices
                    .agentBuilder(com.matrixagents.agents.LoopAgents.StyleEditor.class)
                    .chatModel(chatModel)
                    .outputKey("story")
                    .build();

            // Build loop agent for score->refine cycle
            UntypedAgent styleReviewLoop = AgenticServices.loopBuilder()
                    .name("styleReviewLoop")
                    .subAgents(scorer, refiner)
                    .maxIterations(5)
                    .exitCondition(scope -> scope.readState("score", 0.0) >= 0.8)
                    .build();

            // Build sequence: generate first, then loop score->refine
            UntypedAgent styledWriter = AgenticServices.sequenceBuilder()
                    .name("styledWriter")
                    .subAgents(generator, styleReviewLoop)
                    .listener(listener)
                    .outputKey("story")
                    .build();

            // Execute the workflow - AgenticScope handles all state automatically
            ResultWithAgenticScope<String> result = styledWriter.invokeWithAgenticScope(
                    Map.of("topic", topic, "style", style));

            String finalStory = String.valueOf(result.result());
            
            // Get scope state from listener and AgenticScope
            Map<String, Object> scope = new ConcurrentHashMap<>(listener.getScopeSnapshot());
            scope.put("topic", topic);
            scope.put("style", style);
            
            // Read final score from AgenticScope
            Double finalScore = result.agenticScope().readState("score", 0.0);
            scope.put("finalScore", finalScore);
            scope.put("finalStory", finalStory);

            String output = String.format("**Final Story** (Score: %.2f)\n\n%s", finalScore, finalStory);
            events.add(publishEvent(AgentEvent.completed("loop", output)));
            return ExecutionResult.success(executionId, "loop", output, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("loop", null, e.getMessage())));
            return ExecutionResult.error(executionId, "loop", e.getMessage(), events, startTime);
        }
    }

    /**
     * CONDITIONAL PATTERN: Router -> Expert activation based on category
     * Uses AgenticServices.conditionalBuilder() with lambda-based predicates for routing,
     * composed with CategoryRouter via AgenticServices.sequenceBuilder().
     * The AgenticScope propagates the "category" state from router to conditional predicates.
     */
    private ExecutionResult executeConditional(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());

        try {
            events.add(publishEvent(AgentEvent.started("conditional", "Starting conditional workflow using AgenticServices.conditionalBuilder(): CategoryRouter → Expert routing")));

            // Create listener for real-time WebSocket events
            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "conditional", events);

            // Step 1: Build CategoryRouter agent
            ConditionalAgents.CategoryRouter routerAgent = AgenticServices
                    .agentBuilder(ConditionalAgents.CategoryRouter.class)
                    .chatModel(chatModel)
                    .outputKey("category")
                    .build();

            // Step 2: Build expert agents
            ConditionalAgents.MedicalExpert medicalExpert = AgenticServices
                    .agentBuilder(ConditionalAgents.MedicalExpert.class)
                    .chatModel(chatModel)
                    .outputKey("response")
                    .build();
            ConditionalAgents.LegalExpert legalExpert = AgenticServices
                    .agentBuilder(ConditionalAgents.LegalExpert.class)
                    .chatModel(chatModel)
                    .outputKey("response")
                    .build();
            ConditionalAgents.TechnicalExpert technicalExpert = AgenticServices
                    .agentBuilder(ConditionalAgents.TechnicalExpert.class)
                    .chatModel(chatModel)
                    .outputKey("response")
                    .build();

            // Step 3: Build conditional agent using conditionalBuilder() with lambda predicates
            UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
                    .subAgents(agenticScope -> agenticScope.readState("category", ConditionalAgents.RequestCategory.UNKNOWN) == ConditionalAgents.RequestCategory.MEDICAL, medicalExpert)
                    .subAgents(agenticScope -> agenticScope.readState("category", ConditionalAgents.RequestCategory.UNKNOWN) == ConditionalAgents.RequestCategory.LEGAL, legalExpert)
                    .subAgents(agenticScope -> agenticScope.readState("category", ConditionalAgents.RequestCategory.UNKNOWN) == ConditionalAgents.RequestCategory.TECHNICAL, technicalExpert)
                    .build();

            // Step 4: Compose router + conditional in a sequence
            UntypedAgent expertRouter = AgenticServices.sequenceBuilder()
                    .name("expertRouter")
                    .subAgents(routerAgent, expertsAgent)
                    .listener(listener)
                    .outputKey("response")
                    .build();

            // Execute the full conditional workflow
            ResultWithAgenticScope<String> result = expertRouter.invokeWithAgenticScope(
                    Map.of("request", prompt));

            String response = String.valueOf(result.result());

            // Extract scope state
            Map<String, Object> scope = new ConcurrentHashMap<>(listener.getScopeSnapshot());
            scope.put("request", prompt);
            ConditionalAgents.RequestCategory category = result.agenticScope()
                    .readState("category", ConditionalAgents.RequestCategory.UNKNOWN);
            scope.put("category", category.toString());
            scope.put("response", response);

            events.add(publishEvent(AgentEvent.completed("conditional", response)));
            return ExecutionResult.success(executionId, "conditional", response, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("conditional", null, e.getMessage())));
            return ExecutionResult.error(executionId, "conditional", e.getMessage(), events, startTime);
        }
    }

    /**
     * SUPERVISOR PATTERN: Supervisor coordinates sub-agents with tools
     * Uses AgenticServices.supervisorBuilder() for autonomous agent orchestration.
     */
    private ExecutionResult executeSupervisor(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("supervisor", "Starting supervisor workflow using AgenticServices: Orchestrating banking agents with tools")));

            // Create tools
            BankTool bankTool = new BankTool();
            ExchangeTool exchangeTool = new ExchangeTool();

            scope.put("request", prompt);
            scope.put("initialBalances", bankTool.getAllBalances());
            events.add(publishEvent(AgentEvent.stateUpdated("supervisor", "request", truncate(prompt))));
            events.add(publishEvent(AgentEvent.stateUpdated("supervisor", "balances", bankTool.getAllBalances().toString())));

            // Create listener to publish sub-agent events via WebSocket
            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "supervisor", events);

            // Build sub-agents using AgenticServices.agentBuilder() with tools and listener
            // Listener is attached to each sub-agent so we get events when the supervisor invokes them
            WithdrawAgent withdrawAgent = AgenticServices.agentBuilder(WithdrawAgent.class)
                    .chatModel(chatModel)
                    .tools(bankTool)
                    .listener(listener)
                    .build();

            CreditAgent creditAgent = AgenticServices.agentBuilder(CreditAgent.class)
                    .chatModel(chatModel)
                    .tools(bankTool)
                    .listener(listener)
                    .build();

            ExchangeAgent exchangeAgent = AgenticServices.agentBuilder(ExchangeAgent.class)
                    .chatModel(chatModel)
                    .tools(exchangeTool)
                    .listener(listener)
                    .build();

            // Build supervisor using AgenticServices.supervisorBuilder() with sub-agents
            // Note: supervisorBuilder() doesn't expose listener() on its interface,
            // so we attach listeners to each sub-agent instead
            SupervisorAgent supervisor = AgenticServices.supervisorBuilder()
                    .chatModel(plannerModel)
                    .subAgents(withdrawAgent, creditAgent, exchangeAgent)
                    .responseStrategy(SupervisorResponseStrategy.SUMMARY)
                    .build();

            events.add(publishEvent(AgentEvent.agentInvoked("supervisor", "bankSupervisor", "Analyzing and coordinating request...")));
            
            // Supervisor autonomously plans and executes
            String response = supervisor.invoke(prompt);
            
            scope.put("response", response);
            scope.put("finalBalances", bankTool.getAllBalances());
            events.add(publishEvent(AgentEvent.agentCompleted("supervisor", "bankSupervisor", truncate(response))));
            events.add(publishEvent(AgentEvent.stateUpdated("supervisor", "finalBalances", bankTool.getAllBalances().toString())));

            events.add(publishEvent(AgentEvent.completed("supervisor", response)));
            return ExecutionResult.success(executionId, "supervisor", response, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("supervisor", null, e.getMessage())));
            return ExecutionResult.error(executionId, "supervisor", e.getMessage(), events, startTime);
        }
    }

    /**
     * HUMAN-IN-THE-LOOP PATTERN: Agent proposes, human reviews, agent executes
     * Uses AgenticServices.agentBuilder() for agents with human input integration.
     * Note: Human-in-the-loop requires manual orchestration for input waiting.
     */
    private ExecutionResult executeHumanInLoop(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("human-in-loop", "Starting human-in-the-loop workflow using AgenticServices")));

            // Build agents using AgenticServices.agentBuilder() - proper LangChain4j way
            ZodiacExtractor extractor = AgenticServices.agentBuilder(ZodiacExtractor.class)
                    .chatModel(chatModel)
                    .build();
            
            events.add(publishEvent(AgentEvent.agentInvoked("human-in-loop", "ZodiacExtractor", "Checking for zodiac sign...")));
            String extractedSign = extractor.extract(prompt).trim();
            events.add(publishEvent(AgentEvent.agentCompleted("human-in-loop", "ZodiacExtractor", "Found: " + extractedSign)));
            
            String zodiacSign;
            if (extractedSign.equalsIgnoreCase("UNKNOWN") || extractedSign.isEmpty()) {
                // Need human input
                String requestId = UUID.randomUUID().toString();
                scope.put("requestId", requestId);
                CompletableFuture<String> inputFuture = humanInputService.requestInput(requestId,
                    "Please provide your zodiac sign:");
                events.add(publishEvent(AgentEvent.humanInputRequired("human-in-loop", 
                        "What is your zodiac sign? (e.g., Aries, Taurus, Gemini...)", requestId)));

                try {
                    zodiacSign = inputFuture.get(120, TimeUnit.SECONDS);
                    events.add(publishEvent(AgentEvent.humanInputReceived("human-in-loop", requestId,
                        "Human input received")));
                    events.add(publishEvent(AgentEvent.stateUpdated("human-in-loop", "humanInput", zodiacSign)));
                } catch (TimeoutException e) {
                    // Default to Aries if timeout
                    humanInputService.cancelRequest(requestId);
                    zodiacSign = "Aries";
                    events.add(publishEvent(AgentEvent.humanInputReceived("human-in-loop", requestId,
                        "Human input timed out; using default")));
                    events.add(publishEvent(AgentEvent.stateUpdated("human-in-loop", "timeout", "Using default: Aries")));
                }
            } else {
                zodiacSign = extractedSign;
            }

            scope.put("zodiacSign", zodiacSign);
            events.add(publishEvent(AgentEvent.stateUpdated("human-in-loop", "zodiacSign", zodiacSign)));

            // Generate horoscope using AgenticServices.agentBuilder()
            HoroscopeAgent horoscopeAgent = AgenticServices.agentBuilder(HoroscopeAgent.class)
                    .chatModel(chatModel)
                    .build();
            
            events.add(publishEvent(AgentEvent.agentInvoked("human-in-loop", "HoroscopeAgent", "Generating horoscope for " + zodiacSign)));
            String horoscope = horoscopeAgent.generateHoroscope(zodiacSign);
            scope.put("horoscope", horoscope);
            events.add(publishEvent(AgentEvent.agentCompleted("human-in-loop", "HoroscopeAgent", truncate(horoscope))));

            events.add(publishEvent(AgentEvent.completed("human-in-loop", horoscope)));
            return ExecutionResult.success(executionId, "human-in-loop", horoscope, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("human-in-loop", null, e.getMessage())));
            return ExecutionResult.error(executionId, "human-in-loop", e.getMessage(), events, startTime);
        }
    }

    /**
     * GOAP PATTERN: Goal-Oriented Action Planning (TSP Travel Planner)
     * Uses GoalOrientedPlanner which automatically builds a dependency graph from agent
     * input/output keys and calculates the shortest path from current state to the goal.
     * 
     * Dependency graph:
     *   prompt -> cities (CityParser)
     *   cities -> distances (DistanceCalculator)  ─┐ parallel branches
     *   cities -> attractions (AttractionFinder)  ─┘
     *   distances -> route (RouteOptimizer)
     *   route + attractions -> itinerary (ItineraryPlanner) ← GOAL
     */
    private ExecutionResult executeGOAP(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("goap", "Starting GOAP workflow using GoalOrientedPlanner: TSP Travel Planner with parallel branches")));

            // Create listener for real-time WebSocket events
            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "goap", events);

            // Build agents using AgenticServices.agentBuilder() with proper output keys
            // The GoalOrientedPlanner will analyze these to build the dependency graph:
            // prompt -> cities -> distances, attractions (parallel) -> route -> itinerary
            CityParser cityParser = AgenticServices.agentBuilder(CityParser.class)
                    .chatModel(chatModel)
                    .outputKey("cities")  // prompt -> cities
                    .build();

            DistanceCalculator distanceCalculator = AgenticServices.agentBuilder(DistanceCalculator.class)
                    .chatModel(chatModel)
                    .outputKey("distances")  // cities -> distances
                    .build();

            AttractionFinder attractionFinder = AgenticServices.agentBuilder(AttractionFinder.class)
                    .chatModel(chatModel)
                    .outputKey("attractions")  // cities -> attractions (parallel with distances)
                    .build();

            RouteOptimizer routeOptimizer = AgenticServices.agentBuilder(RouteOptimizer.class)
                    .chatModel(chatModel)
                    .outputKey("route")  // distances -> route
                    .build();

            ItineraryPlanner itineraryPlanner = AgenticServices.agentBuilder(ItineraryPlanner.class)
                    .chatModel(chatModel)
                    .outputKey("itinerary")  // route + attractions -> itinerary (GOAL)
                    .build();

            // Build GOAP workflow using plannerBuilder with GoalOrientedPlanner
            // The planner will:
            // 1. Build dependency graph from agent input/output keys
            // 2. Calculate shortest path from "prompt" to "itinerary"
            // 3. Execute: cityParser -> (distanceCalculator || attractionFinder) -> routeOptimizer -> itineraryPlanner
            UntypedAgent goapWorkflow = AgenticServices.plannerBuilder()
                    .subAgents(cityParser, distanceCalculator, attractionFinder, routeOptimizer, itineraryPlanner)
                    .outputKey("itinerary")  // The goal state we want to reach
                    .planner(GoalOrientedPlanner::new)  // Uses GOAP algorithm!
                    .listener(listener)
                    .build();

            scope.put("prompt", prompt);
            events.add(publishEvent(AgentEvent.stateUpdated("goap", "prompt", truncate(prompt))));

            // Execute the GOAP workflow - the planner automatically determines and executes the path
            events.add(publishEvent(AgentEvent.agentInvoked("goap", "goalOrientedPlanner", "Computing optimal agent path to goal...")));
            ResultWithAgenticScope<String> result = goapWorkflow.invokeWithAgenticScope(Map.of("prompt", prompt));

            String itinerary = result.result();

            // Capture the final scope state
            scope.putAll(listener.getScopeSnapshot());
            scope.put("itinerary", itinerary);

            events.add(publishEvent(AgentEvent.completed("goap", itinerary)));
            return ExecutionResult.success(executionId, "goap", itinerary, events, scope, startTime);

        } catch (Exception e) {
            log.error("GOAP execution failed", e);
            events.add(publishEvent(AgentEvent.error("goap", null, e.getMessage())));
            return ExecutionResult.error(executionId, "goap", e.getMessage(), events, startTime);
        }
    }

    /**
     * P2P PATTERN: Peer-to-Peer agent collaboration
     * Uses P2PPlanner which automatically activates agents when their required inputs 
     * become available in shared state. Continues until exit condition (score threshold) is met.
     */
    private ExecutionResult executeP2P(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("p2p", "Starting P2P workflow using P2PPlanner: Reactive peer collaboration")));

            // Create listener for real-time WebSocket events
            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "p2p", events);

            // Build peer agents using AgenticServices.agentBuilder() with proper output keys
            // P2PPlanner activates agents when their input dependencies become available
            LiteratureAgent literatureAgent = AgenticServices.agentBuilder(LiteratureAgent.class)
                    .chatModel(chatModel)
                    .outputKey("researchFindings")  // topic -> researchFindings
                    .build();

            HypothesisAgent hypothesisAgent = AgenticServices.agentBuilder(HypothesisAgent.class)
                    .chatModel(chatModel)
                    .outputKey("hypothesis")  // researchFindings -> hypothesis
                    .build();

            CriticAgent criticAgent = AgenticServices.agentBuilder(CriticAgent.class)
                    .chatModel(chatModel)
                    .outputKey("critique")  // hypothesis -> critique
                    .build();

            ValidationAgent validationAgent = AgenticServices.agentBuilder(ValidationAgent.class)
                    .chatModel(chatModel)
                    .outputKey("hypothesis")  // hypothesis, critique -> refined hypothesis
                    .build();

            ScorerAgent scorerAgent = AgenticServices.agentBuilder(ScorerAgent.class)
                    .chatModel(chatModel)
                    .outputKey("score")  // hypothesis -> score
                    .build();

            // Build P2P workflow using plannerBuilder with P2PPlanner
            // The planner will:
            // 1. Activate agents reactively when their inputs become available
            // 2. Continue iterating until exit condition is met (score >= 0.75)
            // 3. Max 10 agent invocations to prevent infinite loops
            final double targetScore = 0.75;
            
            UntypedAgent p2pWorkflow = AgenticServices.plannerBuilder()
                    .subAgents(literatureAgent, hypothesisAgent, criticAgent, validationAgent, scorerAgent)
                    .outputKey("hypothesis")  // Final output we want
                    .planner(() -> new P2PPlanner(plannerModel, 10, agenticScope -> {
                        // Exit condition: score threshold reached
                        if (!agenticScope.hasState("score")) {
                            return false;
                        }
                        Double score = agenticScope.readState("score", 0.0);
                        log.info("P2P current hypothesis score: {}", score);
                        return score >= targetScore;
                    }))
                    .listener(listener)
                    .build();

            scope.put("topic", prompt);
            events.add(publishEvent(AgentEvent.stateUpdated("p2p", "topic", truncate(prompt))));
            events.add(publishEvent(AgentEvent.stateUpdated("p2p", "targetScore", String.valueOf(targetScore))));

            // Execute the P2P workflow - agents activate reactively based on available state
            events.add(publishEvent(AgentEvent.agentInvoked("p2p", "p2pPlanner", "Starting reactive peer collaboration...")));
            ResultWithAgenticScope<String> result = p2pWorkflow.invokeWithAgenticScope(Map.of("topic", prompt));

            String hypothesis = result.result();
            
            // Capture the final scope state
            scope.putAll(listener.getScopeSnapshot());
            
            scope.put("hypothesis", hypothesis);
            
            // Get final score from scope
            Double finalScore = result.agenticScope().readState("score", 0.0);
            scope.put("finalScore", finalScore);

            // Log planner completion with score summary
            boolean targetReached = finalScore >= targetScore;
            events.add(publishEvent(AgentEvent.agentCompleted("p2p", "p2pPlanner",
                    String.format("Peer collaboration complete. Score: %.2f (target: %.2f)", finalScore, targetScore))));
            events.add(publishEvent(AgentEvent.stateUpdated("p2p", "hypothesis", truncate(hypothesis))));
            events.add(publishEvent(AgentEvent.stateUpdated("p2p", "finalScore", String.format("%.2f", finalScore))));
            events.add(publishEvent(AgentEvent.stateUpdated("p2p", "exitCondition",
                    targetReached ? "Target score " + targetScore + " reached" : "Max iterations reached")));

            // Format final output
            String finalOutput = String.format("""
                ## P2P Research Results
                
                **Final Hypothesis:** %s
                
                **Quality Score:** %.2f / 1.0
                
                **Status:** %s
                """, 
                hypothesis, 
                finalScore, 
                finalScore >= targetScore ? "✓ Target score reached!" : "Max iterations reached");

            events.add(publishEvent(AgentEvent.completed("p2p", finalOutput)));
            return ExecutionResult.success(executionId, "p2p", finalOutput, events, scope, startTime);

        } catch (Exception e) {
            log.error("P2P execution failed", e);
            events.add(publishEvent(AgentEvent.error("p2p", null, e.getMessage())));
            return ExecutionResult.error(executionId, "p2p", e.getMessage(), events, startTime);
        }
    }

        /**
         * DEBATE PATTERN: adversarial refinement using DebatePlanner.
         */
        private ExecutionResult executeDebate(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());

        try {
            events.add(publishEvent(AgentEvent.started("debate", "Starting debate workflow using DebatePlanner: debaters argue in parallel rounds, then a judge renders a verdict")));

            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "debate", events);

            DebateAgents.ProponentDebater proponent = AgenticServices
                .agentBuilder(DebateAgents.ProponentDebater.class)
                .chatModel(chatModel).outputKey("proponent").build();
            DebateAgents.SkepticDebater skeptic = AgenticServices
                .agentBuilder(DebateAgents.SkepticDebater.class)
                .chatModel(chatModel).outputKey("skeptic").build();
            DebateAgents.PragmatistDebater pragmatist = AgenticServices
                .agentBuilder(DebateAgents.PragmatistDebater.class)
                .chatModel(chatModel).outputKey("pragmatist").build();
            DebateAgents.DebateJudge judge = AgenticServices
                .agentBuilder(DebateAgents.DebateJudge.class)
                .chatModel(chatModel).outputKey("verdict").build();

            UntypedAgent debatePanel = AgenticServices.plannerBuilder()
                .subAgents(proponent, skeptic, pragmatist, judge)
                .outputKey("verdict")
                .planner(() -> new DebatePlanner(3, ConvergenceStrategy.unanimousLastWord()))
                .listener(listener)
                .build();

            events.add(publishEvent(AgentEvent.stateUpdated("debate", "question", truncate(prompt))));
            events.add(publishEvent(AgentEvent.agentInvoked("debate", "debatePanel", "Opening the debate floor...")));

            ResultWithAgenticScope<String> result = debatePanel.invokeWithAgenticScope(Map.of("question", prompt));
            String verdict = String.valueOf(result.result());

            Map<String, Object> scope = new ConcurrentHashMap<>(listener.getScopeSnapshot());
            scope.put("question", prompt);
            scope.put("verdict", verdict);

            String output = "## Debate Verdict\n\n" + verdict;
            events.add(publishEvent(AgentEvent.agentCompleted("debate", "Judge", truncate(verdict))));
            events.add(publishEvent(AgentEvent.completed("debate", output)));
            return ExecutionResult.success(executionId, "debate", output, events, scope, startTime);

        } catch (Exception e) {
            log.error("Debate execution failed", e);
            events.add(publishEvent(AgentEvent.error("debate", null, e.getMessage())));
            return ExecutionResult.error(executionId, "debate", e.getMessage(), events, startTime);
        }
        }

        /**
         * VOTING PATTERN: ensemble decision using VotingPlanner.
         */
        private ExecutionResult executeVoting(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());

        try {
            events.add(publishEvent(AgentEvent.started("voting", "Starting voting workflow using VotingPlanner: an ensemble of analysts vote in parallel, majority wins")));

            WebSocketAgentListener listener = new WebSocketAgentListener(eventPublisher, "voting", events);

            VotingAgents.GrowthAnalyst growth = AgenticServices
                .agentBuilder(VotingAgents.GrowthAnalyst.class)
                .chatModel(chatModel).outputKey("voteGrowth").build();
            VotingAgents.ValueAnalyst value = AgenticServices
                .agentBuilder(VotingAgents.ValueAnalyst.class)
                .chatModel(chatModel).outputKey("voteValue").build();
            VotingAgents.RiskAnalyst risk = AgenticServices
                .agentBuilder(VotingAgents.RiskAnalyst.class)
                .chatModel(chatModel).outputKey("voteRisk").build();

            UntypedAgent votingPanel = AgenticServices.plannerBuilder()
                .subAgents(growth, value, risk)
                .outputKey("decision")
                .planner(() -> new VotingPlanner(VotingStrategy.majority()))
                .listener(listener)
                .build();

            events.add(publishEvent(AgentEvent.stateUpdated("voting", "proposal", truncate(prompt))));
            events.add(publishEvent(AgentEvent.agentInvoked("voting", "votingPanel", "Collecting independent votes from the committee...")));

            ResultWithAgenticScope<String> result = votingPanel.invokeWithAgenticScope(Map.of("proposal", prompt));
            String decision = String.valueOf(result.result()).trim();

            Map<String, Object> scope = new ConcurrentHashMap<>(listener.getScopeSnapshot());
            scope.put("proposal", prompt);

            String voteGrowth = String.valueOf(scope.getOrDefault("voteGrowth", "?")).trim();
            String voteValue = String.valueOf(scope.getOrDefault("voteValue", "?")).trim();
            String voteRisk = String.valueOf(scope.getOrDefault("voteRisk", "?")).trim();
            scope.put("decision", decision);

            String output = String.format("""
            ## Committee Decision: **%s**

            - **Growth Analyst** voted: **%s**
            - **Value Analyst** voted: **%s**
            - **Risk Analyst** voted: **%s**

            _Aggregated by majority vote across the committee._
            """, decision, voteGrowth, voteValue, voteRisk);

            events.add(publishEvent(AgentEvent.agentCompleted("voting", "votingPanel", "Majority decision: " + decision)));
            events.add(publishEvent(AgentEvent.completed("voting", output)));
            return ExecutionResult.success(executionId, "voting", output, events, scope, startTime);

        } catch (Exception e) {
            log.error("Voting execution failed", e);
            events.add(publishEvent(AgentEvent.error("voting", null, e.getMessage())));
            return ExecutionResult.error(executionId, "voting", e.getMessage(), events, startTime);
        }
        }

    // Helper methods

    private AgentEvent publishEvent(AgentEvent event) {
        eventPublisher.publish(event);
        return event;
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}
