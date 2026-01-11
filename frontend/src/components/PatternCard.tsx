import { Link } from 'react-router-dom'
import { PatternInfo } from '../types'
import { motion } from 'framer-motion'
import { getAgentDescription } from '../data/agentDescriptions'
import { 
  ArrowRight, 
  GitBranch, 
  RotateCcw, 
  GitMerge, 
  Users, 
  UserCheck, 
  Target, 
  Network 
} from 'lucide-react'

interface PatternCardProps {
  pattern: PatternInfo
  index: number
}

const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  sequence: ArrowRight,
  parallel: GitBranch,
  loop: RotateCcw,
  conditional: GitMerge,
  supervisor: Users,
  'human-in-loop': UserCheck,
  goap: Target,
  p2p: Network,
}

const categoryColors: Record<string, string> = {
  workflow: 'border-blue-500/50 bg-blue-500/10',
  agentic: 'border-purple-500/50 bg-purple-500/10',
  planning: 'border-orange-500/50 bg-orange-500/10',
}

export default function PatternCard({ pattern, index }: PatternCardProps) {
  const Icon = iconMap[pattern.id] || ArrowRight

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.1 }}
    >
      <Link to={`/pattern/${pattern.id}`}>
        <div className="matrix-card rounded-lg p-6 h-full cursor-pointer group">
          {/* Header */}
          <div className="flex items-start justify-between mb-4">
            <div className={`p-3 rounded-lg ${categoryColors[pattern.category] || 'border-matrix-primary/50 bg-matrix-primary/10'} border`}>
              <Icon className="w-6 h-6" />
            </div>
            <span className={`text-xs px-2 py-1 rounded-full ${categoryColors[pattern.category]} border uppercase tracking-wide`}>
              {pattern.category}
            </span>
          </div>

          {/* Title */}
          <h3 className="text-lg font-semibold mb-2 group-hover:glow-text transition-all">
            {pattern.name}
          </h3>

          {/* Description */}
          <p className="text-matrix-secondary text-sm mb-4 line-clamp-2">
            {pattern.description}
          </p>

          {/* Agents */}
          <div className="flex flex-wrap gap-2 mb-4">
            {pattern.agents.slice(0, 4).map((agent) => (
              <span 
                key={agent}
                title={getAgentDescription(pattern.id, agent)}
                className="text-xs px-2 py-1 rounded bg-matrix-accent/50 border border-matrix-primary/30 cursor-help transition-all duration-200 hover:bg-matrix-primary/30 hover:border-matrix-primary"
              >
                {agent}
              </span>
            ))}
            {pattern.agents.length > 4 && (
              <span className="text-xs px-2 py-1 rounded bg-matrix-accent/50 border border-matrix-primary/30">
                +{pattern.agents.length - 4} more
              </span>
            )}
          </div>

          {/* Topology indicator */}
          <div className="flex items-center justify-between pt-4 border-t border-matrix-primary/20">
            <span className="text-xs text-matrix-secondary">
              Topology: {pattern.topology.type}
            </span>
            <ArrowRight className="w-4 h-4 text-matrix-primary group-hover:translate-x-1 transition-transform" />
          </div>
        </div>
      </Link>
    </motion.div>
  )
}
