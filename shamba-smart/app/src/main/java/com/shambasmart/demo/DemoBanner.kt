package com.shambasmart.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DemoBanner(
    onExitDemo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0A820))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Demo mode — all data is simulated",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF412402)
        )
        TextButton(onClick = onExitDemo) {
            Text(
                text = "Exit demo",
                fontSize = 12.sp,
                color = Color(0xFF412402),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ExitDemoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Exit demo mode?",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Text(
                text = "All demo data will be cleared. Your real farm data is unaffected.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB52626)
                )
            ) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay in demo")
            }
        }
    )
}