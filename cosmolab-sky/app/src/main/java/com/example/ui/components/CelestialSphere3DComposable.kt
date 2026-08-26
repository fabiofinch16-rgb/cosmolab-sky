package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.AstronomyEngine
import com.example.astronomy.CelestialCatalog
import com.example.astronomy.DeepSkyCatalog
import com.example.astronomy.DeepSkyObject
import com.example.astronomy.Planet
import com.example.astronomy.StarObject
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleContainer
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Representa um astro selecionável na Maquete 3D do Horizonte Local.
 */
sealed class CelestialTarget {
    abstract val id: String
    abstract val name: String
    abstract val symbol: String
    abstract val hexColor: String
    abstract val categoryLabel: String

    data class TargetPlanet(val planet: Planet) : CelestialTarget() {
        override val id: String get() = planet.id
        override val name: String get() = planet.portugueseName
        override val symbol: String get() = planet.symbol
        override val hexColor: String get() = planet.hexColor
        override val categoryLabel: String get() = "Planeta"
    }

    object TargetSun : CelestialTarget() {
        override val id: String = "sun"
        override val name: String = "Sol"
        override val symbol: String = "☀️"
        override val hexColor: String = "#FACC15"
        override val categoryLabel: String = "Estrela Central"
    }

    object TargetMoon : CelestialTarget() {
        override val id: String = "moon"
        override val name: String = "Lua"
        override val symbol: String = "🌙"
        override val hexColor: String = "#E2E8F0"
        override val categoryLabel: String = "Satélite Natural"
    }

    data class TargetStar(val star: StarObject) : CelestialTarget() {
        override val id: String get() = "star_${star.name}"
        override val name: String get() = star.name
        override val symbol: String get() = "⭐"
        override val hexColor: String get() = star.colorHex
        override val categoryLabel: String get() = "Estrela"
    }

    data class TargetDso(val dso: DeepSkyObject) : CelestialTarget() {
        override val id: String get() = dso.id
        override val name: String get() = "${dso.messierNgc} ${dso.commonName}"
        override val symbol: String get() = "🌌"
        override val hexColor: String get() = "#C084FC"
        override val categoryLabel: String get() = dso.type.portugueseName
    }
}

/**
 * Ponto amostrado na trajetória aparente de 24 horas.
 */
data class TrajectoryPoint(
    val minuteOfDay: Int,
    val timeStr: String,
    val altitudeDeg: Double,
    val azimuthDeg: Double,
    val isAboveHorizon: Boolean
)

/**
 * Dados completos da trajetória aparente do astro no horizonte local.
 */
data class TrajectoryArc(
    val target: CelestialTarget,
    val points: List<TrajectoryPoint>,
    val risePoint: TrajectoryPoint?,
    val culminationPoint: TrajectoryPoint?,
    val setPoint: TrajectoryPoint?,
    val maxAltitudeDeg: Double,
    val currentPoint: TrajectoryPoint,
    val totalHoursVisible: Double
)

/**
 * Componente MAQUETE 3D DO HORIZONTE LOCAL (Horizon Diorama 3D Maquette).
 *
 * Apresenta uma plataforma terrestre tridimensional em perspectiva oblíqua de maquete física,
 * projetando a trajetória aparente astronômica e usando miniaturas customizadas de cada astro.
 */
