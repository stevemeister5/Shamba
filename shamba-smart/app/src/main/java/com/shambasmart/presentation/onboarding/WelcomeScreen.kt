package com.shambasmart.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shambasmart.presentation.common.theme.*

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = "Shamba Smart Logo",
            modifier = Modifier.size(120.dp),
            tint = Green500
        )

        Spacer(modifier = Modifier.height(Space.space8))

        Text(
            text = "Welcome to\nShamba Smart",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = FontSize.hero,
                lineHeight = LineHeight.hero,
                letterSpacing = LetterSpacing.hero,
                fontWeight = FontWeight.Bold
            ),
            color = Neutral950,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Space.space4))

        Text(
            text = "Your intelligent farming companion",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = FontSize.body
            ),
            color = Neutral800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Space.space12))

        FeatureHighlight(
            icon = Icons.Default.Pets,
            title = "Livestock Management",
            description = "Track health, breeding, and production"
        )

        Spacer(modifier = Modifier.height(Space.space4))

        FeatureHighlight(
            icon = Icons.Default.Grass,
            title = "Crop Planning",
            description = "Optimize planting and harvesting cycles"
        )

        Spacer(modifier = Modifier.height(Space.space4))

        FeatureHighlight(
            icon = Icons.Default.Analytics,
            title = "Smart Analytics",
            description = "AI-powered insights for your farm"
        )
    }
}

@Composable
private fun FeatureHighlight(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceRaised
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.lg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.space4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(IconSize.kpiAccent),
                tint = Green500
            )
            Spacer(modifier = Modifier.width(Space.space4))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Neutral950
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }
        }
    }
}