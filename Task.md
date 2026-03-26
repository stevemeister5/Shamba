# SHAMBA SMART - Development Tasks

> **Instructions:** Complete each task in order. After completing each task:
> 1. Run `git add -A`
> 2. Run `git commit -m "Task X: [Task Name]"`
> 3. Update Context.md with progress

---

## Phase 1: Project Setup & Foundation

### Task 1: Initialize Android Project
- Create new Kotlin Android project with Jetpack Compose
- Configure Gradle with required dependencies (Room, SQLCipher, Compose Adaptive, etc.)
- Set up project structure following clean architecture
- Initialize Git repository
- **Commit:** `git init && git add -A && git commit -m "Task 1: Initialize Android project with dependencies"`

### Task 2: Configure Database Layer
- Set up Room database with SQLCipher encryption
- Create base Entity classes for all modules
- Implement database migrations strategy
- Create TypeConverters for complex data types
- **Commit:** `git add -A && git commit -m "Task 2: Configure Room database with SQLCipher encryption"`

### Task 3: Implement Data Sync Architecture
- Create WorkManager setup for background sync
- Implement sync status tracking (last_synced_timestamp)
- Build delta sync logic (only sync changed data)
- Create conflict resolution strategy
- **Commit:** `git add -A && git commit -m "Task 3: Implement offline-first data sync architecture"`

### Task 4: Build Unidirectional Data Flow (UDF)
- Create Repository pattern for all entities
- Implement Kotlin Flow for reactive data observation
- Build ViewModel base classes
- Set up dependency injection (Hilt)
- **Commit:** `git add -A && git commit -m "Task 4: Implement UDF with repositories and ViewModels"`

---

## Phase 2: Core UI Framework

### Task 5: Create Dual-Pane "War Room" Layout
- Implement NavigableListDetailPaneScaffold
- Create responsive layout for tablet (Xiaomi Pad 7)
- Build navigation graph
- Set up theme and styling
- **Commit:** `git add -A && git commit -m "Task 5: Build dual-pane War Room UI layout"`

### Task 6: Build Home Dashboard
- Create dashboard screen with KPI cards
- Implement alerts section (sick animals, low feed, overdue vaccinations)
- Add quick stats display
- Build task summary widget
- **Commit:** `git add -A && git commit -m "Task 6: Create home dashboard with KPIs and alerts"`

### Task 7: Implement Settings & User Management
- Create settings screen
- Build user roles system (Owner, Farm Manager, Worker)
- Implement language toggle (English/Kiswahili)
- Add farm profile configuration
- Create notification preferences
- **Commit:** `git add -A && git commit -m "Task 7: Implement settings and multi-user roles"`

---

## Phase 3: Farm Setup & Initialization

### Task 8: Create Farm Setup Module
- Build FarmSetupScreen with step-by-step wizard workflow
- Implement farm mapping with GPS boundary drawing
- Create building planner (shelters, storage, cheese room, water points)
- Build task setup for initial infrastructure construction
- Implement flock induction workflow (add initial animals)
- Create crop planting initialization for first season
- Add soil testing and plot preparation tasks
- Generate initial farm setup checklist
- **Commit:** `git add -A && git commit -m "Task 8: Create farm setup and initialization module"`

### Task 9: Build Farm Mapping with ARCore
- Integrate ARCore for augmented reality farm mapping
- Create AR boundary marking with GPS coordinates
- Implement 3D visualization of planned buildings
- Build plot boundary overlay in AR view
- Add infrastructure placement preview in AR
- Create measurement tools for accurate plot sizing
- **Commit:** `git add -A && git commit -m "Task 9: Integrate ARCore for farm mapping"`

---

## Phase 4: Livestock Management Module

### Task 10: Create Animal Profile System
- Build Animal entity and DAO
- Create animal profile screen (add/edit/view)
- Implement photo capture and storage
- Build herd overview dashboard with filters
- Add tag assignment for newborns (untagged entry support)
- **Commit:** `git add -A && git commit -m "Task 10: Build animal profile system with herd overview"`

