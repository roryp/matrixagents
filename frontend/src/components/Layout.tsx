import { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useWebSocket } from '../context/WebSocketContext'
import { Cpu, Wifi, WifiOff } from 'lucide-react'

interface LayoutProps {
  children: ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const { connected } = useWebSocket()

  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="border-b border-matrix-primary/30 bg-matrix-dark/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link to="/" className="flex items-center gap-3 hover:opacity-80 transition-opacity">
              <Cpu className="w-8 h-8 text-matrix-primary" />
              <div>
                <h1 className="text-xl font-bold glow-text">MATRIX AGENTS</h1>
                <p className="text-xs text-matrix-secondary">LangChain4j Agentic Patterns</p>
              </div>
            </Link>
            
            <div className="flex items-center gap-4">
              <div className={`flex items-center gap-2 px-3 py-1 rounded-full border ${
                connected 
                  ? 'border-matrix-primary/50 bg-matrix-primary/10' 
                  : 'border-red-500/50 bg-red-500/10'
              }`}>
                {connected ? (
                  <>
                    <Wifi className="w-4 h-4 text-matrix-primary" />
                    <span className="text-sm text-matrix-primary">Connected</span>
                  </>
                ) : (
                  <>
                    <WifiOff className="w-4 h-4 text-red-500" />
                    <span className="text-sm text-red-500">Disconnected</span>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="border-t border-matrix-primary/20 mt-auto py-6">
        <div className="max-w-7xl mx-auto px-4 text-center text-matrix-secondary text-sm">
          <p>Powered by LangChain4j Agentic Module • Spring Boot 3.2 • Java 21 • React + D3</p>
        </div>
      </footer>
    </div>
  )
}
