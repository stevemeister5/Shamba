package com.shambasmart.presentation.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shambasmart.R

// Custom font families (using system fonts as fallback)
// Note: For production, add Geist and Geist Mono font files to res/font/
val GeistFamily = FontFamily.Default // Fallback to system default
val GeistMonoFamily = FontFamily.Monospace // Fallback to monospace

// Display typeface for hero headings (using serif as fallback)
val DisplayFamily = FontFamily.Serif

// Organic Dark Precision Typography System
val Typography = Typography(
    // Hero: Farm name in welcome screen only
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Light,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.03).sp,
        color = Neutral950
    ),
    // Dashboard section heroes, large KPI numbers
    displayMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).sp,
        color = Neutral950
    ),
    // Large KPI values
    displaySmall = TextStyle(
        fontFamily = GeistMonoFamily,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).sp,
        color = Neutral950
    ),
    // Page titles, module headers
    headlineLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp,
        color = Neutral950
    ),
    // Card headers, section labels
    headlineMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.005).sp,
        color = Neutral950
    ),
    // Section headers
    headlineSmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = Neutral950
    ),
    // Page titles
    titleLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp,
        color = Neutral950
    ),
    // Form labels, table headers - uppercase
    titleMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.02.sp,
        color = Neutral600
    ),
    // Small labels
    titleSmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = Neutral800
    ),
    // Primary reading text, descriptions
    bodyLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = Neutral950
    ),
    // Secondary body text
    bodyMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = Neutral800
    ),
    // Timestamps, source citations, helper text
    bodySmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.01.sp,
        color = Neutral600
    ),
    // Form labels, table headers - uppercase
    labelLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.02.sp,
        color = Neutral600
    ),
    // Status chips, badges, nav labels - uppercase
    labelMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.04.sp,
        color = Neutral600
    ),
    // Micro labels
    labelSmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.04.sp,
        color = Neutral600
    )
)

// KPI value typography (for large numbers)
val KPITypography = TextStyle(
    fontFamily = GeistMonoFamily,
    fontWeight = FontWeight.Light,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = (-0.02).sp,
    color = Neutral950
)

// Numeric data typography
val NumericTypography = TextStyle(
    fontFamily = GeistMonoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    color = Neutral800
)

// Status chip typography
val StatusTypography = TextStyle(
    fontFamily = GeistFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.04.sp
)

// Navigation label typography
val NavLabelTypography = TextStyle(
    fontFamily = GeistFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.04.sp,
    color = Neutral600
)