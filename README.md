# AI Agents - LangChain4j Agentic Patterns Showcase

A showcase application demonstrating **8 agentic patterns** from LangChain4j with real-time visualization using D3.js and WebSocket streaming.

![AI Agents Screenshot](docs/screenshot.png)

## Table of Contents

- [Features](#features)
- [Patterns Demonstrated](#patterns-demonstrated)
- [Quick Start](#getting-started)
- [Azure Deployment](#azure-deployment)
- [Beginner's Guide to Agentic Patterns](#beginners-guide-to-agentic-patterns)
- [Technical Architecture](#technical-architecture)
- [API Reference](#api-endpoints)
- [License](#license)

## Features

- **8 Agentic Patterns** with interactive visualizations
- **Real-time WebSocket** streaming of agent events
- **D3.js** animated topology graphs
- **Dark-themed UI** with Tailwind CSS
- **Azure OpenAI** integration via LangChain4j

## Patterns Demonstrated

### High-Level Architecture

![High-Level Architecture](http://www.plantuml.com/plantuml/png/fVRNb9swDL3rV2jpZTu4a9O0S3oo6qZoViBFg3XDLr0oNpNokSVXHwm6Yf99pOzEblAPBgxJfCQfySddOy-sD4ViH_wKCuCFsI65tdSlsKLgc5Gtl9YEnY-NMpYfnQr6WohbWIig_J3RvoJsV9JDC5Baa7a198WJOF-0vS1kXuilgpvDRP2cvnehxuZg30ZkXnoFPL3n6RK0dzzhUwSPV0Lqwa_qUGZ8JrwHqx2Syohm5oMFxkpMLpbAez-NXS-U2e6Bz_rjLeCqkFo6jPCpR8zOvgyG_A_jDSfee4KXQEmEQp-YF7HCcQcv_OhsPuwvLt46zLAqpYDgd0InJvjKAat932FqTEmxXzMFFVThyfvYsdG59NLoSOfGCp2tKp8MLXufv63aD3uEftPpQ3Jr5Qb0f-oOJdiNdMaiwxOqqS47ILPh_Dw7ZPY1FFit1EldzkR4yCuflVR7nzazmRJaS71sUxsH503Bowms6-Y3eUxnNMV0UiVZGoHMIBsMR6ODgfQJ-ACu7lTZb4BIJ1Mm5Nim3ygZ_liCTu-f9WT2PRlUs1AF3o6T-Wh4ynLhxVy4pqlPmSmB-rMSFnKObfL1CB1Z-NHifAQncyYyj6Lu_XBgozXggjH68yS5Ii01G9RJsyElNDuaccsrtEzY42ZDvWgF7JeMkVxjQFUwUuJuHaW220QN7TY06d2aJrhbx0bvQ_WbdcxxfHxV136JMxD5562lZ4NSdpgigw5bJNRhI34dJqLbYYrsu0j2O01MGw_cyuXKc7OoEKiyVCmcV_32OBIBd6QBNG2k4G2V8IWxl3ieVCrBB1njJSgQEA8fgy-D52t4RUtZ4q2IxzEC9xavzO7oGwiVeIlvOmzoSWSAPSJ6jF3jkt78fw)

This architecture showcases **8 distinct agentic patterns** implemented using LangChain4j, each optimized for different orchestration needs.

### Workflow Patterns (Deterministic Orchestration)

| Pattern | Description | Topology | Diagram |
|---------|-------------|----------|---------|
| **Sequential** | Agents invoked one after another in order | Chain | ![Sequential](http://www.plantuml.com/plantuml/png/dZPBbtswDIbvegrOvTTAFqwtVgw-DC2y7TwgLXpmLMoWqkieRDfw25eyHCfdWvhCS9__k5Sou8QYedg79Yk72hPsMSaVnq3vMeIedtg8tzEMXm-CCxEurjB_Z8RPMjg4_h08F-TQWaYz4D7GcJjVt1_xm0Gl2LIj2NLfgTxbdPAHmSl6uNx0aD08hF4E7bhSChsW5WOiqMSPbWN79AzVJhKyfaGnKOliBZjgMIVwcbP7fm1u3-L3g7bkG_qlrfhNOM5L7wu2PLpzOuX_BdXIuMNEYtzmFpptE3oqYI6UyhXDlx_Homrg0NumhsqIPaZRDEMcq9yffUGmGVQzL8rJSITTymVV-M-gIxpenXFLH3XxVJr-NT1lOcJqUX2ciaR70qu36HQM72RajE-5JlYVxcdZjPXoVidsOri6LP-fpngqHySOtu0YgpmPHGDLGTEuHOQaluFyYy17V2soowJyYRQFTOUoZe96Dcf5ANTYcwIjQydPo6W8f7OGaRwA-95ZEZbiZEZt6hR5Dbkepe4kzG_pFQ) |
| **Parallel** | Multiple agents run simultaneously | Fan-out | ![Parallel](http://www.plantuml.com/plantuml/png/bVNNb9swDL3rV3DuJQYSoEmwovNhaNE1t2HFPk5FD7RNO0JlyZDopP33o2zXdoHAB37oPfJRpu4Co-euMeoLH6khaNAHFV61bdFjAzkWr7V3nS0fnHEerrYYvwXiB1XYGT44ywPkfNRMC8C99-48sm-u8WuFSrFmQ_Ak58aQEYeZvIXVAe3GdQx_XSuE-j1VCgsW5r9AXkk91oVu0TIkB-fKx7eWPCeAASoJ4Wqf3-6qm8_An-6kaYFsYnwZ-qvjVro_uCbXlnwPL8ZARr_Ov91uVYmMOQaC5L4mK-Q_hWupx4boKRXFwub7oCmThq7MIPGuwQhPpvNByQVAHFqfkKkvMUc9Qam-rvD7dsI_e7nxVdIQmpCs4Tk62zVEsxvM_iVVQ7cLtJgfeNGLxGh3o41UVdJnRYt41DQUlerTfWXgCctZ1kejdJ7nA6smkhTobyeDxxNZbesng_Z5NSmLk6VrWE0S44xTYj9Om74sFU5dlHUSel0fGVw1_i2Y15DeqOhYO5tJdgPzhgHaEhZ7JMcAvrMQdCO7j5ZcF8x7z_pNQVLT3pSAlex2z8gdH2O-NSQvhKRmFKTUnbjxCf4H) |
| **Loop** | Iterative refinement until exit condition | Cycle | ![Loop](http://www.plantuml.com/plantuml/png/fVRdb9QwEHz3r1jSl4sEp35AVUVQtToEQuIBFfgBe_Emsc6Jg7O5U_89aztJUyjkdJLPOzM7uzfK3cDoeWytesUNtQQt-kENB9P16LGFPZaH2rux0ztnnYezCwyfFeIjVTha_uQ6TpBTY5hWgHvv3WliX5_juwqVYsOW4KtzPXxDZvIdbHaPpdz9cL1A68dcKSxZOD8H8kqU2JSmx44h-0wdeZRaBjhATR2cXe1vLqvr57CdN_IjYsp4fBn2QJURvYjz6bwANTLucSDI7qWNcL6XrqcIHcJJqeAO3txGFwWw601ZQNagOYyAezcylE6brs7CNOaITAGqAlxYUUR4J_FHm0TLXoP2WHE-gybzBcSy0vRMSdmwxFHM2SDnCW4_wM32XIE8CzJpxLtJ7u_ukS3dZbViOP8_OBZ-jQFfEekQk8RY2Vs1TW4sTxbfLw7Dk6Slx7z-Qk6on_bx1C1fSEuXibQUZpF_r9e0vXdH0vlLnD-2PSNWY60bUqdV-KplhhiIAgSCNmnAZv2_SK47F1RM3TC4agoSwBcOoTbHuUErgSvk_mILS-DFHYmFISVEipdbSDEHOqIdYy21C8NdbWFK9zzzILdvw20vOlNquPE0NM5qaInDNBAMKnUnx_Bi-A0) |
| **Conditional** | Routes to different agents based on conditions | Branch | ![Conditional](http://www.plantuml.com/plantuml/png/jZRRb9MwEMff_SmO9GWTNqkDNrWVhjY2kJBAQjA-wNW5JNYSO9iXlfLpOTtJ13RFWvJi3_3O9_f57JvA6LlravWGK2oIGvRBhUdjW_TYwBr1Y-ldZ_M7VzsPswuM_x5xTwV2NX92lntkUxmmPeDWe7cZoq_meFmgUmy4JrhzNjdsnMUaviMzeQsnHz1aXcGDayWk3J4qhZol9lcgr2RFNtq0aBmyH66TkAwwgE9DmL1bL94WV1PsgXRljcb605-WPCeexSZ7ma-Xi4sp_Y3yA7ahHGak3y-Wyyn6lcoJWMc5zIrLJc3XKkfGNQaC7LYkK1E_tWspgSGOlIo7gvMPo_gV_O7Ib1eQfYEKnwh0RYGhRWOzWAPzhEwDrNQQtB-OUsftX-qXAS106fx2j0x5Bdx4OaGTbCSyM8iafttZLHfNu2C4voaMxwJmCuR7Xi9VcZCdXDuV0ZMsCXmZGm3YyNGdgafQOhvodEKnyqxglxh6PjE5TbNQLTWe6B33cqA2nuNRseJIhgi8WuoAD0qHlP_RGRO8lJna5VBk30NHZSZXMvXQq6Xu8EFsPz8utU9CNldqzzo2nXVxYsqKwRVDG8PkGkfS2HIl5nPoL-jYlwHkRej42SPX0EFoSRusjXQ6xnuS3PemKMjLTLqfqwCFG4Tu7Gkp4G1LIaqFqEypGxnGt-wf) |

### Agentic Patterns (LLM-Driven Orchestration)

| Pattern | Description | Topology | Diagram |
|---------|-------------|----------|---------|
| **Supervisor** | LLM plans and orchestrates sub-agents | Star | ![Supervisor](http://www.plantuml.com/plantuml/png/hVRNbxoxEL37V0yXC0gkgiCiiEOVFNJeEikqiXrhMniHxWLX3treEPrra--n2aaJ9uL1vOd58zzjW2NR2yJL2Re7p4wgQ22YOQiZo8YMtsgPiVaFjJcqVRoGU_RfgFjRDovUflfSVpDjXlgKAHdaq2PNvp7gfIeMWWFTgnWRk34VxkWe0FrSEoZrpweeVe7wyWnEGHLrwi-GNHPHWcFFjtJC9A3loeNHgAZMd9zgZjvnu-tzyi9h97HG411C0paMY70Dg9n25qqPX2qKhe3QvPx_H3v_xvcoE-rQVO-0-BgtbtEQRCVI8DVXOVXK_YoxXyVcfA0LWUD0rFGanYtMJxN4Wa9gp1UGj6iFAqvgBymdCGXGG-luUAJX8pW0hXkFdoj7l5-R91G8oqXgcMaCRP20Dw-PkKcuM1g0B7PYyOklNA5u5NUlVPZs5OwSmur7J7b-LtrlsBQ-booZdcIaBGtZXpN3xtO1a6phZL0XnqHkNBpD1EBlc140OqeHJZmCczKGxfRvzp7w-qoX9WLYmvyO7grCasqHmq-85gpIcSA54H4iuE7Wk9v22qJdDqv7H_vbD8Q2YdZSPhQ884IbaNw00-ic_Ylk-k9z9LJqMu4Z8fkwTaumc72c5Sk5r1zKc3I5KsFw6M7PcCg2EmUMXQG9oQhUhmMhldvQItlbULt6OCF8rZTmezLOJ-_RwsUuynlBienpDzW630DT78LhSsBTOUym2FalxSeJmeCu1lMZXlFKiRNivDiTExeYCmMB_VthSshSZVshHaKyyjBytXmtjN26pX_E_wI) |
| **Human-in-the-Loop** | Pauses for human approval | Gated | ![Human-in-Loop](http://www.plantuml.com/plantuml/png/ZVQ7b9swEN75K67KkgCVkwwJUg9tjL5SIEOAtCgKdDmTZ4m1RLIkZdf99T2KkiWnsAfq9L14POo-RPSxaxvxKtbUErTogwhbbRx6bGGNclt52xn13jbWw9k1pt8M8YE22DXxkzUxQ_a1jjQDrLy3-4F9e4U3GxQi6tgQPHQtmlKbkq3LR2sdPGGM5A2cf8ZICr5ax7zqcCEEysgC3wJ5wbJRS-3QRCi-GNfFVUUmFoABdHqEs7v1jdzcDqSi9-lf12nFm7hav7m7PhV68lZSCJOUy4WjmMKIawwERY_R8llaRz00pJUQKR2Ub4cQSyi-1xhBB-D9wV-rNMoiZdI73l1GiYxlUq_BpL3n9p0Xvzvyh-I1FJkHnrgSYnExMfJmZjYH2_nBB4KuzLtCKHrhN_n3dJFFZmqedpr2AdAoUCS1ojCBXoTMXs9slZKuvKbAAY_osYNLmIDLETePlqNM0QaiGAX-d66tt32Jjfk0yKd5uZgT-sMY3eCIX8IP28FeNw3UuKOfBqHyxP1TeIBNx2W7Aeuc5VthdGTuYrE4CTuGE8byk9dVHRMnzwCcDnXDQ73kYgn9zIynOA5iPpL0-uMfkl3U1oDDLnDLU7GXSn67dAr9aQSGXKZJnIlKvnrasC5vK9ZHUf5zD0DyyIac4YGzlnzhtzOx7PRIFTaX0rau0WgkAbpki80QzpGP8KtTVZsMDZEiJYgnJPVAiHtepm_IPw) |

### Planning Patterns (Custom Planners)

| Pattern | Description | Topology | Diagram |
|---------|-------------|----------|---------|
| **GOAP** | Goal-Oriented Action Planning | DAG | ![GOAP](http://www.plantuml.com/plantuml/png/jVVNb9swDL3rV3DupQaaYt3Wos1haNCP7DIsQDHs0gsj07ZQRTIkZamL_vhRcuzYbQ9LDpGoR_LpkVSufUAXthstPoWaNgQbdF74J2UadLiBNcqnytmtKW6stg6OzjB-R4hbKnGrw701oYPsahVoBFg4Z3d774vPeF6iEEEFTbD8tVjBCkMgZ-D4drGEGdwqRzJQAQvZSq0kLB02dS4EysARfntyguMGJVWDJkDWBdFoDLkM0EPTreGI5LfLq6sp-kFV5u45uBQtwT1b4Ojr-vJLeTHF_rDOemkbWhLHw96hZvPHDg8Mae-VKfZMfNx_DP3jWCS3qMiEBN2l_YAtMOAaPUGWIEo-RB5d0LgSIgoBs-_DbeesREeToCHnrUGtXljHur_FoylZwJ_oZA1n56GGtXKhLrDNorbqb_LsognRh51kuEEttzriCmqIr2lkC1Wsz_zRwETa6Hg8FhBeX2GkTx4BnQiTZKka855aIL5Wz3hEM6JEgkaXmIF9koTH2YstFMpIJjuBbKW8JJ_lA3qk1_5MFDQNHCvFbelQa9JAzyS3QVkjgD8jqqkP5nDINwoZoUPQCEyW5PGe8VAhJix5jLjg-QR_4DxA0_mIeMpB2tNbll0P_g_NhEymzuc90WhX7HcCmzbUPp-CDyz3uLccuwTcN5OK7zt_Do6wmIoxJMwPpe_gYu_1nmP62TbsXSoegXyETCMTbz8ej06K8ZTsFM9GvCA_WVV7eno66ZB9_rFpGBpjeedUVQewJfR1Sg9UwihTzdkwgwVnb184bWVRsy4cJdmHAfOHCeuEnEGcGw-2CWrDPg0yyfhk5unwLjUpuykDVJb8zHATgXU8aem872YVk-5qHsfGeq_WmmI9IBIX4pqX8Z_gHw) |
| **P2P** | Peer-to-peer decentralized coordination | Mesh | ![P2P](http://www.plantuml.com/plantuml/png/jZVtb9MwEMff-1Mc3ZtGomggBiyCadMYYhKgSjy8vzrXxpprZ861Vb89ZydpkrYS9EFpzr_7n_3P2b2tGQNv1la94JLWBGsMtaqfjKsw4BoWqJ9WwW9cce-tD3DxGuN7QHymJW4sf_GOG2RXGqYBcBeC37XZ7y7xaolKsWFLMH8zhzkyU3Aw_U51Cb98JeBqDzOYE4UZ-1m8ZkqhZhH4XVNQIstGmwodw-SbFAvIm0B3K3I8AazBGoYL0m8_XF-P4a_7yssya1P3cCmx8_R9MHLTkzrdn2f_oDUFsvGu57cxdh7_qX2g0KN1uj-wooQLrAkmCTFa-Io6siKlohEwu0lLzYF9ZXQu9CMYByWh5VJjIGieblVPon9mi0wxQ8UsSU5akr6ThdF0EqgmDLqcvISlcYVxqzrr0ORSDh2iChoJ9vIRVIk-LVAe7JcSRiSyA9l6m0PPDGskVWW9r2AjftjGMLj5BJev3l8pkNeBbaRSrFU9nUkaeN5QXCpREbs8O8ponl4OHZqGB1MalBnXT4kp1Ej8w4dAYjYV2UlGbIiRH0cT6OuM6zepKdaqnM4gDcTi0rJulR1LDyQabcut4x97w9sCIjvsDyxG7g7Xmh3Sxs-1i_5X16yr4LedXcOscx3UMcedFGPkChW_6rCGtKfyxldBi4ESTEcNJ-eR8yIWzKpk8Mt2V0I60bS3Fhc-pNMgl-AM0i6uozuaZbOCXEuI0iEN__CgBQhoJVmuItCOPKwpxFxYUIlb45voYzryqN0KzxuZMO-BS9mdpbexJ-TTHaU5xJNVAg-xKCY1jU4OiqiiWUZ2hmXE7ZspNUz0BuIilbqVn_Ef4i8) |

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

**Example prompt:** *"Write a fantasy story for teenagers in a humorous style"*
- CreativeWriter → AudienceEditor → StyleEditor

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

**Example prompt:** *"Transfer 100 USD from Mario to Georgios, then convert 50 USD to EUR"*
- BankSupervisor delegates to: WithdrawAgent, CreditAgent, ExchangeAgent

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

**Example prompt:** *"Generate a personalized horoscope for someone born on March 15th"*
- GOAP calculates the dependency graph and executes: SignExtractor → (HoroscopeGenerator + StoryFinder in parallel) → WriterAgent

---

#### 8. P2P - Peer-to-Peer (Mesh)

**What it does:** Agents collaborate as equals, reacting to each other's outputs without a central controller.

**Real-world analogy:** A research lab:
- **LiteratureAgent** reviews existing research
- **HypothesisAgent** formulates testable hypotheses
- **CriticAgent** challenges weak hypotheses
- **ValidationAgent** refines based on critique
- **ScorerAgent** evaluates quality
- They iterate reactively until quality threshold is met

**When to use:** Research, brainstorming, when you want emergent collaboration.

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

### AgenticScope: Unified State Management

All 8 patterns in this showcase use **LangChain4j's `AgenticServices`** builders with `AgenticScope` for unified state management. The `AgenticScope` provides:

- **State sharing** between agents via `scope.readState()` / `scope.writeState()`
- **Output key mapping** via `@Agent(outputKey = "result")` 
- **Agent invocation tracking** for debugging
- **Real-time events** via `AgentListener` for WebSocket streaming

#### Two Equivalent Approaches: Programmatic vs Declarative

LangChain4j's Agentic framework offers **two equivalent and interchangeable approaches** for building agent workflows:

| Approach | Method | Best For |
|----------|--------|----------|
| **Programmatic** | Builder APIs (`sequenceBuilder()`, `loopBuilder()`, etc.) | Dynamic workflows, runtime configuration, complex orchestration |
| **Declarative** | Annotations + `createAgenticSystem()` | Simple, readable definitions, compile-time validation |

**Both approaches are fully equivalent** — you can achieve the same results with either, and you can even **mix them** in the same application (as this showcase demonstrates).

**Programmatic Example:**
```java
UntypedAgent workflow = AgenticServices.sequenceBuilder()
    .name("myWorkflow")
    .subAgents(agent1, agent2, agent3)
    .listener(listener)
    .build();
```

**Declarative Example:**
```java
@Agent(description = "Orchestrates multiple experts")
interface MyWorkflow {
    @Parallel  // or @Sequential, @Conditional, etc.
    String process(@State("input") String input);
}
MyWorkflow workflow = AgenticServices.createAgenticSystem(MyWorkflow.class, model);
```

#### Pattern Implementation Summary

| Pattern | Approach | API Used |
|---------|----------|----------|
| **Sequence** | Programmatic | `AgenticServices.sequenceBuilder()` |
| **Parallel** | Declarative | `AgenticServices.createAgenticSystem()` with `@Parallel` |
| **Loop** | Programmatic | `AgenticServices.loopBuilder()` |
| **Conditional** | Declarative | `AgenticServices.createAgenticSystem()` with `@Conditional` |
| **Supervisor** | Programmatic | `AgenticServices.supervisorBuilder()` |
| **Human-in-Loop** | Programmatic | `AgenticServices.agentBuilder()` |
| **GOAP** | Programmatic | `AgenticServices.plannerBuilder()` + `GoalOrientedPlanner` |
| **P2P** | Programmatic | `AgenticServices.plannerBuilder()` + `P2PPlanner` |

> **Note:** This showcase intentionally uses both approaches to demonstrate their equivalence. You could rewrite the Parallel pattern using `sequenceBuilder()` + manual parallel execution, or rewrite the Sequence pattern using annotations — the choice is purely stylistic.

#### Real-Time WebSocket Events with AgentListener

The key to real-time UI updates is the `AgentListener` interface. All patterns that use builders like `sequenceBuilder()`, `loopBuilder()`, or `plannerBuilder()` can attach a listener:

```java
// Sequence with real-time WebSocket events
UntypedAgent novelCreator = AgenticServices.sequenceBuilder()
    .name("novelCreator")
    .subAgents(writer, audienceEditor, styleEditor)
    .listener(webSocketListener)  // ← Receives events as agents execute
    .outputKey("story")
    .build();

ResultWithAgenticScope<String> result = novelCreator.invokeWithAgenticScope(
    Map.of("topic", topic, "audience", audience, "style", style));
```

The `WebSocketAgentListener` implements `AgentListener` to capture:
- `beforeAgentExecution()` → Publish "AGENT_INVOKED" event
- `afterAgentExecution()` → Publish "AGENT_COMPLETED" event with output
- State changes → Publish "STATE_UPDATED" events

#### Planning Patterns with Custom Planners

GOAP and P2P use `AgenticServices.plannerBuilder()` with custom planner implementations:

```java
// GOAP - Goal-Oriented Action Planning
UntypedAgent goapWorkflow = AgenticServices.plannerBuilder()
    .subAgents(signExtractor, horoscopeGenerator, storyFinder, writer)
    .outputKey("writeup")  // The goal state
    .planner(GoalOrientedPlanner::new)  // Calculates shortest path to goal
    .listener(listener)
    .build();

// P2P - Peer-to-Peer Reactive Collaboration
UntypedAgent p2pWorkflow = AgenticServices.plannerBuilder()
    .subAgents(literatureAgent, hypothesisAgent, criticAgent, scorerAgent)
    .outputKey("hypothesis")
    .planner(() -> new P2PPlanner(plannerModel, 10, scope -> 
        scope.readState("score", 0.0) >= 0.75  // Exit when score threshold reached
    ))
    .listener(listener)
    .build();
```

#### Accessing AgenticScope State

All patterns can access the shared state after execution:

```java
ResultWithAgenticScope<String> result = workflow.invokeWithAgenticScope(inputs);

// Access the result
String output = result.result();

// Access intermediate state from AgenticScope
AgenticScope scope = result.agenticScope();
Double score = scope.readState("score", 0.0);
String hypothesis = scope.readState("hypothesis", "");
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
AZURE_OPENAI_DEPLOYMENT=gpt-5
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
   - Azure OpenAI with gpt-5-mini and text-embedding-3-small deployments
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
│  │                  │     │ │ AI Agents App          │   │  │
│  └──────────────────┘     │ │ (Java 21 + React)      │   │  │
│                           │ └────────────────────────┘   │  │
│  ┌──────────────────┐     └──────────────────────────────┘  │
│  │ Azure OpenAI     │                │                      │
│  │ - gpt-5-mini     │◀───────────────┘                      │
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
| `AZURE_OPENAI_DEPLOYMENT` | Chat model deployment name | `gpt-5` |
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
- **Subscribe (global)**: `/topic/events`
- **Subscribe (pattern)**: `/topic/patterns/{patternId}`

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
- **Dark-themed** interface

## License

MIT License - see [LICENSE](LICENSE) for details.

## Acknowledgments

- [LangChain4j](https://docs.langchain4j.dev/) - Java LLM framework
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [D3.js](https://d3js.org/) - Data visualization
