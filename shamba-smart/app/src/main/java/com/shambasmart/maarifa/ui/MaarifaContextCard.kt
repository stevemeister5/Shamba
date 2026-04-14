package com.shambasmart.maarifa.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shambasmart.presentation.common.theme.*

enum class ConfidenceTier(val label: String) {
    RULE_GOVERNED("Rule-governed"),
    MULTI_SOURCE("Based on multiple sources"),
    LIMITED_SOURCES("Limited sources — verify"),
    NOT_FOUND("Not in knowledge base")
}

@Composable
fun MaarifaContextCard(
    title: String,
    content: String,
    confidenceTier: ConfidenceTier,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Eco,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    isExpanded: Boolean = true,
    onToggleExpand: (() -> Unit)? = null
) {
    val borderColor = when (confidenceTier) {
        ConfidenceTier.RULE_GOVERNED -> Green800
        ConfidenceTier.MULTI_SOURCE -> Teal600
        ConfidenceTier.LIMITED_SOURCES -> Amber600
        ConfidenceTier.NOT_FOUND -> Neutral600
    }
    
    val confidenceColor = when (confidenceTier) {
        ConfidenceTier.RULE_GOVERNED -> Green400
        ConfidenceTier.MULTI_SOURCE -> Teal400
        ConfidenceTier.LIMITED_SOURCES -> Amber400
        ConfidenceTier.NOT_FOUND -> Neutral600
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Green400
                    )
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Green400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (onToggleExpand != null) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.size(16.dp),
                            tint = Neutral600
                        )
                    }
                }
            }
            
            // Content
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral950,
                        lineHeight = 22.sp
                    )
                    
                    // Confidence indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(3.dp)
                                    .background(confidenceColor, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = confidenceTier.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral600
                            )
                        }
                        
                        if (actionLabel != null && onAction != null) {
                            TextButton(
                                onClick = onAction,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Green400
                                )
                            ) {
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaarifaQuickTip(
    tip: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Green800.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Green400
            )
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral800,
                modifier = Modifier.weight(1f)
            )
        }
    }
}