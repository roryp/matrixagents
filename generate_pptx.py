"""Generate a widescreen PPTX presentation for MatrixAgents showcase."""

from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
import os

DOCS = os.path.join(os.path.dirname(__file__), "docs")

# Widescreen 16:9
prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)

SLIDE_W = prs.slide_width
SLIDE_H = prs.slide_height

# Colors
BG_COLOR = RGBColor(0x0D, 0x11, 0x17)       # dark background
TITLE_COLOR = RGBColor(0x58, 0xA6, 0xFF)     # blue accent
TEXT_COLOR = RGBColor(0xE6, 0xED, 0xF3)       # light text
SUBTITLE_COLOR = RGBColor(0x8B, 0x94, 0x9E)   # muted grey
ACCENT_GREEN = RGBColor(0x3F, 0xB9, 0x50)     # green accent
TABLE_HEADER_BG = RGBColor(0x16, 0x1B, 0x22)
TABLE_ROW_BG = RGBColor(0x0D, 0x11, 0x17)


def set_slide_bg(slide, color=BG_COLOR):
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = color


def add_textbox(slide, left, top, width, height, text, font_size=18,
                color=TEXT_COLOR, bold=False, alignment=PP_ALIGN.LEFT, font_name="Segoe UI"):
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = text
    p.font.size = Pt(font_size)
    p.font.color.rgb = color
    p.font.bold = bold
    p.font.name = font_name
    p.alignment = alignment
    return txBox


def add_image_centered(slide, img_path, top, max_width=None, max_height=None):
    """Add image centered horizontally, fitting within max dimensions."""
    from PIL import Image
    if not os.path.exists(img_path):
        return
    img = Image.open(img_path)
    img_w, img_h = img.size
    dpi = 96
    emu_w = int(img_w / dpi * 914400)
    emu_h = int(img_h / dpi * 914400)

    if max_width is None:
        max_width = SLIDE_W - Inches(1)
    if max_height is None:
        max_height = SLIDE_H - top - Inches(0.5)

    scale = min(max_width / emu_w, max_height / emu_h, 1.0)
    final_w = int(emu_w * scale)
    final_h = int(emu_h * scale)
    left = int((SLIDE_W - final_w) / 2)
    slide.shapes.add_picture(img_path, left, int(top), final_w, final_h)


# ── Slide 1: Title ──────────────────────────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])  # blank
set_slide_bg(slide)
add_textbox(slide, Inches(1), Inches(1.8), Inches(11), Inches(1.5),
            "AI Agents", font_size=54, color=TITLE_COLOR, bold=True,
            alignment=PP_ALIGN.CENTER)
add_textbox(slide, Inches(1), Inches(3.2), Inches(11), Inches(1),
            "LangChain4j Agentic Patterns Showcase", font_size=28,
            color=TEXT_COLOR, alignment=PP_ALIGN.CENTER)
add_textbox(slide, Inches(1), Inches(4.2), Inches(11), Inches(0.8),
            "8 agentic patterns with real-time visualization using D3.js and WebSocket streaming",
            font_size=16, color=SUBTITLE_COLOR, alignment=PP_ALIGN.CENTER)
add_textbox(slide, Inches(1), Inches(5.8), Inches(11), Inches(0.6),
            "Spring Boot 4.0  •  LangChain4j 1.10  •  React 18  •  D3.js  •  Azure OpenAI",
            font_size=14, color=SUBTITLE_COLOR, alignment=PP_ALIGN.CENTER)

# ── Slide 2: Screenshot ─────────────────────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide)
add_textbox(slide, Inches(0.5), Inches(0.3), Inches(12), Inches(0.8),
            "Live Dashboard", font_size=36, color=TITLE_COLOR, bold=True,
            alignment=PP_ALIGN.CENTER)
add_image_centered(slide, os.path.join(DOCS, "screenshot.png"), Inches(1.3))

# ── Slide 3: Architecture ───────────────────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide)
add_textbox(slide, Inches(0.5), Inches(0.3), Inches(12), Inches(0.8),
            "Technical Architecture", font_size=36, color=TITLE_COLOR, bold=True,
            alignment=PP_ALIGN.CENTER)
add_image_centered(slide, os.path.join(DOCS, "architecture.png"), Inches(1.3))

# ── Slide 4: Patterns Overview ──────────────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide)
add_textbox(slide, Inches(0.5), Inches(0.3), Inches(12), Inches(0.8),
            "8 Agentic Patterns Overview", font_size=36, color=TITLE_COLOR, bold=True,
            alignment=PP_ALIGN.CENTER)
add_image_centered(slide, os.path.join(DOCS, "patterns-overview.png"), Inches(1.3))

