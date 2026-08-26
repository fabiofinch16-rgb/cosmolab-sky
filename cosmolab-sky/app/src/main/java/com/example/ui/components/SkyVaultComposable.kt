package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.PlanetObservation
import com.example.astronomy.SkyCondition
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkyVaultComposable(
    observation: PlanetObservation,
    modifier: Modifier = Modifier
) {
    val planet = observation.planet
    val isAboveHorizon = observation.isAboveHorizon
    val skyCondition = observation.skyCondition

    // Animate altitude and azimuth for smooth scrubbing motion
    val animatedAlt by animateFloatAsState(
        targetValue = observation.altitudeDeg.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "altAnim"
    )

    val animatedAz by animateFloatAsState(
        targetValue = observation.azimuthDeg.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "azAnim"
    )

    val textMeasurer = rememberTextMeasurer()

    val skyBackgroundGradient = when (skyCondition) {
        SkyCondition.DAYTIME -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E3A8A),
                Color(0xFF3B82F6),
                Color(0xFF93C5FD)
            )
        )
        SkyCondition.CIVIL_TWILIGHT -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E1B4B),
                Color(0xFF431407),
                Color(0xFF9A3412)
            )
        )
        SkyCondition.NAUTICAL_TWILIGHT -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF312E81)
            )
        )
        SkyCondition.ASTRONOMICAL_TWILIGHT -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0B0F19),
                Color(0xFF111827),
                Color(0xFF1E1B4B)
            )
        )
        SkyCondition.ASTRONOMICAL_NIGHT -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF090A10),
                Color(0xFF121420),
                Color(0xFF1A1C2E)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(skyBackgroundGradient)
            .border(1.dp, SpaceBorder, RoundedCornerShape(28.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val horizonY = height * 0.72f
            val domeRadiusY = horizonY - 20f
            val domeRadiusX = width * 0.44f

            // Draw Ground / Horizon Fill
            val groundPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, horizonY)
                lineTo(width, horizonY)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = groundPath,
                color = Color(0xFF0D0E15)
            )

            // Draw Horizon Line
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(0f, horizonY),
                end = Offset(width, horizonY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )

            // Draw Sky Vault Dome Arc
            val domePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(centerX - domeRadiusX, horizonY)
                quadraticTo(centerX, 20f, centerX + domeRadiusX, horizonY)
            }
            drawPath(
                path = domePath,
                color = Color(0xFF334155),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            )

            // Draw Altitude 30° and 60° reference arcs
            val arc30Path = androidx.compose.ui.graphics.Path().apply {
                val rx = domeRadiusX * 0.75f
                val ry = (horizonY - 20f) * 0.7f
                moveTo(centerX - rx, horizonY)
                quadraticTo(centerX, horizonY - ry, centerX + rx, horizonY)
            }
            drawPath(
                path = arc30Path,
                color = Color(0xFF1E293B),
                style = Stroke(width = 1f)
            )

            // Draw Cardinal Directions along the horizon
            val cardinalLabels = listOf("N", "L", "S", "O")
            val cardinalXPositions = listOf(
                centerX - domeRadiusX,
                centerX - domeRadiusX * 0.33f,
                centerX + domeRadiusX * 0.33f,
                centerX + domeRadiusX
            )

            cardinalLabels.forEachIndexed { idx, label ->
                val x = cardinalXPositions[idx].coerceIn(24f, width - 24f)
                val measured = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = if (label == "L" || label == "S") Color(0xFFD0BCFF) else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(x - measured.size.width / 2f, horizonY + 6f)
                )
            }

            // Calculate Position of Planet
            if (isAboveHorizon) {
                // Map azimuth (0..360) to horizontal axis x
                // Map altitude (0..90) to vertical axis y from horizonY up to top
                val normAz = (animatedAz % 360.0 + 360.0) % 360.0
                val xFraction = ((normAz - 90.0) / 180.0).toFloat().coerceIn(-1.0f, 1.0f)
                val planetX = centerX + (xFraction * domeRadiusX * 0.85f)

                val altFraction = (animatedAlt / 90.0f).coerceIn(0.0f, 1.0f)
                val planetY = horizonY - (altFraction * (horizonY - 36f))

                val pColor = try {
                    Color(android.graphics.Color.parseColor(planet.hexColor))
                } catch (e: Exception) {
                    Color(0xFFFFD54F)
                }

                // Draw Planet Glow
                drawCircle(
                    color = pColor.copy(alpha = 0.3f),
                    radius = 22f,
                    center = Offset(planetX, planetY)
                )
                drawCircle(
                    color = pColor,
                    radius = 12f,
                    center = Offset(planetX, planetY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = Offset(planetX, planetY)
                )

                // Label near planet
                val pLabelMeasured = textMeasurer.measure(
                    text = "${planet.symbol} ${planet.portugueseName}",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                val labelX = (planetX - pLabelMeasured.size.width / 2f).coerceIn(10f, width - pLabelMeasured.size.width - 10f)
                val labelY = if (planetY - 28f < 10f) planetY + 16f else planetY - 28f

                drawText(
                    textLayoutResult = pLabelMeasured,
                    topLeft = Offset(labelX, labelY)
                )
            }
        }

        // Overlay Message if Below Horizon or Daytime
        if (!isAboveHorizon) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceCardSurface.copy(alpha = 0.85f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🌅 ${planet.portugueseName} está abaixo do horizonte",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Próximo nascer: ${observation.riseTimeStr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else if (skyCondition != SkyCondition.ASTRONOMICAL_NIGHT) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${skyCondition.icon} Acima do horizonte (${skyCondition.label})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFDE047),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
