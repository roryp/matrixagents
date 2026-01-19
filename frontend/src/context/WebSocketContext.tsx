import { createContext, useContext, useEffect, useState, useRef, useCallback, ReactNode } from 'react'
import { AgentEvent } from '../types'

interface WebSocketContextType {
  connected: boolean
  events: AgentEvent[]
  subscribe: (patternId: string | null) => void
  unsubscribe: () => void
  clearEvents: () => void
}

const WebSocketContext = createContext<WebSocketContextType | null>(null)

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const [connected, setConnected] = useState(false)
  const [events, setEvents] = useState<AgentEvent[]>([])
  const [currentSubscription, setCurrentSubscription] = useState<string | null>(null)
  const socketRef = useRef<WebSocket | null>(null)

  // Handler for incoming events - deduplicate by eventId
  const handleEvent = useCallback((message: { body: string }) => {
    const event = JSON.parse(message.body) as AgentEvent
    setEvents(prev => {
      // Deduplicate: check if event with same eventId already exists
      if (event.eventId && prev.some(e => e.eventId === event.eventId)) {
        return prev
      }
      return [...prev, event]
    })
  }, [])

  useEffect(() => {
    // Dynamically determine WebSocket URL based on current location
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsHost = window.location.host
    const wsUrl = `${wsProtocol}//${wsHost}/ws`

    const socket = new WebSocket(wsUrl)
    socketRef.current = socket

    socket.onopen = () => {
      console.log('WebSocket connected')
      setConnected(true)
    }

    socket.onmessage = (event) => {
      const agentEvent = JSON.parse(event.data) as AgentEvent
      setEvents(prev => {
        // Deduplicate: check if event with same eventId already exists
        if (agentEvent.eventId && prev.some(e => e.eventId === agentEvent.eventId)) {
          return prev
        }
        return [...prev, agentEvent]
      })
    }

    socket.onclose = () => {
      console.log('WebSocket disconnected')
      setConnected(false)
    }

    socket.onerror = (error) => {
      console.error('WebSocket error', error)
    }

    return () => {
      socket.close()
    }
  }, [])

  const subscribe = useCallback((patternId: string | null) => {
    // Pattern-specific subscription is now handled via filtering on the client side
    // We just track which pattern we're interested in for potential future use
    if (patternId !== currentSubscription) {
      setCurrentSubscription(patternId)
      if (socketRef.current?.readyState === WebSocket.OPEN) {
        socketRef.current.send(JSON.stringify({ type: 'subscribe', patternId }))
      }
    }
  }, [currentSubscription])

  const unsubscribe = useCallback(() => {
    setCurrentSubscription(null)
  }, [])

  const clearEvents = useCallback(() => {
    setEvents([])
  }, [])

  return (
    <WebSocketContext.Provider value={{ connected, events, subscribe, unsubscribe, clearEvents }}>
      {children}
    </WebSocketContext.Provider>
  )
}

export function useWebSocket() {
  const context = useContext(WebSocketContext)
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider')
  }
  return context
}