# ── Pattern slides ──────────────────────────────────────────────
patterns = [
    {
        "title": "1. Sequential Workflow",
        "subtitle": "Chain Orchestration",
        "image": "pattern-sequential.png",
        "bullets": [
            "Agents run one after another, like an assembly line",
            "Each step depends on the previous step's output",
            "Example: CreativeWriter → AudienceEditor → StyleEditor",
            "API: AgenticServices.sequenceBuilder()",
        ],
    },
    {
        "title": "2. Parallel Workflow",
        "subtitle": "Fan-out Orchestration",
        "image": "pattern-parallel.png",
        "bullets": [
            "Multiple agents run simultaneously, results combined",
            "Get diverse perspectives quickly",
            "Example: FoodExpert + MovieExpert → Combiner",
            "API: @Parallel annotation with createAgenticSystem()",
        ],
    },
    {
        "title": "3. Loop Workflow",
        "subtitle": "Cycle Orchestration",
        "image": "pattern-loop.png",
        "bullets": [
            "Iterative refinement until quality threshold is met",
            "Quality matters more than speed",
            "Example: Writer → Scorer → Editor (repeat until score ≥ 0.8)",
            "API: AgenticServices.loopBuilder()",
        ],
    },
    {
        "title": "4. Conditional Routing",
        "subtitle": "Branch Orchestration",
        "image": "pattern-conditional.png",
        "bullets": [
            "Routes to different specialist agents based on input",
            "Different inputs need different expertise",
            "Example: CategoryRouter → Medical / Legal / Technical Expert",
            "API: @Conditional annotation with createAgenticSystem()",
        ],
    },
    {
        "title": "5. Supervisor Agent",
        "subtitle": "Star Orchestration",
        "image": "pattern-supervisor.png",
        "bullets": [
            "A 'boss' agent plans and delegates to worker agents",
            "LLM intelligence decides task decomposition",
            "Example: BankSupervisor → Withdraw / Credit / Exchange Agent",
            "API: AgenticServices.supervisorBuilder()",
        ],
    },
    {
        "title": "6. Human-in-the-Loop",
        "subtitle": "Gated Orchestration",
        "image": "pattern-humaninloop.png",
        "bullets": [
            "Pauses execution for human input or approval",
            "High-stakes decisions, compliance requirements",
            "Example: ZodiacExtractor → Human Input → HoroscopeAgent",
            "API: AgenticServices.agentBuilder() with WebSocket events",
        ],
    },
    {
        "title": "7. GOAP — Goal-Oriented Action Planning",
        "subtitle": "DAG — Custom Planner",
        "image": "pattern-goap.png",
        "bullets": [
            "Finds optimal sequence of agents to reach a goal",
            "Like GPS finding the shortest route",
            "Example: SignExtractor → (Horoscope + StoryFinder) → Writer",
            "API: plannerBuilder() + GoalOrientedPlanner",
        ],
    },
    {
        "title": "8. P2P — Peer-to-Peer",
        "subtitle": "Mesh — Custom Planner",
        "image": "pattern-p2p.png",
        "bullets": [
            "Agents collaborate as equals without a central controller",
            "Emergent collaboration for research & brainstorming",
            "Example: Literature → Hypothesis → Critic → Validation → Scorer",
            "API: plannerBuilder() + P2PPlanner (threshold ≥ 0.75)",
        ],
    },
]

for pat in patterns:
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide)

    # Title
    add_textbox(slide, Inches(0.6), Inches(0.3), Inches(12), Inches(0.7),
                pat["title"], font_size=32, color=TITLE_COLOR, bold=True)
    # Subtitle
    add_textbox(slide, Inches(0.6), Inches(0.9), Inches(12), Inches(0.5),
                pat["subtitle"], font_size=16, color=SUBTITLE_COLOR)

    # Bullets on the left
    bullet_text = "\n".join(f"•  {b}" for b in pat["bullets"])
    add_textbox(slide, Inches(0.6), Inches(1.6), Inches(5.2), Inches(3.5),
                bullet_text, font_size=15, color=TEXT_COLOR)

    # Image on the right
    img_path = os.path.join(DOCS, pat["image"])
    if os.path.exists(img_path):
        from PIL import Image
        img = Image.open(img_path)
        img_w, img_h = img.size
        dpi = 96
        emu_w = int(img_w / dpi * 914400)
        emu_h = int(img_h / dpi * 914400)
        max_w = Inches(6.8)
        max_h = Inches(5.2)
        scale = min(max_w / emu_w, max_h / emu_h, 1.0)
        final_w = int(emu_w * scale)
        final_h = int(emu_h * scale)
        left = Inches(6.2)
        top = Inches(1.5)
        slide.shapes.add_picture(img_path, left, top, final_w, final_h)

