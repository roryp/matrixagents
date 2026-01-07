import { useEffect, useRef, useState } from 'react'
import * as d3 from 'd3'
import { PatternInfo, AgentEvent } from '../types'

interface WorkflowVisualizationProps {
  pattern: PatternInfo
  events: AgentEvent[]
  isExecuting: boolean
}

interface Node {
  id: string
  label: string
  x?: number
  y?: number
  status: 'idle' | 'active' | 'completed' | 'error'
}

interface Edge {
  source: string
  target: string
  label?: string
  active: boolean
}

export default function WorkflowVisualization({ pattern, events, isExecuting }: WorkflowVisualizationProps) {
  const svgRef = useRef<SVGSVGElement>(null)
  const [dimensions, setDimensions] = useState({ width: 800, height: 400 })

  // Track active and completed agents from events
  const activeAgents = new Set<string>()
  const completedAgents = new Set<string>()
  
  events.forEach(event => {
    if (event.eventType === 'AGENT_INVOKED' && event.agentName) {
      activeAgents.add(event.agentName)
    }
    if (event.eventType === 'AGENT_COMPLETED' && event.agentName) {
      activeAgents.delete(event.agentName)
      completedAgents.add(event.agentName)
    }
  })

  useEffect(() => {
    if (!svgRef.current) return

    const svg = d3.select(svgRef.current)
    svg.selectAll('*').remove()

    const { width, height } = dimensions
    const padding = 80

    // Create nodes from pattern agents
    let nodes: Node[] = pattern.agents.map((agent) => ({
      id: agent,
      label: agent,
      status: completedAgents.has(agent) 
        ? 'completed' 
        : activeAgents.has(agent) 
          ? 'active' 
          : 'idle'
    }))

    // For PARALLEL topology, add virtual start and combiner nodes if edges reference them
    if (pattern.topology.type === 'PARALLEL') {
      const edgeNodes = new Set<string>()
      pattern.topology.edges.forEach((e: { from: string; to: string }) => {
        edgeNodes.add(e.from)
        edgeNodes.add(e.to)
      })
      const agentSet = new Set(pattern.agents)
      
      // Add virtual nodes that are referenced in edges but not in agents list
      if (edgeNodes.has('start') && !agentSet.has('start')) {
        nodes = [{ id: 'start', label: 'start', status: 'completed' as const }, ...nodes]
      }
      if (edgeNodes.has('combiner') && !agentSet.has('combiner')) {
        nodes = [...nodes, { id: 'combiner', label: 'combiner', status: 'idle' as const }]
      }
    }

    // Position nodes based on topology
    const positionNodes = () => {
      const centerX = width / 2
      const centerY = height / 2

      switch (pattern.topology.type) {
        case 'SEQUENCE':
          nodes.forEach((node, i) => {
            node.x = padding + (i * (width - 2 * padding)) / (nodes.length - 1 || 1)
            node.y = centerY
          })
          break
        
        case 'PARALLEL':
          // Check if we have start/combiner virtual nodes
          const hasStart = nodes.some(n => n.id === 'start')
          const hasCombiner = nodes.some(n => n.id === 'combiner')
          const workerNodes = nodes.filter(n => n.id !== 'start' && n.id !== 'combiner')
          
          if (hasStart || hasCombiner) {
            // Position start on left, workers in center vertically, combiner on right
            const verticalSpacing = Math.min(80, (height - 2 * padding) / (workerNodes.length + 1))
            const totalHeight = (workerNodes.length - 1) * verticalSpacing
            const startY = centerY - totalHeight / 2
            
            nodes.forEach((node) => {
              if (node.id === 'start') {
                node.x = padding
                node.y = centerY
              } else if (node.id === 'combiner') {
                node.x = width - padding
                node.y = centerY
              } else {
                const workerIndex = workerNodes.findIndex(n => n.id === node.id)
                node.x = centerX
                node.y = startY + workerIndex * verticalSpacing
              }
            })
          } else {
            // Just parallel agents, stack them vertically
            const verticalSpacing = Math.min(80, (height - 2 * padding) / (nodes.length + 1))
            const totalHeight = (nodes.length - 1) * verticalSpacing
            const startY = centerY - totalHeight / 2
            
            nodes.forEach((node, i) => {
              node.x = centerX
              node.y = startY + i * verticalSpacing
            })
          }
          break
        
        case 'LOOP':
          // Circular layout
          nodes.forEach((node, i) => {
            const angle = (i / nodes.length) * 2 * Math.PI - Math.PI / 2
            node.x = centerX + Math.cos(angle) * Math.min(width, height) / 3
            node.y = centerY + Math.sin(angle) * Math.min(width, height) / 4
          })
          break
        
        case 'STAR':
        case 'CONDITIONAL':
          // Star layout with center node
          if (nodes.length > 0) {
            nodes[0].x = centerX
            nodes[0].y = centerY
            nodes.slice(1).forEach((node, i) => {
              const angle = (i / (nodes.length - 1)) * 2 * Math.PI - Math.PI / 2
              node.x = centerX + Math.cos(angle) * Math.min(width, height) / 3
              node.y = centerY + Math.sin(angle) * Math.min(width, height) / 3.5
            })
          }
          break
        
        case 'GOAP':
        case 'P2P':
          // Grid layout
          const cols = Math.ceil(Math.sqrt(nodes.length))
          nodes.forEach((node, i) => {
            const row = Math.floor(i / cols)
            const col = i % cols
            node.x = padding + (col + 0.5) * (width - 2 * padding) / cols
            node.y = padding + (row + 0.5) * (height - 2 * padding) / Math.ceil(nodes.length / cols)
          })
          break
        
        default:
          // Default horizontal layout
          nodes.forEach((node, i) => {
            node.x = padding + (i * (width - 2 * padding)) / (nodes.length - 1 || 1)
            node.y = centerY
          })
      }
    }

    positionNodes()

    // Create edges
    const edges: Edge[] = pattern.topology.edges.map(e => ({
      source: e.from,
      target: e.to,
      label: e.label || e.condition,
      active: activeAgents.has(e.from) || activeAgents.has(e.to)
    }))

    // Define arrow marker
    svg.append('defs').append('marker')
      .attr('id', 'arrowhead')
      .attr('viewBox', '0 -5 10 10')
      .attr('refX', 25)
      .attr('refY', 0)
      .attr('markerWidth', 6)
      .attr('markerHeight', 6)
      .attr('orient', 'auto')
      .append('path')
      .attr('d', 'M0,-5L10,0L0,5')
      .attr('fill', '#00ff41')

    // Define glow filter
    const defs = svg.select('defs')
    const filter = defs.append('filter')
      .attr('id', 'glow')
      .attr('x', '-50%')
      .attr('y', '-50%')
      .attr('width', '200%')
      .attr('height', '200%')
    filter.append('feGaussianBlur')
      .attr('stdDeviation', '3')
      .attr('result', 'coloredBlur')
    const feMerge = filter.append('feMerge')
    feMerge.append('feMergeNode').attr('in', 'coloredBlur')
    feMerge.append('feMergeNode').attr('in', 'SourceGraphic')

    // Draw edges
    const nodeMap = new Map(nodes.map(n => [n.id, n]))
    
    svg.selectAll('.edge')
      .data(edges)
      .enter()
      .append('line')
      .attr('class', d => `edge ${d.active ? 'edge-active' : ''}`)
      .attr('x1', d => nodeMap.get(d.source)?.x || 0)
      .attr('y1', d => nodeMap.get(d.source)?.y || 0)
      .attr('x2', d => nodeMap.get(d.target)?.x || 0)
      .attr('y2', d => nodeMap.get(d.target)?.y || 0)
      .attr('stroke', d => d.active ? '#00ff41' : '#008f11')
      .attr('stroke-width', d => d.active ? 3 : 2)
      .attr('marker-end', 'url(#arrowhead)')
      .style('filter', d => d.active ? 'url(#glow)' : 'none')

    // Draw edge labels
    svg.selectAll('.edge-label')
      .data(edges.filter(e => e.label))
      .enter()
      .append('text')
      .attr('class', 'edge-label')
      .attr('x', d => {
        const s = nodeMap.get(d.source)
        const t = nodeMap.get(d.target)
        return ((s?.x || 0) + (t?.x || 0)) / 2
      })
      .attr('y', d => {
        const s = nodeMap.get(d.source)
        const t = nodeMap.get(d.target)
        return ((s?.y || 0) + (t?.y || 0)) / 2 - 10
      })
      .attr('text-anchor', 'middle')
      .attr('fill', '#008f11')
      .attr('font-size', '10px')
      .text(d => d.label || '')

    // Draw nodes
    const nodeGroups = svg.selectAll('.node')
      .data(nodes)
      .enter()
      .append('g')
      .attr('class', d => `node ${d.status === 'active' ? 'node-active' : ''}`)
      .attr('transform', d => `translate(${d.x}, ${d.y})`)

    // Node circles
    nodeGroups.append('circle')
      .attr('r', 25)
      .attr('fill', d => {
        switch (d.status) {
          case 'active': return '#003b00'
          case 'completed': return '#004d00'
          default: return '#1a1a2e'
        }
      })
      .attr('stroke', d => {
        switch (d.status) {
          case 'active': return '#00ff41'
          case 'completed': return '#00ff41'
          case 'error': return '#ff4141'
          default: return '#008f11'
        }
      })
      .attr('stroke-width', d => d.status === 'active' ? 3 : 2)
      .style('filter', d => d.status === 'active' ? 'url(#glow)' : 'none')

    // Status indicator
    nodeGroups.append('circle')
      .attr('r', 6)
      .attr('cx', 18)
      .attr('cy', -18)
      .attr('fill', d => {
        switch (d.status) {
          case 'active': return '#00ff41'
          case 'completed': return '#00cc33'
          case 'error': return '#ff4141'
          default: return '#333'
        }
      })
      .style('filter', d => d.status === 'active' ? 'url(#glow)' : 'none')

    // Node labels
    nodeGroups.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', 40)
      .attr('fill', d => d.status === 'active' ? '#00ff41' : '#008f11')
      .attr('font-size', '11px')
      .attr('font-family', 'JetBrains Mono, monospace')
      .text(d => d.label)

  }, [pattern, events, dimensions, isExecuting])

  // Handle resize
  useEffect(() => {
    const handleResize = () => {
      if (svgRef.current?.parentElement) {
        setDimensions({
          width: svgRef.current.parentElement.clientWidth,
          height: 400
        })
      }
    }
    handleResize()
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  return (
    <div className="w-full h-[400px] rounded-lg border border-matrix-primary/30 bg-matrix-dark/50 overflow-hidden">
      <svg
        ref={svgRef}
        width={dimensions.width}
        height={dimensions.height}
        className="w-full h-full"
      />
    </div>
  )
}
