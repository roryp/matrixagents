#!/usr/bin/env python3
"""
Generate conceptual diagram images using Azure OpenAI gpt-image-1.5.
Authenticates via Entra ID (DefaultAzureCredential) — no API keys needed.
Requires: pip install azure-identity

Usage:
    python generate_images.py "Your prompt here" output-name.png
    python generate_images.py --list
    python generate_images.py --all
"""

import json, base64, urllib.request, sys, os

ENDPOINT = "https://aoai-cengi3imeb5bg.openai.azure.com/"
DEPLOYMENT = "gpt-image-1.5"
API_VERSION = "2025-04-01-preview"
OUTPUT_DIR = "docs"

# Standard style prefix applied to all prompts (from IMAGE_GENERATION_GUIDE.md)
STYLE_PREFIX = (
    "A premium 16:9 presentation slide. "
    "Deep navy-to-indigo gradient background with subtle electric blue bloom in center "
    "and darker vignetted edges. Faint horizontal guide lines for a futuristic control interface look. "
    "Rounded rectangular panels with thin glowing outlines and subtle transparent fills, "
    "like glass-tech panels floating over the dark background. "
    "Icons have soft inner glow and light bloom. "
    "Labels in glossy pill-shaped tabs with slight bevel and outer glow matching accent color. "
    "Vector-clean style, not painterly, not hand-drawn. "
    "Bold geometric sans-serif typography, white text with selected words highlighted in cyan. "
    "All text must be sharp and legible. "
)

