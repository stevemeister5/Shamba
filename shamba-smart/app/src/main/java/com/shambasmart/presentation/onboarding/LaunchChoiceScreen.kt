package com.shambasmart.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shambasmart.R
import com.shambasmart.presentation.common.theme.*

/**
 * Launch Choice Screen — shown after onboarding to let user choose
 * between setting up their own farm or exploring the demo.
 */
@Composable
fun LaunchChoiceScreen(
    onSetupFarm: () -> Unit,
    onLaunchDemo: () -> Unit,
    demoModeManager: com.shambasmart.demo.DemoModeManager? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Icon(
                imageVector = Icons.Filled.Grass,
                contentDescription = "Shamba Smart",
                modifier = Modifier.size(72.dp),
                tint = Green500
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "How would you like to get started?",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Two cards side by side (landscape tablet layout)
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Card 1: Set Up My Farm
                LaunchCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Agriculture,
                    badge = null,
                    heading = "Set up my farm",
                    body = "Create your farm profile and start tracking your livestock, crops, and finances.",
                    buttonText = "Get started →",
                    buttonColors = ButtonDefaults.buttonColors(
                        containerColor = Green500,
                        contentColor = Green50
                    ),
                    onClick = onSetupFarm
                )
                
                Spacer(modifier = Modifier.width(32.dp))
                
                // Card 2: Explore Demo Farm
                LaunchCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.PlayArrow,
                    badge = "No setup required",
                    heading = "Explore with demo data",
                    body = "Tour a fully set-up farm — all modules, real data, every feature unlocked. No account needed.",
                    buttonText = "Launch demo →",
                    buttonColors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = Amber100
                    ),
                    onClick = onLaunchDemo
                )
            }
        }
    }
}

@Composable
private fun LaunchCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String?,
    heading: String,
    body: String,
    buttonText: String,
    buttonColors: ButtonColors,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(380.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Badge if present
                if (badge != null) {
                    Surface(
                        color = Amber500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber400,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceOverlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Green400
                    )
                }
                
                // Heading
                Text(
                    text = heading,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Body
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
            
            // Button
            Button(
                onClick = onClick,
                colors = buttonColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}