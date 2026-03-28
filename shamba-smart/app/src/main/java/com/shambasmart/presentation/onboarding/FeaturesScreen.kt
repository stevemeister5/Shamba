package com.shambasmart.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shambasmart.presentation.common.theme.*

data class Feature(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun FeaturesScreen() {
    val features = listOf(
        Feature(
            icon = Icons.Default.Camera,
            title = "Vision AI",
            description = "Grade cheese quality with computer vision",
            color = Teal400
        ),
        Feature(
            icon = Icons.Default.Mic,
            title = "Audio Alerts",
            description = "Detect stress in livestock through sound analysis",
            color = Amber400
        ),
        Feature(
            icon = Icons.Default.Water,
            title = "Water Optimizer",
            description = "Smart irrigation scheduling",
            color = Color(0xFF42A5F5)
        ),
        Feature(
            icon = Icons.Default.Map,
            title = "GPS Mapping",
            description = "Track plot boundaries and yields",
            color = Green500
        ),
        Feature(
            icon = Icons.Default.Inventory,
            title = "Inventory",
            description = "Manage feed, cheese, and supplies",
            color = Earth500
        ),
        Feature(
            icon = Icons.Default.CalendarMonth,
            title = "Calendar",
            description = "Schedule tasks and events",
            color = Red400
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Powerful Features",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = FontSize.display,
                fontWeight = FontWeight.Bold
            ),
            color = Neutral950
        )

        Spacer(modifier = Modifier.height(Space.space2))

        Text(
            text = "Everything you need to manage your farm",
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral800
        )

        Spacer(modifier = Modifier.height(Space.space8))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Space.space4),
            contentPadding = PaddingValues(horizontal = Space.space4)
        ) {
            items(features) { feature ->
                FeatureCard(feature = feature)
            }
        }

        Spacer(modifier = Modifier.height(Space.space8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "8+", label = "Modules")
            StatItem(value = "AI", label = "Powered")
            StatItem(value = "24/7", label = "Monitoring")
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceRaised
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.xl)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.space5),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    modifier = Modifier.size(32.dp),
                    tint = feature.color
                )
            }

            Spacer(modifier = Modifier.height(Space.space3))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Neutral950
            )

            Spacer(modifier = Modifier.height(Space.space1))

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Green500
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600
        )
    }
}