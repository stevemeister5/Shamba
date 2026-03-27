package com.shambasmart.presentation.common.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shamba Smart Design Tokens
 * Organic Dark Precision Design System
 * 
 * Complete design token system for consistent implementation across all screens.
 * Optimized for Xiaomi Pad 7: 2560×1600, 11.2", 144Hz, LCD, landscape primary.
 */

// ============================================================================
// SPATIAL SYSTEM (4px base unit)
// ============================================================================

object Space {
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val space10 = 40.dp
    val space12 = 48.dp
    val space16 = 64.dp
}

// ============================================================================
// BORDER RADIUS SCALE
// ============================================================================

object Radius {
    val sm = 6.dp    // chips, badges, small buttons
    val md = 10.dp   // inputs, small cards
    val lg = 14.dp   // cards, panels
    val xl = 20.dp   // modals, large containers
    val full = 9999.dp // pills, avatars, toggles
}

// ============================================================================
// BORDER WIDTHS
// ============================================================================

object BorderWidth {
    val hairline = 0.5.dp  // subtle dividers
    val thin = 1.dp        // card borders
    val medium = 1.5.dp    // focused inputs, selected states
    val thick = 2.dp       // active nav items, featured cards
}

// ============================================================================
// ELEVATION LEVELS (border-based, no shadows on dark)
// ============================================================================

object Elevation {
    // Level 0 — Sunken (input fields, inset areas)
    object Sunken {
        val background = SurfaceSunken
        val border = BorderWidth.thin
        val borderColor = Neutral100
    }
    
    // Level 1 — Base (page background)
    object Base {
        val background = SurfaceBase
    }
    
    // Level 2 — Raised (standard cards)
    object Raised {
        val background = SurfaceRaised
        val border = BorderWidth.thin
        val borderColor = Neutral200
    }
    
    // Level 3 — Elevated (hover state, selected card)
    object Elevated {
        val background = SurfaceElevated
        val border = BorderWidth.thin
        val borderColor = Neutral300
    }
    
    // Level 4 — Overlay (modals, dropdowns, tooltips)
    object Overlay {
        val background = SurfaceOverlay
        val border = BorderWidth.thin
        val borderColor = Neutral300
    }
    
    // Accent elevation — featured cards, active states
    object Accent {
        val border = BorderWidth.medium
        val borderColor = Green700
        val leftEdge = 3.dp
        val leftEdgeColor = Green500
    }
}

// ============================================================================
// COMPONENT DIMENSIONS
// ============================================================================

object Dimensions {
    // Top bar
    val topBarHeight = 56.dp
    
    // Navigation rail
    val navRailWidth = 72.dp
    val navRailIconSize = 22.dp
    
    // Context panel
    val contextPanelWidth = 320.dp
    
    // KPI cards
    val kpiMinHeight = 96.dp
    val kpiValueHeight = 48.dp
    
    // Buttons
    val buttonHeight = 40.dp
    val buttonHeightSmall = 36.dp
    val iconButtonSize = 36.dp
    
    // Input fields
    val inputHeight = 42.dp
    
    // Status chips
    val chipHeight = 22.dp
    
    // Farm health indicator
    val farmHealthSize = 32.dp
    
    // Maarifa floating tab
    val maarifaTabHeight = 140.dp
    val maarifaTabWidth = 32.dp
    
    // Table rows
    val tableHeaderHeight = 36.dp
    val tableRowHeight = 48.dp
    
    // Progress bars
    val progressBarHeight = 6.dp
    val progressBarHeightLarge = 8.dp
    
    // Charts
    val miniChartHeight = 40.dp
    val miniChartBarMinWidth = 24.dp
}

// ============================================================================
// GRID SYSTEM (12-column with 20px gutters)
// ============================================================================

object Grid {
    val columns = 12
    val gutter = 20.dp
    val contentPadding = 24.dp
    
    // Column spans
    val col1 = 1
    val col2 = 2
    val col3 = 3
    val col4 = 4
    val col5 = 5
    val col6 = 6
    val col7 = 7
    val col8 = 8
    val col9 = 9
    val col10 = 10
    val col11 = 11
    val col12 = 12
}

// ============================================================================
// TYPOGRAPHY SCALE
// ============================================================================

object FontSize {
    val hero = 52.sp
    val display = 32.sp
    val title = 22.sp
    val heading = 17.sp
    val body = 15.sp
    val label = 13.sp
    val caption = 12.sp
    val micro = 10.sp
}

object LineHeight {
    val hero = 56.sp
    val display = 38.sp
    val title = 28.sp
    val heading = 24.sp
    val body = 22.sp
    val label = 18.sp
    val caption = 16.sp
    val micro = 14.sp
}

