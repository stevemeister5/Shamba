package com.shambasmart.map.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.osmdroid.util.GeoPoint

data class DrawingPoint(
    val geoPoint: GeoPoint,
    val screenPosition: Offset
)

@Composable
fun PolygonDrawingOverlay(
    modifier: Modifier = Modifier,
    points: List<GeoPoint>,
    onPointAdded: (GeoPoint) -> Unit,
    onPointRemoved: () -> Unit,
    onPolygonComplete: (List<GeoPoint>) -> Unit,
    onCancel: () -> Unit,
    isActive: Boolean = false
) {
    var screenPoints by remember { mutableStateOf(listOf<Offset>()) }
    var currentDragPoint by remember { mutableStateOf<Offset?>(null) }

    if (!isActive) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        // Drawing canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val geoPoint = GeoPoint(0.0, 0.0) // Placeholder - would need map reference
                        screenPoints = screenPoints + offset
                        onPointAdded(geoPoint)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentDragPoint = offset
                        },
                        onDrag = { change, _ ->
                            currentDragPoint = change.position
                        },
                        onDragEnd = {
                            currentDragPoint?.let { offset ->
                                val geoPoint = GeoPoint(0.0, 0.0) // Placeholder
                                screenPoints = screenPoints + offset
                                onPointAdded(geoPoint)
                            }
                            currentDragPoint = null
                        }
                    )
                }
        ) {
            // Draw polygon
            if (screenPoints.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(screenPoints.first().x, screenPoints.first().y)
                    screenPoints.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    if (screenPoints.size > 2) {
                        close()
                    }
                }
                
                drawPath(
                    path = path,
                    color = Color(0xFF2E9E58).copy(alpha = 0.3f) // Green from design system
                )
                
                drawPath(
                    path = path,
                    color = Color(0xFF2E9E58),
                    style = Stroke(width = 2f)
                )
                
                // Draw points
                screenPoints.forEach { point ->
                    drawCircle(
                        color = Color(0xFF2E9E58),
                        radius = 8f,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = point
                    )
                }
            }
            
            // Draw current drag line
            currentDragPoint?.let { dragPoint ->
                if (screenPoints.isNotEmpty()) {
                    val lastPoint = screenPoints.last()
                    drawLine(
                        color = Color(0xFF2E9E58).copy(alpha = 0.5f),
                        start = lastPoint,
                        end = dragPoint,
                        strokeWidth = 2f
                    )
                }
            }
        }
        
        // Control buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Undo button
            if (screenPoints.isNotEmpty()) {
                IconButton(
                    onClick = {
                        screenPoints = screenPoints.dropLast(1)
                        onPointRemoved()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFF141A17),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFF202C27), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = Color(0xFF8A9E96)
                    )
                }
            }
            
            // Complete button (needs at least 3 points)
            if (screenPoints.size >= 3) {
                IconButton(
                    onClick = {
                        val geoPoints = screenPoints.map { GeoPoint(0.0, 0.0) } // Placeholder
                        onPolygonComplete(geoPoints)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFF2E9E58),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Complete",
                        tint = Color(0xFF051208)
                    )
                }
            }
            
            // Cancel button
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFF141A17),
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, Color(0xFF202C27), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color(0xFF8A9E96)
                )
            }
        }
        
        // Instructions
        Text(
            text = when {
                screenPoints.isEmpty() -> "Tap to add points"
                screenPoints.size < 3 -> "Add ${3 - screenPoints.size} more points"
                else -> "Tap done to complete polygon"
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(
                    Color(0xFF141A17).copy(alpha = 0.9f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            color = Color(0xFFF8FAF9),
            fontSize = 14.sp
        )
    }
}