### Task 11: Implement Health Records
- Create HealthRecord entity (vaccinations, deworming, treatments)
- Build health record entry forms
- Implement automated health alerts system
- Create vaccination schedule view
- Handle dead animal records (auto-close and archive)
- **Commit:** `git add -A && git commit -m "Task 11: Implement health records with automated alerts"`

### Task 12: Build Reproduction Tracking
- Create ReproductionRecord entity
- Implement heat detection logging
- Build mating records (sire + dam pairing)
- Create pregnancy tracking with expected dates
- Add birth record entry (multiple kids support)
- **Commit:** `git add -A && git commit -m "Task 12: Build reproduction and birth tracking system"`

### Task 13: Create Milk Production Logging
- Build MilkProduction entity
- Create daily AM/PM yield entry forms
- Implement lactation curve chart
- Build per-doe production history
- Handle transitional period (pregnant + milking doe)
- **Commit:** `git add -A && git commit -m "Task 13: Create milk production logging with charts"`

### Task 14: Implement Growth Tracking & Analytics
- Create WeightEntry entity
- Build weight tracking with growth charts
- Calculate feed conversion ratio
- Implement culling recommendation engine
- **Commit:** `git add -A && git commit -m "Task 14: Implement growth tracking and culling recommendations"`

---

## Phase 5: Crop & Pasture Management Module

### Task 15: Build Field/Plot Registry
- Create Plot entity with GPS coordinates
- Build 16-acre map view with plot boundaries
- Implement plot editing (name, size, soil type, current use)
- Add current crop assignment
- **Commit:** `git add -A && git commit -m "Task 15: Build field registry with map view"`

### Task 16: Create Crop Planting Records
- Build CropPlanting entity
- Create planting record entry forms
- Implement growth stage tracking with reminders
- Build input application log (fertilizer, pesticide, herbicide)
- **Commit:** `git add -A && git commit -m "Task 16: Create crop planting and growth stage tracking"`

### Task 17: Implement Harvest & Silage Module
- Create HarvestRecord entity
- Build harvest entry with quality grading
- Implement silage pit inventory tracking
- Create silage fermentation monitoring
- Handle multi-day harvest splits
- Support intercropped plots
- **Commit:** `git add -A && git commit -m "Task 17: Implement harvest and silage management"`

### Task 18: Build Weather & Analytics
- Create WeatherLog entity for manual entry
- Integrate open weather API (Korogwe coordinates)
- Build yield history charts
- Implement season comparison analytics
- Create crop rotation planner
- **Commit:** `git add -A && git commit -m "Task 18: Add weather logging and yield analytics"`

---

## Phase 6: Cheese & Value Addition Module

### Task 19: Create Milk Collection System
- Build MilkCollection entity
- Create collection log with quality checks
- Link to contributing does
- Implement rejection logging (sour/contaminated milk)
- **Commit:** `git add -A && git commit -m "Task 19: Build milk collection logging system"`

### Task 20: Build Cheese Production Tracking
- Create CheeseBatch entity
- Build batch record entry (milk volume, type, culture, rennet)
- Implement aging tracking with location
- Create spoilage/loss logging
- **Commit:** `git add -A && git commit -m "Task 20: Implement cheese batch production tracking"`

### Task 21: Create Cheese Inventory & Sales
- Build packaging log functionality
- Implement cost per batch calculator
- Create inventory dashboard (on hand, aging, reserved)
- Build sales log with payment tracking
- Generate revenue vs. cost margin reports
- **Commit:** `git add -A && git commit -m "Task 21: Build cheese inventory and sales tracking"`

---

## Phase 7: Feed & Store Management Module

