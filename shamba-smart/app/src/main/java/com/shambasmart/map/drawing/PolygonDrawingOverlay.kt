package com.shambasmart.map.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point

/**
 * Data class representing a point being drawn on the map
 */
data class DrawingPoint(
    val x: Float,
    val y: Float,
    val mapPoint: Point? = null // Corresponding map coordinate
)

/**
 * State of the polygon drawing process
 */
data class DrawingState(
    val points: List<DrawingPoint> = emptyList(),
    val isClosed: Boolean = false,
    val isDragging: Boolean = false,
    val draggedPointIndex: Int? = null
)

/**
 * Overlay composable for drawing polygons on the map.
 * 
 * Features:
 * - Tap to add points
 * - Drag points to adjust
 * - Auto-close polygon when last point is near first
 * - Undo last point
 * - Finish and save polygon
 */
@Composable
fun PolygonDrawingOverlay(
    onPointAdded: (DrawingPoint) -> Unit,
    onPointMoved: (Int, DrawingPoint) -> Unit,
    onFinish: (List<DrawingPoint>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var drawingState by remember { mutableStateOf(DrawingState()) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Drawing canvas overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newPoint = DrawingPoint(offset.x, offset.y)
                        
                        // Check if closing the polygon
                        if (drawingState.points.size >= 3) {
                            val firstPoint = drawingState.points.first()
                            val distance = kotlin.math.sqrt(
                                (offset.x - firstPoint.x) * (offset.x - firstPoint.x) +
                                (offset.y - firstPoint.y) * (offset.y - firstPoint.y)
                            )
                            
                            if (distance < 50f) { // Close threshold
                                drawingState = drawingState.copy(isClosed = true)
                                return@detectTapGestures
                            }
                        }
                        
                        drawingState = drawingState.copy(
                            points = drawingState.points + newPoint
                        )
                        onPointAdded(newPoint)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Find nearest point
                            val nearestIndex = drawingState.points.indexOfMinBy { point ->
                                kotlin.math.sqrt(
                                    (offset.x - point.x) * (offset.x - point.x) +
                                    (offset.y - point.y) * (offset.y - point.y)
                                )
                            }
                            
                            if (nearestIndex != null) {
                                val dist = kotlin.math.sqrt(
                                    (offset.x - drawingState.points[nearestIndex].x).let { it * it } +
                                    (offset.y - drawingState.points[nearestIndex].y).let { it * it }
                                )
                                if (dist < 50f) {
                                    drawingState = drawingState.copy(
                                        isDragging = true,
                                        draggedPointIndex = nearestIndex
                                    )
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (drawingState.isDragging && drawingState.draggedPointIndex != null) {
                                val updatedPoints = drawingState.points.toMutableList()
                                updatedPoints[drawingState.draggedPointIndex!!] = 
                                    DrawingPoint(change.position.x, change.position.y)
                                drawingState = drawingState.copy(points = updatedPoints)
                                onPointMoved(drawingState.draggedPointIndex!!, updatedPoints[drawingState.draggedPointIndex!!])
                            }
                        },
                        onDragEnd = {
                            drawingState = drawingState.copy(
                                isDragging = false,
                                draggedPointIndex = null
                            )
                        }
                    )
                }
        ) {
            val points = drawingState.points
            if (points.size >= 2) {
                // Draw lines connecting points
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    if (drawingState.isClosed) {
                        close()
                    }
                }
                
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = 3f)
                )
                
                // Draw closing line preview if not closed
                if (!drawingState.isClosed && points.size >= 3) {
                    drawLine(
                        color = Color(0xFF2196F3).copy(alpha = 0.3f),
                        start = Offset(points.last().x, points.last().y),
                        end = Offset(points.first().x, points.first().y),
                        strokeWidth = 2f
                    )
                }
                
                // Fill polygon if closed
                if (drawingState.isClosed) {
                    drawPath(
                        path = path,
                        color = Color(0xFF2196F3).copy(alpha = 0.2f)
                    )
                }
            }
            
            // Draw points
            points.forEachIndexed { index, point ->
                val isSelected = drawingState.draggedPointIndex == index
                val isFirst = index == 0
                
                drawCircle(
                    color = when {
                        isFirst -> Color(0xFF4CAF50) // Green for first point
                        isSelected -> Color(0xFFFF5722) // Red for selected
                        else -> Color(0xFF2196F3) // Blue for others
                    },
                    radius = if (isSelected) 15f else 10f,
                    center = Offset(point.x, point.y)
                )
            }
        }
        
        // Drawing toolbar at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Points counter
            if (drawingState.points.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "${drawingState.points.size} points" + 
                            if (drawingState.isClosed) " (Closed)" else "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Undo button
                Button(
                    onClick = {
                        if (drawingState.points.isNotEmpty()) {
                            drawingState = drawingState.copy(
                                points = drawingState.points.dropLast(1),
                                isClosed = false
                            )
                        }
                    },
                    enabled = drawingState.points.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }
                
                // Clear button
                Button(
                    onClick = {
                        drawingState = DrawingState()
                    },
                    enabled = drawingState.points.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
                
                // Finish button
                Button(
                    onClick = {
                        if (drawingState.points.size >= 3) {
                            onFinish(drawingState.points)
                        }
                    },
                    enabled = drawingState.points.size >= 3
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finish")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cancel button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel Drawing")
            }
        }
        
        // Instructions overlay at top
        if (drawingState.points.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Draw Polygon Boundary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Tap to add points", style = MaterialTheme.typography.bodyMedium)
                    Text("• Drag points to adjust", style = MaterialTheme.typography.bodyMedium)
                    Text("• Tap near first point to close", style = MaterialTheme.typography.bodyMedium)
                    Text("• Minimum 3 points required", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/**
 * Helper extension to find index of minimum by selector
 */
private fun <T> List<T>.indexOfMinBy(selector: (T) -> Float): Int? {
    if (isEmpty()) return null
    var minIndex = 0
    var minValue = selector(this[0])
    for (i in 1 until size) {
        val value = selector(this[i])
        if (value < minValue) {
            minValue = value
            minIndex = i
        }
    }
    return minIndex
}