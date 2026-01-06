import { motion } from 'framer-motion'
import { ChevronDown, ChevronRight } from 'lucide-react'
import { useState } from 'react'

interface ScopeInspectorProps {
  scope: Record<string, unknown>
}

export default function ScopeInspector({ scope }: ScopeInspectorProps) {
  const [expanded, setExpanded] = useState<Set<string>>(new Set())

  const toggleExpanded = (key: string) => {
    const newExpanded = new Set(expanded)
    if (newExpanded.has(key)) {
      newExpanded.delete(key)
    } else {
      newExpanded.add(key)
    }
    setExpanded(newExpanded)
  }

  const renderValue = (value: unknown, key: string, depth: number = 0): JSX.Element => {
    if (value === null) {
      return <span className="text-gray-500">null</span>
    }

    if (typeof value === 'undefined') {
      return <span className="text-gray-500">undefined</span>
    }

    if (typeof value === 'string') {
      const truncated = value.length > 100
      const displayValue = truncated ? value.substring(0, 100) + '...' : value
      return (
        <span className="text-green-400">
          "{displayValue}"
          {truncated && (
            <button
              onClick={() => toggleExpanded(key)}
              className="ml-2 text-xs text-matrix-secondary hover:text-matrix-primary"
            >
              {expanded.has(key) ? 'less' : 'more'}
            </button>
          )}
          {truncated && expanded.has(key) && (
            <div className="mt-2 p-2 bg-matrix-dark/50 rounded text-xs whitespace-pre-wrap">
              {value}
            </div>
          )}
        </span>
      )
    }

    if (typeof value === 'number') {
      return <span className="text-blue-400">{value}</span>
    }

    if (typeof value === 'boolean') {
      return <span className="text-purple-400">{value.toString()}</span>
    }

    if (Array.isArray(value)) {
      return (
        <span className="text-matrix-secondary">
          Array({value.length})
        </span>
      )
    }

    if (typeof value === 'object') {
      return (
        <span className="text-matrix-secondary">
          Object({Object.keys(value).length} keys)
        </span>
      )
    }

    return <span>{String(value)}</span>
  }

  const entries = Object.entries(scope)

  if (entries.length === 0) {
    return (
      <div className="text-matrix-secondary text-sm text-center py-4">
        No scope variables yet
      </div>
    )
  }

  return (
    <div className="space-y-1">
      {entries.map(([key, value], index) => (
        <motion.div
          key={key}
          initial={{ opacity: 0, x: -10 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: index * 0.05 }}
          className="p-2 rounded bg-matrix-dark/30 border border-matrix-primary/10 hover:border-matrix-primary/30 transition-colors"
        >
          <div className="flex items-start gap-2">
            {typeof value === 'object' && value !== null ? (
              <button
                onClick={() => toggleExpanded(key)}
                className="mt-0.5 text-matrix-secondary hover:text-matrix-primary"
              >
                {expanded.has(key) ? (
                  <ChevronDown className="w-4 h-4" />
                ) : (
                  <ChevronRight className="w-4 h-4" />
                )}
              </button>
            ) : (
              <span className="w-4" />
            )}
            <span className="text-matrix-primary font-semibold text-sm">{key}:</span>
            <span className="text-sm flex-1">{renderValue(value, key)}</span>
          </div>
          {expanded.has(key) && typeof value === 'object' && value !== null && (
            <div className="ml-6 mt-2 pl-4 border-l border-matrix-primary/20">
              <pre className="text-xs text-matrix-secondary whitespace-pre-wrap">
                {JSON.stringify(value, null, 2)}
              </pre>
            </div>
          )}
        </motion.div>
      ))}
    </div>
  )
}
