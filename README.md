# Matrix Agents - LangChain4j Agentic Patterns Showcase

A showcase application demonstrating **8 agentic patterns** from LangChain4j with real-time visualization using D3.js and WebSocket streaming.

![Matrix Agents Screenshot](docs/screenshot.png)

## Features

- **8 Agentic Patterns** with interactive visualizations
- **Real-time WebSocket** streaming of agent events
- **D3.js** animated topology graphs
- **Matrix-themed UI** with Tailwind CSS
- **Azure OpenAI** integration via LangChain4j

## Patterns Demonstrated

### Workflow Patterns (Deterministic Orchestration)
| Pattern | Description | Topology |
|---------|-------------|----------|
| **Sequential** | Agents invoked one after another in order | Chain |
| **Parallel** | Multiple agents run simultaneously | Fan-out |
| **Loop** | Iterative refinement until exit condition | Cycle |
| **Conditional** | Routes to different agents based on conditions | Branch |

### Agentic Patterns (LLM-Driven Orchestration)
| Pattern | Description | Topology |
|---------|-------------|----------|
| **Supervisor** | LLM plans and orchestrates sub-agents | Star |
| **Human-in-the-Loop** | Pauses for human approval | Gated |

### Planning Patterns (Custom Planners)
| Pattern | Description | Topology |
|---------|-------------|----------|
| **GOAP** | Goal-Oriented Action Planning | DAG |
| **P2P** | Peer-to-peer decentralized coordination | Mesh |

## Beginner's Guide to Agentic Patterns

New to AI agents? This guide explains each pattern in plain English with real-world analogies.

### What is an "Agent"?

An **agent** is an AI that can take actions autonomously. Unlike a simple chatbot that just responds to questions, an agent can:
- Break down complex tasks into steps
- Use tools and call other agents
- Make decisions based on context
- Remember state across interactions

Think of agents like specialized workers in a factory - each has a specific job, and they work together to produce a result.

---

### Workflow Patterns

These patterns follow **deterministic rules** - you define exactly how agents interact.

#### 1. Sequential Workflow (Chain)

**What it does:** Agents run one after another, like an assembly line.

**Real-world analogy:** Writing a book where:
1. **Researcher** gathers facts
2. **Writer** creates the draft
3. **Editor** polishes the final text

**When to use:** When each step depends on the previous step's output.

**Example prompt:** *"Write a blog post about renewable energy"*
- creativeWriter → audienceEditor → styleEditor

---

#### 2. Parallel Workflow (Fan-out)

**What it does:** Multiple agents run at the same time, then results are combined.

**Real-world analogy:** Getting opinions from multiple experts simultaneously:
- **Technical Expert** evaluates feasibility
- **Business Expert** evaluates cost
- **Creative Expert** evaluates user appeal

**When to use:** When you need diverse perspectives quickly.

**Example prompt:** *"Evaluate this startup idea: AI-powered pet translator"*

---

#### 3. Loop Workflow (Cycle)

**What it does:** Agents iterate and refine until a quality threshold is met.

**Real-world analogy:** Code review cycles:
1. **Generator** writes code
2. **Critic** reviews and finds issues
3. **Refiner** improves based on feedback
4. Repeat until the critic approves

**When to use:** When quality matters more than speed.

**Example prompt:** *"Write a haiku about coding"* (iterates until the critic gives 8+/10)

---

#### 4. Conditional Routing (Branch)

**What it does:** Routes to different specialist agents based on the input.

**Real-world analogy:** Hospital triage:
- Heart problem → **Cardiologist**
- Broken bone → **Orthopedist**
- Skin issue → **Dermatologist**

**When to use:** When different inputs need different expertise.

**Example prompt:** *"I have chest pain"* → routes to medical expert

---

### Agentic Patterns

These patterns use **LLM intelligence** to decide how agents interact.

#### 5. Supervisor Agent (Star)

**What it does:** A "boss" agent plans and delegates to worker agents.

**Real-world analogy:** A project manager who:
1. Receives a complex request
2. Breaks it into subtasks
3. Assigns each subtask to the right specialist
4. Combines their outputs into a final deliverable

**When to use:** Complex tasks requiring multiple skills.

**Example prompt:** *"Research Tesla stock and calculate if I should invest $10,000"*
- Supervisor delegates to: researcher, calculator, writer

---