object LetterSpacing {
    val hero = (-0.03).sp
    val display = (-0.02).sp
    val title = (-0.01).sp
    val heading = (-0.005).sp
    val body = 0.sp
    val label = 0.02.sp
    val caption = 0.01.sp
    val micro = 0.04.sp
}

object FontWeight {
    const val Light = 300
    const val Regular = 400
    const val Medium = 500
    const val SemiBold = 600
    const val Bold = 700
}

// ============================================================================
// ICON SIZES
// ============================================================================

object IconSize {
    val navRail = 22.dp
    val cardHeader = 16.dp
    val inline = 14.dp
    val kpiAccent = 18.dp
    val button = 16.dp
    val status = 12.dp
}

// ============================================================================
// STATUS COLORS
// ============================================================================

object StatusColors {
    // Animal status
    val healthy = Green400
    val healthyBackground = Green400.copy(alpha = 0.15f)
    val healthyBorder = Green700
    val healthyText = Green300
    
    val pregnant = Amber400
    val pregnantBackground = Amber400.copy(alpha = 0.15f)
    val pregnantBorder = Amber600
    val pregnantText = Amber300
    
    val sick = Red400
    val sickBackground = Red400.copy(alpha = 0.15f)
    val sickBorder = Red500
    val sickText = Red300
    
    val dry = Teal400
    val dryBackground = Teal400.copy(alpha = 0.12f)
    val dryBorder = Teal600
    val dryText = Teal300
    
    val pending = Neutral400
    val pendingBackground = Neutral400.copy(alpha = 0.12f)
    val pendingBorder = Neutral400
    val pendingText = Neutral600
    
    // Farm health
    val farmHealthy = Green400
    val farmHealthyBackground = Green900
    
    val farmAlert = Amber300
    val farmAlertBackground = Earth800
    
    val farmCritical = Red300
    val farmCriticalBackground = Red600
}

// ============================================================================
// CROP COLORS
// ============================================================================

object CropColors {
    val grass = Green500
    val grain = Amber400
    val vegetables = Teal400
    val default = Neutral400
}

// ============================================================================
// ALERT COLORS
// ============================================================================

object AlertColors {
    // Critical
    val criticalBackground = Red400.copy(alpha = 0.05f)
    val criticalBorder = Red400.copy(alpha = 0.25f)
    val criticalIcon = Red300
    
    // Warning
    val warningBackground = Amber400.copy(alpha = 0.04f)
    val warningBorder = Amber400.copy(alpha = 0.2f)
    val warningIcon = Amber300
    
    // Info
    val infoBackground = Color(0xFF42A5F5).copy(alpha = 0.04f)
    val infoBorder = Color(0xFF42A5F5).copy(alpha = 0.2f)
    val infoIcon = Color(0xFF42A5F5)
}

// ============================================================================
// ANIMATION TIMING
// ============================================================================

object Timing {
    // Micro-interactions
    val hover = 80
    val press = 60
    
    // State transitions
    val tabSwitch = 150
    val filterChange = 120
    
    // Panel animations
    val panelSlide = 200
    val contentFade = 100
    
    // Page transitions
    val pageTransition = 180
    val staggerDelay = 20
    val maxStagger = 100
    
    // Loading
    val shimmer = 1200
    
    // Counters
    val countUp = 600
    
    // Progress
    val progressFill = 500
    
    // Status dots
    val healthyPulse = 3000
    val alertPulse = 1500
    val criticalPulse = 800
}

// ============================================================================
// BREAKPOINTS
// ============================================================================

object Breakpoints {
    val compact = 600.dp
    val medium = 840.dp
    val expanded = 1200.dp
}

// ============================================================================
// ACCESSIBILITY
// ============================================================================

object Accessibility {
    val minTouchTarget = 44.dp
    val focusRingWidth = 3.dp
    val focusRingColor = Green500
}

// ============================================================================
// CONTENT LIMITS
// ============================================================================

object ContentLimits {
    val maxLineLength = 75
    val maxLabelLength = 20
    val maxDescriptionLines = 3
}

// ============================================================================
// FARM MAP
// ============================================================================

object FarmMap {
    val height = 340.dp
    val plotGridColumns = 4
    val plotCardHeight = 200.dp
}

// ============================================================================
// WEATHER STRIP
// ============================================================================

object Weather {
    val dayCardWidth = 48.dp
    val iconSize = 18.dp
    val tempFontSize = 12.sp
}

// ============================================================================
// MILK PRODUCTION
// ============================================================================

object MilkProduction {
    val chartHeight = 280.dp
    val doeBarLabelWidth = 56.dp
    val doeBarValueWidth = 44.dp
}

// ============================================================================
// CHEESE INVENTORY
// ============================================================================

object CheeseInventory {
    val gaugeSize = 120.dp
    val gaugeThickness = 8.dp
}