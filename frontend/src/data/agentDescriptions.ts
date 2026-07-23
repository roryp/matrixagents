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
    ZodiacExtractor: "Extracts a zodiac sign from the prompt. If none is present, the workflow pauses and asks the user.",
    Human: "Provides the missing zodiac sign through the real-time input modal so the workflow can resume.",
    HoroscopeAgent: "Generates the personalized horoscope after the zodiac sign is available."
  },
  goap: {
    CityParser: "Extracts the list of cities from the user's travel request. The entry point that feeds both parallel branches.",
    DistanceCalculator: "Calculates distances between all city pairs. Runs in PARALLEL with AttractionFinder (both depend only on cities).",
    AttractionFinder: "Finds top tourist attractions for each city. Runs in PARALLEL with DistanceCalculator (both depend only on cities).",
    RouteOptimizer: "Solves the Travelling Salesman Problem to find the optimal route visiting all cities. Depends on distances.",
    ItineraryPlanner: "Creates the final day-by-day travel itinerary. CONVERGES both parallel branches (route + attractions)."
  },
  p2p: {
    LiteratureAgent: "Searches and summarizes relevant research literature on the topic. Gathers foundational knowledge for hypothesis formation.",
    HypothesisAgent: "Formulates hypotheses based on research findings. Creates testable propositions from gathered data.",
    CriticAgent: "Critiques hypotheses and identifies weaknesses or gaps. Provides adversarial review of propositions.",
    ValidationAgent: "Validates or reformulates hypotheses based on critique. Refines propositions for accuracy.",
    ScorerAgent: "Scores the quality of the final hypothesis from 0.0 to 1.0. Determines if the result meets quality standards."
  },
  "parallel-mapper": {
    start: "Virtual entry point that fans the batch of items out to parallel worker instances.",
    ReviewAnalyzer: "Analyzes a single customer review, classifying sentiment and extracting the key point. The mapper creates one instance per review and runs them concurrently.",
    combiner: "Virtual aggregation point that collects every worker result into a single ordered list."
  },
  debate: {
    Judge: "Impartial moderator and the last sub-agent. Reads the full debate history and synthesizes a balanced final verdict.",
    Proponent: "Argues in favor of the proposition, emphasizing benefits and opportunities. Refines its stance after reading the other debaters.",
    Skeptic: "Argues against the proposition, emphasizing risks, costs, and unintended consequences. Refines its stance each round.",
    Pragmatist: "Argues from practical outcomes, feasibility, and real-world trade-offs. Refines its stance after weighing the other arguments."
  },
  voting: {
    start: "Virtual entry point that dispatches the same proposal to every voter concurrently.",
    GrowthAnalyst: "Votes BUY/HOLD/SELL from a growth and upside perspective. One independent ballot in the ensemble.",
    ValueAnalyst: "Votes BUY/HOLD/SELL from a valuation and fundamentals perspective. One independent ballot in the ensemble.",
    RiskAnalyst: "Votes BUY/HOLD/SELL from a risk-management perspective. One independent ballot in the ensemble.",
    combiner: "Virtual aggregation point that tallies the ballots and applies the majority voting strategy."
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
