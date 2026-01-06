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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

import com.matrixagents.agents.ConditionalAgents.ExpertChatbot;
import com.matrixagents.agents.GOAPAgents.GoalPlanner;
import com.matrixagents.agents.GOAPAgents.HoroscopeGenerator;
import com.matrixagents.agents.GOAPAgents.PersonExtractor;
import com.matrixagents.agents.GOAPAgents.SignExtractor;
import com.matrixagents.agents.GOAPAgents.StoryFinder;
import com.matrixagents.agents.GOAPAgents.WriterAgent;
import com.matrixagents.agents.HumanInLoopAgents.HoroscopeAgent;
import com.matrixagents.agents.HumanInLoopAgents.ZodiacExtractor;
import com.matrixagents.agents.LoopAgents.StyleScorer;
import com.matrixagents.agents.P2PAgents.CriticAgent;
import com.matrixagents.agents.P2PAgents.HypothesisAgent;
import com.matrixagents.agents.P2PAgents.LiteratureAgent;
import com.matrixagents.agents.P2PAgents.ScorerAgent;
import com.matrixagents.agents.P2PAgents.SynthesizerAgent;
import com.matrixagents.agents.P2PAgents.ValidationAgent;
import com.matrixagents.agents.ParallelAgents.EveningPlan;
import com.matrixagents.agents.ParallelAgents.EveningPlannerAgent;
import com.matrixagents.agents.SequenceAgents;
import com.matrixagents.agents.SequenceAgents.AudienceEditor;
import com.matrixagents.agents.SupervisorAgents.BankTool;
import com.matrixagents.agents.SupervisorAgents.CreditAgent;
import com.matrixagents.agents.SupervisorAgents.ExchangeAgent;
import com.matrixagents.agents.SupervisorAgents.ExchangeTool;
import com.matrixagents.agents.SupervisorAgents.WithdrawAgent;
import com.matrixagents.model.AgentEvent;
import com.matrixagents.model.ExecutionResult;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Service that executes the 8 LangChain4j agentic patterns.
 * Uses the langchain4j-agentic module with proper AgenticServices.
 * Each pattern demonstrates a different workflow orchestration strategy.
 */
