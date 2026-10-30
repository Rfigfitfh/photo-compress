package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StrokePath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun DigitalSignaturePad(
    modifier: Modifier = Modifier,
    onSignatureReady: (Bitmap) -> Unit
) {
    val strokes = remember { mutableStateListOf<StrokePath>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeThickness by remember { mutableStateOf(5f) }
    var padWidth by remember { mutableStateOf(300) }
    var padHeight by remember { mutableStateOf(160) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drawing Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .pointerInput(selectedColor, strokeThickness) {
                    padWidth = size.width
                    padHeight = size.height

                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints = currentPoints + change.position
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                strokes.add(StrokePath(currentPoints, selectedColor, strokeThickness))
                                currentPoints = emptyList()
                            }
                        },
                        onDragCancel = {
                            currentPoints = emptyList()
                        }
                    )
                }
                .testTag("signature_canvas")
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Baseline guide line for signature
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.6f),
                    start = Offset(20f, size.height * 0.75f),
                    end = Offset(size.width - 20f, size.height * 0.75f),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw completed strokes
                for (stroke in strokes) {
                    if (stroke.points.size > 1) {
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            path.lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else if (stroke.points.size == 1) {
                        drawCircle(
                            color = stroke.color,
                            radius = stroke.strokeWidth / 2f,
                            center = stroke.points[0]
                        )
                    }
                }

                // Draw stroke currently being drawn
                if (currentPoints.size > 1) {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(currentPoints[0].x, currentPoints[0].y)
                    for (i in 1 until currentPoints.size) {
                        path.lineTo(currentPoints[i].x, currentPoints[i].y)
                    }
                    drawPath(
                        path = path,
                        color = selectedColor,
                        style = Stroke(
                            width = strokeThickness,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (strokes.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = "Sign here with finger / stylus",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ink and Tool Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ink color selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ink:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Black Ink
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(
                            width = if (selectedColor == Color.Black) 2.5.dp else 1.dp,
                            color = if (selectedColor == Color.Black) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = Color.Black }
                        .testTag("color_black")
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Royal Navy Blue Ink (Exam standard)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2B5C))
                        .border(
                            width = if (selectedColor == Color(0xFF0F2B5C)) 2.5.dp else 1.dp,
                            color = if (selectedColor == Color(0xFF0F2B5C)) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = Color(0xFF0F2B5C) }
                        .testTag("color_blue")
                )
            }

            // Undo and Clear Buttons
            Row {
                FilledTonalButton(
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            strokes.removeAt(strokes.lastIndex)
                        }
                    },
                    enabled = strokes.isNotEmpty(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = com.example.ui.theme.CreamSurfaceVariant,
                        contentColor = com.example.ui.theme.PrimaryEspresso
                    ),
                    modifier = Modifier.testTag("undo_stroke_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { strokes.clear() },
                    enabled = strokes.isNotEmpty(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CreamBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = com.example.ui.theme.CreamSurface,
                        contentColor = com.example.ui.theme.TextPrimary
                    ),
                    modifier = Modifier.testTag("clear_signature_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Export Canvas Button
        Button(
            onClick = {
                val bmp = renderStrokesToBitmap(padWidth, padHeight, strokes)
                onSignatureReady(bmp)
            },
            enabled = strokes.isNotEmpty(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("use_drawn_signature_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.example.ui.theme.PrimaryEspresso,
                contentColor = Color(0xFFFAF7F2)
            )
        ) {
            Text("Use Drawn Signature", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

private fun renderStrokesToBitmap(
    width: Int,
    height: Int,
    strokes: List<StrokePath>
): Bitmap {
    val finalWidth = if (width > 0) width else 400
    val finalHeight = if (height > 0) height else 200

    val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    for (stroke in strokes) {
        if (stroke.points.size > 1) {
            val paint = Paint().apply {
                color = stroke.color.toArgb()
                strokeWidth = stroke.strokeWidth
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            val path = Path()
            path.moveTo(stroke.points[0].x, stroke.points[0].y)
            for (i in 1 until stroke.points.size) {
                path.lineTo(stroke.points[i].x, stroke.points[i].y)
            }
            canvas.drawPath(path, paint)
        }
    }

    return bitmap
}
