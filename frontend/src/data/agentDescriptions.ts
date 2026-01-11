// Agent descriptions for all patterns - used for tooltips
export const agentDescriptions: Record<string, Record<string, string>> = {
  sequence: {
    CreativeWriter: "Generates an initial story based on the given topic. This is the first agent in the sequence that creates the raw creative content.",
    AudienceEditor: "Adapts the story for a specific target audience (e.g., children, professionals). Modifies tone, vocabulary, and complexity.",
    StyleEditor: "Applies a specific writing style to the story (e.g., humorous, dramatic, formal). The final agent that polishes the output."
  },
  parallel: {
    start: "Virtual entry point that initiates parallel execution of multiple agents simultaneously.",
    FoodExpert: "Suggests food and meal options based on the user's mood or occasion. Runs in parallel with MovieExpert for simultaneous suggestions.",
    MovieExpert: "Recommends movies based on the user's mood or occasion. Runs in parallel with FoodExpert for simultaneous suggestions.",
    combiner: "Virtual aggregation point that combines results from all parallel agents into a unified response."
  },
  loop: {
    CreativeWriter: "Creates an initial story for refinement. The starting point of the iterative improvement loop.",
    StyleScorer: "Evaluates how well the story matches the target style, returning a score from 0.0 to 1.0. Determines if more iterations are needed.",
    StyleEditor: "Improves the story to better match the target style based on scorer feedback. Refines until quality threshold is met."
  },
  conditional: {
    CategoryRouter: "Classifies user requests into categories: MEDICAL, LEGAL, TECHNICAL, or UNKNOWN. Routes to the appropriate specialist agent.",
    MedicalExpert: "Provides medical-related information and guidance. Activated when the router detects a health-related query.",
    LegalExpert: "Provides legal-related information and guidance. Activated when the router detects a law-related query.",
    TechnicalExpert: "Provides technical and programming information. Activated when the router detects a technology-related query."
  },
  supervisor: {
    BankSupervisor: "LLM-based supervisor that dynamically plans and coordinates sub-agents. Decides which agent to call based on the user's banking request.",
    WithdrawAgent: "Handles withdrawal requests using the BankTool. Called by the Supervisor for debit operations.",
    CreditAgent: "Handles deposit and credit requests using the BankTool. Called by the Supervisor for credit operations.",
    ExchangeAgent: "Handles currency exchange operations using the ExchangeTool. Called by the Supervisor for forex operations."
  },
  "human-in-loop": {
    ProposalAgent: "Creates proposals that require human approval. Generates initial recommendations or actions for review.",
    Human: "Human reviewer who approves or rejects proposals. Provides feedback and final authorization.",
    ExecutionAgent: "Executes approved proposals incorporating human feedback. Only acts after receiving human authorization."
  },
  goap: {
    SignExtractor: "Extracts the zodiac sign from the user's prompt. The first planning step in the GOAP chain.",
    HoroscopeGenerator: "Creates a horoscope reading for the extracted zodiac sign. Provides astrological predictions.",
    StoryFinder: "Finds mythology and stories related to the zodiac sign. Adds cultural and historical context.",
    WriterAgent: "Composes the final writeup combining horoscope and mythology. Synthesizes all gathered information."
  },
  p2p: {
    LiteratureAgent: "Searches and summarizes relevant research literature on the topic. Gathers foundational knowledge for hypothesis formation.",
    HypothesisAgent: "Formulates hypotheses based on research findings. Creates testable propositions from gathered data.",
    CriticAgent: "Critiques hypotheses and identifies weaknesses or gaps. Provides adversarial review of propositions.",
    ValidationAgent: "Validates or reformulates hypotheses based on critique. Refines propositions for accuracy.",
    ScorerAgent: "Scores the quality of the final hypothesis from 0.0 to 1.0. Determines if the result meets quality standards."
  }
};

// Helper function to get description for an agent
export function getAgentDescription(patternId: string, agentName: string): string {
  const pattern = agentDescriptions[patternId.toLowerCase()];
  if (!pattern) {
    return `Agent in the ${patternId} pattern`;
  }
  return pattern[agentName] || `Agent in the ${patternId} pattern`;
}
