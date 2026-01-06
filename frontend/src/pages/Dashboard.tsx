import { PatternInfo } from '../types'
import PatternCard from '../components/PatternCard'
import { motion } from 'framer-motion'
import { Layers, Zap, Brain } from 'lucide-react'

interface DashboardProps {
  patterns: PatternInfo[]
}

export default function Dashboard({ patterns }: DashboardProps) {
  const workflowPatterns = patterns.filter(p => p.category === 'workflow')
  const agenticPatterns = patterns.filter(p => p.category === 'agentic')
  const planningPatterns = patterns.filter(p => p.category === 'planning')

  return (
    <div className="space-y-12">
      {/* Hero Section */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-center py-8"
      >
        <h1 className="text-4xl md:text-5xl font-bold mb-4 glow-text">
          Agentic Patterns Showcase
        </h1>
        <p className="text-matrix-secondary text-lg max-w-2xl mx-auto">
          Explore 8 powerful patterns from the LangChain4j Agentic module.
          Watch real-time agent orchestration with interactive D3 visualizations.
        </p>
        
        {/* Stats */}
        <div className="flex justify-center gap-8 mt-8">
          <div className="text-center">
            <div className="text-3xl font-bold text-matrix-primary">{patterns.length}</div>
            <div className="text-sm text-matrix-secondary">Patterns</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-matrix-primary">
              {patterns.reduce((acc, p) => acc + p.agents.length, 0)}
            </div>
            <div className="text-sm text-matrix-secondary">Agents</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-matrix-primary">3</div>
            <div className="text-sm text-matrix-secondary">Categories</div>
          </div>
        </div>
      </motion.div>

      {/* Workflow Patterns */}
      <section>
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 rounded-lg bg-blue-500/20 border border-blue-500/50">
            <Layers className="w-5 h-5 text-blue-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Workflow Patterns</h2>
            <p className="text-matrix-secondary text-sm">
              Deterministic, structured agent orchestration
            </p>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {workflowPatterns.map((pattern, index) => (
            <PatternCard key={pattern.id} pattern={pattern} index={index} />
          ))}
        </div>
      </section>

      {/* Agentic Patterns */}
      <section>
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 rounded-lg bg-purple-500/20 border border-purple-500/50">
            <Brain className="w-5 h-5 text-purple-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Agentic Patterns</h2>
            <p className="text-matrix-secondary text-sm">
              LLM-driven autonomous orchestration
            </p>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {agenticPatterns.map((pattern, index) => (
            <PatternCard key={pattern.id} pattern={pattern} index={index} />
          ))}
        </div>
      </section>

      {/* Planning Patterns */}
      <section>
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 rounded-lg bg-orange-500/20 border border-orange-500/50">
            <Zap className="w-5 h-5 text-orange-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Planning Patterns</h2>
            <p className="text-matrix-secondary text-sm">
              Custom planners for advanced orchestration
            </p>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {planningPatterns.map((pattern, index) => (
            <PatternCard key={pattern.id} pattern={pattern} index={index} />
          ))}
        </div>
      </section>

      {/* Technology Stack */}
      <motion.section
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
        className="mt-16 text-center"
      >
        <h3 className="text-lg font-semibold mb-4">Built With</h3>
        <div className="flex flex-wrap justify-center gap-4">
          {['LangChain4j 1.10', 'Spring Boot 4.0', 'Java 21', 'React 18', 'D3.js', 'TypeScript', 'Tailwind CSS', 'WebSocket'].map((tech) => (
            <span
              key={tech}
              className="px-4 py-2 rounded-full border border-matrix-primary/30 bg-matrix-dark/50 text-sm"
            >
              {tech}
            </span>
          ))}
        </div>
      </motion.section>
    </div>
  )
}
