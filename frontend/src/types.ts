export interface PatternInfo {
  id: string
  name: string
  description: string
  category: string
  agents: string[]
  topology: {
    type: string
    edges: Array<{
      from: string
      to: string
      condition?: string
      label?: string
      bidirectional?: boolean
    }>
    maxIterations?: number
    hasHuman?: boolean
  }
  examplePrompt: string
}

export interface AgentEvent {
  eventId: string
  patternName: string
  agentName: string | null
  eventType: 'STARTED' | 'AGENT_INVOKED' | 'AGENT_COMPLETED' | 'STATE_UPDATED' | 'HUMAN_INPUT_REQUIRED' | 'HUMAN_INPUT_RECEIVED' | 'ERROR' | 'COMPLETED'
  message: string
  data: Record<string, unknown>
  timestamp: string
}

export interface ExecutionResult {
  executionId: string
  patternId: string
  status: 'COMPLETED' | 'ERROR' | 'PENDING_HUMAN_INPUT'
  result: string
  events: AgentEvent[]
  scopeSnapshot: Record<string, unknown>
  startTime: string
  endTime: string | null
  durationMs: number
}

export interface ExecutionRequest {
  patternId: string
  prompt: string
  parameters?: Record<string, unknown>
}