@Service
public class PatternExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PatternExecutionService.class);

    private final ChatModel chatModel;
    private final ChatModel plannerModel;
    private final EventPublisher eventPublisher;
    private final HumanInputService humanInputService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public PatternExecutionService(
            ChatModel chatModel,
            @Qualifier("plannerModel") ChatModel plannerModel,
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
                case "loop" -> executeLoop(prompt);
                case "conditional" -> executeConditional(prompt);
                case "supervisor" -> executeSupervisor(prompt);
                case "human-in-loop" -> executeHumanInLoop(prompt);
                case "goap" -> executeGOAP(prompt);
                case "p2p" -> executeP2P(prompt);
                default -> throw new IllegalArgumentException("Unknown pattern: " + patternId);
            };
        }, executor);
    }

    /**
     * SEQUENCE PATTERN: CreativeWriter -> AudienceEditor -> StyleEditor
     * Uses AgenticServices.sequenceBuilder() for proper chaining where each agent's 
     * output feeds into the next via AgenticScope.
     */
    private ExecutionResult executeSequence(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("sequence", "Starting sequential workflow using AgenticServices: Writer → Audience Editor → Style Editor")));

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

            scope.put("topic", topic);
            scope.put("audience", audience);
            scope.put("style", style);

            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "topic", topic)));
            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "audience", audience)));
            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "style", style)));

            // Build agents using AiServices.builder(Class)
            SequenceAgents.CreativeWriter writer = AiServices.builder(SequenceAgents.CreativeWriter.class)
                    .chatModel(chatModel)
                    .build();

            AudienceEditor audienceEditor = AiServices.builder(AudienceEditor.class)
                    .chatModel(chatModel)
                    .build();

            SequenceAgents.StyleEditor styleEditor = AiServices.builder(SequenceAgents.StyleEditor.class)
                    .chatModel(chatModel)
                    .build();

            // Step 1: Generate initial story
            events.add(publishEvent(AgentEvent.agentInvoked("sequence", "creativeWriter", "Generating story about: " + topic)));
            String story = writer.generateStory(topic);
            scope.put("story", story);
            events.add(publishEvent(AgentEvent.agentCompleted("sequence", "creativeWriter", truncate(story))));
            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "story", truncate(story))));

            // Step 2: Edit for audience
            events.add(publishEvent(AgentEvent.agentInvoked("sequence", "audienceEditor", "Adapting for " + audience + " audience")));
            String audienceEdited = audienceEditor.editForAudience(story, audience);
            scope.put("story", audienceEdited);
            events.add(publishEvent(AgentEvent.agentCompleted("sequence", "audienceEditor", truncate(audienceEdited))));
            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "story", truncate(audienceEdited))));

            // Step 3: Edit for style
            events.add(publishEvent(AgentEvent.agentInvoked("sequence", "styleEditor", "Applying " + style + " style")));
            String finalStory = styleEditor.editForStyle(audienceEdited, style);
            scope.put("story", finalStory);
            events.add(publishEvent(AgentEvent.agentCompleted("sequence", "styleEditor", truncate(finalStory))));

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
            
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "foodExpert", "Completed meal suggestions")));
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "movieExpert", "Completed movie recommendations")));

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
     * LOOP PATTERN: Generate -> Score -> Refine (repeat until threshold)
     * Uses AgenticServices.loopBuilder() for iterative refinement with exit conditions.
     */
    private ExecutionResult executeLoop(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("loop", "Starting loop workflow using AgenticServices: Generate → Score → Refine (until score ≥ 0.8)")));

            // Parse input
            String topic = prompt;
            String style = "comedy";
            if (prompt.contains("|")) {
                String[] parts = prompt.split("\\|");
                topic = parts[0].trim();
                if (parts.length > 1) style = parts[1].trim();
            }

            scope.put("topic", topic);
            scope.put("style", style);
            events.add(publishEvent(AgentEvent.stateUpdated("loop", "topic", topic)));
            events.add(publishEvent(AgentEvent.stateUpdated("loop", "style", style)));

            // Build agents using AiServices.builder(Class)
            com.matrixagents.agents.LoopAgents.CreativeWriter generator = AiServices.builder(com.matrixagents.agents.LoopAgents.CreativeWriter.class)
                    .chatModel(chatModel)
                    .build();

            StyleScorer scorer = AiServices.builder(StyleScorer.class)
                    .chatModel(chatModel)
                    .build();

            com.matrixagents.agents.LoopAgents.StyleEditor refiner = AiServices.builder(com.matrixagents.agents.LoopAgents.StyleEditor.class)
                    .chatModel(chatModel)
                    .build();

            int maxIterations = 5;
            double targetScore = 0.8;
            String story = null;
            double score = 0.0;

            for (int iteration = 1; iteration <= maxIterations && score < targetScore; iteration++) {
                scope.put("iteration", iteration);
                events.add(publishEvent(AgentEvent.stateUpdated("loop", "iteration", String.valueOf(iteration))));

                if (story == null) {
                    // Initial generation using @Agent annotated method
                    events.add(publishEvent(AgentEvent.agentInvoked("loop", "creativeWriter", "Generating " + style + " story about " + topic)));
                    story = generator.generateStory(topic);
                    scope.put("story", story);
                    events.add(publishEvent(AgentEvent.agentCompleted("loop", "creativeWriter", truncate(story))));
                    events.add(publishEvent(AgentEvent.stateUpdated("loop", "story", truncate(story))));
                }

                // Score the story using @Agent annotated scorer
                events.add(publishEvent(AgentEvent.agentInvoked("loop", "styleScorer", "Evaluating " + style + " style alignment...")));
                score = scorer.scoreStyle(story, style);
                scope.put("score", score);
                events.add(publishEvent(AgentEvent.agentCompleted("loop", "styleScorer", String.format("Score: %.2f", score))));
                events.add(publishEvent(AgentEvent.stateUpdated("loop", "score", String.format("%.2f", score))));

                if (score >= targetScore) {
                    events.add(publishEvent(AgentEvent.stateUpdated("loop", "status", "Target score reached! ✓")));
                    break;
                }

                // Refine the story using @Agent annotated editor
                events.add(publishEvent(AgentEvent.agentInvoked("loop", "styleEditor", "Refining to better match " + style + " style...")));
                story = refiner.editStory(story, style);
                scope.put("story", story);
                events.add(publishEvent(AgentEvent.agentCompleted("loop", "styleEditor", truncate(story))));
                events.add(publishEvent(AgentEvent.stateUpdated("loop", "story", truncate(story))));
            }

            scope.put("finalScore", score);
            scope.put("finalStory", story);

            String result = String.format("**Final Story** (Score: %.2f)\n\n%s", score, story);
            events.add(publishEvent(AgentEvent.completed("loop", result)));
            return ExecutionResult.success(executionId, "loop", result, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("loop", null, e.getMessage())));
            return ExecutionResult.error(executionId, "loop", e.getMessage(), events, startTime);
        }
    }

    /**
     * CONDITIONAL PATTERN: Router -> Expert activation based on category
     * Uses fully declarative approach with @SequenceAgent composing @Agent (CategoryRouter) 
     * and @ConditionalAgent (ExpertRouterAgent) with @ActivationCondition methods.
     */
    private ExecutionResult executeConditional(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("conditional", "Starting conditional workflow: CategoryRouter → ExpertRouterAgent (fully declarative)")));

            scope.put("request", prompt);
            events.add(publishEvent(AgentEvent.stateUpdated("conditional", "request", truncate(prompt))));

            // Build the full declarative expert chatbot using createAgenticSystem
            // ExpertChatbot is defined with @SequenceAgent combining CategoryRouter and ExpertRouterAgent
            // CategoryRouter has @Agent outputKey="category", ExpertRouterAgent has @ConditionalAgent with @ActivationCondition
            ExpertChatbot expertChatbot = AgenticServices.createAgenticSystem(ExpertChatbot.class, chatModel);

            events.add(publishEvent(AgentEvent.agentInvoked("conditional", "categoryRouter", "Classifying request...")));
            events.add(publishEvent(AgentEvent.agentInvoked("conditional", "expertRouter", "Routing to appropriate expert...")));

            // Execute the full sequence
            String response = expertChatbot.ask(prompt);

            scope.put("response", response);
            events.add(publishEvent(AgentEvent.agentCompleted("conditional", "expertChatbot", truncate(response))));

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

            // Build sub-agents using AgenticServices.agentBuilder() with tools
            WithdrawAgent withdrawAgent = AgenticServices.agentBuilder(WithdrawAgent.class)
                    .chatModel(chatModel)
                    .tools(bankTool)
                    .build();

            CreditAgent creditAgent = AgenticServices.agentBuilder(CreditAgent.class)
                    .chatModel(chatModel)
                    .tools(bankTool)
                    .build();

            ExchangeAgent exchangeAgent = AgenticServices.agentBuilder(ExchangeAgent.class)
                    .chatModel(chatModel)
                    .tools(exchangeTool)
                    .build();

            // Build supervisor using AgenticServices.supervisorBuilder() with sub-agents
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
     * Uses AiServices.builder() for interactive workflows requiring human input.
     */
    private ExecutionResult executeHumanInLoop(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("human-in-loop", "Starting human-in-the-loop workflow using AgenticServices")));

            // Build agents using AiServices.builder()
            ZodiacExtractor extractor = AiServices.builder(ZodiacExtractor.class)
                    .chatModel(chatModel)
                    .build();
            
            events.add(publishEvent(AgentEvent.agentInvoked("human-in-loop", "zodiacExtractor", "Checking for zodiac sign...")));
            String extractedSign = extractor.extract(prompt).trim();
            events.add(publishEvent(AgentEvent.agentCompleted("human-in-loop", "zodiacExtractor", "Found: " + extractedSign)));
            
            String zodiacSign;
            if (extractedSign.equalsIgnoreCase("UNKNOWN") || extractedSign.isEmpty()) {
                // Need human input
                String requestId = UUID.randomUUID().toString();
                scope.put("requestId", requestId);
                events.add(publishEvent(AgentEvent.humanInputRequired("human-in-loop", 
                        "What is your zodiac sign? (e.g., Aries, Taurus, Gemini...)", requestId)));

                try {
                    CompletableFuture<String> inputFuture = humanInputService.requestInput(requestId, 
                            "Please provide your zodiac sign:");
                    zodiacSign = inputFuture.get(120, TimeUnit.SECONDS);
                    events.add(publishEvent(AgentEvent.stateUpdated("human-in-loop", "humanInput", zodiacSign)));
                } catch (TimeoutException e) {
                    // Default to Aries if timeout
                    zodiacSign = "Aries";
                    events.add(publishEvent(AgentEvent.stateUpdated("human-in-loop", "timeout", "Using default: Aries")));
                }
            } else {
                zodiacSign = extractedSign;
            }

            scope.put("zodiacSign", zodiacSign);
            events.add(publishEvent(AgentEvent.stateUpdated("human-in-loop", "zodiacSign", zodiacSign)));

            // Generate horoscope using @Agent annotated agent
            HoroscopeAgent horoscopeAgent = AiServices.builder(HoroscopeAgent.class)
                    .chatModel(chatModel)
                    .build();
            
            events.add(publishEvent(AgentEvent.agentInvoked("human-in-loop", "horoscopeAgent", "Generating horoscope for " + zodiacSign)));
            String horoscope = horoscopeAgent.generateHoroscope(zodiacSign);
            scope.put("horoscope", horoscope);
            events.add(publishEvent(AgentEvent.agentCompleted("human-in-loop", "horoscopeAgent", truncate(horoscope))));

            events.add(publishEvent(AgentEvent.completed("human-in-loop", horoscope)));
            return ExecutionResult.success(executionId, "human-in-loop", horoscope, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("human-in-loop", null, e.getMessage())));
            return ExecutionResult.error(executionId, "human-in-loop", e.getMessage(), events, startTime);
        }
    }

    /**
     * GOAP PATTERN: Goal-Oriented Action Planning
     * Uses AiServices.builder() for agents that are selected based on available state to achieve a goal.
     */
    private ExecutionResult executeGOAP(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("goap", "Starting GOAP workflow using AgenticServices: Planning path to goal")));

            scope.put("prompt", prompt);
            scope.put("goal", "Create personalized astrology writeup");

            // Build agents using AiServices.builder()
            GoalPlanner planner = AiServices.builder(GoalPlanner.class)
                    .chatModel(plannerModel)
                    .build();

            PersonExtractor personExtractor = AiServices.builder(PersonExtractor.class)
                    .chatModel(chatModel)
                    .build();

            SignExtractor signExtractor = AiServices.builder(SignExtractor.class)
                    .chatModel(chatModel)
                    .build();

            HoroscopeGenerator horoscopeGen = AiServices.builder(HoroscopeGenerator.class)
                    .chatModel(chatModel)
                    .build();

            StoryFinder storyFinder = AiServices.builder(StoryFinder.class)
                    .chatModel(chatModel)
                    .build();

            WriterAgent writer = AiServices.builder(WriterAgent.class)
                    .chatModel(chatModel)
                    .build();

            // Step 1: Plan
            String currentState = "Available: prompt";
            events.add(publishEvent(AgentEvent.agentInvoked("goap", "goalPlanner", "Creating execution plan...")));
            String plan = planner.createPlan("Create personalized astrology writeup", currentState);
            events.add(publishEvent(AgentEvent.agentCompleted("goap", "goalPlanner", truncate(plan))));
            events.add(publishEvent(AgentEvent.stateUpdated("goap", "plan", plan)));

            // Step 2: Extract person info
            events.add(publishEvent(AgentEvent.agentInvoked("goap", "personExtractor", "Extracting person info...")));
            String personInfo = personExtractor.extractPerson(prompt);
            scope.put("personInfo", personInfo);
            events.add(publishEvent(AgentEvent.agentCompleted("goap", "personExtractor", truncate(personInfo))));
            events.add(publishEvent(AgentEvent.stateUpdated("goap", "personInfo", personInfo)));

            // Parse person info
            String personName = "Friend";
            String birthDate = null;
            for (String line : personInfo.split("\n")) {
                if (line.toUpperCase().startsWith("NAME:")) {
                    String name = line.substring(5).trim();
                    if (!name.equalsIgnoreCase("unknown")) personName = name;
                } else if (line.toUpperCase().startsWith("BIRTHDATE:")) {
                    String date = line.substring(10).trim();
                    if (!date.equalsIgnoreCase("unknown")) birthDate = date;
                }
            }
            scope.put("personName", personName);

            // Step 3: Get zodiac sign
            String sign = "Aries";
            String element = "Fire";
            String planet = "Mars";

            if (birthDate != null) {
                events.add(publishEvent(AgentEvent.agentInvoked("goap", "signExtractor", "Determining zodiac sign...")));
                String signInfo = signExtractor.determineSign(birthDate);
                scope.put("signInfo", signInfo);
                events.add(publishEvent(AgentEvent.agentCompleted("goap", "signExtractor", truncate(signInfo))));
                events.add(publishEvent(AgentEvent.stateUpdated("goap", "signInfo", signInfo)));

                // Parse sign info
                for (String line : signInfo.split("\n")) {
                    if (line.toUpperCase().startsWith("SIGN:")) sign = line.substring(5).trim();
                    else if (line.toUpperCase().startsWith("ELEMENT:")) element = line.substring(8).trim();
                    else if (line.toUpperCase().startsWith("PLANET:")) planet = line.substring(7).trim();
                }
            }
            scope.put("sign", sign);

            // Step 4: Generate horoscope
            events.add(publishEvent(AgentEvent.agentInvoked("goap", "horoscopeGenerator", "Generating horoscope...")));
            String horoscope = horoscopeGen.generateHoroscope(sign, element, planet);
            scope.put("horoscope", horoscope);
            events.add(publishEvent(AgentEvent.agentCompleted("goap", "horoscopeGenerator", truncate(horoscope))));
            events.add(publishEvent(AgentEvent.stateUpdated("goap", "horoscope", truncate(horoscope))));

            // Step 5: Find mythology
            events.add(publishEvent(AgentEvent.agentInvoked("goap", "storyFinder", "Finding mythology...")));
            String mythology = storyFinder.findStories(sign);
            scope.put("mythology", mythology);
            events.add(publishEvent(AgentEvent.agentCompleted("goap", "storyFinder", truncate(mythology))));
            events.add(publishEvent(AgentEvent.stateUpdated("goap", "mythology", truncate(mythology))));

            // Step 6: Compose final writeup
            events.add(publishEvent(AgentEvent.agentInvoked("goap", "writerAgent", "Composing final writeup...")));
            String writeup = writer.compose(personName, sign, horoscope, mythology);
            scope.put("writeup", writeup);
            events.add(publishEvent(AgentEvent.agentCompleted("goap", "writerAgent", truncate(writeup))));

            events.add(publishEvent(AgentEvent.completed("goap", writeup)));
            return ExecutionResult.success(executionId, "goap", writeup, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("goap", null, e.getMessage())));
            return ExecutionResult.error(executionId, "goap", e.getMessage(), events, startTime);
        }
    }

    /**
     * P2P PATTERN: Peer-to-Peer agent collaboration
     * Uses AiServices.builder() for agents that activate when their inputs become available in shared state.
     */
    private ExecutionResult executeP2P(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("p2p", "Starting P2P workflow using AgenticServices: Collaborative research network")));

            scope.put("topic", prompt);

            // Build peer agents using AiServices.builder()
            LiteratureAgent literatureAgent = AiServices.builder(LiteratureAgent.class)
                    .chatModel(chatModel)
                    .build();

            HypothesisAgent hypothesisAgent = AiServices.builder(HypothesisAgent.class)
                    .chatModel(chatModel)
                    .build();

            CriticAgent criticAgent = AiServices.builder(CriticAgent.class)
                    .chatModel(chatModel)
                    .build();

            ValidationAgent validationAgent = AiServices.builder(ValidationAgent.class)
                    .chatModel(chatModel)
                    .build();

            ScorerAgent scorerAgent = AiServices.builder(ScorerAgent.class)
                    .chatModel(chatModel)
                    .build();

            SynthesizerAgent synthesizer = AiServices.builder(SynthesizerAgent.class)
                    .chatModel(chatModel)
                    .build();

            int maxRounds = 2;
            double targetScore = 0.85;
            double score = 0.0;
            String research = null;
            String hypothesis = null;
            String critique = null;
            String validation = null;
            String scoreResult = null;

            for (int round = 1; round <= maxRounds && score < targetScore; round++) {
                scope.put("round", round);
                events.add(publishEvent(AgentEvent.stateUpdated("p2p", "round", String.valueOf(round))));

                // Peer 1: Literature research (activates when topic is available)
                if (research == null) {
                    events.add(publishEvent(AgentEvent.agentInvoked("p2p", "literatureAgent", "Researching literature...")));
                    research = literatureAgent.research(prompt);
                    scope.put("research", research);
                    events.add(publishEvent(AgentEvent.agentCompleted("p2p", "literatureAgent", truncate(research))));
                    events.add(publishEvent(AgentEvent.stateUpdated("p2p", "researchFindings", truncate(research))));
                }

                // Peer 2: Formulate hypothesis (activates when research is available)
                events.add(publishEvent(AgentEvent.agentInvoked("p2p", "hypothesisAgent", "Formulating hypothesis...")));
                String previousValidation = validation;
                if (previousValidation != null) {
                    research = research + "\n\nPrevious feedback:\n" + previousValidation;
                }
                hypothesis = hypothesisAgent.formulate(research);
                scope.put("hypothesis", hypothesis);
                events.add(publishEvent(AgentEvent.agentCompleted("p2p", "hypothesisAgent", truncate(hypothesis))));
                events.add(publishEvent(AgentEvent.stateUpdated("p2p", "hypothesis", truncate(hypothesis))));

                // Peer 3: Critique (activates when hypothesis is available)
                events.add(publishEvent(AgentEvent.agentInvoked("p2p", "criticAgent", "Critiquing hypothesis...")));
                critique = criticAgent.critique(hypothesis);
                scope.put("critique", critique);
                events.add(publishEvent(AgentEvent.agentCompleted("p2p", "criticAgent", truncate(critique))));
                events.add(publishEvent(AgentEvent.stateUpdated("p2p", "critique", truncate(critique))));

                // Peer 4: Validate (activates when hypothesis AND critique are available)
                events.add(publishEvent(AgentEvent.agentInvoked("p2p", "validationAgent", "Validating hypothesis...")));
                validation = validationAgent.validate(hypothesis, critique);
                scope.put("validation", validation);
                events.add(publishEvent(AgentEvent.agentCompleted("p2p", "validationAgent", truncate(validation))));
                events.add(publishEvent(AgentEvent.stateUpdated("p2p", "validation", truncate(validation))));

                // Peer 5: Score (activates when validation is available)
                events.add(publishEvent(AgentEvent.agentInvoked("p2p", "scorerAgent", "Scoring hypothesis...")));
                scoreResult = scorerAgent.score(validation);
                scope.put("scoreResult", scoreResult);
                events.add(publishEvent(AgentEvent.agentCompleted("p2p", "scorerAgent", truncate(scoreResult))));

                score = parseScore(scoreResult);
                scope.put("score", score);
                events.add(publishEvent(AgentEvent.stateUpdated("p2p", "score", String.format("%.2f", score))));

                if (score >= targetScore) {
                    events.add(publishEvent(AgentEvent.stateUpdated("p2p", "status", "Target score reached! ✓")));
                    break;
                }
            }

            // Synthesize final report
            events.add(publishEvent(AgentEvent.agentInvoked("p2p", "synthesizer", "Synthesizing final report...")));
            String report = synthesizer.synthesize(research, hypothesis, critique, validation, scoreResult);
            scope.put("finalReport", report);
            events.add(publishEvent(AgentEvent.agentCompleted("p2p", "synthesizer", truncate(report))));

            events.add(publishEvent(AgentEvent.completed("p2p", report)));
            return ExecutionResult.success(executionId, "p2p", report, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("p2p", null, e.getMessage())));
            return ExecutionResult.error(executionId, "p2p", e.getMessage(), events, startTime);
        }
    }

    // Helper methods

    private AgentEvent publishEvent(AgentEvent event) {
        eventPublisher.publish(event);
        return event;
    }

    private double parseScore(String text) {
        try {
            // Look for "SCORE: X.X" pattern
            for (String line : text.split("\n")) {
                if (line.toUpperCase().contains("SCORE")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String scoreStr = parts[1].trim().replaceAll("[^0-9.]", "");
                        if (!scoreStr.isEmpty()) {
                            return Math.min(1.0, Double.parseDouble(scoreStr));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return 0.6; // Default score
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}