@Composable
fun CelestialSphere3DComposable(
    viewModel: CosmoLabViewModel,
    modifier: Modifier = Modifier,
    highlightTargetId: String? = null,
    onNavigateToPlanet: ((Planet) -> Unit)? = null,
    onNavigateToDso: ((DeepSkyObject) -> Unit)? = null
) {
    val calendar = viewModel.selectedCalendar
    val city = viewModel.selectedCity
    val lat = city.latitude
    val lon = city.longitude

    // Lista de alvos disponíveis para seleção rápida
    val availableTargets = remember(highlightTargetId) {
        val list = mutableListOf<CelestialTarget>(
            CelestialTarget.TargetSun,
            CelestialTarget.TargetMoon,
            CelestialTarget.TargetPlanet(Planet.MERCURY),
            CelestialTarget.TargetPlanet(Planet.VENUS),
            CelestialTarget.TargetPlanet(Planet.MARS),
            CelestialTarget.TargetPlanet(Planet.JUPITER),
            CelestialTarget.TargetPlanet(Planet.SATURN),
            CelestialTarget.TargetPlanet(Planet.URANUS),
            CelestialTarget.TargetPlanet(Planet.NEPTUNE)
        )

        if (highlightTargetId != null) {
            val matchingPlanet = Planet.values().firstOrNull { it.id == highlightTargetId }
            if (matchingPlanet == null) {
                val matchingStar = CelestialCatalog.brightStars.firstOrNull { "star_${it.name}" == highlightTargetId }
                if (matchingStar != null) {
                    list.add(0, CelestialTarget.TargetStar(matchingStar))
                } else {
                    val matchingDso = DeepSkyCatalog.objects.firstOrNull { it.id == highlightTargetId }
                    if (matchingDso != null) {
                        list.add(0, CelestialTarget.TargetDso(matchingDso))
                    }
                }
            }
        }
        list
    }

    // Astro selecionado
    var selectedTarget by remember(highlightTargetId) {
        val initial = if (highlightTargetId != null) {
            availableTargets.firstOrNull { it.id == highlightTargetId } ?: CelestialTarget.TargetPlanet(Planet.SATURN)
        } else {
            CelestialTarget.TargetPlanet(Planet.SATURN)
        }
        mutableStateOf(initial)
    }

    // Câmera 3D Interativa
    var cameraYawDeg by remember { mutableFloatStateOf(0f) }   // Rotação horizontal
    var cameraPitchDeg by remember { mutableFloatStateOf(34f) } // Perspectiva oblíqua
    var cameraZoom by remember { mutableFloatStateOf(1.0f) }     // Zoom

    // Reprodução de Animação
    var isPlayingAnim by remember { mutableStateOf(false) }
    var animSpeed by remember { mutableFloatStateOf(100f) }

    LaunchedEffect(isPlayingAnim, animSpeed) {
        if (isPlayingAnim) {
            while (isActive && isPlayingAnim) {
                delay(30)
                val minutesPerFrame = (animSpeed * 0.05)
                viewModel.advanceTimeMinutes(minutesPerFrame)
            }
        }
    }

    // Cálculo exato da trajetória de 24 horas para o astro selecionado
    val trajectoryArc = remember(selectedTarget, calendar, lat, lon) {
        calculateTrajectoryArc(selectedTarget, calendar, lat, lon)
    }

    // Cor de destaque do astro
    val accentColor = remember(selectedTarget) {
        try {
            Color(android.graphics.Color.parseColor(selectedTarget.hexColor))
        } catch (e: Exception) {
            CosmicPurplePrimary
        }
    }

    // Offset de tempo atual em minutos relativo ao momento real (-720 a +720)
    val nowMillis = System.currentTimeMillis()
    val timeOffsetMinutes = ((calendar.timeInMillis - nowMillis) / (60.0 * 1000.0)).toFloat().coerceIn(-720f, 720f)

    // Posição do Sol no horário selecionado para iluminação dinâmica do céu
    val sunPosition = remember(calendar, lat, lon) {
        AstronomyEngine.calculateSunPosition(calendar, lat, lon)
    }
    val sunAlt = sunPosition.altitudeDeg

    // Posição da Lua no horário selecionado (sempre calculada em tempo real)
    val currentMoonPosition = remember(calendar, lat, lon) {
        AstronomyEngine.calculateMoonPosition(calendar, lat, lon)
    }

    // Separação angular em graus entre o astro selecionado e a Lua
    val lunarSeparationDeg = remember(trajectoryArc.currentPoint, currentMoonPosition) {
        val curr = trajectoryArc.currentPoint
        val moonAltRad = Math.toRadians(currentMoonPosition.altitudeDeg)
        val moonAzRad = Math.toRadians(currentMoonPosition.azimuthDeg)
        val targetAltRad = Math.toRadians(curr.altitudeDeg)
        val targetAzRad = Math.toRadians(curr.azimuthDeg)

        val cosSep = kotlin.math.sin(moonAltRad) * kotlin.math.sin(targetAltRad) +
                kotlin.math.cos(moonAltRad) * kotlin.math.cos(targetAltRad) * kotlin.math.cos(moonAzRad - targetAzRad)
        val sepRad = kotlin.math.acos(cosSep.coerceIn(-1.0, 1.0))
        Math.toDegrees(sepRad)
    }

    // Cores e rótulo do estado de iluminação do céu
    val (skyGradientColors, skyPhaseText, skyPhaseIcon) = remember(sunAlt) {
        when {
            sunAlt > 6.0 -> Triple(
                listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF1E3A8A)),
                "Céu Diurno (Dia)",
                "☀️"
            )
            sunAlt in 0.0..6.0 -> Triple(
                listOf(Color(0xFFF97316), Color(0xFFC2410C), Color(0xFF31103F)),
                "Amanhecer / Pôr do Sol",
                "🌅"
            )
            sunAlt in -6.0..0.0 -> Triple(
                listOf(Color(0xFFD97706), Color(0xFF7C2D12), Color(0xFF1E1B4B)),
                "Crepúsculo Civil",
                "🟠"
            )
            sunAlt in -12.0..-6.0 -> Triple(
                listOf(Color(0xFF6B21A8), Color(0xFF3B0764), Color(0xFF0F172A)),
                "Crepúsculo Náutico",
                "🌆"
            )
            sunAlt in -18.0..-12.0 -> Triple(
                listOf(Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF090714)),
                "Crepúsculo Astronômico",
                "🌃"
            )
            else -> Triple(
                listOf(Color(0xFF090714), Color(0xFF120D26), Color(0xFF080612)),
                "Noite Escura (Céu Estrelado)",
                "🌌"
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpaceBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Cabeçalho da Maquete e Botões da Câmera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📐 MAQUETE 3D DO HORIZONTE",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Trajetória aparente de ${selectedTarget.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Controles de Câmera 3D
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            cameraYawDeg = 0f
                            cameraPitchDeg = 34f
                            cameraZoom = 1.0f
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Resetar Visão 3D",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { cameraZoom = (cameraZoom + 0.15f).coerceAtMost(2.2f) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Mais Zoom",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { cameraZoom = (cameraZoom - 0.15f).coerceAtLeast(0.6f) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Menos Zoom",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. Seletor de Astros (Sol, Lua, Planetas)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableTargets.forEach { target ->
                    val isSelected = target.id == selectedTarget.id
                    val chipBg = if (isSelected) accentColor.copy(alpha = 0.22f) else CosmicPurpleContainer.copy(alpha = 0.4f)
                    val chipBorder = if (isSelected) accentColor else SpaceBorder
                    val chipTextColor = if (isSelected) TextPrimary else TextSecondary

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedTarget = target }
                            .border(1.dp, chipBorder, RoundedCornerShape(16.dp)),
                        color = chipBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = target.symbol, fontSize = 13.sp)
                            Text(
                                text = target.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = chipTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 3. CANVAS DA MAQUETE 3D DO HORIZONTE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = skyGradientColors
                        )
                    )
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            cameraYawDeg = (cameraYawDeg + dragAmount.x * 0.45f) % 360f
                            cameraPitchDeg = (cameraPitchDeg - dragAmount.y * 0.35f).coerceIn(12f, 70f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoomChange, _ ->
                            cameraZoom = (cameraZoom * zoomChange).coerceIn(0.6f, 2.2f)
                        }
                    }
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulseAnim")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                val textMeasurer = rememberTextMeasurer()

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val cx = width / 2f
                    val cy = height / 2f + 18f

                    // Dimensões do Pedestal da Maquete
                    val baseRadius = min(width, height) * 0.33f * cameraZoom
                    val maxHeight3D = baseRadius * 1.15f
                    val platformDepth = 16f // Espessura 3D da base terrestre

                    val yawRad = Math.toRadians(cameraYawDeg.toDouble())
                    val pitchRad = Math.toRadians(cameraPitchDeg.toDouble())

                    val sinYaw = sin(yawRad)
                    val cosYaw = cos(yawRad)
                    val sinPitch = sin(pitchRad)
                    val cosPitch = cos(pitchRad)

                    // Projeção 3D em perspectiva para a Maquete
                    fun project3D(x: Double, y: Double, z: Double): Triple<Float, Float, Double> {
                        // Rotação Yaw (Horizontal)
                        val x1 = x * cosYaw - z * sinYaw
                        val z1 = x * sinYaw + z * cosYaw
                        val y1 = y

                        // Rotação Pitch (Inclinada com horizonte em profundidade)
                        val x2 = x1
                        val y2 = y1 * cosPitch + z1 * sinPitch
                        val z2 = -y1 * sinPitch + z1 * cosPitch

                        // Escala de Perspectiva
                        val focalDist = 2.8
                        val scale = focalDist / (focalDist + z2 / (baseRadius * 1.6))

                        val screenX = (cx + x2 * scale).toFloat()
                        val screenY = (cy - y2 * scale).toFloat()

                        return Triple(screenX, screenY, z2)
                    }

                    fun altAzTo3D(altDeg: Double, azDeg: Double, radius: Float = baseRadius): Triple<Double, Double, Double> {
                        val azRad = Math.toRadians(azDeg)
                        val altRad = Math.toRadians(altDeg)

                        val r = radius * cos(altRad)
                        val x = r * sin(azRad)
                        val z = r * cos(azRad)
                        val y = maxHeight3D * sin(altRad)

                        return Triple(x, y, z)
                    }

                    // A) BASE / PEDESTAL TRIDIMENSIONAL DA MAQUETE (Superfície de Grama + Camada de Solo/Terra Exposta)
                    val stepDeg = 6
                    val bottomRimPath = Path()
                    var firstBottom = true
                    for (az in 0..360 step stepDeg) {
                        val pBottom = project3D(baseRadius * sin(Math.toRadians(az.toDouble())), -platformDepth.toDouble(), baseRadius * cos(Math.toRadians(az.toDouble())))
                        if (firstBottom) {
                            bottomRimPath.moveTo(pBottom.first, pBottom.second)
                            firstBottom = false
                        } else {
                            bottomRimPath.lineTo(pBottom.first, pBottom.second)
                        }
                    }
                    bottomRimPath.close()

                    // Sombra/Base Inferior do Solo
                    drawPath(
                        path = bottomRimPath,
                        color = Color(0xFF100B06)
                    )

                    // Camada de Solo/Terra Exposta (Paredes Laterais 3D)
                    for (az in 0 until 360 step stepDeg) {
                        val az1 = az.toDouble()
                        val az2 = (az + stepDeg).toDouble()

                        val pTop1 = project3D(baseRadius * sin(Math.toRadians(az1)), 0.0, baseRadius * cos(Math.toRadians(az1)))
                        val pTop2 = project3D(baseRadius * sin(Math.toRadians(az2)), 0.0, baseRadius * cos(Math.toRadians(az2)))
                        val pBot1 = project3D(baseRadius * sin(Math.toRadians(az1)), -platformDepth.toDouble(), baseRadius * cos(Math.toRadians(az1)))
                        val pBot2 = project3D(baseRadius * sin(Math.toRadians(az2)), -platformDepth.toDouble(), baseRadius * cos(Math.toRadians(az2)))

                        val wallSegmentPath = Path().apply {
                            moveTo(pTop1.first, pTop1.second)
                            lineTo(pTop2.first, pTop2.second)
                            lineTo(pBot2.first, pBot2.second)
                            lineTo(pBot1.first, pBot1.second)
                            close()
                        }

                        // Gradiente de Solo/Terra (Escuro a Marrom Orgânico)
                        drawPath(
                            path = wallSegmentPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3B2616), // Topo do solo (marrom terra)
                                    Color(0xFF22150B)  // Base do solo
                                ),
                                startY = kotlin.math.min(pTop1.second, pTop2.second),
                                endY = kotlin.math.max(pBot1.second, pBot2.second)
                            )
                        )
                    }

                    // Topo da Plataforma do Horizonte (Superfície de Grama Verde Natural)
                    val topRimPath = Path()
                    var firstTop = true
                    for (az in 0..360 step stepDeg) {
                        val pTop = project3D(baseRadius * sin(Math.toRadians(az.toDouble())), 0.0, baseRadius * cos(Math.toRadians(az.toDouble())))
                        if (firstTop) {
                            topRimPath.moveTo(pTop.first, pTop.second)
                            firstTop = false
                        } else {
                            topRimPath.lineTo(pTop.first, pTop.second)
                        }
                    }
                    topRimPath.close()

                    // Preenchimento com Grama Verde Escura em Campo Noturno
                    drawPath(
                        path = topRimPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E3A20), // Grama verde natural no centro
                                Color(0xFF142917), // Grama de campo escuro
                                Color(0xFF0C190D)  // Borda da grama
                            ),
                            center = Offset(cx, cy),
                            radius = baseRadius * 1.2f
                        )
                    )

                    // Borda da Superfície de Grama (Transição Suave para a Camada de Terra)
                    drawPath(
                        path = topRimPath,
                        color = Color(0xFF2E5A32),
                        style = Stroke(width = 2.0f)
                    )
                    drawPath(
                        path = topRimPath,
                        color = Color(0xFF4D2E19).copy(alpha = 0.5f),
                        style = Stroke(width = 1.0f)
                    )

                    // B) CÍRCULOS CONCÊNTRICOS DE ALTITUDE NA TERRA (Grid de Maquete)
                    listOf(30.0, 60.0).forEach { altRing ->
                        val ringRadius = baseRadius * cos(Math.toRadians(altRing))
                        val ringPath = Path()
                        var firstRingPoint = true
                        for (az in 0..360 step 10) {
                            val p = project3D(ringRadius * sin(Math.toRadians(az.toDouble())), 0.0, ringRadius * cos(Math.toRadians(az.toDouble())))
                            if (firstRingPoint) {
                                ringPath.moveTo(p.first, p.second)
                                firstRingPoint = false
                            } else {
                                ringPath.lineTo(p.first, p.second)
                            }
                        }
                        ringPath.close()

                        drawPath(
                            path = ringPath,
                            color = Color.White.copy(alpha = 0.08f),
                            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                        )
                    }

                    // C) EIXOS CARDEAIS E ROSA DOS VENTOS COMPLETA (8 PONTOS)
                    val compassDirections = listOf(
                        Triple("N", 0.0, Color(0xFFEF4444)),     // Norte (0°/360°) - Vermelho
                        Triple("NE", 45.0, Color(0xFF10B981)),  // Nordeste (45°) - Verde Esmeralda
                        Triple("L", 90.0, Color(0xFF06B6D4)),    // Leste (90°) - Ciano
                        Triple("SE", 135.0, Color(0xFF6366F1)),  // Sudeste (135°) - Índigo
                        Triple("S", 180.0, Color(0xFF3B82F6)),   // Sul (180°) - Azul
                        Triple("SO", 225.0, Color(0xFFA855F7)),  // Sudoeste (225°) - Púrpuras
                        Triple("O", 270.0, Color(0xFFF59E0B)),   // Oeste (270°) - Âmbar
                        Triple("NO", 315.0, Color(0xFFF97316))   // Noroeste (315°) - Laranja
                    )

                    // Linhas Radiais da Rosa dos Ventos no Chão da Maquete
                    compassDirections.forEach { (_, azDeg, color) ->
                        val azRad = Math.toRadians(azDeg)
                        val pInner = project3D(baseRadius * 0.25 * sin(azRad), 0.0, baseRadius * 0.25 * cos(azRad))
                        val pOuter = project3D(baseRadius * sin(azRad), 0.0, baseRadius * cos(azRad))

                        drawLine(
                            color = color.copy(alpha = 0.25f),
                            start = Offset(pInner.first, pInner.second),
                            end = Offset(pOuter.first, pOuter.second),
                            strokeWidth = 1.2f
                        )
                    }

                    // Pedestal Central do Observador
                    val pCenter = project3D(0.0, 0.0, 0.0)
                    drawCircle(color = CosmicPurplePrimary.copy(alpha = 0.35f), radius = 9f, center = Offset(pCenter.first, pCenter.second))
                    drawCircle(color = Color.White, radius = 3.5f, center = Offset(pCenter.first, pCenter.second))

                    // ROSA DOS VENTOS - BADGES DOS 8 PONTOS CARDEAIS E COLATERAIS
                    compassDirections.forEach { (label, azDeg, color) ->
                        val azRad = Math.toRadians(azDeg)
                        val proj = project3D(baseRadius * sin(azRad), 0.0, baseRadius * cos(azRad))

                        val isMajor = label in listOf("N", "L", "S", "O")
                        val badgeRadius = if (isMajor) 12f else 9.5f

                        drawCircle(color = color.copy(alpha = 0.25f), radius = badgeRadius + 3f, center = Offset(proj.first, proj.second))
                        drawCircle(color = color, radius = badgeRadius, center = Offset(proj.first, proj.second))

                        val labelText = if (isMajor) label else "$label\n${azDeg.toInt()}°"
                        val fontSize = if (isMajor) 11.sp else 8.sp

                        val textLayout = textMeasurer.measure(
                            text = label,
                            style = TextStyle(color = Color.White, fontSize = fontSize, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(proj.first - textLayout.size.width / 2f, proj.second - textLayout.size.height / 2f)
                        )
                    }

                    // E) DESENHAR A TRAJETÓRIA DO ASTRO (Subterrânea e Visível)
                    val points = trajectoryArc.points
                    if (points.isNotEmpty()) {
                        val visiblePath = Path()
                        val subterraneanPath = Path()

                        var hasStartedVisible = false
                        var hasStartedSubterranean = false

                        points.forEach { pt ->
                            val (x3d, y3d, z3d) = altAzTo3D(pt.altitudeDeg, pt.azimuthDeg)
                            val (sx, sy, _) = project3D(x3d, y3d, z3d)

                            if (pt.altitudeDeg >= 0.0) {
                                if (!hasStartedVisible) {
                                    visiblePath.moveTo(sx, sy)
                                    hasStartedVisible = true
                                } else {
                                    visiblePath.lineTo(sx, sy)
                                }
                            } else {
                                if (!hasStartedSubterranean) {
                                    subterraneanPath.moveTo(sx, sy)
                                    hasStartedSubterranean = true
                                } else {
                                    subterraneanPath.lineTo(sx, sy)
                                }
                            }
                        }

                        // Trilha Abaixo do Horizonte (Dashed Discreta)
                        drawPath(
                            path = subterraneanPath,
                            color = accentColor.copy(alpha = 0.22f),
                            style = Stroke(
                                width = 1.8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        )

                        // Trilha Acima do Horizonte (Brilhante e Glow)
                        drawPath(
                            path = visiblePath,
                            color = accentColor.copy(alpha = 0.35f),
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = visiblePath,
                            color = accentColor,
                            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                        )

                        // Setas de Sentido do Movimento Temporal (Leste -> Oeste)
                        for (i in 3 until points.size - 3 step 8) {
                            val pCurrent = points[i]
                            val pNext = points[min(i + 2, points.size - 1)]

                            if (pCurrent.altitudeDeg >= -2.0) {
                                val (x1, y1, z1) = altAzTo3D(pCurrent.altitudeDeg, pCurrent.azimuthDeg)
                                val (x2, y2, z2) = altAzTo3D(pNext.altitudeDeg, pNext.azimuthDeg)

                                val proj1 = project3D(x1, y1, z1)
                                val proj2 = project3D(x2, y2, z2)

                                val dx = proj2.first - proj1.first
                                val dy = proj2.second - proj1.second
                                val angle = atan2(dy.toDouble(), dx.toDouble())

                                val arrowLen = 10f
                                val arrowAngle = Math.toRadians(28.0)

                                val arrowPath = Path().apply {
                                    moveTo(proj2.first, proj2.second)
                                    lineTo(
                                        (proj2.first - arrowLen * cos(angle - arrowAngle)).toFloat(),
                                        (proj2.second - arrowLen * sin(angle - arrowAngle)).toFloat()
                                    )
                                    moveTo(proj2.first, proj2.second)
                                    lineTo(
                                        (proj2.first - arrowLen * cos(angle + arrowAngle)).toFloat(),
                                        (proj2.second - arrowLen * sin(angle + arrowAngle)).toFloat()
                                    )
                                }

                                drawPath(
                                    path = arrowPath,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // LANDMARKS DE TRAJETÓRIA (Nasce, Culmina, Se Põe)
                        // 1. 🌅 NASCE (Para o astro selecionado)
                        trajectoryArc.risePoint?.let { rise ->
                            val (x, y, z) = altAzTo3D(rise.altitudeDeg, rise.azimuthDeg)
                            val proj = project3D(x, y, z)

                            drawCircle(color = Color(0xFFF97316), radius = 5.5f, center = Offset(proj.first, proj.second))
                            drawCircle(color = Color.White, radius = 2.5f, center = Offset(proj.first, proj.second))

                            val txt = textMeasurer.measure(
                                text = "🌅 ${rise.timeStr}",
                                style = TextStyle(color = Color(0xFFFFEDD5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = txt,
                                topLeft = Offset(proj.first + 8f, proj.second - 14f)
                            )
                        }

                        // 2. ⬆️ CULMINA
                        trajectoryArc.culminationPoint?.let { culm ->
                            val (x, y, z) = altAzTo3D(culm.altitudeDeg, culm.azimuthDeg)
                            val proj = project3D(x, y, z)

                            drawCircle(color = CosmicPurplePrimary, radius = 6f, center = Offset(proj.first, proj.second))
                            drawCircle(color = Color.White, radius = 3f, center = Offset(proj.first, proj.second))

                            val txt = textMeasurer.measure(
                                text = "⬆️ CULMINA ${culm.timeStr} (${String.format("%.0f", culm.altitudeDeg)}°)",
                                style = TextStyle(color = CosmicPurplePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = txt,
                                topLeft = Offset(proj.first - txt.size.width / 2f, proj.second - 22f)
                            )
                        }

                        // 3. 🌇 SE PÔE (Para o astro selecionado)
                        trajectoryArc.setPoint?.let { setPt ->
                            val (x, y, z) = altAzTo3D(setPt.altitudeDeg, setPt.azimuthDeg)
                            val proj = project3D(x, y, z)

                            drawCircle(color = Color(0xFFEF4444), radius = 5.5f, center = Offset(proj.first, proj.second))
                            drawCircle(color = Color.White, radius = 2.5f, center = Offset(proj.first, proj.second))

                            val txt = textMeasurer.measure(
                                text = "🌇 ${setPt.timeStr}",
                                style = TextStyle(color = Color(0xFFFFD2D2), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = txt,
                                topLeft = Offset(proj.first - txt.size.width - 8f, proj.second - 14f)
                            )
                        }

                        // F) MARCADOR MINIATURA DO PRÓPRIO ASTRO NO MOMENTO SELECIONADO
                        val curr = trajectoryArc.currentPoint
                        val (currX, currY, currZ) = altAzTo3D(curr.altitudeDeg, curr.azimuthDeg)
                        val projCurr = project3D(currX, currY, currZ)

                        // Projeção do ponto no chão da plataforma
                        val (groundX, groundY, groundZ) = altAzTo3D(0.0, curr.azimuthDeg)
                        val projGround = project3D(groundX, groundY, groundZ)

                        // Haste de alinhamento com a superfície da maquete
                        drawLine(
                            color = accentColor.copy(alpha = 0.5f),
                            start = Offset(projCurr.first, projCurr.second),
                            end = Offset(projGround.first, projGround.second),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )

                        // Retículo na plataforma
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = 6.5f,
                            center = Offset(projGround.first, projGround.second)
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 3f,
                            center = Offset(projGround.first, projGround.second)
                        )

                        // Halo pulsante atrás da miniatura
                        val markerCenter = Offset(projCurr.first, projCurr.second)
                        drawCircle(
                            color = accentColor.copy(alpha = 0.22f),
                            radius = 18f * pulseScale,
                            center = markerCenter
                        )

                        // MINIATURA REALISTA DO PRÓPRIO ASTRO
                        drawCelestialMiniature(
                            target = selectedTarget,
                            center = markerCenter,
                            sizePx = 28f,
                            isAboveHorizon = curr.isAboveHorizon
                        )

                        // Etiqueta com Nome e Altitude
                        val badgeText = "${selectedTarget.symbol} ${selectedTarget.name.uppercase()} (${String.format("%.1f", curr.altitudeDeg)}°)"
                        val badgeLayout = textMeasurer.measure(
                            text = badgeText,
                            style = TextStyle(
                                color = if (curr.isAboveHorizon) Color.White else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        val badgeTopLeft = Offset(
                            projCurr.first - badgeLayout.size.width / 2f,
                            projCurr.second - 32f
                        )

                        drawRoundRect(
                            color = Color(0xDD0B0F19),
                            topLeft = badgeTopLeft.copy(x = badgeTopLeft.x - 6f, y = badgeTopLeft.y - 2f),
                            size = Size(
                                badgeLayout.size.width + 12f,
                                badgeLayout.size.height + 4f
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                        )

                        drawText(
                            textLayoutResult = badgeLayout,
                            topLeft = badgeTopLeft
                        )

                        // G) DESENHAR A LUA PERMANENTE NA MAQUETE
                        if (selectedTarget.id != CelestialTarget.TargetMoon.id) {
                            val mAlt = currentMoonPosition.altitudeDeg
                            val mAz = currentMoonPosition.azimuthDeg
                            val mIsAbove = mAlt > 0.0

                            val (mX, mY, mZ) = altAzTo3D(mAlt, mAz)
                            val projMoon = project3D(mX, mY, mZ)

                            val (mGroundX, mGroundY, mGroundZ) = altAzTo3D(0.0, mAz)
                            val projMoonGround = project3D(mGroundX, mGroundY, mGroundZ)

                            // Haste pontilhada para o chão da maquete
                            drawLine(
                                color = Color(0xFFCBD5E1).copy(alpha = 0.45f),
                                start = Offset(projMoon.first, projMoon.second),
                                end = Offset(projMoonGround.first, projMoonGround.second),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                            )

                            // Retículo no chão da maquete
                            drawCircle(
                                color = Color(0xFF94A3B8).copy(alpha = 0.35f),
                                radius = 5.5f,
                                center = Offset(projMoonGround.first, projMoonGround.second)
                            )
                            drawCircle(
                                color = Color(0xFFE2E8F0),
                                radius = 2.5f,
                                center = Offset(projMoonGround.first, projMoonGround.second)
                            )

                            // Halo suave da Lua
                            val moonCenter = Offset(projMoon.first, projMoon.second)
                            drawCircle(
                                color = Color(0xFFE2E8F0).copy(alpha = 0.22f),
                                radius = 16f * pulseScale,
                                center = moonCenter
                            )

                            // Miniatura com Fase Real da Lua
                            drawMoonPhaseMiniature(
                                center = moonCenter,
                                sizePx = 25f,
                                illuminationPercent = currentMoonPosition.illuminationPercent,
                                isAboveHorizon = mIsAbove
                            )

                            // Linha de Conjunção quando muito próximos (< 15°)
                            if (lunarSeparationDeg < 15.0 && curr.isAboveHorizon && mIsAbove) {
                                drawLine(
                                    color = Color(0xFFA855F7).copy(alpha = 0.65f),
                                    start = Offset(projCurr.first, projCurr.second),
                                    end = Offset(projMoon.first, projMoon.second),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                                )
                            }

                            // Rótulo da Lua
                            val moonBadgeText = "🌙 LUA (${String.format("%.1f", mAlt)}°)"
                            val moonBadgeLayout = textMeasurer.measure(
                                text = moonBadgeText,
                                style = TextStyle(
                                    color = if (mIsAbove) Color(0xFFF1F5F9) else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            val moonBadgeTopLeft = Offset(
                                projMoon.first - moonBadgeLayout.size.width / 2f,
                                projMoon.second - 28f
                            )

                            drawRoundRect(
                                color = Color(0xDD0B0F19),
                                topLeft = moonBadgeTopLeft.copy(x = moonBadgeTopLeft.x - 5f, y = moonBadgeTopLeft.y - 2f),
                                size = Size(
                                    moonBadgeLayout.size.width + 10f,
                                    moonBadgeLayout.size.height + 4f
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )

                            drawText(
                                textLayoutResult = moonBadgeLayout,
                                topLeft = moonBadgeTopLeft
                            )
                        }

                        // H) DESENHAR O MARCADOR DO POLO CELESTIAL QUANDO ATIVADO VIA BOTÃO "VER NO MAPA"
                        if (viewModel.showCelestialPoleMarker) {
                            val scpAz = if (lat < 0) 180.0 else 0.0
                            val scpAlt = kotlin.math.abs(lat)
                            val scpName = if (lat < 0) "Polo Celestial Sul" else "Polo Celestial Norte"

                            val (p3X, p3Y, p3Z) = altAzTo3D(scpAlt, scpAz)
                            val projPole = project3D(p3X, p3Y, p3Z)

                            val (gX, gY, gZ) = altAzTo3D(0.0, scpAz)
                            val projGround = project3D(gX, gY, gZ)

                            val px = projPole.first
                            val py = projPole.second
                            val gx = projGround.first
                            val gy = projGround.second

                            // Linha/indicador discreto ligando o ponto ao horizonte
                            drawLine(
                                color = Color(0xFFFACC15).copy(alpha = 0.75f),
                                start = Offset(px, py),
                                end = Offset(gx, gy),
                                strokeWidth = 1.8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )

                            // Ponto de fixação no horizonte
                            drawCircle(
                                color = Color(0xFFFACC15).copy(alpha = 0.6f),
                                radius = 4f,
                                center = Offset(gx, gy)
                            )

                            // Brilho suave em volta do X
                            drawCircle(
                                color = Color(0xFFFACC15).copy(alpha = 0.25f),
                                radius = 14f,
                                center = Offset(px, py)
                            )

                            // Marcador "X" Amarelo Elegante
                            val xSize = 7.5f
                            drawLine(
                                color = Color(0xFFFACC15),
                                start = Offset(px - xSize, py - xSize),
                                end = Offset(px + xSize, py + xSize),
                                strokeWidth = 2.8f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFFFACC15),
                                start = Offset(px - xSize, py + xSize),
                                end = Offset(px + xSize, py - xSize),
                                strokeWidth = 2.8f,
                                cap = StrokeCap.Round
                            )

                            // Etiqueta com Nome e Altura do Alvo do Eixo Polar
                            val poleBadgeText = "Polo celestial - Alvo do eixo • Alt. ${scpAlt.roundToInt()}°"
                            val poleBadgeLayout = textMeasurer.measure(
                                text = poleBadgeText,
                                style = TextStyle(
                                    color = Color(0xFFFEF08A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            val poleBadgeTopLeft = Offset(
                                px - poleBadgeLayout.size.width / 2f,
                                py - 28f
                            )

                            drawRoundRect(
                                color = Color(0xEE1E1B28),
                                topLeft = poleBadgeTopLeft.copy(x = poleBadgeTopLeft.x - 6f, y = poleBadgeTopLeft.y - 2f),
                                size = Size(
                                    poleBadgeLayout.size.width + 12f,
                                    poleBadgeLayout.size.height + 4f
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )

                            drawRoundRect(
                                color = Color(0xFFFACC15),
                                topLeft = poleBadgeTopLeft.copy(x = poleBadgeTopLeft.x - 6f, y = poleBadgeTopLeft.y - 2f),
                                size = Size(
                                    poleBadgeLayout.size.width + 12f,
                                    poleBadgeLayout.size.height + 4f
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                                style = Stroke(width = 1f)
                            )

                            drawText(
                                textLayoutResult = poleBadgeLayout,
                                topLeft = poleBadgeTopLeft
                            )
                        }
                    }
                }

                // Distintivo Superior de Iluminação do Céu
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 10.dp, start = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp)),
                    color = Color(0xDD0B0F19)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = skyPhaseIcon, fontSize = 12.sp)
                        Text(
                            text = "$skyPhaseText (${String.format("%.1f", sunAlt)}°)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Distintivo Superior Direito: Separação Angular da Lua
                if (selectedTarget.id != CelestialTarget.TargetMoon.id) {
                    val isConjunction = lunarSeparationDeg < 8.0
                    val badgeBg = if (isConjunction) Color(0xEE2E1065) else Color(0xDD0B0F19)
                    val badgeBorder = if (isConjunction) Color(0xFFA855F7) else SpaceBorder
                    val iconStr = if (isConjunction) "✨" else "🌙"
                    val sepText = if (isConjunction) {
                        "Conjunção! (${String.format("%.1f", lunarSeparationDeg)}°)"
                    } else {
                        "Sep. Lunar: ${String.format("%.1f", lunarSeparationDeg)}°"
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, badgeBorder, RoundedCornerShape(14.dp)),
                        color = badgeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = iconStr, fontSize = 12.sp)
                            Text(
                                text = sepText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isConjunction) Color(0xFFE9D5FF) else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Banner Inferior de Status do Astro
                val curr = trajectoryArc.currentPoint
                val statusText = if (curr.isAboveHorizon) {
                    "🟢 ${selectedTarget.name} visível no céu • Alt: ${String.format("%.1f", curr.altitudeDeg)}° • Az: ${String.format("%.0f", curr.azimuthDeg)}° (${AstronomyEngine.convertAzimuthToDirection(curr.azimuthDeg)})"
                } else {
                    val riseStr = trajectoryArc.risePoint?.timeStr ?: "--:--"
                    val riseAzStr = trajectoryArc.risePoint?.let { "${String.format("%.0f", it.azimuthDeg)}°" } ?: ""
                    "🌅 ${selectedTarget.name} está abaixo do horizonte • Próximo nascer: $riseStr $riseAzStr"
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp, start = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp)),
                    color = Color(0xEE0B0F19)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (curr.isAboveHorizon) CosmicPurplePrimary else Color(0xFFF97316),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 3.1 LEGENDA EDUCATIVA FIXA DA ALTITUDE NO CÉU
            AltitudeLegendSection(
                modifier = Modifier.fillMaxWidth()
            )

            // 4. BARRA PRINCIPAL DE CONTROLE INTERATIVO DE HORÁRIO (SLIDER -12h a +12h)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CosmicPurpleContainer.copy(alpha = 0.22f))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cabeçalho do Horário Selecionado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🕒 BARRA DE HORÁRIO: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        val currTimeStr = trajectoryArc.currentPoint.timeStr
                        val offsetLabel = when {
                            timeOffsetMinutes > 0.5f -> "+${String.format("%.1f", timeOffsetMinutes / 60f)}h"
                            timeOffsetMinutes < -0.5f -> "${String.format("%.1f", timeOffsetMinutes / 60f)}h"
                            else -> "Agora"
                        }
                        Text(
                            text = "$currTimeStr ($offsetLabel)",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Reset AGORA
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.resetToCurrentTime()
                                isPlayingAnim = false
                            }
                            .border(1.dp, CosmicPurplePrimary, RoundedCornerShape(12.dp)),
                        color = CosmicPurplePrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "AGORA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // SLIDER INTERATIVO PRINCIPAL (-12h a +12h)
                Slider(
                    value = timeOffsetMinutes,
                    onValueChange = { newOffset ->
                        viewModel.setTimeOffsetFromNowMinutes(newOffset.toDouble())
                    },
                    valueRange = -720f..720f,
                    colors = SliderDefaults.colors(
                        thumbColor = CosmicPurplePrimary,
                        activeTrackColor = CosmicPurplePrimary,
                        inactiveTrackColor = CosmicPurpleContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Botões de Salto Rápido (-12h, -1h, -15m, AGORA, +15m, +1h, +12h)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "-12h" to -720,
                        "-6h" to -360,
                        "-1h" to -60,
                        "-15m" to -15,
                        "Agora" to 0,
                        "+15m" to 15,
                        "+1h" to 60,
                        "+6h" to 360,
                        "+12h" to 720
                    ).forEach { (label, deltaMins) ->
                        val isNow = deltaMins == 0
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isNow) {
                                        viewModel.resetToCurrentTime()
                                        isPlayingAnim = false
                                    } else {
                                        viewModel.updateTimeOffset(deltaMins)
                                    }
                                }
                                .border(
                                    1.dp,
                                    if (isNow) CosmicPurplePrimary else SpaceBorder,
                                    RoundedCornerShape(14.dp)
                                ),
                            color = if (isNow) CosmicPurplePrimary.copy(alpha = 0.25f) else SpaceCardSurface
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isNow) TextPrimary else TextSecondary,
                                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Reprodução secundária ▶ Play / Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { isPlayingAnim = !isPlayingAnim },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CosmicPurplePrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlayingAnim) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingAnim) "Pausar" else "Reproduzir Animação",
                                tint = CosmicPurpleOnPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = if (isPlayingAnim) "REPRODUZINDO..." else "REPRODUZIR",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Seletor de Velocidades (1x, 10x, 100x, 1000x)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1f, 10f, 100f, 1000f).forEach { speed ->
                            val isSelected = animSpeed == speed
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { animSpeed = speed }
                                    .border(
                                        1.dp,
                                        if (isSelected) CosmicPurplePrimary else SpaceBorder,
                                        RoundedCornerShape(12.dp)
                                    ),
                                color = if (isSelected) CosmicPurplePrimary.copy(alpha = 0.2f) else SpaceCardSurface
                            ) {
                                Text(
                                    text = "${speed.toInt()}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. RESUMO DE LANDMARKS (Nasce, Culmina, Se Põe, Visibilidade)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicPurpleContainer.copy(alpha = 0.25f))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // NASCE
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌅 NASCE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = trajectoryArc.risePoint?.timeStr ?: "--:--",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trajectoryArc.risePoint?.let { "${String.format("%.0f", it.azimuthDeg)}° ${AstronomyEngine.convertAzimuthToDirection(it.azimuthDeg)}" } ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                // CULMINA
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⬆️ CULMINA", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = trajectoryArc.culminationPoint?.timeStr ?: "--:--",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Alt ${String.format("%.0f", trajectoryArc.maxAltitudeDeg)}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                // SE PÔE
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌇 SE PÔE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = trajectoryArc.setPoint?.timeStr ?: "--:--",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trajectoryArc.setPoint?.let { "${String.format("%.0f", it.azimuthDeg)}° ${AstronomyEngine.convertAzimuthToDirection(it.azimuthDeg)}" } ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                // VISÍVEL
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⏱️ VISÍVEL", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "${String.format("%.1f", trajectoryArc.totalHoursVisible)}h",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "no dia",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // 6. AVISO PERMANENTE DE SEGURANÇA PARA O SOL
            if (selectedTarget.id == CelestialTarget.TargetSun.id) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1515))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "☀️ ATENÇÃO",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nunca observe ou aponte telescópios, binóculos ou outros instrumentos ópticos diretamente para o Sol sem equipamento solar apropriado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 7. AVISO DE SEGURANÇA DIURNO PARA A LUA
            if (selectedTarget.id == CelestialTarget.TargetMoon.id) {
                val sunPos = AstronomyEngine.calculateSunPosition(calendar, lat, lon)
                val isDaytime = sunPos.altitudeDeg > -2.0
                val angularSeparationDeg = AstronomyEngine.calculateMoonSunAngularSeparationDeg(calendar, lat, lon)

                if (isDaytime) {
                    if (angularSeparationDeg < AstronomyEngine.MIN_SAFE_SOLAR_ELONGATION_DEG) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1515))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🌙 ALERTA DE SEGURANÇA SOLAR",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🌙 A Lua está próxima demais do Sol (${String.format("%.1f", angularSeparationDeg)}° < ${AstronomyEngine.MIN_SAFE_SOLAR_ELONGATION_DEG.toInt()}°) para ser recomendada como alvo de observação neste momento.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFCA5A5),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "☀️ ATENÇÃO AO OBSERVAR A LUA DURANTE O DIA",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "A Lua pode ser observada durante o dia, mas nunca aponte binóculos ou telescópios para uma região próxima ao Sol.\n\nNunca olhe para o Sol através de instrumentos ópticos sem proteção solar apropriada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renderiza uma miniatura 3D/vetorial detalhada e característica do astro selecionado.
 */
private fun DrawScope.drawCelestialMiniature(
    target: CelestialTarget,
    center: Offset,
    sizePx: Float,
    isAboveHorizon: Boolean
) {
    val alpha = if (isAboveHorizon) 1.0f else 0.45f
    val r = sizePx / 2f

    when (target) {
        is CelestialTarget.TargetPlanet -> {
            when (target.planet) {
                Planet.SATURN -> {
                    // Saturno: Globo Amarelado + Anéis 3D Inclinados
                    val ringPathBack = Path().apply {
                        addOval(Rect(center.x - r * 1.7f, center.y - r * 0.55f, center.x + r * 1.7f, center.y + r * 0.55f))
                    }
                    drawPath(
                        path = ringPathBack,
                        color = Color(0xFFCA8A04).copy(alpha = 0.8f * alpha),
                        style = Stroke(width = 4f)
                    )

                    // Globo de Saturno
                    drawCircle(color = Color(0xFFEAB308).copy(alpha = alpha), radius = r * 0.8f, center = center)
                    drawCircle(color = Color(0xFFFEF08A).copy(alpha = 0.6f * alpha), radius = r * 0.5f, center = center.copy(x = center.x - r * 0.2f, y = center.y - r * 0.2f))

                    // Anel Frontal com Sombra na Equador
                    val ringPathFront = Path().apply {
                        addArc(
                            Rect(center.x - r * 1.7f, center.y - r * 0.55f, center.x + r * 1.7f, center.y + r * 0.55f),
                            0f, 180f
                        )
                    }
                    drawPath(
                        path = ringPathFront,
                        color = Color(0xFFFDE047).copy(alpha = alpha),
                        style = Stroke(width = 4.5f)
                    )
                }

                Planet.JUPITER -> {
                    // Júpiter: Faixas de Nuvens + Grande Mancha Vermelha
                    drawCircle(color = Color(0xFFF97316).copy(alpha = alpha), radius = r, center = center)

                    // Faixa 1 de Nuvens
                    drawRect(
                        color = Color(0xFFC2410C).copy(alpha = alpha),
                        topLeft = Offset(center.x - r * 0.9f, center.y - r * 0.35f),
                        size = Size(r * 1.8f, r * 0.25f)
                    )

                    // Faixa 2 de Nuvens
                    drawRect(
                        color = Color(0xFF7C2D12).copy(alpha = alpha),
                        topLeft = Offset(center.x - r * 0.85f, center.y + r * 0.15f),
                        size = Size(r * 1.7f, r * 0.22f)
                    )

                    // Grande Mancha Vermelha (Great Red Spot)
                    drawOval(
                        color = Color(0xFFDC2626).copy(alpha = alpha),
                        topLeft = Offset(center.x + r * 0.2f, center.y + r * 0.15f),
                        size = Size(r * 0.45f, r * 0.3f)
                    )
                }

                Planet.MARS -> {
                    // Marte: Planeta Ferrugem + Calota Polar de Gelo Branca
                    drawCircle(color = Color(0xFFEF4444).copy(alpha = alpha), radius = r * 0.9f, center = center)

                    // Mancha Escura de Superfície (Maria/Syrtis Major)
                    drawCircle(color = Color(0xFF991B1B).copy(alpha = alpha), radius = r * 0.4f, center = Offset(center.x - r * 0.1f, center.y + r * 0.1f))

                    // Calota Polar de Gelo
                    drawCircle(color = Color.White.copy(alpha = alpha), radius = r * 0.28f, center = Offset(center.x, center.y - r * 0.65f))
                }

                Planet.MERCURY -> {
                    drawCircle(color = Color(0xFFA8A29E).copy(alpha = alpha), radius = r * 0.7f, center = center)
                    drawCircle(color = Color(0xFF57534E).copy(alpha = alpha), radius = r * 0.25f, center = Offset(center.x - r * 0.15f, center.y + r * 0.15f))
                }

                Planet.VENUS -> {
                    drawCircle(color = Color(0xFFFEF08A).copy(alpha = alpha), radius = r * 0.85f, center = center)
                    drawCircle(color = Color.White.copy(alpha = 0.5f * alpha), radius = r * 0.5f, center = Offset(center.x - r * 0.2f, center.y - r * 0.2f))
                }

                Planet.URANUS -> {
                    drawCircle(color = Color(0xFF22D3EE).copy(alpha = alpha), radius = r * 0.85f, center = center)
                    drawCircle(color = Color(0xFF06B6D4).copy(alpha = 0.5f * alpha), radius = r * 0.4f, center = Offset(center.x + r * 0.15f, center.y + r * 0.15f))
                }

                Planet.NEPTUNE -> {
                    drawCircle(color = Color(0xFF3B82F6).copy(alpha = alpha), radius = r * 0.85f, center = center)
                    drawCircle(color = Color(0xFF1D4ED8).copy(alpha = 0.6f * alpha), radius = r * 0.4f, center = Offset(center.x - r * 0.15f, center.y - r * 0.15f))
                }
            }
        }

        CelestialTarget.TargetSun -> drawSunMiniature(center, r, alpha)

        CelestialTarget.TargetMoon -> {
            drawMoonPhaseMiniature(
                center = center,
                sizePx = sizePx,
                illuminationPercent = 85,
                isAboveHorizon = isAboveHorizon
            )
        }

        is CelestialTarget.TargetStar -> {
            // Estrela: Spikes de Difração e Núcleo Brilhante
            val spikeColor = Color(0xFFFDE047).copy(alpha = alpha)
            drawLine(color = spikeColor, start = Offset(center.x - r * 1.4f, center.y), end = Offset(center.x + r * 1.4f, center.y), strokeWidth = 2.5f)
            drawLine(color = spikeColor, start = Offset(center.x, center.y - r * 1.4f), end = Offset(center.x, center.y + r * 1.4f), strokeWidth = 2.5f)
            drawCircle(color = Color.White.copy(alpha = alpha), radius = r * 0.6f, center = center)
        }

        is CelestialTarget.TargetDso -> {
            // Galáxia/Nebulosa: Espiral ou Nuvem Púrpura
            drawCircle(color = Color(0xFFC084FC).copy(alpha = 0.5f * alpha), radius = r * 1.1f, center = center)
            drawCircle(color = Color(0xFFA855F7).copy(alpha = 0.8f * alpha), radius = r * 0.6f, center = center)
            drawCircle(color = Color.White.copy(alpha = alpha), radius = r * 0.3f, center = center)
        }
    }
}

private fun DrawScope.drawSunMiniature(center: Offset, r: Float, alpha: Float) {
    // Halo Flare do Sol
    drawCircle(color = Color(0xFFFDE047).copy(alpha = 0.35f * alpha), radius = r * 1.4f, center = center)
    // Sol central
    drawCircle(color = Color(0xFFFACC15).copy(alpha = alpha), radius = r * 0.95f, center = center)
    drawCircle(color = Color.White.copy(alpha = 0.85f * alpha), radius = r * 0.5f, center = center)
}

private fun DrawScope.drawMoonPhaseMiniature(
    center: Offset,
    sizePx: Float,
    illuminationPercent: Int,
    isAboveHorizon: Boolean
) {
    val alpha = if (isAboveHorizon) 1.0f else 0.45f
    val r = sizePx / 2f

    // Disco escuro base da Lua
    drawCircle(color = Color(0xFF334155).copy(alpha = alpha), radius = r, center = center)

    // Lado Iluminado
    val illumRatio = (illuminationPercent / 100f).coerceIn(0f, 1f)
    if (illumRatio > 0.05f) {
        if (illumRatio >= 0.95f) {
            // Lua Cheia
            drawCircle(color = Color(0xFFF8FAFC).copy(alpha = alpha), radius = r * 0.95f, center = center)
        } else {
            // Lua Crescente / Minguante
            drawCircle(
                color = Color(0xFFF1F5F9).copy(alpha = alpha),
                radius = r * (0.35f + 0.6f * illumRatio),
                center = center.copy(x = center.x + r * (0.5f - illumRatio) * 0.4f)
            )
        }
    }

    // Detalhes de crateras na superfície
    drawCircle(color = Color(0xFF64748B).copy(alpha = 0.5f * alpha), radius = r * 0.25f, center = Offset(center.x - r * 0.2f, center.y - r * 0.15f))
    drawCircle(color = Color(0xFF475569).copy(alpha = 0.4f * alpha), radius = r * 0.2f, center = Offset(center.x + r * 0.25f, center.y + r * 0.2f))
}

/**
 * Função para calcular a trajetória aparente astronômica de 24h para o astro selecionado.
 */
private fun calculateTrajectoryArc(
    target: CelestialTarget,
    currentCalendar: Calendar,
    lat: Double,
    lon: Double
): TrajectoryArc {
    val sampleCalendar = currentCalendar.clone() as Calendar
    sampleCalendar.set(Calendar.HOUR_OF_DAY, 0)
    sampleCalendar.set(Calendar.MINUTE, 0)
    sampleCalendar.set(Calendar.SECOND, 0)
    sampleCalendar.set(Calendar.MILLISECOND, 0)

    val points = mutableListOf<TrajectoryPoint>()

    var risePoint: TrajectoryPoint? = null
    var setPoint: TrajectoryPoint? = null
    var maxAlt = -90.0
    var culminationPoint: TrajectoryPoint? = null

    val currentMinuteOfDay = currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(Calendar.MINUTE)
    var currentPoint: TrajectoryPoint? = null

    var prevAlt = Double.NaN

    // Amostragem de 24h (96 pontos a cada 15 min)
    for (m in 0..1440 step 15) {
        val h = min(m / 60, 23)
        val minVal = m % 60
        sampleCalendar.set(Calendar.HOUR_OF_DAY, h)
        sampleCalendar.set(Calendar.MINUTE, minVal)

        val (alt, az) = getTargetAltitudeAzimuth(target, sampleCalendar, lat, lon)
        val isAbove = alt >= 0.0
        val timeStr = String.format("%02d:%02d", h, minVal)

        val pt = TrajectoryPoint(
            minuteOfDay = m,
            timeStr = timeStr,
            altitudeDeg = alt,
            azimuthDeg = az,
            isAboveHorizon = isAbove
        )
        points.add(pt)

        if (alt > maxAlt) {
            maxAlt = alt
            culminationPoint = pt
        }

        if (!prevAlt.isNaN()) {
            if (prevAlt < 0.0 && alt >= 0.0 && risePoint == null) {
                risePoint = pt
            }

            if (prevAlt >= 0.0 && alt < 0.0 && setPoint == null) {
                setPoint = pt
            }
        }

        prevAlt = alt
    }

    // Posição no momento atual exato
    val (currAlt, currAz) = getTargetAltitudeAzimuth(target, currentCalendar, lat, lon)
    val currTimeStr = String.format(
        "%02d:%02d",
        currentCalendar.get(Calendar.HOUR_OF_DAY),
        currentCalendar.get(Calendar.MINUTE)
    )

    currentPoint = TrajectoryPoint(
        minuteOfDay = currentMinuteOfDay,
        timeStr = currTimeStr,
        altitudeDeg = currAlt,
        azimuthDeg = currAz,
        isAboveHorizon = currAlt >= 0.0
    )

    val visibleCount = points.count { it.isAboveHorizon }
    val totalHoursVisible = visibleCount * 0.25

    return TrajectoryArc(
        target = target,
        points = points,
        risePoint = risePoint,
        culminationPoint = culminationPoint,
        setPoint = setPoint,
        maxAltitudeDeg = maxAlt,
        currentPoint = currentPoint,
        totalHoursVisible = totalHoursVisible
    )
}

/**
 * Auxiliar para obter (Altitude, Azimute) usando AstronomyEngine.
 */
private fun getTargetAltitudeAzimuth(
    target: CelestialTarget,
    calendar: Calendar,
    lat: Double,
    lon: Double
): Pair<Double, Double> {
    return when (target) {
        is CelestialTarget.TargetPlanet -> {
            val obs = AstronomyEngine.calculatePosition(target.planet, calendar, lat, lon)
            Pair(obs.altitudeDeg, obs.azimuthDeg)
        }
        CelestialTarget.TargetSun -> {
            val sun = AstronomyEngine.calculateSunPosition(calendar, lat, lon)
            Pair(sun.altitudeDeg, sun.azimuthDeg)
        }
        CelestialTarget.TargetMoon -> {
            val moon = AstronomyEngine.calculateMoonPosition(calendar, lat, lon)
            Pair(moon.altitudeDeg, moon.azimuthDeg)
        }
        is CelestialTarget.TargetStar -> {
            val eq = AstronomyEngine.calculateEquatorialPosition(target.star.raDeg, target.star.decDeg, calendar, lat, lon)
            Pair(eq.altitudeDeg, eq.azimuthDeg)
        }
        is CelestialTarget.TargetDso -> {
            val eq = AstronomyEngine.calculateEquatorialPosition(target.dso.raDeg, target.dso.declinationDeg, calendar, lat, lon)
            Pair(eq.altitudeDeg, eq.azimuthDeg)
        }
    }
}

/**
 * Seção educativa FIXA com a legenda e escala de altitude no céu (EXPANSÍVEL / RECOLHIDA POR PADRÃO).
 */
@Composable
private fun AltitudeLegendSection(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CosmicPurpleContainer.copy(alpha = 0.22f))
            .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp))
            .animateContentSize()
    ) {
        // TÍTULO COMPACTO (CABEÇALHO CLICÁVEL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "📐", fontSize = 13.sp)
                Text(
                    text = "ENTENDA A ALTITUDE NO CÉU",
                    style = MaterialTheme.typography.labelMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Informações sobre altitude",
                    tint = CosmicPurplePrimary,
                    modifier = Modifier.size(15.dp)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // CONTEÚDO EXPANDIDO (APENAS QUANDO EXPANDIDO)
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ESCALA VERTICAL DE ALTITUDE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Desenho gráfico da régua vertical (Seta no Zênite 90° e Base no Horizonte 0°)
                    Canvas(
                        modifier = Modifier
                            .width(18.dp)
                            .height(132.dp)
                    ) {
                        val startY = 12f
                        val endY = size.height - 8f
                        val strokeW = 3f

                        // Linha vertical principal
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(size.width / 2f, startY),
                            end = Offset(size.width / 2f, endY),
                            strokeWidth = strokeW
                        )

                        // Seta superior apontando para o Zênite
                        val arrowSize = 5f
                        drawPath(
                            path = Path().apply {
                                moveTo(size.width / 2f, startY - 6f)
                                lineTo(size.width / 2f - arrowSize, startY + arrowSize)
                                lineTo(size.width / 2f + arrowSize, startY + arrowSize)
                                close()
                            },
                            color = Color(0xFF38BDF8)
                        )

                        // Traço horizontal da base no Horizonte
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(2f, endY),
                            end = Offset(size.width - 2f, endY),
                            strokeWidth = strokeW
                        )

                        // Marcas/Pontos nos níveis (90°, 70°, 45°, 30°, 10°, 0°)
                        val fractions = listOf(0.0f, 0.22f, 0.50f, 0.67f, 0.89f, 1.0f)
                        fractions.forEach { f ->
                            val y = startY + (endY - startY) * f
                            drawCircle(
                                color = Color.White,
                                radius = 2.5f,
                                center = Offset(size.width / 2f, y)
                            )
                        }
                    }

                    // Descrições dos níveis de altitude
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        AltitudeLevelItem(deg = "90°", label = "ZÊNITE", desc = "Exatamente acima da sua cabeça", isHighlight = true)
                        AltitudeLevelItem(deg = "70°", label = "Muito alto no céu")
                        AltitudeLevelItem(deg = "45°", label = "Altura intermediária")
                        AltitudeLevelItem(deg = "30°", label = "Mais próximo do horizonte")
                        AltitudeLevelItem(deg = "10°", label = "Muito baixo no céu")
                        AltitudeLevelItem(deg = "0°", label = "HORIZONTE", desc = "", isHighlight = true)
                    }
                }

                // NOTA ILUSTRATIVA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33000000))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "💡", fontSize = 11.sp)
                    Text(
                        text = "Quanto menor a altitude, mais próximo do horizonte o astro estará.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // EXPLICAÇÃO DA CULMINAÇÃO
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicPurpleContainer.copy(alpha = 0.35f))
                        .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Culminação é a maior altitude que o astro alcança no céu naquele percurso. Ela não significa necessariamente que ele passará pelo zênite.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AltitudeLevelItem(
    deg: String,
    label: String,
    desc: String = "",
    isHighlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Text(
            text = deg,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlight) Color(0xFF38BDF8) else TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.width(26.dp)
        )
        Text(
            text = "—",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontSize = 10.sp
        )
        Text(
            text = if (desc.isNotEmpty()) "$label ($desc)" else label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isHighlight) TextPrimary else TextSecondary,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp
        )
    }
}