### Task 22: Build Feed Inventory System
- Create FeedInventory entity
- Build stock level tracking for all feed types
- Implement daily feed allocation logging
- Create feed requirement calculator
- Add silage pit inventory with draw-down tracking
- Implement fermentation warnings
- **Commit:** `git add -A && git commit -m "Task 22: Build feed inventory and allocation system"`

### Task 23: Create Store Management
- Build StoreItem entity
- Implement inventory tracking (seeds, fertilizers, chemicals, medicine)
- Add expiry date monitoring
- Create procurement logging
- Implement reorder level alerts
- **Commit:** `git add -A && git commit -m "Task 23: Create store inventory and procurement system"`

---

## Phase 8: Financial Management Module

### Task 24: Build Income & Expense Tracking
- Create Income and Expense entities
- Build categorized entry forms
- Implement petty cash log
- Create transaction history view
- **Commit:** `git add -A && git commit -m "Task 24: Build income and expense tracking"`

### Task 25: Implement P&L Reports
- Create profit & loss calculation engine
- Build weekly/monthly/seasonal/annual reports
- Implement enterprise-level P&L (goats, sheep, crops, cheese)
- Create budget vs. actual comparison
- Add PDF and CSV export functionality
- **Commit:** `git add -A && git commit -m "Task 25: Implement P&L reports with export"`

### Task 26: Build Credit & Loan Tracking
- Create Loan entity
- Implement loan repayment schedule
- Build outstanding payment tracker (animals sold on credit)
- Add worker advance payment tracking
- **Commit:** `git add -A && git commit -m "Task 26: Build credit and loan tracking system"`

---

## Phase 9: Labour Management Module

### Task 27: Create Worker Management
- Build Worker entity
- Create worker profile forms
- Support seasonal worker date-bounded employment
- Build worker list with role filters
- **Commit:** `git add -A && git commit -m "Task 27: Create worker profile management"`

### Task 28: Implement Attendance & Tasks
- Build AttendanceRecord entity
- Create daily attendance logging
- Implement task assignment system
- Build daily work journal
- Add task status tracking
- **Commit:** `git add -A && git commit -m "Task 28: Implement attendance and task management"`

### Task 29: Build Payroll System
- Create payroll calculation engine
- Build days worked × rate computation
- Implement deduction tracking
- Add advance payment integration
- Generate payroll summaries
- **Commit:** `git add -A && git commit -m "Task 29: Build payroll calculation system"`

---

## Phase 10: Calendar & Task Planner

### Task 30: Create Farm Calendar
- Build calendar view with all farm events
- Integrate planting dates, harvest windows, vaccination schedules
- Add cheese aging milestones and market days
- Include loan repayment dates
- **Commit:** `git add -A && git commit -m "Task 30: Create unified farm calendar"`

### Task 31: Implement Task Automation
- Build recurring task templates (dipping, deworming, soil testing)
- Create task generation from schedules
- Implement push notification system
- Build season planner with crop rotation
- **Commit:** `git add -A && git commit -m "Task 31: Implement recurring tasks and notifications"`

---

## Phase 11: Dashboard & Analytics

### Task 32: Build Advanced Charts
- Implement milk trend charts (30/90 days)
- Create revenue vs. expenses monthly chart
- Build herd growth curve visualization
- Add crop yield by season charts
- **Commit:** `git add -A && git commit -m "Task 32: Build advanced analytics charts"`

### Task 33: Create Alerts Engine
- Implement alert rules (animal not weighed, vaccination overdue, low feed, etc.)
- Build alert priority system
- Create alert notification delivery
- Add alert dismissal and tracking
- **Commit:** `git add -A && git commit -m "Task 33: Create comprehensive alerts engine"`

---

## Phase 12: Land & Infrastructure

### Task 34: Build Farm Map Module
- Create farm map with plot boundaries
- Add animal shelters, water points, storage locations
- Implement cheese room and compost pit markers
- Build infrastructure condition logging
- **Commit:** `git add -A && git commit -m "Task 34: Build farm map and infrastructure tracking"`

