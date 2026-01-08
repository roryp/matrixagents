import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import ReactMarkdown from 'react-markdown'
import { 
  ArrowLeft, 
  Play, 
  Loader2, 
  Terminal, 
  Eye, 
  Database 
} from 'lucide-react'
import { PatternInfo, AgentEvent, ExecutionResult } from '../types'
import { executePattern, provideHumanInput } from '../api'
import { useWebSocket } from '../context/WebSocketContext'
import WorkflowVisualization from '../components/WorkflowVisualization'
import EventLog from '../components/EventLog'
import ScopeInspector from '../components/ScopeInspector'
import HumanInputModal from '../components/HumanInputModal'

interface PatternDetailProps {
  patterns: PatternInfo[]
}

export default function PatternDetail({ patterns }: PatternDetailProps) {
  const { patternId } = useParams<{ patternId: string }>()
  const pattern = patterns.find(p => p.id === patternId)

  const [prompt, setPrompt] = useState('')
  const [isExecuting, setIsExecuting] = useState(false)
  const [result, setResult] = useState<ExecutionResult | null>(null)
  const [localEvents, setLocalEvents] = useState<AgentEvent[]>([])
  const [scope, setScope] = useState<Record<string, unknown>>({})
  const [humanInputRequest, setHumanInputRequest] = useState<{ requestId: string; prompt: string } | null>(null)
  const [handledRequestIds, setHandledRequestIds] = useState<Set<string>>(new Set())
  const [activeTab, setActiveTab] = useState<'visualization' | 'events' | 'scope'>('visualization')

  const { events: wsEvents, subscribe, clearEvents } = useWebSocket()

  useEffect(() => {
    if (patternId) {
      subscribe(patternId)
    }
  }, [patternId, subscribe])

  // Update local events from WebSocket
  useEffect(() => {
    const patternEvents = wsEvents.filter(e => e.patternName === patternId)
    if (patternEvents.length > 0) {
      setLocalEvents(patternEvents)
      
      // Update scope from events
      patternEvents.forEach(event => {
        if (event.eventType === 'STATE_UPDATED' && event.data) {
          const key = event.data.key as string
          const value = event.data.value
          setScope(prev => ({ ...prev, [key]: value }))
        }
        
        // Check for human input request (only if not already handled)
        if (event.eventType === 'HUMAN_INPUT_REQUIRED') {
          const requestId = event.data.requestId as string
          if (!handledRequestIds.has(requestId)) {
            setHumanInputRequest({
              requestId,
              prompt: event.message
            })
          }
        }
      })
    }
  }, [wsEvents, patternId])

  useEffect(() => {
    if (pattern) {
      setPrompt(pattern.examplePrompt)
    }
  }, [pattern])

  if (!pattern) {
    return (
      <div className="text-center py-16">
        <h2 className="text-2xl font-bold mb-4">Pattern not found</h2>
        <Link to="/" className="text-matrix-primary hover:underline">
          ← Back to Dashboard
        </Link>
      </div>
    )
  }

  const handleExecute = async () => {
    if (!prompt.trim() || isExecuting) return

    setIsExecuting(true)
    setResult(null)
    setLocalEvents([])
    setScope({})
    setHandledRequestIds(new Set())
    clearEvents()

    try {
      const executionResult = await executePattern({
        patternId: pattern.id,
        prompt: prompt.trim(),
      })
      setResult(executionResult)
      if (executionResult.scopeSnapshot) {
        setScope(executionResult.scopeSnapshot)
      }
    } catch (error) {
      console.error('Execution failed:', error)
    } finally {
      setIsExecuting(false)
    }
  }

  const handleHumanInput = async (input: string) => {
    if (humanInputRequest) {
      const requestId = humanInputRequest.requestId
      await provideHumanInput(requestId, input)
      setHandledRequestIds(prev => new Set(prev).add(requestId))
      setHumanInputRequest(null)
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link 
            to="/" 
            className="p-2 rounded hover:bg-matrix-primary/10 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold">{pattern.name}</h1>
            <p className="text-matrix-secondary">{pattern.description}</p>
          </div>
        </div>
        <span className={`px-3 py-1 rounded-full text-sm border ${
          pattern.category === 'workflow' 
            ? 'border-blue-500/50 bg-blue-500/10 text-blue-400'
            : pattern.category === 'agentic'
            ? 'border-purple-500/50 bg-purple-500/10 text-purple-400'
            : 'border-orange-500/50 bg-orange-500/10 text-orange-400'
        }`}>
          {pattern.category}
        </span>
      </div>

      {/* Prompt Input */}
      <div className="matrix-card rounded-lg p-4">
        <label className="block text-sm font-semibold mb-2">Prompt</label>
        <textarea
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Enter your prompt..."
          className="matrix-input min-h-[100px] resize-y"
          disabled={isExecuting}
        />
        <div className="flex justify-between items-center mt-4">
          <div className="flex gap-2">
            {pattern.agents.map((agent) => (
              <span 
                key={agent}
                className="text-xs px-2 py-1 rounded bg-matrix-accent/50 border border-matrix-primary/30"
              >
                {agent}
              </span>
            ))}
          </div>
          <button
            onClick={handleExecute}
            disabled={isExecuting || !prompt.trim()}
            className="matrix-btn rounded flex items-center gap-2"
          >
            {isExecuting ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Executing...
              </>
            ) : (
              <>
                <Play className="w-4 h-4" />
                Execute Pattern
              </>
            )}
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b border-matrix-primary/20">
        {[
          { id: 'visualization', label: 'Visualization', icon: Eye },
          { id: 'events', label: 'Event Log', icon: Terminal },
          { id: 'scope', label: 'Scope', icon: Database },
        ].map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => setActiveTab(id as typeof activeTab)}
            className={`flex items-center gap-2 px-4 py-2 border-b-2 transition-colors ${
              activeTab === id
                ? 'border-matrix-primary text-matrix-primary'
                : 'border-transparent text-matrix-secondary hover:text-matrix-primary'
            }`}
          >
            <Icon className="w-4 h-4" />
            {label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <AnimatePresence mode="wait">
            {activeTab === 'visualization' && (
              <motion.div
                key="visualization"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
              >
                <WorkflowVisualization
                  pattern={pattern}
                  events={localEvents}
                  isExecuting={isExecuting}
                />
              </motion.div>
            )}
            {activeTab === 'events' && (
              <motion.div
                key="events"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className="matrix-card rounded-lg p-4"
              >
                <h3 className="font-semibold mb-4 flex items-center gap-2">
                  <Terminal className="w-4 h-4" />
                  Event Log
                </h3>
                <EventLog events={localEvents} />
              </motion.div>
            )}
            {activeTab === 'scope' && (
              <motion.div
                key="scope"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className="matrix-card rounded-lg p-4"
              >
                <h3 className="font-semibold mb-4 flex items-center gap-2">
                  <Database className="w-4 h-4" />
                  AgenticScope Variables
                </h3>
                <ScopeInspector scope={scope} />
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Sidebar */}
        <div className="space-y-4">
          {/* Result Panel */}
          {result && (
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              className="matrix-card rounded-lg p-4"
            >
              <h3 className="font-semibold mb-2 flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full ${
                  result.status === 'COMPLETED' ? 'bg-green-500' :
                  result.status === 'ERROR' ? 'bg-red-500' : 'bg-yellow-500'
                }`} />
                Result
              </h3>
              <div className="text-sm">
                <div className="flex justify-between text-matrix-secondary mb-2">
                  <span>Status:</span>
                  <span className={
                    result.status === 'COMPLETED' ? 'text-green-400' :
                    result.status === 'ERROR' ? 'text-red-400' : 'text-yellow-400'
                  }>
                    {result.status}
                  </span>
                </div>
                {result.durationMs > 0 && (
                  <div className="flex justify-between text-matrix-secondary mb-2">
                    <span>Duration:</span>
                    <span>{result.durationMs}ms</span>
                  </div>
                )}
                <div className="mt-4 p-3 bg-matrix-dark/50 rounded border border-matrix-primary/20 max-h-64 overflow-y-auto prose prose-invert prose-sm max-w-none prose-p:my-1 prose-strong:text-matrix-primary prose-headings:text-matrix-primary">
                  <ReactMarkdown>
                    {result.result === 'null' || !result.result 
                      ? '(No result returned)'
                      : result.result}
                  </ReactMarkdown>
                </div>
              </div>
            </motion.div>
          )}

          {/* Quick Info */}
          <div className="matrix-card rounded-lg p-4">
            <h3 className="font-semibold mb-3">Pattern Info</h3>
            <dl className="space-y-2 text-sm">
              <div className="flex justify-between">
                <dt className="text-matrix-secondary">Topology:</dt>
                <dd>{pattern.topology.type}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-matrix-secondary">Agents:</dt>
                <dd>{pattern.agents.length}</dd>
              </div>
              {pattern.topology.maxIterations && (
                <div className="flex justify-between">
                  <dt className="text-matrix-secondary">Max Iterations:</dt>
                  <dd>{pattern.topology.maxIterations}</dd>
                </div>
              )}
              {pattern.topology.hasHuman && (
                <div className="flex justify-between">
                  <dt className="text-matrix-secondary">Human Input:</dt>
                  <dd className="text-orange-400">Required</dd>
                </div>
              )}
            </dl>
          </div>
        </div>
      </div>

      {/* Human Input Modal */}
      <AnimatePresence>
        {humanInputRequest && (
          <HumanInputModal
            requestId={humanInputRequest.requestId}
            prompt={humanInputRequest.prompt}
            onSubmit={handleHumanInput}
            onCancel={() => setHumanInputRequest(null)}
          />
        )}
      </AnimatePresence>
    </div>
  )
}
