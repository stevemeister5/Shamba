package com.shambasmart.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shambasmart.presentation.common.theme.*

@Composable
fun LaunchChoiceScreen(
    onSetupFarm: () -> Unit,
    onExploreDemo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceBase
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Space.space8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Shamba Smart Logo",
                modifier = Modifier.size(80.dp),
                tint = Green500
            )

            Spacer(modifier = Modifier.height(Space.space6))

            // Header
            Text(
                text = "How would you like to get started?",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = FontSize.title,
                    lineHeight = LineHeight.title,
                    letterSpacing = LetterSpacing.title,
                    fontWeight = FontWeight.Bold
                ),
                color = Neutral950,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Space.space12))

            // Two cards side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.space6)
            ) {
                // Card 1: Set Up My Farm
                LaunchCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Agriculture,
                    title = "Set up my farm",
                    description = "Create your farm profile and start tracking your livestock, crops, and finances.",
                    buttonText = "Get started →",
                    onClick = onSetupFarm,
                    isPrimary = true
                )

                // Card 2: Explore Demo Farm
                LaunchCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PlayCircle,
                    title = "Explore with demo data",
                    description = "Tour a fully set-up farm — all modules, real data, every feature unlocked. No account needed.",
                    badge = "No setup required",
                    buttonText = "Launch demo →",
                    onClick = onExploreDemo,
                    isPrimary = false
                )
            }
        }
    }
}

@Composable
private fun LaunchCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    buttonText: String,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Green900 else SurfaceRaised
        ),
        shape = RoundedCornerShape(Radius.xl),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.space6),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge (only for demo card)
            if (badge != null) {
                Surface(
                    modifier = Modifier.padding(bottom = Space.space4),
                    color = Amber400,
                    shape = RoundedCornerShape(Radius.sm)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(
                            horizontal = Space.space3,
                            vertical = Space.space1
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF412402)
                    )
                }
            }

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(IconSize.kpiAccent),
                tint = if (isPrimary) Green400 else Green500
            )

            Spacer(modifier = Modifier.height(Space.space4))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isPrimary) Green100 else Neutral950,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Space.space3))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPrimary) Green200 else Neutral600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Space.space6))

            // Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPrimary) Green500 else SurfaceElevated,
                    contentColor = if (isPrimary) Green50 else Neutral950
                ),
                shape = RoundedCornerShape(Radius.md)
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}