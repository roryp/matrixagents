import { PatternInfo, ExecutionResult, ExecutionRequest } from './types'

const API_BASE = '/api'

export async function fetchPatterns(): Promise<PatternInfo[]> {
  const response = await fetch(`${API_BASE}/patterns`)
  if (!response.ok) throw new Error('Failed to fetch patterns')
  return response.json()
}

export async function fetchPattern(patternId: string): Promise<PatternInfo> {
  const response = await fetch(`${API_BASE}/patterns/${patternId}`)
  if (!response.ok) throw new Error('Failed to fetch pattern')
  return response.json()
}

export async function executePattern(request: ExecutionRequest): Promise<ExecutionResult> {
  const response = await fetch(`${API_BASE}/patterns/${request.patternId}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!response.ok) throw new Error('Failed to execute pattern')
  return response.json()
}

export async function provideHumanInput(requestId: string, input: string): Promise<void> {
  const response = await fetch(`${API_BASE}/human-input/${requestId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ input }),
  })
  if (!response.ok) throw new Error('Failed to provide human input')
}

export async function getPendingHumanInputs(): Promise<Record<string, string>> {
  const response = await fetch(`${API_BASE}/human-input/pending`)
  if (!response.ok) throw new Error('Failed to fetch pending inputs')
  return response.json()
}
