import { Routes, Route } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import PatternDetail from './pages/PatternDetail'
import { WebSocketProvider } from './context/WebSocketContext'
import { PatternInfo } from './types'
import { fetchPatterns } from './api'

function App() {
  const [patterns, setPatterns] = useState<PatternInfo[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchPatterns()
      .then(setPatterns)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl font-bold glow-text mb-4">MATRIX AGENTS</div>
          <div className="text-matrix-secondary">Initializing system...</div>
        </div>
      </div>
    )
  }

  return (
    <WebSocketProvider>
      <div className="matrix-bg" />
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard patterns={patterns} />} />
          <Route path="/pattern/:patternId" element={<PatternDetail patterns={patterns} />} />
        </Routes>
      </Layout>
    </WebSocketProvider>
  )
}

export default App
