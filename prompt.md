# SHAMBA SMART - Farm Management Application

## PROJECT OVERVIEW
Build a mobile-first farm management and tracking application called Shamba Smart (or similar Swahili-inspired name) for a 16-acre mixed farm in Korogwe, Tanga, Tanzania.

### Farm Operations
- **Livestock:** Goats (dairy focus) and sheep
- **Feed production:** Silage crops (napier grass, maize for silage)
- **Food crops:** Maize, beans, cassava, sweet potato
- **Vegetables:** Tomatoes, kale, onions, capsicum
- **Value addition:** Goat milk cheese production and sales

### Core Requirements
- Offline-first (unreliable connectivity in rural Tanga)
- Sync when online
- Swahili and English support
- Usable by farm workers with basic smartphone literacy

---

## FEATURE MODULES

### 1. 🐐 LIVESTOCK MANAGEMENT

**Animal Profiles:**
- Individual records: ID/tag, species (goat/sheep), breed, sex, DOB, weight history, photo
- Herd overview dashboard: total count by species, age group, sex

**Health Records:**
- Vaccinations (dates, vaccine name, batch, next due)
- Deworming schedule, treatments, vet visits, illness history
- Automated health alerts: upcoming vaccinations, deworming due, sick animals

**Reproduction Tracking:**
- Heat detection log, mating records (sire + dam)
- Pregnancy confirmation, expected kidding/lambing date
- Birth records (number born, alive, stillborn, birth weight)
- Multiple kids born in one birth — batch birth entry

**Growth & Production:**
- Weight entries with chart, feed conversion ratio
- Milk production log per doe: daily AM/PM yield in litres
- Lactation curve chart, dry-off date
- Culling recommendations (poor milk yield, repeated illness, age)

**Mortality & Edge Cases:**
- Mortality log: date, animal ID, cause of death
- Animal with no tag yet (newborn) — allow unnamed/untagged entry
- Doe that is both pregnant and still producing milk (transitional period)
- Dead animal with open health records — auto-close and archive
- Animal sold on credit — outstanding payment tracker

---

### 2. 🌾 CROP & PASTURE MANAGEMENT

**Field Registry:**
- Name, size (acres), GPS boundary or map pin, soil type, current use
- 16-acre map view showing plots and current crop/status

**Crop Records:**
- Planting: crop type, variety, planting date, seed source, seed quantity
- Growth stage tracking with reminders (germination, thinning, topdressing, harvest)
- Input application log: fertiliser, pesticide, herbicide — date, product, rate, plot, applicator
- Irrigation log: date, method, duration, water source

**Harvest & Silage:**
- Harvest records: date, quantity (kg/bags), quality grade, destination
- Silage module: chopping date, crop type, pit/bale number, fermentation start, feed-out date, quality notes
- Crop harvest split across multiple days or buyers
- Plot used for both silage and intercrop (maize + beans)

**Analytics:**
- Yield history and season comparison charts
- Weather log (manual entry): rainfall mm, temperature, unusual events
- Integration with open weather API (Korogwe coordinates: -5.15, 38.48)
- Season planner: long rains (March–May), short rains (Oct–Dec), dry seasons

---

### 3. 🧀 CHEESE & VALUE ADDITION

**Milk & Production:**
- Milk collection log: date, quantity, contributing does, quality check (smell, colour, pH)
- Cheese batch records: batch ID, date, milk volume, cheese type, starter culture, rennet, yield (kg)
- Aging tracking: start date, location, quality notes, outcome rating
- Milk batch rejected for cheese (sour, contaminated) — log reason and disposal
- Cheese batch lost to spoilage — loss entry, reason, write-off

**Inventory & Sales:**
- Packaging log: units packed, weight, packaging date, label batch
- Cost per batch calculator (milk cost + inputs + labour + packaging)
- Inventory: quantity on hand, aging stage, reserved vs. available
- Sales log: buyer name, contact, date, quantity, price per kg, payment method, paid/unpaid
- Revenue vs. cost per batch margin summary

---

### 4. 📦 FEED & STORE MANAGEMENT

**Feed Inventory:**
- Silage (tonnes), hay, concentrates, minerals, salt licks — current stock levels
- Daily feed allocation log: ration per animal group, total consumed
- Feed requirement calculator: based on herd size and stage (lactating, dry, growing, pregnant)
- Silage pit inventory: pit ID, fill date, tonnage, feed-out start, daily draw-down log
- Silage pit opened before fermentation complete — warn user
- Reorder alerts when feed stock falls below threshold

**Store Management:**
- Inventory: seeds, fertilisers, chemicals, medicine, equipment — quantity, unit, expiry date
- Procurement log: item, supplier, quantity, unit cost, total, delivery date, invoice number
- Reorder level alerts

---

### 5. 💰 FINANCIAL MANAGEMENT

**Income & Expenses:**
- Income tracker: milk sales, cheese sales, live animal sales, crop sales, manure sales
- Expense tracker: feed, labour, vet, medicine, seeds, fertiliser, fuel, equipment, repairs, packaging
- Petty cash log

**Reports & Analysis:**
- Categorised P&L summary: weekly, monthly, seasonal, annual
- Enterprise profitability: separate P&L for goats, sheep, crops, cheese
- Budget vs. actual comparison
- Export reports as PDF or CSV

**Credit & Loans:**
- Loan/credit tracker: lender, amount, disbursement date, repayment schedule, balance
- Animal sold on credit — outstanding payment tracker
- Worker partial payments (advance payments)

---

### 6. 👷 LABOUR MANAGEMENT

