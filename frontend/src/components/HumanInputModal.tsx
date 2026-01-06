import { useState } from 'react'
import { motion } from 'framer-motion'
import { Send, MessageSquare } from 'lucide-react'

interface HumanInputModalProps {
  requestId: string
  prompt: string
  onSubmit: (input: string) => void
  onCancel: () => void
}

export default function HumanInputModal({ requestId, prompt, onSubmit, onCancel }: HumanInputModalProps) {
  const [input, setInput] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (input.trim()) {
      onSubmit(input.trim())
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm"
    >
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.9, opacity: 0 }}
        className="matrix-card rounded-lg p-6 max-w-2xl w-full mx-4 glow-border"
      >
        <div className="flex items-center gap-3 mb-4">
          <div className="p-2 rounded-lg bg-orange-500/20 border border-orange-500/50">
            <MessageSquare className="w-5 h-5 text-orange-400" />
          </div>
          <div>
            <h3 className="text-lg font-semibold">Human Input Required</h3>
            <p className="text-xs text-matrix-secondary">Request ID: {requestId}</p>
          </div>
        </div>

        <div className="bg-matrix-dark/50 rounded p-4 mb-4 border border-matrix-primary/20 max-h-64 overflow-y-auto">
          <pre className="whitespace-pre-wrap text-sm text-matrix-secondary">{prompt}</pre>
        </div>

        <form onSubmit={handleSubmit}>
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Enter your response or approval..."
            className="matrix-input min-h-[100px] mb-4 resize-y"
            autoFocus
          />

          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 rounded border border-matrix-primary/30 text-matrix-secondary hover:text-matrix-primary hover:border-matrix-primary transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!input.trim()}
              className="matrix-btn rounded flex items-center gap-2"
            >
              <Send className="w-4 h-4" />
              Submit Response
            </button>
          </div>
        </form>
      </motion.div>
    </motion.div>
  )
}
