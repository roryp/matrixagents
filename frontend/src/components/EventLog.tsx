import { useEffect, useRef } from 'react'
import { AgentEvent } from '../types'
import { motion, AnimatePresence } from 'framer-motion'
import ReactMarkdown from 'react-markdown'
import { 
  Play, 
  CheckCircle, 
  XCircle, 
  RefreshCw, 
  User, 
  AlertCircle,
  Cpu
} from 'lucide-react'

interface EventLogProps {
  events: AgentEvent[]
  maxHeight?: string
}

const eventIcons: Record<AgentEvent['eventType'], React.ComponentType<{ className?: string }>> = {
  STARTED: Play,
  AGENT_INVOKED: Cpu,
  AGENT_COMPLETED: CheckCircle,
  STATE_UPDATED: RefreshCw,
  HUMAN_INPUT_REQUIRED: User,
  HUMAN_INPUT_RECEIVED: User,
  ERROR: XCircle,
  COMPLETED: CheckCircle,
}

const eventColors: Record<AgentEvent['eventType'], string> = {
  STARTED: 'text-blue-400 border-blue-400/30',
  AGENT_INVOKED: 'text-yellow-400 border-yellow-400/30',
  AGENT_COMPLETED: 'text-green-400 border-green-400/30',
  STATE_UPDATED: 'text-purple-400 border-purple-400/30',
  HUMAN_INPUT_REQUIRED: 'text-orange-400 border-orange-400/30',
  HUMAN_INPUT_RECEIVED: 'text-orange-400 border-orange-400/30',
  ERROR: 'text-red-400 border-red-400/30',
  COMPLETED: 'text-matrix-primary border-matrix-primary/30',
}

export default function EventLog({ events, maxHeight = '400px' }: EventLogProps) {
  const containerRef = useRef<HTMLDivElement>(null)

  // Auto-scroll to bottom on new events
  useEffect(() => {
    if (containerRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight
    }
  }, [events])

  if (events.length === 0) {
    return (
      <div className="flex items-center justify-center h-32 text-matrix-secondary">
        <AlertCircle className="w-5 h-5 mr-2" />
        <span>No events yet. Execute a pattern to see the workflow.</span>
      </div>
    )
  }

  return (
    <div 
      ref={containerRef}
      className="overflow-y-auto space-y-2 pr-2"
      style={{ maxHeight }}
    >
      <AnimatePresence mode="popLayout">
        {events.map((event, index) => {
          const Icon = eventIcons[event.eventType]
          const colorClass = eventColors[event.eventType]

          return (
            <motion.div
              key={event.eventId || index}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.2 }}
              className={`log-entry p-3 rounded border bg-matrix-dark/50 ${colorClass}`}
            >
              <div className="flex items-start gap-3">
                <Icon className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-semibold text-sm">
                      {event.eventType.replace(/_/g, ' ')}
                    </span>
                    {event.agentName && (
                      <span className="text-xs px-2 py-0.5 rounded bg-matrix-accent/50 border border-current/30">
                        {event.agentName}
                      </span>
                    )}
                    <span className="text-xs text-matrix-secondary ml-auto">
                      {new Date(event.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <div className="text-sm text-matrix-secondary break-words prose prose-invert prose-sm max-w-none prose-p:my-1 prose-strong:text-matrix-primary">
                    <ReactMarkdown>
                      {(event.message === 'null' || !event.message) 
                        ? '(completed with no output)'
                        : event.message.length > 200 
                          ? event.message.substring(0, 200) + '...' 
                          : event.message}
                    </ReactMarkdown>
                  </div>
                </div>
              </div>
            </motion.div>
          )
        })}
      </AnimatePresence>
    </div>
  )
}
