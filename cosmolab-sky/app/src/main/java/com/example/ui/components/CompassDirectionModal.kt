package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.astronomy.AstronomyEngine
import com.example.astronomy.Planet
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.TextMuted
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class AstroReference(
    val id: String,
    val name: String,
    val icon: String,
    val azimuthGeo: Double,
    val altitudeDeg: Double,
    val cardinal: String,
    val reason: String,
    val isBest: Boolean = false,
    val score: Int = 0
)

@Composable
fun CompassDirectionModal(
    targetName: String,
    targetAzimuthGeo: Double,
    targetAltitudeDeg: Double,
    directionLabel: String,
    latitude: Double,
    longitude: Double,
    calendar: Calendar = Calendar.getInstance(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    // Calculate magnetic declination for location
    val magDeclination = remember(latitude, longitude) {
        AstronomyEngine.calculateMagneticDeclination(latitude, longitude)
    }

    // Compute sky references available at this exact date & time
    val astroReferences = remember(latitude, longitude, calendar) {
        computeAstroReferences(calendar, latitude, longitude)
    }

    var selectedRefId by remember {
        mutableStateOf(astroReferences.firstOrNull { it.isBest }?.id ?: astroReferences.firstOrNull()?.id)
    }

    val selectedReference = remember(selectedRefId, astroReferences) {
        astroReferences.firstOrNull { it.id == selectedRefId }
    }

    // Calculate solar proximity for safety warning
    val solarProximity = remember(targetAltitudeDeg, targetAzimuthGeo, latitude, longitude, calendar) {
        try {
            AstronomyEngine.calculateSolarProximityFromAltAz(
                targetAltDeg = targetAltitudeDeg,
                targetAzDeg = targetAzimuthGeo,
                calendar = calendar,
                latitude = latitude,
                longitude = longitude,
                targetName = targetName
            )
        } catch (_: Exception) {
            null
        }
    }

    var sensorAzimuth by remember { mutableStateOf<Float?>(null) }
    var manualHeadingOffset by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Sensor registration
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            hasSensor = false
            onDispose { }
        } else {
            val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            var lastAzimuth = 0f

            val listener = object : SensorEventListener {
                private val rotationMatrix = FloatArray(9)
                private val orientationAngles = FloatArray(3)
                private var accelValues: FloatArray? = null
                private var magValues: FloatArray? = null

                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        var az = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        if (az < 0) az += 360f

                        val smoothed = smoothAngle(lastAzimuth, az, 0.15f)
                        lastAzimuth = smoothed
                        sensorAzimuth = smoothed
                    } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        accelValues = event.values.clone()
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        magValues = event.values.clone()
                    }

                    if (rotationSensor == null && accelValues != null && magValues != null) {
                        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)) {
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            var az = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                            if (az < 0) az += 360f
                            val smoothed = smoothAngle(lastAzimuth, az, 0.15f)
                            lastAzimuth = smoothed
                            sensorAzimuth = smoothed
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            var registered = false
            if (rotationSensor != null) {
                registered = sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            }
            if (!registered && accelSensor != null && magSensor != null) {
                val r1 = sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                val r2 = sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
                registered = r1 && r2
            }

            if (!registered && rotationSensor == null) {
                hasSensor = false
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    // True heading of device (applying magnetic declination to sensor reading)
    val currentHeadingTrue = remember(sensorAzimuth, manualHeadingOffset, magDeclination) {
        if (sensorAzimuth != null) {
            (sensorAzimuth!!.toDouble() + magDeclination + 360.0) % 360.0
        } else {
            (manualHeadingOffset.toDouble() + 360.0) % 360.0
        }
    }

    // Relative angle between device top and target geographic bearing
    // Delta = TargetGeo - DeviceHeadingTrue
    val headingDelta = remember(currentHeadingTrue, targetAzimuthGeo) {
        var d = (targetAzimuthGeo - currentHeadingTrue + 360.0) % 360.0
        if (d > 180.0) d -= 360.0
        d
    }

    val absDelta = abs(headingDelta)
    val isAligned = absDelta <= 3.5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceBackground.copy(alpha = 0.98f))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BÚSSOLA ASTRONÔMICA",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = targetName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF231E33))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White
                        )
                    }
                }

                // Info banner (Azimuth & Altitude of Target)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF161224))
                        .border(1.dp, Color(0xFF3B2F5C), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎯 AZIMUTE DO ALVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", targetAzimuthGeo)}°",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFEF08A),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = directionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = Color(0xFF32284D)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⬆ ALTITUDE DO ALVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", targetAltitudeDeg)}°",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (targetAltitudeDeg >= 0) "Acima do horizonte" else "Abaixo do horizonte",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Solar proximity warning card
                if (solarProximity != null && solarProximity.showAlert) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF261908))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "☀️", fontSize = 16.sp)
                                Text(
                                    text = "ALERTA DE PROXIMIDADE SOLAR (${solarProximity.angularSeparationDeg.toInt()}°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "O Sol está acima do horizonte e a apenas ${String.format(Locale.US, "%.1f", solarProximity.angularSeparationDeg)}° deste alvo. NUNCA aponte telescópios ou binóculos nesta direção sem filtro solar adequado!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFEF3C7),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Sensor warning banner if missing or manual mode
                if (!hasSensor || sensorAzimuth == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C1919))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⚠️ Seu dispositivo não possui sensor de orientação.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🧭 ORIENTAÇÃO MANUAL: Escolha uma referência no céu abaixo e use-a para orientar a bússola arrastando com o dedo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Sky References Selector Section
                if (astroReferences.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF181329))
                            .border(1.dp, Color(0xFF382C5A), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🧭 ESCOLHA UMA REFERÊNCIA PARA ORIENTAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CosmicPurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            astroReferences.forEach { ref ->
                                val isSelected = ref.id == selectedRefId
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) Color(0xFF2B2048) else Color(0xFF120E20)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF271F3B),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedRefId = ref.id }
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedRefId = ref.id },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Color(0xFF38BDF8),
                                                    unselectedColor = Color(0xFF64748B)
                                                ),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${ref.icon} ${ref.name}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    if (ref.isBest) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "✨ MELHOR REFERÊNCIA",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color(0xFFFDE047),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.sp
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Az. ${ref.azimuthGeo.roundToInt()}° · Alt. ${ref.altitudeDeg.roundToInt()}° · ${ref.cardinal}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Reference vs Target Relationship Banner
                selectedReference?.let { ref ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF131B2E))
                            .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⭐ REFERÊNCIA PARA ORIENTAR: ${ref.name} — ${ref.azimuthGeo.roundToInt()}° (${ref.cardinal})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "↓ Procure ${ref.name} no céu e alinhe a bússola para achar o alvo abaixo",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "🎯 ALVO A ENCONTRAR: ${targetAzimuthGeo.roundToInt()}° (${directionLabel})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFEF08A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // COMPASS CANVAS WITH FULL 360° DEGREE SCALE
                Box(
                    modifier = Modifier
                        .size(310.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                if (sensorAzimuth == null) {
                                    manualHeadingOffset = (manualHeadingOffset - dragAmount.x * 0.4f + 360f) % 360f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 22.dp.toPx()

                        // Outer ring background
                        drawCircle(
                            color = Color(0xFF18132A),
                            radius = radius + 12.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF332952),
                            radius = radius + 12.dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF0F0C1B),
                            radius = radius,
                            center = center
                        )

                        // Rotate dial according to true device heading
                        rotate(-currentHeadingTrue.toFloat(), pivot = center) {
                            // FULL 360 DEGREE SCALE TICKS (0° to 359°)
                            for (angle in 0 until 360) {
                                val rad = Math.toRadians(angle.toDouble())
                                val is10Deg = angle % 10 == 0
                                val is5Deg = angle % 5 == 0

                                val tickLength = when {
                                    is10Deg -> 12.dp.toPx()
                                    is5Deg -> 7.dp.toPx()
                                    else -> 3.5.dp.toPx()
                                }

                                val strokeW = when {
                                    is10Deg -> 2.0.dp.toPx()
                                    is5Deg -> 1.2.dp.toPx()
                                    else -> 0.8.dp.toPx()
                                }

                                val tickColor = when (angle) {
                                    0 -> Color(0xFFEF4444) // North
                                    90, 270 -> Color(0xFF38BDF8) // East/West
                                    180 -> Color(0xFFE2E8F0) // South
                                    else -> if (is10Deg) Color(0xFFCBD5E1) else if (is5Deg) Color(0xFF64748B) else Color(0xFF332952)
                                }

                                val startX = center.x + (radius - tickLength) * sin(rad).toFloat()
                                val startY = center.y - (radius - tickLength) * cos(rad).toFloat()
                                val endX = center.x + radius * sin(rad).toFloat()
                                val endY = center.y - radius * cos(rad).toFloat()

                                drawLine(
                                    color = tickColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }

                            // DEGREE NUMBERS EVERY 10° (0°, 10°, 20°, ... 350°)
                            for (angle in 0 until 360 step 10) {
                                val rad = Math.toRadians(angle.toDouble())
                                val labelRadius = radius - 22.dp.toPx()
                                val lx = center.x + labelRadius * sin(rad).toFloat()
                                val ly = center.y - labelRadius * cos(rad).toFloat()

                                val textStr = when (angle) {
                                    0 -> "N 0°"
                                    90 -> "L 90°"
                                    180 -> "S 180°"
                                    270 -> "O 270°"
                                    else -> "$angle°"
                                }

                                val isMajorCardinal = angle in listOf(0, 90, 180, 270)

                                val textStyle = TextStyle(
                                    color = when (angle) {
                                        0 -> Color(0xFFEF4444)
                                        90, 270 -> Color(0xFF38BDF8)
                                        180 -> Color(0xFFE2E8F0)
                                        else -> Color(0xFF94A3B8)
                                    },
                                    fontSize = if (isMajorCardinal) 11.sp else 8.sp,
                                    fontWeight = if (isMajorCardinal) FontWeight.Bold else FontWeight.Medium
                                )
                                val measuredText = textMeasurer.measure(textStr, textStyle)

                                rotate(currentHeadingTrue.toFloat(), pivot = Offset(lx, ly)) {
                                    drawText(
                                        textLayoutResult = measuredText,
                                        topLeft = Offset(
                                             lx - measuredText.size.width / 2f,
                                             ly - measuredText.size.height / 2f
                                        )
                                    )
                                }
                            }

                            // INTERCARDINAL LABELS (NE, SE, SO, NO)
                            val intercardinals = listOf(
                                45 to "NE", 135 to "SE", 225 to "SO", 315 to "NO"
                            )
                            for ((deg, label) in intercardinals) {
                                val rad = Math.toRadians(deg.toDouble())
                                val labelRadius = radius - 35.dp.toPx()
                                val lx = center.x + labelRadius * sin(rad).toFloat()
                                val ly = center.y - labelRadius * cos(rad).toFloat()

                                val textStyle = TextStyle(
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val measuredText = textMeasurer.measure(label, textStyle)

                                rotate(currentHeadingTrue.toFloat(), pivot = Offset(lx, ly)) {
                                    drawText(
                                        textLayoutResult = measuredText,
                                        topLeft = Offset(
                                            lx - measuredText.size.width / 2f,
                                            ly - measuredText.size.height / 2f
                                        )
                                    )
                                }
                            }

                            // REFERENCE MARKER (Cyan Star Pin) if selected
                            selectedReference?.let { ref ->
                                val refRad = Math.toRadians(ref.azimuthGeo)
                                val refX = center.x + radius * sin(refRad).toFloat()
                                val refY = center.y - radius * cos(refRad).toFloat()

                                drawLine(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.7f),
                                    start = center,
                                    end = Offset(refX, refY),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                drawCircle(
                                    color = Color(0xFF38BDF8),
                                    radius = 8.dp.toPx(),
                                    center = Offset(refX, refY)
                                )
                                drawCircle(
                                    color = Color(0xFF0F172A),
                                    radius = 3.dp.toPx(),
                                    center = Offset(refX, refY)
                                )
                            }

                            // TARGET MARKER PIN (Yellow Target Pin)
                            val targetRad = Math.toRadians(targetAzimuthGeo)
                            val targetX = center.x + radius * sin(targetRad).toFloat()
                            val targetY = center.y - radius * cos(targetRad).toFloat()

                            // Ray line to target
                            drawLine(
                                color = Color(0xFFFEF08A).copy(alpha = 0.9f),
                                start = center,
                                end = Offset(targetX, targetY),
                                strokeWidth = 3.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Target marker pin circle
                            drawCircle(
                                color = Color(0xFFFACC15),
                                radius = 11.dp.toPx(),
                                center = Offset(targetX, targetY)
                            )
                            drawCircle(
                                color = Color(0xFF1E1B2E),
                                radius = 4.dp.toPx(),
                                center = Offset(targetX, targetY)
                            )
                        }

                        // FIXED TOP DEVICE POINTER (Phone Direction Arrow)
                        val topPointerPath = Path().apply {
                            moveTo(center.x, center.y - radius - 18.dp.toPx())
                            lineTo(center.x - 11.dp.toPx(), center.y - radius - 2.dp.toPx())
                            lineTo(center.x + 11.dp.toPx(), center.y - radius - 2.dp.toPx())
                            close()
                        }
                        drawPath(topPointerPath, color = Color(0xFFA855F7))

                        // CENTER READOUT BADGE
                        drawCircle(
                            color = Color(0xFF161026),
                            radius = 48.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = if (isAligned) Color(0xFF22C55E) else Color(0xFF7E22CE),
                            radius = 48.dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    // Center overlay text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎯 ${targetAzimuthGeo.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFEF08A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = directionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }

                // Alignment Status / Guidance Card
                if (isAligned) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF14532D))
                            .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓ DIREÇÃO ALINHADA (${targetAzimuthGeo.roundToInt()}°)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF86EFAC),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    val turnDirection = if (headingDelta > 0) "direita" else "esquerda"
                    val turnDegrees = absDelta.roundToInt()

                    val statusTitle = if (absDelta <= 15) "Quase lá!" else "Ajuste a direção:"
                    val turnHint = "Vire $turnDegrees° para a $turnDirection"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF231B38))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = turnHint,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Footer Note & Magnetic Declination Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Direção corrigida para o Norte magnético.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { showInfoDialog = !showInfoDialog },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Informação sobre declinação magnética",
                                tint = CosmicPurplePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Explanatory info box when 'i' clicked
                    AnimatedVisibility(
                        visible = showInfoDialog,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1D172E))
                                .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "A bússola do celular usa o Norte magnético. O CosmoLab Sky aplica uma correção de declinação magnética de ${String.format(Locale.US, "%.1f°", abs(magDeclination))} ${if (magDeclination < 0) "Oeste" else "Leste"} para apontar exatamente na direção geográfica do astro.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Large Dismiss Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B2D54),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "✕ Fechar Bússola",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun computeAstroReferences(
    calendar: Calendar,
    latitude: Double,
    longitude: Double
): List<AstroReference> {
    val list = mutableListOf<AstroReference>()

    // 1. Sun
    try {
        val sunPos = AstronomyEngine.calculateSunPosition(calendar, latitude, longitude)
        if (sunPos.altitudeDeg >= -1.0) {
            list.add(
                AstroReference(
                    id = "sun",
                    name = "Sol",
                    icon = "☀️",
                    azimuthGeo = sunPos.azimuthDeg,
                    altitudeDeg = sunPos.altitudeDeg,
                    cardinal = AstronomyEngine.convertAzimuthToDirection(sunPos.azimuthDeg),
                    reason = if (sunPos.altitudeDeg > 10) "Muito brilhante e fácil de identificar no céu diurno." else "Visível próximo do horizonte.",
                    isBest = false,
                    score = if (sunPos.altitudeDeg >= 5) 100 else 70
                )
            )
        }
    } catch (_: Exception) {}

    // 2. Moon
    try {
        val moonPos = AstronomyEngine.calculateMoonPosition(calendar, latitude, longitude)
        if (moonPos.altitudeDeg >= -1.0) {
            list.add(
                AstroReference(
                    id = "moon",
                    name = "Lua",
                    icon = "🌙",
                    azimuthGeo = moonPos.azimuthDeg,
                    altitudeDeg = moonPos.altitudeDeg,
                    cardinal = AstronomyEngine.convertAzimuthToDirection(moonPos.azimuthDeg),
                    reason = "Objeto mais brilhante do céu noturno (${moonPos.phaseName}).",
                    isBest = false,
                    score = if (moonPos.altitudeDeg >= 5) 90 else 65
                )
            )
        }
    } catch (_: Exception) {}

    // 3. Venus
    try {
        val venusPos = AstronomyEngine.calculatePosition(Planet.VENUS, calendar, latitude, longitude)
        if (venusPos.altitudeDeg >= 2.0 && venusPos.sunAltitudeDeg <= 5.0) {
            list.add(
                AstroReference(
                    id = "venus",
                    name = "Vênus",
                    icon = "⭐",
                    azimuthGeo = venusPos.azimuthDeg,
                    altitudeDeg = venusPos.altitudeDeg,
                    cardinal = AstronomyEngine.convertAzimuthToDirection(venusPos.azimuthDeg),
                    reason = "Muito brilhante e fácil de identificar no céu.",
                    isBest = false,
                    score = if (venusPos.altitudeDeg >= 8) 85 else 60
                )
            )
        }
    } catch (_: Exception) {}

    // 4. Jupiter
    try {
        val jupiterPos = AstronomyEngine.calculatePosition(Planet.JUPITER, calendar, latitude, longitude)
        if (jupiterPos.altitudeDeg >= 4.0 && jupiterPos.sunAltitudeDeg <= 0.0) {
            list.add(
                AstroReference(
                    id = "jupiter",
                    name = "Júpiter",
                    icon = "⭐",
                    azimuthGeo = jupiterPos.azimuthDeg,
                    altitudeDeg = jupiterPos.altitudeDeg,
                    cardinal = AstronomyEngine.convertAzimuthToDirection(jupiterPos.azimuthDeg),
                    reason = "Ponto muito brilhante e inconfundível no céu noturno.",
                    isBest = false,
                    score = if (jupiterPos.altitudeDeg >= 10) 75 else 55
                )
            )
        }
    } catch (_: Exception) {}

    // 5. Saturn
    try {
        val saturnPos = AstronomyEngine.calculatePosition(Planet.SATURN, calendar, latitude, longitude)
        if (saturnPos.altitudeDeg >= 8.0 && saturnPos.sunAltitudeDeg <= -6.0) {
            list.add(
                AstroReference(
                    id = "saturn",
                    name = "Saturno",
                    icon = "🪐",
                    azimuthGeo = saturnPos.azimuthDeg,
                    altitudeDeg = saturnPos.altitudeDeg,
                    cardinal = AstronomyEngine.convertAzimuthToDirection(saturnPos.azimuthDeg),
                    reason = "Ponto amarelado constante e visível.",
                    isBest = false,
                    score = 50
                )
            )
        }
    } catch (_: Exception) {}

    // 6. Sunrise / Sunset
    try {
        val sunPosNow = AstronomyEngine.calculateSunPosition(calendar, latitude, longitude)
        if (sunPosNow.altitudeDeg in -12.0..15.0) {
            if (sunPosNow.azimuthDeg in 40.0..140.0) {
                list.add(
                    AstroReference(
                        id = "sunrise",
                        name = "Nascer do Sol",
                        icon = "🌅",
                        azimuthGeo = sunPosNow.azimuthDeg,
                        altitudeDeg = sunPosNow.altitudeDeg,
                        cardinal = AstronomyEngine.convertAzimuthToDirection(sunPosNow.azimuthDeg),
                        reason = "O Sol nasce hoje neste azimute. Excelente referência de Leste.",
                        isBest = false,
                        score = 72
                    )
                )
            } else if (sunPosNow.azimuthDeg in 220.0..320.0) {
                list.add(
                    AstroReference(
                        id = "sunset",
                        name = "Pôr do Sol",
                        icon = "🌇",
                        azimuthGeo = sunPosNow.azimuthDeg,
                        altitudeDeg = sunPosNow.altitudeDeg,
                        cardinal = AstronomyEngine.convertAzimuthToDirection(sunPosNow.azimuthDeg),
                        reason = "O Sol se põe hoje neste azimute. Excelente referência de Oeste.",
                        isBest = false,
                        score = 72
                    )
                )
            }
        }
    } catch (_: Exception) {}

    val sorted = list.sortedByDescending { it.score }.take(4)
    if (sorted.isNotEmpty()) {
        val topScore = sorted.first().score
        return sorted.mapIndexed { index, item ->
            if (index == 0 && topScore >= 60) item.copy(isBest = true) else item
        }
    }
    return sorted
}

private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
    var diff = (target - current + 360f) % 360f
    if (diff > 180f) diff -= 360f
    var next = current + diff * factor
    if (next < 0f) next += 360f
    if (next >= 360f) next -= 360f
    return next
}