#### 6. Human-in-the-Loop (Gated)

**What it does:** Pauses execution to get human input or approval.

**Real-world analogy:** Expense approval workflow:
1. **System** prepares an expense report
2. **Human** reviews and approves
3. **System** processes the approved expense

**When to use:** High-stakes decisions, legal/compliance requirements, or when AI needs human judgment.

**Example prompt:** *"What is the zodiac"*
- Asks human: "What is your zodiac sign?"
- Uses human's answer to generate personalized horoscope

---

### Planning Patterns

These patterns use **advanced planning algorithms** for complex orchestration.

#### 7. GOAP - Goal-Oriented Action Planning (DAG)

**What it does:** Finds the optimal sequence of agents to reach a goal, like GPS finding the shortest route.

**Real-world analogy:** Planning a dinner party:
- **Goal:** Serve a gourmet meal
- **Available actions:** Buy ingredients, prep vegetables, cook main dish, set table, plate food
- **GOAP finds:** The most efficient order considering dependencies (can't cook before buying ingredients)

**When to use:** Complex goals with many possible paths.

**Example prompt:** *"Create a comprehensive market analysis report"*
- GOAP calculates: dataCollector → analyzer → visualizer → reporter

---

#### 8. P2P - Peer-to-Peer (Mesh)

**What it does:** Agents collaborate as equals, reacting to each other's outputs without a central controller.

**Real-world analogy:** A writers' room:
- **Idea Generator** throws out concepts
- **Critic** challenges weak ideas
- **Validator** checks feasibility
- **Scorer** ranks the best options
- They iterate until consensus emerges

**When to use:** Creative tasks, brainstorming, when you want emergent behavior.

**Example prompt:** *"Generate and evaluate startup ideas for AI in healthcare"*

---

### Choosing the Right Pattern

| Situation | Recommended Pattern |
|-----------|---------------------|
| Simple pipeline with clear steps | Sequential |
| Need multiple perspectives fast | Parallel |
| Quality is critical, time isn't | Loop |
| Different inputs need different handling | Conditional |
| Complex task, unclear how to break down | Supervisor |
| Need human approval or input | Human-in-the-Loop |
| Many dependencies, need optimal path | GOAP |
| Creative/brainstorming, want collaboration | P2P |

---

## Technical Architecture

### AgenticScope: When It's Used vs Manual Orchestration

LangChain4j's `langchain4j-agentic` module provides `AgenticScope` for sharing state between agents. This showcase uses **both approaches** depending on the pattern's needs:

#### Pattern Implementation Summary

| Pattern | Implementation | Uses AgenticScope? | Why? |
|---------|---------------|-------------------|------|
| **Sequence** | Manual `AiServices.builder()` | ❌ Manual | Real-time step-by-step events |
| **Parallel** | `AgenticServices.createAgenticSystem()` | ✅ Internal | Concurrent execution handled by framework |
| **Loop** | Manual `AiServices.builder()` | ❌ Manual | Per-iteration WebSocket updates |
| **Conditional** | `AgenticServices.createAgenticSystem()` | ✅ Internal | Declarative routing via annotations |
| **Supervisor** | `AgenticServices.supervisorBuilder()` | ✅ Internal | LLM-driven planning needs framework |
| **Human-in-Loop** | Manual `AiServices.builder()` | ❌ Manual | Human interaction points need control |
| **GOAP** | Manual `AiServices.builder()` | ❌ Manual | Custom planning algorithm |
| **P2P** | Manual `AiServices.builder()` | ❌ Manual | Custom peer coordination |

#### How AgenticScope Works (Patterns that use it)

For **Supervisor**, **Parallel**, and **Conditional** patterns, we use `AgenticServices` builders:

```java
// Supervisor - LLM plans and coordinates sub-agents autonomously
SupervisorAgent supervisor = AgenticServices.supervisorBuilder()
    .chatModel(plannerModel)
    .subAgents(withdrawAgent, creditAgent, exchangeAgent)
    .responseStrategy(SupervisorResponseStrategy.SUMMARY)
    .build();

String response = supervisor.invoke(prompt);  // AgenticScope created internally
```

The `AgenticScope` is automatically created when `invoke()` is called. It:
- Stores each sub-agent's output via `@Agent(outputKey = "result")`
- Provides state sharing between agents via `scope.readState()` / `scope.writeState()`
- Tracks agent invocations for debugging

#### Why Manual Orchestration (Patterns that don't use it)

For **Loop**, **Sequence**, **Human-in-Loop**, **GOAP**, and **P2P** patterns:

```java
// Loop - Manual orchestration for real-time UI updates
Map<String, Object> scope = new ConcurrentHashMap<>();

for (int iteration = 1; iteration <= maxIterations; iteration++) {
    // Generate
    String story = generator.generateStory(topic);
    scope.put("story", story);
    events.add(publishEvent(AgentEvent.agentCompleted("loop", "creativeWriter", story)));
    
    // Score  
    double score = scorer.scoreStyle(story, style);
    scope.put("score", score);
    events.add(publishEvent(AgentEvent.stateUpdated("loop", "score", score)));  // ← WebSocket event!
    
    if (score >= 0.8) break;
    
    // Refine...
}
```

**Why not use AgenticServices.loopBuilder()?**
- `loopBuilder()` runs internally without per-iteration callbacks
- We need `publishEvent()` after **each agent call** for real-time UI
- The manual `ConcurrentHashMap` mirrors AgenticScope but with full control

#### Trade-offs

| Feature | AgenticServices (Supervisor, etc.) | Manual Orchestration (Loop, etc.) |
|---------|-----------------------------------|----------------------------------|
| Code simplicity | ✅ Declarative, less code | ❌ More boilerplate |
| Framework features | ✅ Error recovery, persistence | ❌ Must implement yourself |
| Real-time events | ❌ Single result only | ✅ Per-step WebSocket updates |
| UI visualization | ❌ Final state only | ✅ Incremental progress |
| Custom logic | ❌ Limited to framework | ✅ Full control |

#### Why Supervisor Works Without Explicit AgenticScope

Looking at the code, you might wonder how `SupervisorAgent` works:

```java
SupervisorAgent supervisor = AgenticServices.supervisorBuilder()
    .subAgents(withdrawAgent, creditAgent, exchangeAgent)
    .build();

String response = supervisor.invoke(prompt);  // Just returns a string!
```

The `AgenticScope` is **internal** to the framework:
1. `invoke()` creates an ephemeral `AgenticScope`
2. The supervisor LLM plans which sub-agents to call
3. Each sub-agent writes results to the scope (internally)
4. The supervisor reads results and produces a summary
5. The scope is discarded after `invoke()` returns

We don't see it because we only need the final response. If we wanted the scope:

```java
// To access the AgenticScope (not used in this showcase)
ResultWithAgenticScope<String> result = supervisor.invokeWithScope(prompt);
AgenticScope agenticScope = result.agenticScope();
agenticScope.readState("withdrawResult");  // Access internal state
```

---

## Tech Stack

### Backend
- **Java 21** with Virtual Threads
- **Spring Boot 4.0.1**
- **LangChain4j 1.10.0** (Core)
- **LangChain4j Agentic 1.10.0-beta18** (Agent framework)
- **LangChain4j OpenAI Official 1.10.0-beta18** (Azure OpenAI)
- **Spring Dotenv** for `.env` file support
- **WebSocket** (STOMP over SockJS)

### Frontend
- **React 18** with TypeScript
- **Vite 5** build tool
- **D3.js** for visualizations
- **Tailwind CSS** for styling
- **React Router** for navigation

## Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- Maven 3.9+
- Azure OpenAI API access

### Backend Setup

1. Create a `.env` file in the project root with your Azure OpenAI credentials:
```env
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
AZURE_OPENAI_API_KEY=your-api-key
AZURE_OPENAI_DEPLOYMENT=gpt-4o
AZURE_OPENAI_EMBEDDING_DEPLOYMENT=text-embedding-3-small
```

> **Note:** The `.env` file is excluded from git via `.gitignore` to keep your credentials secure.

2. Run the backend:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## Azure Deployment

This application includes full Azure infrastructure-as-code for one-click deployment using the Azure Developer CLI (azd).

### Prerequisites
- [Azure Developer CLI (azd)](https://learn.microsoft.com/azure/developer/azure-developer-cli/install-azd)
- [Azure subscription](https://azure.microsoft.com/free/)
- [Docker](https://docs.docker.com/get-docker/) (for container builds)

### Deploy to Azure

1. **Login to Azure**
   ```bash
   azd auth login
   ```

2. **Initialize environment** (first time only)
   ```bash
   azd init
   ```

3. **Provision infrastructure and deploy**
   ```bash
   azd up
   ```

   This will create:
   - Azure Resource Group
   - Azure Container Registry
   - Azure OpenAI with gpt-5-nano (fastest) and text-embedding-3-small deployments
   - Azure Container Apps Environment
   - Azure Container App (auto-scaling 1-3 replicas)
   - Log Analytics Workspace + Application Insights

4. **Access your app**
   
   After deployment, the Container App URL will be displayed in the terminal output.

### Azure Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Azure Resource Group                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐     ┌──────────────────────────────┐  │
│  │ Container        │     │ Container Apps Environment   │  │
│  │ Registry         │────▶│ ┌────────────────────────┐   │  │
│  │                  │     │ │ Matrix Agents App      │   │  │
│  └──────────────────┘     │ │ (Java 21 + React)      │   │  │
│                           │ └────────────────────────┘   │  │
│  ┌──────────────────┐     └──────────────────────────────┘  │
│  │ Azure OpenAI     │                │                      │
│  │ - gpt-5-nano     │◀───────────────┘                      │
│  │ - text-embedding │                                       │
│  └──────────────────┘     ┌──────────────────────────────┐  │
│                           │ Monitoring                   │  │
│                           │ - Log Analytics              │  │
│                           │ - Application Insights       │  │
│                           └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Clean Up Resources

To delete all Azure resources:
```bash
azd down
```

## Project Structure

```
matrixagents/
├── src/main/java/com/matrixagents/
│   ├── MatrixAgentsApplication.java    # Spring Boot entry
│   ├── agents/                         # Agent interfaces
│   │   ├── SequenceAgents.java
│   │   ├── ParallelAgents.java
│   │   ├── LoopAgents.java
│   │   ├── ConditionalAgents.java
│   │   ├── SupervisorAgents.java
│   │   ├── HumanInLoopAgents.java
│   │   ├── GOAPAgents.java
│   │   └── P2PAgents.java
│   ├── config/
│   │   ├── LangChainConfig.java        # LLM configuration
│   │   └── WebSocketConfig.java        # WebSocket setup
│   ├── controller/
│   │   └── PatternController.java      # REST endpoints
│   └── service/
│       └── PatternExecutionService.java # Pattern orchestration
├── frontend/
│   ├── src/
│   │   ├── components/                 # React components
│   │   ├── context/                    # WebSocket context
│   │   ├── pages/                      # Page components
│   │   └── types/                      # TypeScript types
│   └── package.json
└── pom.xml
```

## Configuration

### Environment Variables

The application reads Azure OpenAI configuration from a `.env` file in the project root:

| Variable | Description | Example |
|----------|-------------|---------|
| `AZURE_OPENAI_ENDPOINT` | Your Azure OpenAI resource endpoint | `https://your-resource.openai.azure.com/` |
| `AZURE_OPENAI_API_KEY` | Your Azure OpenAI API key | `your-api-key` |
| `AZURE_OPENAI_DEPLOYMENT` | Chat model deployment name | `gpt-4o` |
| `AZURE_OPENAI_EMBEDDING_DEPLOYMENT` | Embedding model deployment name | `text-embedding-3-small` |

### Azure OpenAI

The application uses `langchain4j-open-ai-official` which wraps the official OpenAI Java SDK with Azure support:

```java
OpenAiOfficialChatModel.builder()
    .baseUrl(endpoint)
    .apiKey(apiKey)
    .modelName(deploymentName)
    .isAzure(true)
    .build();
```

### WebSocket

Events are streamed via STOMP over SockJS:
- **Endpoint**: `/ws`
- **Subscribe**: `/topic/events/{executionId}`
- **Send**: `/app/execute`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/patterns` | List all patterns |
| GET | `/api/patterns/{id}` | Get pattern details |
| POST | `/api/patterns/{id}/execute` | Execute a pattern |

## UI Features

- **Real-time visualization** of agent execution
- **Event log** with timestamped agent activities
- **Scope view** showing shared state
- **Animated D3 graphs** with agent highlighting
- **Matrix-style** dark theme

## License

MIT License - see [LICENSE](LICENSE) for details.

## Acknowledgments

- [LangChain4j](https://docs.langchain4j.dev/) - Java LLM framework
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [D3.js](https://d3js.org/) - Data visualization
