import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { AgentEvent } from '../types'

interface WebSocketContextType {
  connected: boolean
  events: AgentEvent[]
  subscribe: (patternId: string) => void
  unsubscribe: () => void
  clearEvents: () => void
}

const WebSocketContext = createContext<WebSocketContextType | null>(null)

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const [client, setClient] = useState<Client | null>(null)
  const [connected, setConnected] = useState(false)
  const [events, setEvents] = useState<AgentEvent[]>([])
  const [currentSubscription, setCurrentSubscription] = useState<string | null>(null)

  useEffect(() => {
    const stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected')
        setConnected(true)
        
        // Subscribe to global events
        stompClient.subscribe('/topic/events', (message) => {
          const event = JSON.parse(message.body) as AgentEvent
          setEvents(prev => [...prev, event])
        })
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
    setClient(stompClient)

    return () => {
      stompClient.deactivate()
    }
  }, [])

  const subscribe = (patternId: string) => {
    if (client && connected && patternId !== currentSubscription) {
      setCurrentSubscription(patternId)
      client.subscribe(`/topic/patterns/${patternId}`, (message) => {
        const event = JSON.parse(message.body) as AgentEvent
        setEvents(prev => [...prev, event])
      })
    }
  }

  const unsubscribe = () => {
    setCurrentSubscription(null)
  }

  const clearEvents = () => {
    setEvents([])
  }

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
