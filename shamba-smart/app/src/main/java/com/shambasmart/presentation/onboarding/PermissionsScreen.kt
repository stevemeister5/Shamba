package com.shambasmart.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shambasmart.presentation.common.theme.*

data class PermissionItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val isGranted: Boolean = false
)

@Composable
fun PermissionsScreen(
    onRequestPermissions: () -> Unit,
    permissionsGranted: Boolean
) {
    val permissions = listOf(
        PermissionItem(
            icon = Icons.Default.Camera,
            title = "Camera",
            description = "For cheese grading and crop scouting",
            isGranted = permissionsGranted
        ),
        PermissionItem(
            icon = Icons.Default.LocationOn,
            title = "Location",
            description = "For GPS boundary mapping",
            isGranted = permissionsGranted
        ),
        PermissionItem(
            icon = Icons.Default.Mic,
            title = "Microphone",
            description = "For livestock audio monitoring",
            isGranted = permissionsGranted
        ),
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "For alerts and reminders",
            isGranted = permissionsGranted
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Permissions",
            modifier = Modifier.size(80.dp),
            tint = Green500
        )

        Spacer(modifier = Modifier.height(Space.space6))

        Text(
            text = "Enable Permissions",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = FontSize.display,
                fontWeight = FontWeight.Bold
            ),
            color = Neutral950
        )

        Spacer(modifier = Modifier.height(Space.space2))

        Text(
            text = "Allow access to unlock all features",
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral800
        )

        Spacer(modifier = Modifier.height(Space.space8))

        permissions.forEach { permission ->
            PermissionRow(
                icon = permission.icon,
                title = permission.title,
                description = permission.description,
                isGranted = permission.isGranted
            )
            Spacer(modifier = Modifier.height(Space.space3))
        }

        Spacer(modifier = Modifier.height(Space.space8))

        if (!permissionsGranted) {
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.button)
                )
                Spacer(modifier = Modifier.width(Space.space2))
                Text(
                    text = "Grant Permissions",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = StatusColors.healthyBackground
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space.space4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusColors.healthy
                    )
                    Spacer(modifier = Modifier.width(Space.space2))
                    Text(
                        text = "All permissions granted!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = StatusColors.healthyText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.space4))

        TextButton(
            onClick = { /* Skip permissions */ }
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) StatusColors.healthyBackground else SurfaceRaised
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
                tint = if (isGranted) StatusColors.healthy else Green500
            )
            Spacer(modifier = Modifier.width(Space.space4))
            Column(modifier = Modifier.weight(1f)) {
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
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = StatusColors.healthy,
                    modifier = Modifier.size(IconSize.button)
                )
            }
        }
    }
}