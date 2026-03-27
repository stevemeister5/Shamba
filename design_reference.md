🎨 Shamba Smart — Visual Design Overhaul Prompt
Xiaomi Pad 7 · 2560×1600 · 144Hz · 11.2" · Modern Tablet-First Design

DEVICE CONTEXT
Design exclusively for the Xiaomi Pad 7 display characteristics:

Resolution: 2560×1600 pixels
Aspect ratio: 16:10
Screen size: 11.2 inches
Refresh rate: 144Hz — all animations and transitions must be tuned for 144Hz. Nothing should feel choppy or over-eased. Motion should feel physical and immediate.
Brightness: up to 900 nits — colours must remain vivid and readable at high brightness outdoor settings
Display type: LCD — avoid pure OLED-optimised design choices like pure black (#000000) backgrounds. Use deep but slightly lifted darks.
Pixel density: ~275 PPI — every detail renders crisply at this density. Fine typography, thin borders, subtle textures all look sharp. Do not shy away from detail.
Orientation: landscape primary. The app lives in landscape. No portrait mode required.
Safe area: account for the front camera notch position in landscape. Do not place critical UI elements in the top-right corner notch zone.


DESIGN DIRECTION — ORGANIC DARK PRECISION
The aesthetic is called Organic Dark Precision. It sits at the intersection of three references:
Linear B meets agricultural intelligence — the structured confidence of Bloomberg Terminal-style data density, but with warmth and organic texture woven through it. Data is king but the farm is alive.
East African craft meets silicon — the rich ochres, terracottas, and forest greens of Tanzanian textiles and landscape, expressed through a modern lens. Not folksy. Not rustic. Elevated and contemporary.
Instrument panel meets field notebook — the app should feel like a precision instrument that also understands dirt, seasons, and living things. Think the cockpit of a Land Cruiser that has been beautifully customised.
The result: a deep, rich dark interface with warm undertones, razor-sharp typography, data-dense layouts that breathe, and microanimations that feel physical and satisfying at 144Hz.

COLOUR SYSTEM
Define all colours as CSS/design tokens. Every colour choice has a reason.
Base Surfaces
--surface-base:       #0D1210   Deep forest black, slightly warm
--surface-raised:     #141A17   Cards and panels
--surface-elevated:   #1C2420   Modals, dropdowns, overlays
--surface-overlay:    #232E29   Hover states, selected rows
--surface-sunken:     #0A0F0D   Input fields, inset wells
Primary Brand — Forest Green Ramp
--green-950:  #051208
--green-900:  #0A2114
--green-800:  #0F3320
--green-700:  #1A5C35
--green-600:  #237A45
--green-500:  #2E9E58   Primary action colour
--green-400:  #42C06E   Interactive highlights
--green-300:  #6DD68F   Success states
--green-200:  #A3E8BB   Light accents
--green-100:  #D4F5E2   Text on dark green
--green-50:   #EDFBF3   Lightest tint
Warm Accent — Korogwe Earth Ramp
--earth-900:  #2A1505
--earth-800:  #4A2508
--earth-700:  #7A3F0D
--earth-600:  #A85A16
--earth-500:  #D4751F   Primary warm accent
--earth-400:  #E8922E   Alerts, harvest indicators
--earth-300:  #F2B06A   Secondary warm
--earth-200:  #F8D4A8   Light warm
--earth-100:  #FDF0E0   Warmest tint
Teal — Data and Milk
--teal-600:   #0D6B62
--teal-500:   #0E8F82
--teal-400:   #12B5A5
--teal-300:   #3DCEC0
--teal-200:   #87E4DC
--teal-100:   #C8F5F1
Amber — Warnings and Gold
--amber-600:  #92600A
--amber-500:  #C4820E
--amber-400:  #F0A820
--amber-300:  #F7C55A
--amber-200:  #FADA96
--amber-100:  #FEF3D0
Red — Alerts and Danger
--red-600:    #8B1C1C
--red-500:    #B52626
--red-400:    #DE3535
--red-300:    #F06666
--red-200:    #F8AAAA
--red-100:    #FDDEDE
Neutral — Text and Structure
--neutral-950: #F8FAF9   Primary text (warm white, not pure)
--neutral-800: #C4CEC9   Secondary text
--neutral-600: #8A9E96   Tertiary text, placeholders
--neutral-400: #4A5C55   Disabled, muted
--neutral-300: #2E3D37   Borders, dividers
--neutral-200: #202C27   Subtle borders
--neutral-100: #171F1C   Hairline borders

TYPOGRAPHY SYSTEM
Primary typeface: Geist (Vercel's open-source font — sharp, modern, technical, excellent at small sizes, beautiful at large sizes. Free. Available via npm or Google Fonts mirror.)
Monospace typeface: Geist Mono (for all numeric data — milk yields, weights, TZS amounts, dates, IDs. Numbers should feel like instrument readouts.)
Display typeface: Zodiak or Canela for the wordmark and hero-level headings only. If unavailable, DM Serif Display as fallback. Used sparingly — maximum 3 instances in the entire app.
Type Scale:

--text-hero:    52px / 56px  / weight 300  / tracking -0.03em
                (Farm name in welcome screen only)

--text-display: 32px / 38px  / weight 400  / tracking -0.02em
                (Dashboard section heroes, large KPI numbers)

--text-title:   22px / 28px  / weight 500  / tracking -0.01em
                (Page titles, module headers)

--text-heading: 17px / 24px  / weight 500  / tracking -0.005em
                (Card headers, section labels)

--text-body:    15px / 22px  / weight 400  / tracking 0
                (Primary reading text, descriptions)

--text-label:   13px / 18px  / weight 500  / tracking 0.02em
                (Form labels, table headers — uppercase)

--text-caption: 12px / 16px  / weight 400  / tracking 0.01em
                (Timestamps, source citations, helper text)

--text-micro:   10px / 14px  / weight 500  / tracking 0.04em  uppercase
                (Status chips, badges, nav labels)

All numeric data rendered in Geist Mono.
KPI values: 36–48px, weight 300, Geist Mono, tracking -0.02em.
This makes large numbers feel precise and instrument-like.

SPATIAL SYSTEM
Base unit: 4px

Spacing scale:
  --space-1:   4px
  --space-2:   8px
  --space-3:   12px
  --space-4:   16px
  --space-5:   20px
  --space-6:   24px
  --space-8:   32px
  --space-10:  40px
  --space-12:  48px
  --space-16:  64px

Border radius scale:
  --radius-sm:   6px    (chips, badges, small buttons)
  --radius-md:   10px   (inputs, small cards)
  --radius-lg:   14px   (cards, panels)
  --radius-xl:   20px   (modals, large containers)
  --radius-full: 9999px (pills, avatars, toggles)

Border widths:
  --border-hairline: 0.5px  (subtle dividers)
  --border-thin:     1px    (card borders)
  --border-medium:   1.5px  (focused inputs, selected states)
  --border-thick:    2px    (active nav items, featured cards)

ELEVATION AND DEPTH SYSTEM
Do not use drop shadows — they look muddy on dark interfaces. Use border-based elevation instead:
Level 0 — Sunken (input fields, inset areas)
  background: --surface-sunken
  border: 1px solid --neutral-100

Level 1 — Base (page background)
  background: --surface-base
  no border

Level 2 — Raised (standard cards)
  background: --surface-raised
  border: 1px solid --neutral-200
  
Level 3 — Elevated (hover state, selected card)
  background: --surface-elevated
  border: 1px solid --neutral-300

Level 4 — Overlay (modals, dropdowns, tooltips)
  background: --surface-overlay
  border: 1px solid --neutral-300
  backdrop: blur(20px) on content behind

Accent elevation — featured cards, active states:
  border: 1.5px solid --green-700
  background: linear gradient from --green-950 to --surface-raised
  (subtle, almost imperceptible gradient — 8% opacity difference only)

LAYOUT ARCHITECTURE
The app uses a three-zone persistent layout in landscape:
┌─────────────────────────────────────────────────────────────┐
│  TOP BAR  (56px height, full width)                         │
├──────┬──────────────────────────────────────┬───────────────┤
│      │                                      │               │
│ NAV  │         MAIN CONTENT AREA            │  CONTEXT      │
│ RAIL │         (flexible, scrollable)       │  PANEL        │
│      │                                      │  (320px)      │
│(72px)│                                      │               │
│      │                                      │               │
└──────┴──────────────────────────────────────┴───────────────┘
Top bar: 56px. Full bleed across the screen. Contains farm name (wordmark style), breadcrumb trail, global search, notification centre, weather summary, user avatar. Separated from content below by a 1px hairline border only — no shadow.
Navigation rail: 72px wide. Fixed. Contains icon + micro-label navigation items. Never expands to a full sidebar — stays as a rail always. The rail has its own background one step darker than the main surface. Active item gets a green pill indicator to its left (4px wide, full height of the icon area, border-radius full). Hovering an item shows a soft green-tinted background. Bottom of rail: farm status indicator (a small animated dot — green pulsing for healthy, amber for alerts, red for critical).
Main content area: Fills the remaining width between rail and context panel. Scrolls vertically. Never scrolls horizontally. Uses a 12-column grid with 20px gutters. Content padding: 24px top, 24px left, 24px right.
Context panel: 320px fixed right panel. Always visible. Contains the Maarifa Ask panel, quick animal lookup, or context-sensitive information about whatever is selected in the main area. Has a subtle left border separating it from the main content. Can be collapsed to 0px with a smooth 200ms ease animation, giving the main content area more width. A small pull-tab handle sits at the left edge of the panel for collapse/expand.

COMPONENT LIBRARY
Navigation Rail Items
Default state:
  Icon: 22px, colour --neutral-600
  Label: 10px, weight 500, uppercase, --neutral-600
  Background: transparent
  Left indicator: hidden

Hover state (144Hz — instant response, 80ms fade):
  Icon: --neutral-800
  Label: --neutral-800
  Background: rgba(46, 158, 88, 0.08) with --radius-md
  Transition: background 80ms ease

Active state:
  Icon: --green-400
  Label: --green-300
  Background: rgba(46, 158, 88, 0.12)
  Left indicator: 4px × 32px pill, --green-500, visible
  Border-radius on indicator: --radius-full
KPI Cards
The most important UI component in the app. Must feel like precision instruments.
Container:
  Background: --surface-raised
  Border: 1px solid --neutral-200
  Border-radius: --radius-lg
  Padding: 20px 24px
  Min-height: 96px

Layout (vertical stack):
  Row 1: icon (16px, coloured) + label (--text-micro, uppercase, --neutral-600)
         spaced apart with space-between
  Row 2: primary value (36–44px, Geist Mono, weight 300, --neutral-950)
  Row 3: delta or subtitle (--text-caption, coloured by direction)

Delta indicators:
  Positive: --green-400, ↑ prefix
  Negative: --red-400, ↓ prefix
  Neutral: --neutral-600, — prefix

Accent variant (for the single most critical metric on screen):
  Border: 1.5px solid --green-700
  Left edge: 3px solid --green-500 (full height, flush border-radius-lg)
  Background: subtle green-950 tint

Hover state:
  Border-colour transitions to --neutral-300
  Background lifts to --surface-elevated
  Transition: 100ms ease
Data Table Rows
Header row:
  Background: transparent
  Text: --text-micro, uppercase, --neutral-400, letter-spacing 0.06em
  Border-bottom: 1px solid --neutral-200
  Height: 36px

Data row default:
  Background: transparent
  Text: --text-body, --neutral-800
  Border-bottom: 0.5px solid --neutral-100
  Height: 48px
  Padding: 0 16px

Data row hover:
  Background: rgba(46, 158, 88, 0.05)
  Border-bottom: 0.5px solid --neutral-200
  Transition: 60ms ease (snappy at 144Hz)

Data row selected:
  Background: rgba(46, 158, 88, 0.10)
  Border-left: 3px solid --green-500
  Border-bottom: 0.5px solid --green-900

Numeric cells:
  Font: Geist Mono
  Text-align: right
  Tabular-nums: true (all numbers same width)
Status Chips
Base:
  Height: 22px
  Padding: 0 8px
  Border-radius: --radius-full
  Font: --text-micro, weight 600, uppercase

Healthy:
  Background: rgba(45, 158, 88, 0.15)
  Border: 0.5px solid --green-700
  Text: --green-300

Pregnant:
  Background: rgba(240, 168, 32, 0.15)
  Border: 0.5px solid --amber-600
  Text: --amber-300

Sick:
  Background: rgba(222, 53, 53, 0.15)
  Border: 0.5px solid --red-500
  Text: --red-300

Dry:
  Background: rgba(18, 181, 165, 0.12)
  Border: 0.5px solid --teal-600
  Text: --teal-300

Pending:
  Background: rgba(138, 158, 150, 0.12)
  Border: 0.5px solid --neutral-400
  Text: --neutral-600
Input Fields
Container:
  Background: --surface-sunken
  Border: 1px solid --neutral-200
  Border-radius: --radius-md
  Height: 42px
  Padding: 0 14px
  Font: --text-body, Geist

Focus state:
  Border: 1.5px solid --green-500
  Background: --surface-sunken
  Box-shadow: 0 0 0 3px rgba(46, 158, 88, 0.12)
  Transition: 80ms ease

Label above input:
  Font: --text-label, uppercase, --neutral-600
  Margin-bottom: 6px

Helper text below:
  Font: --text-caption, --neutral-400
  Margin-top: 4px

Error state:
  Border: 1.5px solid --red-500
  Box-shadow: 0 0 0 3px rgba(222, 53, 53, 0.10)
Buttons
Primary button:
  Background: --green-500
  Text: --green-50, --text-body, weight 500
  Height: 40px
  Padding: 0 20px
  Border-radius: --radius-md
  Border: none
  
  Hover: Background --green-400, transition 80ms
  Active: scale(0.97), transition 60ms
  Disabled: Background --green-900, text --green-700, cursor not-allowed

Ghost button:
  Background: transparent
  Border: 1px solid --neutral-300
  Text: --neutral-800
  
  Hover: Background rgba(255,255,255,0.04), border --neutral-400
  Active: scale(0.97)

Danger button:
  Background: rgba(222, 53, 53, 0.15)
  Border: 1px solid --red-600
  Text: --red-300
  
  Hover: Background rgba(222, 53, 53, 0.25)

Icon button (square):
  Width: 36px, Height: 36px
  Background: transparent
  Border: 1px solid --neutral-200
  Border-radius: --radius-md
  Icon: 16px, --neutral-600
  
  Hover: Border --neutral-300, icon --neutral-800, background rgba(255,255,255,0.04)
Cards
Standard card:
  Background: --surface-raised
  Border: 1px solid --neutral-200
  Border-radius: --radius-lg
  Padding: 20px 24px
  
  Card header:
    Font: --text-heading, --neutral-950
    Border-bottom: 0.5px solid --neutral-200
    Padding-bottom: 14px
    Margin-bottom: 16px
    Display: flex, space-between
    Right side: ghost action button or sec-action link

Featured card (Maarifa briefing, critical alert):
  Border: 1px solid --green-800
  Background: linear-gradient(135deg, #0A1A10 0%, #141A17 100%)
  Border-radius: --radius-xl
  Has a faint green glow on the top edge:
    box-shadow: inset 0 1px 0 rgba(66, 192, 110, 0.2)

Alert card:
  Red critical:
    Border-left: 3px solid --red-500
    Border: 1px solid rgba(222, 53, 53, 0.25)
    Background: rgba(222, 53, 53, 0.05)
  
  Amber warning:
    Border-left: 3px solid --amber-400
    Border: 1px solid rgba(240, 168, 32, 0.2)
    Background: rgba(240, 168, 32, 0.04)
  
  Blue info:
    Border-left: 3px solid #42A5F5
    Border: 1px solid rgba(66, 165, 245, 0.2)
    Background: rgba(66, 165, 245, 0.04)
Progress and Bar Charts
Progress bar:
  Track: --surface-sunken, height 6px, --radius-full
  Fill: gradient from --green-600 to --green-400
  Border-radius: --radius-full on fill as well

Milk production bar (per doe):
  Track height: 8px
  Label left: Geist Mono, 12px, --neutral-600, right-aligned in 56px column
  Value right: Geist Mono, 12px, --neutral-950, left-aligned in 44px column
  Fill colours:
    > 80% of herd average: --teal-400
    50–80%: --green-400
    < 50%: --amber-400

Mini bar chart (7-day history):
  Bar width: flexible, min 24px
  Gap: 4px
  Corner radius on bars: 3px top only
  Today bar: --green-400
  Past bars: --green-800 with --green-600 on hover
  Height scale: relative to max value in set
  Baseline: 1px solid --neutral-200

MOTION DESIGN — 144Hz SPECIFICATION
Every animation must be designed knowing the screen refreshes every 6.94ms. Sluggish, over-eased animations feel broken at 144Hz. Every transition should feel immediate and physical.
Micro-interactions (hover, press):
  Duration: 60–80ms
  Easing: linear or ease-out
  Properties: background-color, border-color, opacity, scale

State transitions (tab switch, filter change):
  Duration: 120–150ms
  Easing: cubic-bezier(0.2, 0, 0, 1)  — fast out, no bounce
  Properties: opacity, transform (translateY 4px → 0)

Panel slide (context panel collapse/expand):
  Duration: 200ms
  Easing: cubic-bezier(0.4, 0, 0.2, 1)
  Property: width, with content fading at 100ms

Page/module transitions:
  Duration: 180ms
  Easing: cubic-bezier(0.2, 0, 0, 1)
  Outgoing: opacity 1→0, translateX 0→-8px
  Incoming: opacity 0→1, translateX 8px→0
  Stagger child elements: 20ms delay per element, max 5 elements

Data loading (skeleton screens):
  Shimmer animation: 1200ms loop
  Direction: left to right
  Colour: from --neutral-100 to --neutral-200 and back
  Easing: ease-in-out
  No pulse — shimmer only, feels more refined

Number counters (KPI values on load):
  Count up from 0 to final value
  Duration: 600ms
  Easing: cubic-bezier(0.2, 0, 0, 1)
  Only trigger on first load or explicit refresh

Progress bar fills:
  Duration: 500ms
  Easing: cubic-bezier(0.2, 0, 0.4, 1)
  Trigger: on scroll into view

Status dot (farm health indicator in nav rail):
  Healthy: slow pulse, 3000ms, scale 1→1.3→1, opacity 1→0.6→1
  Alert: faster pulse, 1500ms, amber
  Critical: rapid pulse, 800ms, red

SCREEN-BY-SCREEN DESIGN SPECIFICATIONS
DASHBOARD
The dashboard uses the full widescreen canvas. Three-column layout within the main content area:
Left column (4 of 12 columns):

Morning briefing card — featured card style, full width of column. Maarifa icon (a small leaf/circuit hybrid SVG) in top left. Green tinted background. Text in --text-body. Feels like an instrument reading, not a chatbot.
Below it: 5-day weather strip. Horizontal row of 5 day cards. Each card: day name (micro), weather icon (18px SVG not emoji), high temp (Geist Mono), rainfall estimate (blue tinted). Compact and data-dense.

Centre column (5 of 12 columns):

KPI strip: 2×2 grid of KPI cards at top. Total animals + Milk today on top row. Revenue + Open tasks on bottom row.
Milk production card below: 7-day bar chart + per-doe breakdown bars. This is a hero card — give it generous height (280px minimum).
Alerts panel below milk card: compact list. Each alert is one line with a coloured left border, icon, text, timestamp. Tappable — opens detail.

Right column (3 of 12 columns):

Today's tasks card. Full height of column. Checklist with priority indicators. Clean, airy spacing. Checking a task triggers a satisfying 80ms scale animation on the checkbox.
Cheese inventory summary card below tasks. Shows active batches as compact progress bars. Aging progress in amber/gold.

KPI strip variant — hero numbers:
The five main KPIs render as a single horizontal strip pinned below the top bar (not in the column layout). Each KPI is separated by a 0.5px vertical divider. No card borders on this strip — it floats as a unified bar. Total animals, milk today, cheese stock, month revenue, open tasks — all in one scannable horizontal line. This frees the column layout below for richer content.
LIVESTOCK MODULE
Full-width data table with a master-detail pattern:
Left: Animal table (7 of 12 columns)
A high-density data table. Every row is an animal. Columns: status chip, ID (Geist Mono), name, breed, sex icon, weight (Geist Mono with kg unit in --neutral-400), today's yield (teal, Geist Mono). Table is sorted by status (sick first, then pregnant, then healthy). Filter bar sits above the table: species toggle (Goat / Sheep / All as pill toggles), status filter chips, search input.
Right: Detail panel (5 of 12 columns)
When an animal row is tapped, the right panel slides in (180ms) with the full animal profile. Tabs across the top of the panel: Overview, Health, Milk, Reproduction, Treatments. Each tab renders without a page reload. The Maarifa context card for this specific animal is always pinned at the bottom of the detail panel, below a 1px divider.
CROPS MODULE
Farm map as the hero element:
Top: Map view (full width, 340px height)
Mapbox offline map showing the 16-acre farm with plot polygons overlaid. Each polygon is filled with a semi-transparent colour corresponding to crop type (green for grass/silage, amber for grain crops, teal for vegetables). Selected plot gets a white border and a label callout. The map has a dark map style (Mapbox Dark or a custom dark style matching the app palette).
Below map: Plot grid
8-card grid. Each plot card: plot name, acreage (Geist Mono), crop emoji + crop name, growth stage label, progress bar to harvest, last activity date. Cards are compact — 4 columns × 2 rows. Tap a card to expand to full plot detail in the context panel.
MAARIFA PANEL
The context panel transforms into the Maarifa interface when the Ask tab is active:
Ask tab:
Top area: conversation history. Each exchange is a distinct bubble. User queries: right-aligned, dark green background, white text. Maarifa answers: left-aligned, --surface-elevated background, structured with internal typography hierarchy. Source citations render as small chips below each answer — tappable to see full source details.
Answer confidence tier renders as a coloured bar at the left edge of each answer bubble:

Green: rule-governed
Blue: retrieval-confirmed
Amber: retrieval-partial
Grey: not found

Input area: pinned at bottom of panel. Large text input (full width minus send button). Send button: green, icon only (arrow), 40px × 40px, --radius-md.
Browse tab:
Collapsible tree navigation. Each domain is a top-level item with a count badge. Expanding a domain shows topic entries. Entries have a bookmark icon on the right edge. Selected entry highlights in green and renders content in a reading view that replaces the tree (with a back button).

TEXTURE AND DETAIL LAYER
Applied subtly — never overwhelming. These details make the interface feel crafted rather than generated:
Noise texture on top bar and nav rail:
A very subtle grain overlay (SVG filter or PNG texture at 3–5% opacity) on the top bar and nav rail surfaces. This breaks up the flatness of solid dark colours and adds tactile quality without looking retro or grungy.
Grid dot pattern on empty states:
When a module has no data (empty animal list, no plots added yet), the background of the empty area shows a very faint dot grid (1px dots, 24px spacing, --neutral-100 colour, 30% opacity). This makes empty states feel designed rather than broken.
Animated gradient border on featured/active cards:
The morning briefing card and the currently selected animal profile card have a very subtle animated gradient on their border — the green light slowly rotates around the border at 8-second cycle. Implementation: conic-gradient rotating via CSS animation. Opacity very low (20%) so it reads as a gentle shimmer rather than a neon effect.
Leaf vein motif in the top bar:
Extremely subtle — a SVG path in the far right background of the top bar (behind content, opacity 4%) that resembles a magnified leaf vein structure. Colour: --green-800. This ties the interface to the agricultural context without being literal or decorative in a heavy-handed way.

ICONOGRAPHY
Use Phosphor Icons (React Native compatible, consistent stroke weight, 592 icons, free). Set weight to regular throughout with bold only for active/selected states.
Icon sizes:
Navigation rail: 22px
Card headers: 16px
Inline (within text rows): 14px
KPI card accent: 18px
Button icons: 16px
Status indicators: 12px
All icons inherit their parent's colour unless explicitly overridden. Never use emoji as icons in the interface — SVG icons only. Emoji are permitted only in the farm map plot overlays and knowledge browse entries where they serve as visual shorthand for crop types.

SPECIFIC COMPONENT DESIGNS
The Farm Health Indicator (nav rail bottom)
A 32px × 32px circle at the bottom of the nav rail. Contains a simplified farm silhouette icon (house + leaf). Background colour shifts with farm status:

All healthy: --green-900 background, --green-400 icon, slow pulse
Alerts present: --amber-700 background, --amber-300 icon, medium pulse
Critical alerts: --red-800 background, --red-300 icon, fast pulse

Tapping it opens the full alerts screen.
The Maarifa Floating Tab
A vertical pill on the right edge of the screen (outside the context panel, between context panel and screen edge). 140px tall × 32px wide. Background: --green-700. Border-radius: --radius-full on left side only. Contains: a small leaf+circuit icon (16px) and the text "Ask" rotated 90°. On tap: context panel expands to Ask tab with a 200ms slide. The pill has a very subtle pulsing animation if Maarifa has a proactive suggestion pending (new morning briefing, triggered alert).
Withdrawal Period Tracker
When an animal is in a withdrawal period, its row in the livestock table gets a special treatment:

A thin amber line runs across the full width of the row (1px, top edge)
The milk yield cell shows a lock icon instead of a yield figure
Hovering/tapping shows a tooltip: "Withdrawal period — safe from [date]. X days remaining."
The tooltip has a Geist Mono countdown format and a small progress bar showing days elapsed vs total withdrawal days.

Silage Depletion Gauge
In the feed module, the silage stock is shown as a circular gauge rather than a progress bar. The gauge is 120px diameter. The arc fills clockwise from the bottom. Colour transitions: green (>50% remaining) → amber (20–50%) → red (<20%). The centre of the gauge shows the days remaining in large Geist Mono. Below the gauge: "at current draw rate."

EMPTY STATES AND LOADING STATES
Empty states:
Each module has a designed empty state — not a generic "No data" message. Examples:

Empty livestock: silhouette of a goat in --neutral-200, text "Add your first animal to begin tracking your herd."
Empty plots: aerial farm silhouette in --neutral-200, text "Register your plots to start tracking crops."
Empty cheese batches: wheel of cheese outline in --neutral-200, text "Start your first cheese batch."

All empty state illustrations are simple SVG paths — monochrome, --neutral-300, no fill. Elegant and minimal.
Loading/skeleton states:
Every card, table row, and KPI value has a skeleton state. Skeletons match the exact dimensions of the content they represent — no generic grey boxes. They shimmer with the left-to-right animation specified in the motion system. Data appears progressively as it loads from SQLite — the app never shows a full-page spinner.

ACCESSIBILITY
Despite the dark aesthetic, maintain WCAG AA contrast ratios throughout:

Primary text (--neutral-950 on --surface-raised): contrast ratio > 7:1
Secondary text (--neutral-800 on --surface-raised): contrast ratio > 4.5:1
All interactive elements have visible focus states: 3px green focus ring
Touch targets minimum 44×44px (important for farm-dirty hands outdoors)
Status information never conveyed by colour alone — always paired with icon or text label


DESIGN SYSTEM DELIVERABLES
The developer implementing this design should produce:

A complete design token file (tokens.ts) exporting all colour, spacing, radius, and typography values as typed constants
A component library covering all components specified above, each with default, hover, active, disabled, and focus states
A theme provider wrapping the app that applies all tokens globally
A motion utility (animations.ts) exporting all animation configurations as reusable constants
A skeleton component system where every data-displaying component has a corresponding skeleton variant
Storybook or equivalent component documentation showing every component in all states


DESIGN REFERENCES FOR MOOD ALIGNMENT
When the developer or designer needs to calibrate the overall feel, reference these:

Linear app — for the precision, data density, and dark surface quality
Vercel dashboard — for the typography system and monospace data treatment
Raycast — for the microinteraction quality and keyboard-first refinement
Nothing OS — for the restraint and use of white space within a dark system
Bloomberg Terminal (modernised) — for the data-dense but readable instrument panel feeling

The result should feel like those references but warmer, more organic, and unmistakably connected to the land it is designed to serve.