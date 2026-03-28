package com.shambasmart.presentation.cheese

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheeseScreen() {
    // Design system colors (from design_reference.md)
    val surfaceBase = Color(0xFF0D1210)
    val surfaceRaised = Color(0xFF141A17)
    val neutral200 = Color(0xFF202C27)
    val neutral300 = Color(0xFF2E3D37)
    val neutral600 = Color(0xFF8A9E96)
    val neutral800 = Color(0xFFC4CEC9)
    val neutral950 = Color(0xFFF8FAF9)
    val amber500 = Color(0xFFD4751F)
    val amber400 = Color(0xFFE8922E)
    val amber900 = Color(0xFF2A1505)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Page header
            Text(
                text = "Cheese Production",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = neutral950,
                letterSpacing = (-0.01).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Track batches, aging progress, and milk collection",
                fontSize = 15.sp,
                color = neutral600
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Empty state card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = neutral200,
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceRaised
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = neutral300
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No Cheese Batches",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = neutral800,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Start your first cheese batch to track aging progress and manage your dairy production.",
                        fontSize = 15.sp,
                        color = neutral600,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = amber500
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start First Batch",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = amber900
                        )
                    }
                }
            }
        }
    }
}