### Task 35: Implement Maintenance Scheduling
- Create maintenance task system with vehicle tracking
- Build dipping tank cleaning schedule
- Implement equipment servicing reminders
- Add infrastructure repair tracking
- Create Vehicle entity for fleet management
- Build VehiclePart entity for parts inventory
- Track mileage, hours run, and service intervals
- Implement parts reorder alerts
- **Commit:** `git add -A && git commit -m "Task 35: Implement maintenance scheduling with vehicle tracking"`

---

## Phase 13: Advanced Profit Optimization Features

### Task 36: Build Least-Cost Ration (LCR) Solver ✅
- Implement Pearson Square algorithm in Kotlin
- Create linear programming solver for feed optimization
- Build UI for entering supplement prices
- Calculate DM and CP requirements
- Display optimized feed mix recommendations
- **Commit:** `git add -A && git commit -m "Task 36: Build Least-Cost Ration solver"`
- **Status:** ✅ Completed

### Task 37: Implement Computer Vision Colorimeter ✅
- Integrate OpenCV for Android
- Build HSV color space analysis
- Create maturity grading for crops and cheese
- Implement "Harvest Window Alerts"
- Generate QR invoices with maturity metadata
- **Commit:** `git add -A && git commit -m "Task 37: Implement HSV colorimetric grading"`
- **Status:** ✅ Completed

### Task 38: Build Evapotranspiration Water Optimizer ✅
- Implement Penman-Monteith (FAO-56) formula
- Create daily ET₀ calculation
- Build irrigation decision logic
- Implement "Pumping Block" feature
- Add soil moisture threshold alerts
- **Commit:** `git add -A && git commit -m "Task 38: Build water optimization module"`
- **Status:** ✅ Completed

### Task 39: Create Digital Twin Plot Benchmarking ✅
- Build plot performance tracking
- Implement ROI calculation per plot
- Create "Clone the Champion" feature
- Generate success SOP from best-performing plots
- Build $/m² profit heatmap overlay
- **Commit:** `git add -A && git commit -m "Task 39: Build digital twin and profit heatmap"`
- **Status:** ✅ Completed

### Task 40: Implement Acoustic Guard ✅
- Integrate Qualcomm Sensing Hub
- Build pest detection audio classifier
- Create animal distress sound detection
- Implement push notifications for detections
- **Commit:** `git add -A && git commit -m "Task 40: Implement acoustic pest and distress detection"`
- **Status:** ✅ Completed

### Task 41: Configure NPU Delegation ✅
- Set up QNN delegate for Hexagon NPU
- Configure INT4 quantization for ML models
- Implement volatile RAM processing for vision
- Optimize for 144Hz UI stability
- **Commit:** `git add -A && git commit -m "Task 41: Configure NPU delegation and quantization"`
- **Status:** ✅ Completed

---

## Phase 14: Localization & Final Polish

### Task 42: Implement Swahili Localization
- Create string resources for all UI text
- Build language toggle functionality
- Test Swahili translations
- Ensure proper text rendering
- **Commit:** `git add -A && git commit -m "Task 42: Implement Swahili localization"`

### Task 43: Final Integration Testing
- Test all modules end-to-end
- Verify offline/online sync
- Test data export (PDF/CSV)
- Validate backup and restore
- Perform user acceptance testing
- **Commit:** `git add -A && git commit -m "Task 43: Final integration testing and polish"`

---

## Summary
- **Total Tasks:** 43
- **Estimated Duration:** 14-18 weeks
- **Key Milestones:**
  - Phase 1-2: Foundation (Week 1-2)
  - Phase 3: Farm Setup (Week 2-3)
  - Phase 4-5: Core Modules (Week 3-6)
  - Phase 6-9: Business Modules (Week 7-12)
  - Phase 10-12: Advanced Features (Week 13-15)
  - Phase 13-14: Optimization & Polish (Week 16-18)