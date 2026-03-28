package com.shambasmart.presentation.common.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Organic Dark Precision Motion System
 * All animations tuned for 144Hz displays (6.94ms per frame)
 * Motion should feel physical and immediate, not sluggish or over-eased
 */

// Micro-interactions (hover, press) - 60-80ms
object MicroAnimations {
    val hover = tween<Float>(
        durationMillis = 80,
        easing = FastOutSlowInEasing
    )
    
    val press = tween<Float>(
        durationMillis = 60,
        easing = LinearEasing
    )
    
    val backgroundColor = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 80,
        easing = FastOutSlowInEasing
    )
    
    val borderColor = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 80,
        easing = FastOutSlowInEasing
    )
    
    val scale = tween<Float>(
        durationMillis = 60,
        easing = FastOutSlowInEasing
    )
}

// State transitions (tab switch, filter change) - 120-150ms
object StateTransitions {
    val tabSwitch = tween<Float>(
        durationMillis = 150,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    )
    
    val filterChange = tween<Float>(
        durationMillis = 120,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    )
    
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = 150,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        )
    )
    
    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = 120,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        )
    )
    
    val slideIn = slideInVertically(
        animationSpec = tween(
            durationMillis = 150,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        ),
        initialOffsetY = { it / 10 }
    )
    
    val slideOut = slideOutVertically(
        animationSpec = tween(
            durationMillis = 120,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        ),
        targetOffsetY = { -it / 10 }
    )
}

// Panel slide (context panel collapse/expand) - 200ms
object PanelAnimations {
    val slide = tween<Dp>(
        durationMillis = 200,
        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    )
    
    val contentFade = tween<Float>(
        durationMillis = 100,
        easing = LinearEasing
    )
    
    val expand = expandVertically(
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        )
    )
    
    val collapse = shrinkVertically(
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        )
    )
}

// Page/module transitions - 180ms
object PageTransitions {
    val duration = 180
    val easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    
    val enterTransition = slideInHorizontally(
        animationSpec = tween(durationMillis = duration, easing = easing),
        initialOffsetX = { it / 10 }
    ) + fadeIn(
        animationSpec = tween(durationMillis = duration, easing = easing)
    )
    
    val exitTransition = slideOutHorizontally(
        animationSpec = tween(durationMillis = duration, easing = easing),
        targetOffsetX = { -it / 10 }
    ) + fadeOut(
        animationSpec = tween(durationMillis = duration, easing = easing)
    )
    
    val popEnterTransition = slideInHorizontally(
        animationSpec = tween(durationMillis = duration, easing = easing),
        initialOffsetX = { -it / 10 }
    ) + fadeIn(
        animationSpec = tween(durationMillis = duration, easing = easing)
    )
    
    val popExitTransition = slideOutHorizontally(
        animationSpec = tween(durationMillis = duration, easing = easing),
        targetOffsetX = { it / 10 }
    ) + fadeOut(
        animationSpec = tween(durationMillis = duration, easing = easing)
    )
    
    // Stagger child elements: 20ms delay per element, max 5 elements
    fun staggerDelay(index: Int): Int = (index * 20).coerceAtMost(100)
}

// Data loading (skeleton screens) - 1200ms shimmer
object LoadingAnimations {
    val shimmer = infiniteRepeatable(
        animation = tween<Float>(
            durationMillis = 1200,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
    )
    
    val shimmerSpec = ShimmerSpec(
        durationMillis = 1200,
        easing = LinearEasing
    )
}

// Number counters (KPI values on load) - 600ms
object CounterAnimations {
    val countUp = tween<Int>(
        durationMillis = 600,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    )
    
    val countUpFloat = tween<Float>(
        durationMillis = 600,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    )
}

// Progress bar fills - 500ms
object ProgressAnimations {
    val fill = tween<Float>(
        durationMillis = 500,
        easing = CubicBezierEasing(0.2f, 0f, 0.4f, 1f)
    )
    
    val width = tween<Dp>(
        durationMillis = 500,
        easing = CubicBezierEasing(0.2f, 0f, 0.4f, 1f)
    )
}

// Status dot animations (farm health indicator)
object StatusAnimations {
    // Healthy: slow pulse, 3000ms
    val healthy = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 3000
            1f at 0
            1.3f at 1500
            1f at 3000
        },
        repeatMode = RepeatMode.Restart
    )
    
    val healthyOpacity = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 3000
            1f at 0
            0.6f at 1500
            1f at 3000
        },
        repeatMode = RepeatMode.Restart
    )
    
    // Alert: medium pulse, 1500ms
    val alert = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 1500
            1f at 0
            1.2f at 750
            1f at 1500
        },
        repeatMode = RepeatMode.Restart
    )
    
    // Critical: rapid pulse, 800ms
    val critical = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 800
            1f at 0
            1.3f at 400
            1f at 800
        },
        repeatMode = RepeatMode.Restart
    )
}

// Checkbox animation
object CheckboxAnimations {
    val check = tween<Float>(
        durationMillis = 80,
        easing = FastOutSlowInEasing
    )
    
    val scale = tween<Float>(
        durationMillis = 80,
        easing = FastOutSlowInEasing
    )
}

// Card hover elevation
object CardAnimations {
    val elevation = tween<Dp>(
        durationMillis = 100,
        easing = FastOutSlowInEasing
    )
    
    val border = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 100,
        easing = FastOutSlowInEasing
    )
}

// Shimmer specification data class
data class ShimmerSpec(
    val durationMillis: Int,
    val easing: Easing
)

// Extension functions for common animations
@Composable
fun animateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = MicroAnimations.hover
) = androidx.compose.animation.core.animateFloatAsState(
    targetValue = targetValue,
    animationSpec = animationSpec,
    label = "floatAnimation"
)

@Composable
fun animateDpAsState(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = PanelAnimations.slide
) = androidx.compose.animation.core.animateDpAsState(
    targetValue = targetValue,
    animationSpec = animationSpec,
    label = "dpAnimation"
)

// Page transition composable
@Composable
fun PageTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = PageTransitions.enterTransition,
        exit = PageTransitions.exitTransition,
        content = content
    )
}

// Staggered animation helper
@Composable
fun <T> StaggeredAnimation(
    items: List<T>,
    delayProvider: (Int) -> Int = PageTransitions::staggerDelay,
    content: @Composable (T, Int) -> Unit
) {
    items.forEachIndexed { index, item ->
        val delay = delayProvider(index)
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(delay.toLong())
            visible = true
        }
        
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                )
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                ),
                initialOffsetY = { it / 10 }
            )
        ) {
            content(item, index)
        }
    }
}