**Worker Profiles:**
- Name, role, contact, hire date, daily/monthly rate
- Seasonal worker (not permanent) — date-bounded employment record

**Attendance & Tasks:**
- Daily attendance log: present, absent, half-day, reason
- Task assignment: task name, assigned to, plot/animal group, due date, status
- Daily work journal: what was done, who did it, time spent

**Payroll:**
- Days worked × rate, deductions, net pay per period
- Advance payment tracking

---

### 7. 📅 CALENDAR & TASK PLANNER

- Farm calendar: planting dates, harvest windows, vaccination schedules, cheese aging milestones, market days, loan repayment dates
- Recurring task templates (weekly dipping, monthly deworming, quarterly soil testing)
- Push notifications for overdue tasks and upcoming deadlines
- Season planner with crop rotation

---

### 8. 📊 DASHBOARD & ANALYTICS

**Home Dashboard:**
- Today's tasks, alerts (sick animals, low feed, overdue vaccinations), quick stats
- KPI cards: herd size, milk yield today, cheese inventory, cash balance, open tasks

**Charts & Trends:**
- Milk trend (30/90 days), revenue vs. expenses (monthly)
- Herd growth curve, crop yield by season

**Alerts Engine:**
- Animal not weighed in 30 days
- Vaccination overdue
- Silage stock < 2 weeks
- Payment overdue
- Crop past harvest window

---

### 9. 🗺️ LAND & INFRASTRUCTURE

**Farm Map:**
- Plot boundaries, animal shelters, water points, storage, cheese room, compost pits

**Infrastructure:**
- Fence condition, water trough status, shelter repairs needed
- Maintenance schedule: dipping tank cleaning, equipment servicing

---

### 10. ⚙️ SETTINGS & SYSTEM

**User Management:**
- Multi-user with roles: Owner (full access), Farm Manager (all except financials), Worker (tasks + daily logs only)

**Data & Sync:**
- Offline-first with background sync (SQLite local + cloud backup)
- Data export: full farm report PDF, CSV for financials
- Backup and restore

**Configuration:**
- Language toggle: English / Kiswahili
- Farm profile: name, location (Korogwe, Tanga), owner contact, farm size, registration number
- Notification preferences

---

## ADVANCED FEATURES (PROFIT OPTIMIZATION)

### 1. 🧮 LEAST-COST RATION (LCR) SOLVER
Feed is the #1 variable cost. Optimize mix of home-grown silage with purchased supplements.

- Uses Pearson Square algorithm or linear programming (offline)
- Input current market prices per kg of supplement
- Calculate Dry Matter (DM) and Crude Protein (CP) requirements
- **Profit Impact:** Reduces purchased feed costs by 15–20%

### 2. 📷 COMPUTER VISION COLORIMETER
HSV-Space Colorimetric Grader for crops and cheese maturity.

- Analyze Hue, Saturation, Value color spaces
- "Harvest Window Alerts" comparing against "Peak Value" database
- Generates signed QR Invoice with embedded maturity metadata
- **Profit Impact:** Grade A produce commands 25%+ price premium

### 3. 💧 EVAPOTRANSPIRATION (ET) WATER OPTIMIZER
Penman-Monteith (FAO-56) formula for irrigation optimization.

- Calculate daily Reference Evapotranspiration (ET₀)
- Log daily max/min temp and wind
- "Pumping Block" — prevents wasteful fuel/energy spend
- **Profit Impact:** Saves fuel and prevents nutrient leaching

### 4. 📈 DIGITAL TWIN PLOT BENCHMARKING
Virtual replica of each plot tracking Input Intensity vs. Yield Output.

- "Clone the Champion" — identifies most profitable plot
- Generates step-by-step "Success SOP" for underperforming plots
- $/m² Profit Heatmap overlay on 16-acre farm map

### 5. 🔊 ACOUSTIC GUARD (PEST & DISTRESS)
Always-on audio classifier for pest and animal distress detection.

- Detects Fall Armyworm "munching" or goat respiratory distress
- Push notification to dashboard
- Uses Qualcomm Sensing Hub for low-power operation

---

## ARCHITECTURAL REQUIREMENTS

### Compute & Performance
- **NPU Acceleration:** Offload all ML workloads to Hexagon NPU via QNN delegate
- **INT4 Quantization:** Maximize TOPS while maintaining 144Hz screen fluidity
- **Volatile RAM Processing:** All vision processing in volatile RAM for privacy

### Data Architecture
- **Persistence:** Encrypted Room DB (via SQLCipher)
- **Sync:** WorkManager for "Silent Delta Sync" using last_synced_timestamp
- **UDF (Unidirectional Data Flow):** UI observes Kotlin Flow from DB; network updates DB, never UI directly

### UI/UX
- **Layout:** NavigableListDetailPaneScaffold (dual-pane "War Room")
- **Left Pane:** High-density list of assets (Plots, Animals, Workers)
- **Right Pane:** Detailed telemetry, 3D volumetric scans (ARCore), real-time P&L charts
- **Multitasking:** Compatible with HyperOS Workstation Mode (floating windows)

---

## DEVELOPER DELEGATION PROMPT

Build a Kotlin-based Android ERP for the SM7675-AB SoC using a Single Source of Truth architecture (Room + SQLCipher). Implement a dual-pane 'War Room' UI using Jetpack Compose Adaptive layouts. Offload an OpenCV-based HSV grading model and a Simplex LCR solver to the Hexagon NPU via the QNN delegate. Ensure all vision processing is handled in volatile RAM for privacy. Include a Penman-Monteith water-balancing module and a $/m² profit heatmap. Prioritize INT4 quantization for all ML models to ensure 144Hz UI stability.