# Pre-defined image prompts
PROMPTS = {
    "pattern-selection-guide": (
        "titled 'Pattern Selection Guide' showing a decision matrix for multi-agent orchestration patterns. "
        "The title 'Pattern Selection Guide' appears at the top as a large rounded glowing plaque like a backlit "
        "glass sign with a bright cyan halo behind it. "
        "Below, arrange 8 rows in a table layout with two columns: 'Situation' and 'Recommended Pattern'. "
        "Row 1: 'Simple pipeline with clear steps' maps to 'Sequential' (green accent). "
        "Row 2: 'Need multiple perspectives fast' maps to 'Parallel' (blue accent). "
        "Row 3: 'Quality is critical, time is not' maps to 'Loop' (purple accent). "
        "Row 4: 'Different inputs need different handling' maps to 'Conditional' (orange accent). "
        "Row 5: 'Complex task, unclear how to break down' maps to 'Supervisor' (red accent). "
        "Row 6: 'Need human approval or input' maps to 'Human-in-the-Loop' (teal accent). "
        "Row 7: 'Many dependencies, need optimal path' maps to 'GOAP' (gold accent). "
        "Row 8: 'Creative / brainstorming collaboration' maps to 'P2P' (pink accent). "
        "Each row has a small vector workflow icon matching the pattern. "
        "An arrow connects each situation to its pattern."
    ),
    "azure-architecture": (
        "titled 'AI Agents - Azure Architecture' showing cloud deployment architecture. "
        "The title appears at the top as a large rounded glowing plaque with bright cyan halo. "
        "Below is a large outer container labeled 'Azure Resource Group' with cyan outline. "
        "Inside, at top center: 'Container Registry' panel with cyan accent. "
        "A glowing downward arrow labeled 'deploy' connects to a mid-level container "
        "'Container Apps Environment' with blue-green outline. Inside: 'AI Agents App' "
        "with subtitle '(Java 21 + React)' in bright blue. "
        "Two connection lines extend down: left to 'Azure OpenAI' (green accent) containing "
        "'text-embedding-3-small' and 'gpt-5-mini' sub-panels, arrow labeled 'API calls'. "
        "Right to 'Monitoring' (purple accent) containing 'Application Insights' and "
        "'Log Analytics' sub-panels, arrow labeled 'telemetry'. "
        "Small vector icons accompany each service. Arrows have soft glow."
    ),
    "what-is-an-agent": (
        "titled 'What is an AI Agent?' showing a split-screen comparison. "
        "The title appears at the top as a large rounded glowing plaque with bright cyan halo. "
        "LEFT side labeled 'Simple Chatbot' (gray accent): a single user icon at top sends a message arrow "
        "down to an LLM brain icon, which sends a reply arrow back up. Simple two-way exchange. "
        "A small label says 'Ask question, get answer. No memory, no actions.' "
        "RIGHT side labeled 'AI Agent' (bright cyan accent): a user icon at top sends a request "
        "to a larger LLM brain icon in the center, but the agent has FOUR glowing capability panels "
        "radiating outward like satellite modules: "
        "'Reason' (purple, brain with gears icon) - breaks down complex tasks into steps, "
        "'Act' (green, lightning bolt icon) - calls tools and APIs to take real actions, "
        "'Remember' (blue, memory chip icon) - maintains state across interactions, "
        "'Collaborate' (orange, network mesh icon) - works with other agents toward a shared goal. "
        "A glowing arrow from the agent back to the user labeled 'Autonomous result'. "
        "A dashed divider separates left and right. The contrast should be stark: "
        "left side muted and simple, right side vibrant and complex."
    ),
    "agent-anatomy": (
        "titled 'Anatomy of a LangChain4j Agent' showing the internal building blocks of an agent. "
        "The title appears at the top as a large rounded glowing plaque with bright cyan halo. "
        "Center: a large rounded glass-tech container labeled 'Agent' with bright blue outline. "
        "Inside the agent container, four stacked horizontal layers from top to bottom: "
        "Layer 1 'System Prompt' (teal accent) - defines the agent's role, personality, and constraints. "
        "Small text: 'You are a financial expert who...' "
        "Layer 2 'LLM' (purple accent) - the reasoning engine. Shows 'Azure OpenAI gpt-5' with a brain icon. "
        "Layer 3 'Tools' (green accent) - actions the agent can take. Shows 3 small tool panels side by side: "
        "'Calculator', 'Web Search', 'Database Query' each with a small icon. "
        "Layer 4 'Output Key' (orange accent) - where the agent writes its result. "
        "Shows '@Agent(outputKey = \"result\")' in code font. "
        "OUTSIDE the agent container on the left: an arrow labeled 'Input from AgenticScope' pointing in. "
        "OUTSIDE on the right: an arrow labeled 'Output to AgenticScope' pointing out. "
        "Below the agent: a horizontal bar labeled 'AgenticScope (Shared State)' in cyan, "
        "connecting the input and output arrows, suggesting state flows through the scope."
    ),
    "single-vs-multi-agent": (
        "titled 'Single Agent vs Multi-Agent Systems' showing the evolution from one agent to many. "
        "The title appears at the top as a large rounded glowing plaque with bright cyan halo. "
        "LEFT side labeled 'Single Agent' (blue accent): one large agent panel in the center "
        "connected to a user icon above. The single agent has labels: 'Does everything', "
        "'One role, one prompt, one output'. Simple and direct but limited. "
        "A small caution icon with text 'Limited by one perspective'. "
        "RIGHT side labeled 'Multi-Agent System' (cyan accent): three smaller agent panels arranged "
        "in a triangle formation labeled 'Research Agent', 'Writing Agent', 'Review Agent'. "
        "Glowing connection lines between all three agents showing collaboration. "
        "A shared state bar labeled 'AgenticScope' runs along the bottom connecting them. "
        "Above the triangle, a user icon connects to the system. "
        "Label: 'Specialized agents collaborate through shared state'. "
        "Benefits listed as glowing badges: 'Specialization', 'Parallelism', 'Quality'. "
        "A dashed vertical divider separates left and right halves."
    ),
    "agentic-patterns-overview": (
        "titled 'The Three Categories of Agentic Patterns' showing how the 8 patterns group into 3 tiers. "
        "The title appears at the top as a large rounded glowing plaque with bright cyan halo. "
        "Three horizontal tiers arranged top to bottom, each a glass-tech container: "
        "TIER 1 'Workflow Patterns' (green accent, top): subtitle 'You define the rules'. "
        "Four small panels in a row: 'Sequential' with chain icon, 'Parallel' with fan-out icon, "
        "'Loop' with circular arrow icon, 'Conditional' with branch icon. "
        "TIER 2 'Agentic Patterns' (purple accent, middle): subtitle 'The LLM decides'. "
        "Two panels in a row: 'Supervisor' with star/hub icon, 'Human-in-the-Loop' with person+gate icon. "
        "TIER 3 'Planning Patterns' (orange accent, bottom): subtitle 'Algorithms optimize'. "
        "Two panels in a row: 'GOAP' with directed graph icon, 'P2P' with mesh network icon. "
        "On the right side, a vertical arrow spanning all three tiers pointing DOWNWARD "
        "labeled 'Increasing Autonomy' with 'Low' at the top near Tier 1 and 'High' at the bottom near Tier 3. "
        "On the left side, a vertical gradient bar labeled 'Complexity' with 'Simple' at the top and 'Advanced' at the bottom, "
        "so that Tier 1 at top is simple/low autonomy and Tier 3 at bottom is advanced/high autonomy."
    ),
}


