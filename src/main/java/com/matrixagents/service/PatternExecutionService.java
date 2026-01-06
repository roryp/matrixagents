package com.matrixagents.service;

import com.matrixagents.agents.SequenceAgents.*;
import com.matrixagents.agents.ParallelAgents.*;
import com.matrixagents.agents.LoopAgents.*;
import com.matrixagents.agents.ConditionalAgents.*;
import com.matrixagents.agents.SupervisorAgents.*;
import com.matrixagents.agents.HumanInLoopAgents.*;
import com.matrixagents.agents.GOAPAgents.*;
import com.matrixagents.agents.P2PAgents.*;
import com.matrixagents.model.AgentEvent;
import com.matrixagents.model.ExecutionResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service that executes the 8 LangChain4j agentic patterns.
 * Each pattern demonstrates a different workflow orchestration strategy.
 */
@Service
public class PatternExecutionService {

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
     * Demonstrates chaining where each agent's output feeds into the next.
     */
    private ExecutionResult executeSequence(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("sequence", "Starting sequential workflow: Writer → Audience Editor → Style Editor")));

            // Create agents using AiServices
            CreativeWriter writer = AiServices.create(CreativeWriter.class, chatModel);
            AudienceEditor audienceEditor = AiServices.create(AudienceEditor.class, chatModel);
            StyleEditor styleEditor = AiServices.create(StyleEditor.class, chatModel);

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

            // Step 1: Generate initial story
            events.add(publishEvent(AgentEvent.agentInvoked("sequence", "creativeWriter", "Generating story about: " + topic)));
            String story = writer.writeStory(topic);
            scope.put("initialStory", story);
            events.add(publishEvent(AgentEvent.agentCompleted("sequence", "creativeWriter", truncate(story))));
            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "story", truncate(story))));

            // Step 2: Edit for audience
            events.add(publishEvent(AgentEvent.agentInvoked("sequence", "audienceEditor", "Adapting for " + audience + " audience")));
            String audienceEdited = audienceEditor.editForAudience(story, audience);
            scope.put("audienceEdited", audienceEdited);
            events.add(publishEvent(AgentEvent.agentCompleted("sequence", "audienceEditor", truncate(audienceEdited))));
            events.add(publishEvent(AgentEvent.stateUpdated("sequence", "audienceEdited", truncate(audienceEdited))));

            // Step 3: Edit for style
            events.add(publishEvent(AgentEvent.agentInvoked("sequence", "styleEditor", "Applying " + style + " style")));
            String finalStory = styleEditor.editForStyle(audienceEdited, style);
            scope.put("finalStory", finalStory);
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
     * Demonstrates concurrent agent execution with result combination.
     */
    private ExecutionResult executeParallel(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("parallel", "Starting parallel workflow: Food + Movie experts running concurrently")));

            // Create agents
            FoodExpert foodExpert = AiServices.create(FoodExpert.class, chatModel);
            MovieExpert movieExpert = AiServices.create(MovieExpert.class, chatModel);
            PlanCombiner combiner = AiServices.create(PlanCombiner.class, chatModel);

            String mood = prompt.isEmpty() ? "romantic" : prompt;
            scope.put("mood", mood);

            // Execute in parallel
            events.add(publishEvent(AgentEvent.agentInvoked("parallel", "foodExpert", "Suggesting meals for " + mood + " mood...")));
            events.add(publishEvent(AgentEvent.agentInvoked("parallel", "movieExpert", "Recommending movies for " + mood + " mood...")));

            CompletableFuture<String> mealsFuture = CompletableFuture.supplyAsync(() -> foodExpert.suggestMeals(mood), executor);
            CompletableFuture<String> moviesFuture = CompletableFuture.supplyAsync(() -> movieExpert.recommendMovies(mood), executor);

            String meals = mealsFuture.get(60, TimeUnit.SECONDS);
            scope.put("meals", meals);
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "foodExpert", truncate(meals))));
            events.add(publishEvent(AgentEvent.stateUpdated("parallel", "meals", meals)));

            String movies = moviesFuture.get(60, TimeUnit.SECONDS);
            scope.put("movies", movies);
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "movieExpert", truncate(movies))));
            events.add(publishEvent(AgentEvent.stateUpdated("parallel", "movies", movies)));

            // Combine results
            events.add(publishEvent(AgentEvent.agentInvoked("parallel", "planCombiner", "Creating evening plans...")));
            String plans = combiner.combinePlans(movies, meals);
            scope.put("plans", plans);
            events.add(publishEvent(AgentEvent.agentCompleted("parallel", "planCombiner", truncate(plans))));

            events.add(publishEvent(AgentEvent.completed("parallel", plans)));
            return ExecutionResult.success(executionId, "parallel", plans, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("parallel", null, e.getMessage())));
            return ExecutionResult.error(executionId, "parallel", e.getMessage(), events, startTime);
        }
    }

    /**
     * LOOP PATTERN: Generate -> Score -> Refine (repeat until threshold)
     * Demonstrates iterative refinement with exit conditions.
     */
    private ExecutionResult executeLoop(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("loop", "Starting loop workflow: Generate → Score → Refine (until score ≥ 0.8)")));

            // Create agents
            StoryGenerator generator = AiServices.create(StoryGenerator.class, chatModel);
            StyleScorer scorer = AiServices.create(StyleScorer.class, chatModel);
            StyleRefiner refiner = AiServices.create(StyleRefiner.class, chatModel);

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

            int maxIterations = 3;
            double targetScore = 0.8;
            String story = null;
            double score = 0.0;

            for (int iteration = 1; iteration <= maxIterations && score < targetScore; iteration++) {
                scope.put("iteration", iteration);
                events.add(publishEvent(AgentEvent.stateUpdated("loop", "iteration", String.valueOf(iteration))));

                if (story == null) {
                    // Initial generation
                    events.add(publishEvent(AgentEvent.agentInvoked("loop", "storyGenerator", "Generating " + style + " story about " + topic)));
                    story = generator.generate(topic, style);
                    scope.put("story", story);
                    events.add(publishEvent(AgentEvent.agentCompleted("loop", "storyGenerator", truncate(story))));
                    events.add(publishEvent(AgentEvent.stateUpdated("loop", "story", truncate(story))));
                }

                // Score the story
                events.add(publishEvent(AgentEvent.agentInvoked("loop", "styleScorer", "Evaluating " + style + " style alignment...")));
                String feedback = scorer.score(story, style);
                scope.put("feedback", feedback);
                events.add(publishEvent(AgentEvent.agentCompleted("loop", "styleScorer", truncate(feedback))));

                score = parseScore(feedback);
                scope.put("score", score);
                events.add(publishEvent(AgentEvent.stateUpdated("loop", "score", String.format("%.2f", score))));

                if (score >= targetScore) {
                    events.add(publishEvent(AgentEvent.stateUpdated("loop", "status", "Target score reached! ✓")));
                    break;
                }

                // Refine the story
                events.add(publishEvent(AgentEvent.agentInvoked("loop", "styleRefiner", "Refining to better match " + style + " style...")));
                story = refiner.refine(story, style, feedback);
                scope.put("story", story);
                events.add(publishEvent(AgentEvent.agentCompleted("loop", "styleRefiner", truncate(story))));
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
     * Demonstrates routing to different agents based on classification.
     */
    private ExecutionResult executeConditional(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("conditional", "Starting conditional workflow: Router → Expert activation")));

            // Create router
            CategoryRouter router = AiServices.create(CategoryRouter.class, chatModel);

            scope.put("request", prompt);

            // Classify the request
            events.add(publishEvent(AgentEvent.agentInvoked("conditional", "categoryRouter", "Classifying request...")));
            String category = router.classify(prompt).trim().toUpperCase();
            scope.put("category", category);
            events.add(publishEvent(AgentEvent.agentCompleted("conditional", "categoryRouter", "Category: " + category)));
            events.add(publishEvent(AgentEvent.stateUpdated("conditional", "category", category)));

            // Route to appropriate expert
            String response;
            String expertName;

            if (category.contains("MEDICAL")) {
                expertName = "medicalExpert";
                MedicalExpert expert = AiServices.create(MedicalExpert.class, chatModel);
                events.add(publishEvent(AgentEvent.agentInvoked("conditional", expertName, "Consulting medical expert...")));
                response = expert.answer(prompt);
            } else if (category.contains("LEGAL")) {
                expertName = "legalExpert";
                LegalExpert expert = AiServices.create(LegalExpert.class, chatModel);
                events.add(publishEvent(AgentEvent.agentInvoked("conditional", expertName, "Consulting legal expert...")));
                response = expert.answer(prompt);
            } else if (category.contains("TECHNICAL")) {
                expertName = "technicalExpert";
                TechnicalExpert expert = AiServices.create(TechnicalExpert.class, chatModel);
                events.add(publishEvent(AgentEvent.agentInvoked("conditional", expertName, "Consulting technical expert...")));
                response = expert.answer(prompt);
            } else {
                expertName = "generalExpert";
                GeneralExpert expert = AiServices.create(GeneralExpert.class, chatModel);
                events.add(publishEvent(AgentEvent.agentInvoked("conditional", expertName, "Consulting general expert...")));
                response = expert.answer(prompt);
            }

            scope.put("expertUsed", expertName);
            scope.put("response", response);
            events.add(publishEvent(AgentEvent.agentCompleted("conditional", expertName, truncate(response))));

            events.add(publishEvent(AgentEvent.completed("conditional", response)));
            return ExecutionResult.success(executionId, "conditional", response, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("conditional", null, e.getMessage())));
            return ExecutionResult.error(executionId, "conditional", e.getMessage(), events, startTime);
        }
    }

    /**
     * SUPERVISOR PATTERN: Supervisor coordinates sub-agents with tools
     * Demonstrates autonomous agent orchestration.
     */
    private ExecutionResult executeSupervisor(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("supervisor", "Starting supervisor workflow: Orchestrating banking agents with tools")));

            // Create tools
            BankTool bankTool = new BankTool();
            ExchangeTool exchangeTool = new ExchangeTool();

            scope.put("request", prompt);
            scope.put("initialBalances", bankTool.getAllBalances());

            // Create supervisor to analyze the request
            BankSupervisor supervisor = AiServices.create(BankSupervisor.class, plannerModel);
            
            events.add(publishEvent(AgentEvent.agentInvoked("supervisor", "bankSupervisor", "Analyzing request...")));
            String analysis = supervisor.analyze(prompt);
            events.add(publishEvent(AgentEvent.agentCompleted("supervisor", "bankSupervisor", truncate(analysis))));
            events.add(publishEvent(AgentEvent.stateUpdated("supervisor", "analysis", analysis)));

            // Determine operation type and execute
            String response;
            String upperAnalysis = analysis.toUpperCase();

            if (upperAnalysis.contains("WITHDRAW")) {
                WithdrawAgent agent = AiServices.builder(WithdrawAgent.class)
                        .chatModel(chatModel)
                        .tools(bankTool)
                        .build();
                events.add(publishEvent(AgentEvent.agentInvoked("supervisor", "withdrawAgent", "Processing withdrawal...")));
                response = agent.processWithdrawal(prompt);
                events.add(publishEvent(AgentEvent.agentCompleted("supervisor", "withdrawAgent", truncate(response))));
            } else if (upperAnalysis.contains("CREDIT") || upperAnalysis.contains("DEPOSIT")) {
                CreditAgent agent = AiServices.builder(CreditAgent.class)
                        .chatModel(chatModel)
                        .tools(bankTool)
                        .build();
                events.add(publishEvent(AgentEvent.agentInvoked("supervisor", "creditAgent", "Processing deposit...")));
                response = agent.processCredit(prompt);
                events.add(publishEvent(AgentEvent.agentCompleted("supervisor", "creditAgent", truncate(response))));
            } else if (upperAnalysis.contains("EXCHANGE")) {
                ExchangeAgent agent = AiServices.builder(ExchangeAgent.class)
                        .chatModel(chatModel)
                        .tools(exchangeTool)
                        .build();
                events.add(publishEvent(AgentEvent.agentInvoked("supervisor", "exchangeAgent", "Processing exchange...")));
                response = agent.processExchange(prompt);
                events.add(publishEvent(AgentEvent.agentCompleted("supervisor", "exchangeAgent", truncate(response))));
            } else {
                // Balance inquiry - use the tool directly
                response = "Account Balances:\n";
                for (var entry : bankTool.getAllBalances().entrySet()) {
                    response += "- " + entry.getKey() + ": $" + entry.getValue() + "\n";
                }
            }

            scope.put("response", response);
            scope.put("finalBalances", bankTool.getAllBalances());

            events.add(publishEvent(AgentEvent.completed("supervisor", response)));
            return ExecutionResult.success(executionId, "supervisor", response, events, scope, startTime);

        } catch (Exception e) {
            events.add(publishEvent(AgentEvent.error("supervisor", null, e.getMessage())));
            return ExecutionResult.error(executionId, "supervisor", e.getMessage(), events, startTime);
        }
    }

    /**
     * HUMAN-IN-THE-LOOP PATTERN: Agent proposes, human reviews, agent executes
     * Demonstrates interactive workflows requiring human input.
     */
    private ExecutionResult executeHumanInLoop(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("human-in-loop", "Starting human-in-the-loop workflow")));

            // Try to extract zodiac sign from prompt
            ZodiacExtractor extractor = AiServices.create(ZodiacExtractor.class, chatModel);
            
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

            // Generate horoscope
            HoroscopeAgent horoscopeAgent = AiServices.create(HoroscopeAgent.class, chatModel);
            
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
     * Agents are selected based on available state to achieve a goal.
     */
    private ExecutionResult executeGOAP(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("goap", "Starting GOAP workflow: Planning path to goal")));

            scope.put("prompt", prompt);
            scope.put("goal", "Create personalized astrology writeup");

            // Create agents
            GoalPlanner planner = AiServices.create(GoalPlanner.class, plannerModel);
            PersonExtractor personExtractor = AiServices.create(PersonExtractor.class, chatModel);
            SignExtractor signExtractor = AiServices.create(SignExtractor.class, chatModel);
            HoroscopeGenerator horoscopeGen = AiServices.create(HoroscopeGenerator.class, chatModel);
            StoryFinder storyFinder = AiServices.create(StoryFinder.class, chatModel);
            WriterAgent writer = AiServices.create(WriterAgent.class, chatModel);

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
     * Agents activate when their inputs become available in shared state.
     */
    private ExecutionResult executeP2P(String prompt) {
        String executionId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        List<AgentEvent> events = Collections.synchronizedList(new ArrayList<>());
        Map<String, Object> scope = new ConcurrentHashMap<>();

        try {
            events.add(publishEvent(AgentEvent.started("p2p", "Starting P2P workflow: Collaborative research network")));

            scope.put("topic", prompt);

            // Create peer agents
            LiteratureAgent literatureAgent = AiServices.create(LiteratureAgent.class, chatModel);
            HypothesisAgent hypothesisAgent = AiServices.create(HypothesisAgent.class, chatModel);
            CriticAgent criticAgent = AiServices.create(CriticAgent.class, chatModel);
            ValidationAgent validationAgent = AiServices.create(ValidationAgent.class, chatModel);
            ScorerAgent scorerAgent = AiServices.create(ScorerAgent.class, chatModel);
            SynthesizerAgent synthesizer = AiServices.create(SynthesizerAgent.class, chatModel);

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