# ── Slide: Choosing the Right Pattern ───────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide)
add_textbox(slide, Inches(0.5), Inches(0.3), Inches(12), Inches(0.8),
            "Choosing the Right Pattern", font_size=36, color=TITLE_COLOR, bold=True,
            alignment=PP_ALIGN.CENTER)

table_data = [
    ("Situation", "Recommended Pattern"),
    ("Simple pipeline with clear steps", "Sequential"),
    ("Need multiple perspectives fast", "Parallel"),
    ("Quality is critical, time isn't", "Loop"),
    ("Different inputs need different handling", "Conditional"),
    ("Complex task, unclear how to break down", "Supervisor"),
    ("Need human approval or input", "Human-in-the-Loop"),
    ("Many dependencies, need optimal path", "GOAP"),
    ("Creative / brainstorming collaboration", "P2P"),
]

rows = len(table_data)
cols = 2
tbl_w = Inches(10)
tbl_h = Inches(4.5)
left = int((SLIDE_W - tbl_w) / 2)
top = Inches(1.5)
table_shape = slide.shapes.add_table(rows, cols, left, top, tbl_w, tbl_h)
table = table_shape.table
table.columns[0].width = Inches(6.5)
table.columns[1].width = Inches(3.5)

for r, (col0, col1) in enumerate(table_data):
    for c, val in enumerate((col0, col1)):
        cell = table.cell(r, c)
        cell.text = val
        p = cell.text_frame.paragraphs[0]
        p.font.size = Pt(14)
        p.font.name = "Segoe UI"
        if r == 0:
            p.font.bold = True
            p.font.color.rgb = TITLE_COLOR
            cell.fill.solid()
            cell.fill.fore_color.rgb = TABLE_HEADER_BG
        else:
            p.font.color.rgb = TEXT_COLOR
            cell.fill.solid()
            cell.fill.fore_color.rgb = TABLE_ROW_BG

# ── Slide: Tech Stack ───────────────────────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide)
add_textbox(slide, Inches(0.5), Inches(0.3), Inches(12), Inches(0.8),
            "Tech Stack", font_size=36, color=TITLE_COLOR, bold=True,
            alignment=PP_ALIGN.CENTER)

backend_text = (
    "Backend\n\n"
    "•  Java 21 with Virtual Threads\n"
    "•  Spring Boot 4.0\n"
    "•  LangChain4j 1.10.0 (Core)\n"
    "•  LangChain4j Agentic 1.10.0-beta18\n"
    "•  Azure OpenAI integration\n"
    "•  STOMP over SockJS WebSockets"
)
add_textbox(slide, Inches(0.8), Inches(1.5), Inches(5.5), Inches(5),
            backend_text, font_size=16, color=TEXT_COLOR)

frontend_text = (
    "Frontend\n\n"
    "•  React 18 with TypeScript\n"
    "•  Vite 5 build tool\n"
    "•  D3.js for visualizations\n"
    "•  Tailwind CSS for styling\n"
    "•  React Router for navigation"
)
add_textbox(slide, Inches(6.8), Inches(1.5), Inches(5.5), Inches(5),
            frontend_text, font_size=16, color=TEXT_COLOR)

# ── Slide: AgenticScope ─────────────────────────────────────────
slide = prs.slides.add_slide(prs.slide_layouts[6])
set_slide_bg(slide)
add_textbox(slide, Inches(0.5), Inches(0.3), Inches(12), Inches(0.8),
            "AgenticScope: Unified State Management", font_size=36, color=TITLE_COLOR,
            bold=True, alignment=PP_ALIGN.CENTER)

scope_text = (
    "All 8 patterns use LangChain4j's AgenticServices builders with AgenticScope\n\n"
    "•  State sharing between agents via scope.readState() / scope.writeState()\n"
    "•  Output key mapping via @Agent(outputKey = \"result\")\n"
    "•  Agent invocation tracking for debugging\n"
    "•  Real-time events via AgentListener for WebSocket streaming\n\n"
    "Two Equivalent Approaches:\n\n"
    "•  Programmatic — Builder APIs (sequenceBuilder, loopBuilder, etc.)\n"
    "   Best for dynamic workflows and runtime configuration\n\n"
    "•  Declarative — Annotations + createAgenticSystem()\n"
    "   Best for simple, readable definitions with compile-time validation"
)
add_textbox(slide, Inches(0.8), Inches(1.4), Inches(11.5), Inches(5.5),
            scope_text, font_size=16, color=TEXT_COLOR)

# ── Save ─────────────────────────────────────────────────────────
output_path = os.path.join(os.path.dirname(__file__), "MatrixAgents-Showcase.pptx")
prs.save(output_path)
print(f"Presentation saved to: {output_path}")
