import { createContext, useContext, useEffect, useState, useRef, useCallback, ReactNode } from 'react'
import { Client, StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
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
  const subscriptionRef = useRef<StompSubscription | null>(null)

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
    const wsProtocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    const wsHost = window.location.host
    const wsUrl = `${wsProtocol}//${wsHost}/ws`

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected')
        setConnected(true)
        
        // Subscribe to global events only (pattern-specific subscription is handled separately)
        subscriptionRef.current = stompClient.subscribe('/topic/events', handleEvent)
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected')
        setConnected(false)
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame)
      },
    })

    stompClient.activate()

    return () => {
      stompClient.deactivate()
    }
  }, [handleEvent])

  const subscribe = useCallback((patternId: string | null) => {
    // Pattern-specific subscription is now handled via filtering on the client side
    // The global /topic/events subscription receives all events
    // We just track which pattern we're interested in for potential future use
    if (patternId !== currentSubscription) {
      setCurrentSubscription(patternId)
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