def get_token():
    """Get an Entra ID bearer token for Azure Cognitive Services."""
    from azure.identity import DefaultAzureCredential
    cred = DefaultAzureCredential()
    return cred.get_token("https://cognitiveservices.azure.com/.default").token


def generate_image(prompt, output_filename):
    """Call Azure OpenAI image generation and save the result."""
    full_prompt = STYLE_PREFIX + prompt
    url = f"{ENDPOINT}openai/deployments/{DEPLOYMENT}/images/generations?api-version={API_VERSION}"

    token = get_token()

    body = json.dumps({
        "prompt": full_prompt,
        "n": 1,
        "size": "1536x1024",
        "quality": "high",
        "output_format": "png"
    }).encode("utf-8")

    req = urllib.request.Request(url, data=body, headers={
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    })

    print(f"Generating {output_filename}...")
    resp = urllib.request.urlopen(req, timeout=300)
    data = json.loads(resp.read().decode("utf-8"))

    img_bytes = base64.b64decode(data["data"][0]["b64_json"])
    output_path = os.path.join(OUTPUT_DIR, output_filename)
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    with open(output_path, "wb") as f:
        f.write(img_bytes)

    size_kb = len(img_bytes) / 1024
    print(f"  Saved: {output_path} ({size_kb:.0f} KB)")
    return output_path


def main():
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python generate_images.py <prompt> <output-name.png>")
        print("  python generate_images.py --list")
        print("  python generate_images.py --all")
        print("  python generate_images.py --name <preset-name>")
        sys.exit(1)

    if sys.argv[1] == "--list":
        print("Available presets:")
        for name in PROMPTS:
            print(f"  {name}")
        return

    if sys.argv[1] == "--all":
        for name, prompt in PROMPTS.items():
            generate_image(prompt, f"{name}.png")
        print("Done.")
        return

    if sys.argv[1] == "--name":
        if len(sys.argv) < 3:
            print("Error: --name requires a preset name")
            sys.exit(1)
        name = sys.argv[2]
        if name not in PROMPTS:
            print(f"Error: unknown preset '{name}'. Use --list to see available presets.")
            sys.exit(1)
        generate_image(PROMPTS[name], f"{name}.png")
        return

    # Custom prompt mode
    if len(sys.argv) < 3:
        print("Error: provide both a prompt and output filename")
        print("  python generate_images.py \"Your prompt\" output.png")
        sys.exit(1)

    prompt = sys.argv[1]
    output = sys.argv[2]
    generate_image(prompt, output)


if __name__ == "__main__":
    main()
