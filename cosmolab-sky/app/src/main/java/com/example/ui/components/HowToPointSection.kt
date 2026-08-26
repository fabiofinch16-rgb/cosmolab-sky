package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.TelescopeMountType
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HowToPointSection(
    targetName: String,
    altitudeDeg: Double,
    azimuthDeg: Double,
    directionLabel: String,
    heightLabel: String,
    isAboveHorizon: Boolean,
    rightAscensionDeg: Double = 0.0,
    declinationDeg: Double = 0.0,
    magnitude: Double? = null,
    constellation: String? = null,
    isNakedEyeVisible: Boolean? = null,
    currentMountType: TelescopeMountType = TelescopeMountType.UNIVERSAL,
    onMountTypeSelected: ((TelescopeMountType) -> Unit)? = null,
    isCelestialPoleVisible: Boolean = false,
    onToggleCelestialPoleMarker: (() -> Unit)? = null,
    latitude: Double = -26.7389,
    longitude: Double = -49.18,
    cityName: String = "Pomerode, SC",
    calendar: java.util.Calendar = java.util.Calendar.getInstance(),
    modifier: Modifier = Modifier
) {
    var selectedMount by remember(currentMountType) { mutableStateOf(currentMountType) }
    var showCelestialPoleHelp by remember { mutableStateOf(false) }
    var poleSearchTab by remember { mutableStateOf(0) }
    var showCompassModal by remember { mutableStateOf(false) }

    val handleMountSelect: (TelescopeMountType) -> Unit = { mount ->
        selectedMount = mount
        onMountTypeSelected?.invoke(mount)
    }

    val upperDir = directionLabel.uppercase()
    val cardinalTip = when {
        upperDir.contains("LESTE") -> "Ponto cardeal do nascente (onde os astros surgem)"
        upperDir.contains("OESTE") -> "Ponto cardeal do poente (onde os astros se põem)"
        upperDir.contains("NORTE") -> "Olhe em direção à região Norte do horizonte"
        upperDir.contains("SUL") -> "Olhe em direção à região Sul do horizonte"
        else -> "Região intermediária no horizonte"
    }

    // Height Classification Rules (Strictly as specified):
    // 0–10°: "Muito próximo do horizonte"
    // 10–30°: "Baixo no céu"
    // 30–60°: "Altura intermediária"
    // 60–80°: "Muito alto no céu"
    // 80–90°: "Quase no zênite (diretamente acima)"
    val heightDescription = when {
        !isAboveHorizon -> "Abaixo do horizonte"
        altitudeDeg < 10.0 -> "Muito próximo do horizonte"
        altitudeDeg < 30.0 -> "Baixo no céu"
        altitudeDeg < 60.0 -> "Altura intermediária"
        altitudeDeg < 80.0 -> "Muito alto no céu"
        else -> "Quase no zênite (diretamente acima)"
    }

    val elevationPercent = (altitudeDeg.coerceIn(0.0, 90.0) / 90.0).toFloat()

    // Coordinate Formatters
    val raFormatted = formatRightAscension(rightAscensionDeg)
    val decFormatted = formatDeclination(declinationDeg)
    val azFormatted = "${azimuthDeg.roundToInt()}°"
    val altFormatted = "${altitudeDeg.roundToInt()}°"

    // Naked Eye / Binoculars / Telescope Recommendation Logic
    val isNakedEye = isNakedEyeVisible ?: (magnitude != null && magnitude <= 5.5)
    val isBinocularRecommended = magnitude != null && magnitude > 5.5 && magnitude <= 8.5

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Subtitle
            Column {
                Text(
                    text = "🔭👁️ COMO APONTAR / OBSERVAR",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Guia simples para localizar, observar e apontar seu instrumento",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Mount Selector Chips (Order: 1. Olho nu/Binóculo, 2. Equatorial, 3. Altazimutal/Dobsoniana)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tipo de observação / montagem:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1B26))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val mountsInOrder = listOf(
                        TelescopeMountType.UNIVERSAL,
                        TelescopeMountType.EQUATORIAL,
                        TelescopeMountType.ALT_AZIMUTH
                    )

                    mountsInOrder.forEach { mount ->
                        val isSelected = mount == selectedMount
                        val chipText = when (mount) {
                            TelescopeMountType.UNIVERSAL -> "👁️ Olho nu / Binóculo"
                            TelescopeMountType.EQUATORIAL -> "🔭 Equatorial"
                            TelescopeMountType.ALT_AZIMUTH -> "🔭 Altazimutal / Dobson"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CosmicPurplePrimary else Color.Transparent)
                                .clickable { handleMountSelect(mount) }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chipText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CosmicPurpleOnPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Equipment Banner with Vector Illustration & Type Description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E1B28))
                    .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Didactic Vector Illustration Graphic
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF14111E)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (selectedMount) {
                            TelescopeMountType.UNIVERSAL -> NakedEyeBinocularGraphic()
                            TelescopeMountType.EQUATORIAL -> EquatorialTelescopeGraphic()
                            TelescopeMountType.ALT_AZIMUTH -> DobsonianTelescopeGraphic()
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        val typeTitle = when (selectedMount) {
                            TelescopeMountType.UNIVERSAL -> "👁️ Olho nu / Binóculo"
                            TelescopeMountType.EQUATORIAL -> "🔭 Montagem Equatorial"
                            TelescopeMountType.ALT_AZIMUTH -> "🔭 Altazimutal / Dobsoniana"
                        }
                        val typeDesc = when (selectedMount) {
                            TelescopeMountType.UNIVERSAL -> "Sem montagem: observe diretamente ou use um binóculo."
                            TelescopeMountType.EQUATORIAL -> "Montagem com eixo inclinado para acompanhar o movimento do céu."
                            TelescopeMountType.ALT_AZIMUTH -> "Movimente o telescópio para os lados e para cima/baixo."
                        }

                        Text(
                            text = typeTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = typeDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Explicação Curta sobre o Eixo Polar + Botão Opcional "Ver no mapa" (Somente no Modo Equatorial)
            if (selectedMount == TelescopeMountType.EQUATORIAL) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1C182A))
                        .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🔭", fontSize = 14.sp)
                                Text(
                                    text = "Eixo polar da montagem",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFF1F5F9),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // Botão Opcional "🧭 Ver no mapa"
                            Button(
                                onClick = { onToggleCelestialPoleMarker?.invoke() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCelestialPoleVisible) Color(0xFF4C1D95) else Color(0xFF282338),
                                    contentColor = if (isCelestialPoleVisible) Color(0xFFFACC15) else TextSecondary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCelestialPoleVisible) Color(0xFFFACC15) else SpaceBorder
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (isCelestialPoleVisible) "🧭 Ocultar no mapa" else "🧭 Ver no mapa",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "🎯 O eixo polar é o eixo inclinado da montagem equatorial que permite acompanhar o movimento aparente do céu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Text(
                            text = "Aponte esse eixo para o:\n• Polo celestial - Alvo do eixo (X amarelo no mapa).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFEF08A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        // Botão Discreto: "ⓘ Como encontrar o Polo Celeste?"
                        Button(
                            onClick = { showCelestialPoleHelp = !showCelestialPoleHelp },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCelestialPoleHelp) Color(0xFF3B0764) else Color(0xFF252033),
                                contentColor = Color(0xFFA855F7)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Text(
                                text = if (showCelestialPoleHelp) "▲ Ocultar explicação do Polo Celeste" else "ⓘ Como encontrar o Polo Celeste?",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Área Explicativa Expansível sobre Como Encontrar o Polo Celeste
                        if (showCelestialPoleHelp) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF13101E))
                                    .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // Header & Close button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "✨ Como localizar o Polo Celestial Sul",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFFF1F5F9),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFF2D2640))
                                                .clickable { showCelestialPoleHelp = false }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "✕ Fechar",
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Selector: Com Bússola vs Sem Bússola
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E1A2C))
                                            .padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (poleSearchTab == 0) Color(0xFF6B21A8) else Color.Transparent)
                                                .clickable { poleSearchTab = 0 }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "🧭 COM BÚSSOLA",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (poleSearchTab == 0) Color.White else TextMuted,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (poleSearchTab == 1) Color(0xFF6B21A8) else Color.Transparent)
                                                .clickable { poleSearchTab = 1 }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "⭐ SEM BÚSSOLA — CÉU",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (poleSearchTab == 1) Color.White else TextMuted,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    val magDec = com.example.astronomy.AstronomyEngine.calculateMagneticDeclination(latitude, longitude)
                                    val absDecStr = String.format(java.util.Locale.US, "%.1f°", kotlin.math.abs(magDec))
                                    val decDirStr = if (magDec < 0) "Oeste (W)" else "Leste (E)"
                                    val correctionDirStr = if (magDec < 0) "sentido horário (à direita)" else "sentido anti-horário (à esquerda)"
                                    val latAngleStr = String.format(java.util.Locale.US, "%.1f°", kotlin.math.abs(latitude))

                                    if (poleSearchTab == 0) {
                                        // 🧭 COM BÚSSOLA
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Orientação com Bússola:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            StepItem("1.", "Use a bússola para encontrar o Sul.")
                                            StepItem("2.", "A bússola aponta para o Sul magnético, então é necessário considerar a declinação magnética do local.")
                                            
                                            // Highlighted Box with Calculated Magnetic Declination
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF1E293B))
                                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "📍 Correção automática para $cityName:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF38BDF8),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                    Text(
                                                        text = "• Declinação magnética: $absDecStr ($decDirStr)\n• O Sul Verdadeiro (Geográfico) fica a $absDecStr no $correctionDirStr do Sul indicado pela bússola.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFF1F5F9),
                                                        fontSize = 10.sp,
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }

                                            StepItem("4.", "Depois da correção, use essa direção como referência para o Polo Celestial Sul.")
                                            StepItem("5.", "Ajuste a inclinação do eixo polar para $latAngleStr de altura (acordo com a latitude do local).")
                                        }
                                    } else {
                                        // ⭐ SEM BÚSSOLA — USANDO O CÉU
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Orientação usando o Cruzeiro do Sul:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFFFACC15),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            StepItem("1.", "Localize o Cruzeiro do Sul no céu.")
                                            StepItem("2.", "Prolongue imaginariamente o eixo maior do Cruzeiro do Sul aproximadamente 4,5 vezes.")
                                            StepItem("3.", "Essa direção leva aproximadamente à região do Polo Celestial Sul.")
                                            StepItem("4.", "Use essa direção como referência para apontar o eixo polar.")
                                            StepItem("5.", "Ajuste a inclinação do eixo polar de acordo com a latitude do local ($latAngleStr acima do horizonte).")

                                            Text(
                                                text = "💡 O Cruzeiro do Sul serve como uma referência visual aproximada para encontrar a região do Polo Celestial Sul.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Layer 1: Layperson Visual Orientation Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: PARA ONDE OLHAR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1B28))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👀", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PARA ONDE OLHAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = "Olhe para o $upperDir",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )

                        Text(
                            text = cardinalTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Card 2: ALTURA NO CÉU
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1B28))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⬆️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ALTURA NO CÉU",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = if (isAboveHorizon) "$altFormatted — $heightDescription" else "Abaixo do horizonte",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isAboveHorizon) TextPrimary else Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )

                        // Visual Horizon-to-Zenith Scale Gauge
                        if (isAboveHorizon) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF332D42))
                            ) {
                                // Track line
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(elevationPercent.coerceIn(0.05f, 1f))
                                        .clip(CircleShape)
                                        .background(CosmicPurplePrimary)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Horizonte", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                                Text("●", style = MaterialTheme.typography.labelSmall, color = CosmicPurplePrimary, fontSize = 8.sp)
                                Text("Zênite", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }

            // Naked Eye / Binocular Recommendation Card (When in Universal Mode)
            if (selectedMount == TelescopeMountType.UNIVERSAL && isAboveHorizon) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1826))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isNakedEye) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👁️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "A OLHO NU",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Este objeto pode ser localizado sem equipamento.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        } else if (isBinocularRecommended) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔭", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COM BINÓCULO",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF60A5FA),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Use um binóculo para aumentar a chance de encontrá-lo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🚫", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "A OLHO NU",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Este objeto não é visível a olho nu. Use binóculo ou telescópio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Constellation tip if available
                        if (!constellation.isNull_or_blank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🌟 DICA PARA ENCONTRAR: Procure primeiro a região da constelação $constellation. O objeto está próximo dessa região.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicPurplePrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "💡 Dica: Primeiro localize a região a olho nu. Depois use o binóculo para procurar o objeto nessa mesma região.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Practical Step-by-Step Instructions Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF181522))
                    .border(1.dp, CosmicPurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val modeTitle = when (selectedMount) {
                        TelescopeMountType.UNIVERSAL -> "OLHO NU / BINÓCULO"
                        TelescopeMountType.EQUATORIAL -> "EQUATORIAL"
                        TelescopeMountType.ALT_AZIMUTH -> "ALTAZIMUTAL / DOBSONIANA"
                    }

                    Text(
                        text = "📖 INSTRUÇÕES PASSO A PASSO ($modeTitle)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    if (!isAboveHorizon) {
                        Text(
                            text = "⚠️ O astro está atualmente abaixo do horizonte no local e horário selecionados. Ele não pode ser observado neste momento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        when (selectedMount) {
                            TelescopeMountType.UNIVERSAL -> {
                                StepRow(
                                    number = "1",
                                    icon = "👀",
                                    title = "Posicionamento",
                                    detail = "Fique de frente para o $upperDir."
                                )
                                StepRow(
                                    number = "2",
                                    icon = "⬆️",
                                    title = "Elevação visual",
                                    detail = "Olhe aproximadamente $altFormatted acima do horizonte (${heightDescription.lowercase()})."
                                )
                                StepRow(
                                    number = "3",
                                    icon = "🔍",
                                    title = "Varredura com o instrumento",
                                    detail = if (isNakedEye) {
                                        "Observe essa região do céu e procure o objeto indicado."
                                    } else {
                                        "Aproxime o binóculo dos olhos e mova suavemente pela região indicada até encontrar o objeto."
                                    }
                                )
                            }
                            TelescopeMountType.EQUATORIAL -> {
                                StepRow(
                                    number = "1",
                                    icon = "🌎",
                                    title = "Alinhe a montagem com o eixo terrestre",
                                    detail = "Aponte o eixo polar da montagem para o Polo celestial - Alvo do eixo (X amarelo no mapa)."
                                )
                                StepRow(
                                    number = "2",
                                    icon = "↔️",
                                    title = "Ajuste o movimento lateral (Ascensão Reta)",
                                    detail = "Solte a trava do eixo R.A. e rode o tubo na direção $upperDir."
                                )
                                StepRow(
                                    number = "3",
                                    icon = "↕️",
                                    title = "Ajuste a altura (Declinação)",
                                    detail = "Ajuste a Declinação até alinhar com o astro no campo de visão."
                                )
                                StepRow(
                                    number = "4",
                                    icon = "🎯",
                                    title = "Centralize o astro no buscador",
                                    detail = "Acompanhe o movimento aparente do céu girando suavemente o cabo micrométrico da R.A."
                                )
                            }
                            TelescopeMountType.ALT_AZIMUTH -> {
                                StepRow(
                                    number = "1",
                                    icon = "↔️",
                                    title = "Gire a base para os lados",
                                    detail = "Vire o telescópio na horizontal até apontar para o $upperDir."
                                )
                                StepRow(
                                    number = "2",
                                    icon = "↕️",
                                    title = "Incline o tubo para cima ou para baixo",
                                    detail = "Eleve o tubo do telescópio até ${heightDescription.lowercase()} (~$altFormatted de inclinação)."
                                )
                                StepRow(
                                    number = "3",
                                    icon = "🎯",
                                    title = "Centralize o astro no buscador",
                                    detail = "Olhe pela buscadora (Red Dot ou Finderscope) para alinhar o astro no centro do retículo."
                                )
                            }
                        }

                        // Layer 2: Precision Coordinates Block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF13101B))
                                .border(1.dp, SpaceBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val precisionTitle = if (selectedMount == TelescopeMountType.UNIVERSAL) {
                                    "📐 POSIÇÃO EXATA NO CÉU"
                                } else {
                                    "📐 AJUSTE PRECISO"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = precisionTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CosmicPurplePrimary.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )

                                    Button(
                                        onClick = { showCompassModal = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2A1F45),
                                            contentColor = Color(0xFFFDE047)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFF7E22CE)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = "🧭 LOCALIZAR DIREÇÃO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                if (selectedMount == TelescopeMountType.EQUATORIAL) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CoordChip(label = "Ascensão Reta (RA)", value = raFormatted, modifier = Modifier.weight(1f))
                                        CoordChip(label = "Declinação (Dec)", value = decFormatted, modifier = Modifier.weight(1f))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CoordChip(label = "Azimute", value = azFormatted, modifier = Modifier.weight(1f))
                                        CoordChip(label = "Altitude", value = altFormatted, modifier = Modifier.weight(1f))
                                    }
                                    Text(
                                        text = "Essas coordenadas permitem localizar o astro com maior precisão.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                } else if (selectedMount == TelescopeMountType.ALT_AZIMUTH) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CoordChip(label = "Azimute", value = azFormatted, modifier = Modifier.weight(1f))
                                        CoordChip(label = "Altitude", value = altFormatted, modifier = Modifier.weight(1f))
                                    }
                                    Text(
                                        text = "Use esses valores para fazer um ajuste mais preciso na montagem.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                } else {
                                    // Universal mode (Olho nu / Binóculo)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CoordChip(label = "Azimute", value = azFormatted, modifier = Modifier.weight(1f))
                                        CoordChip(label = "Altitude", value = altFormatted, modifier = Modifier.weight(1f))
                                    }
                                    Text(
                                        text = "Use estes valores como referência para localizar a região exata do céu.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
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

    if (showCompassModal) {
        CompassDirectionModal(
            targetName = targetName,
            targetAzimuthGeo = azimuthDeg,
            targetAltitudeDeg = altitudeDeg,
            directionLabel = directionLabel,
            latitude = latitude,
            longitude = longitude,
            calendar = calendar,
            onDismiss = { showCompassModal = false }
        )
    }
}

// Vector Illustration 1: Naked Eye / Binocular Observer
@Composable
private fun NakedEyeBinocularGraphic() {
    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height

        // Horizon line
        drawLine(
            color = Color(0xFF3B3353),
            start = Offset(0f, h * 0.85f),
            end = Offset(w, h * 0.85f),
            strokeWidth = 2f
        )

        // Stars in night sky
        drawCircle(Color.White, radius = 1.5f, center = Offset(w * 0.2f, h * 0.2f))
        drawCircle(Color.White, radius = 2f, center = Offset(w * 0.5f, h * 0.15f))
        drawCircle(Color.White, radius = 1.5f, center = Offset(w * 0.8f, h * 0.25f))
        drawCircle(Color(0xFFA78BFA), radius = 2.5f, center = Offset(w * 0.7f, h * 0.18f))

        // Light direction cone from eye/binoculars
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.1f)
            lineTo(w * 0.95f, h * 0.25f)
            close()
        }
        drawPath(path, color = Color(0x33A78BFA))

        // Observer Binoculars figure
        drawCircle(Color(0xFFC4B5FD), radius = 4f, center = Offset(w * 0.35f, h * 0.62f))
        drawCircle(Color(0xFFC4B5FD), radius = 4f, center = Offset(w * 0.45f, h * 0.6f))
        drawLine(
            color = Color(0xFFC4B5FD),
            start = Offset(w * 0.35f, h * 0.62f),
            end = Offset(w * 0.45f, h * 0.6f),
            strokeWidth = 3f
        )
    }
}

// Vector Illustration 2: German Equatorial Mount Telescope (Didactic & Clear)
@Composable
private fun EquatorialTelescopeGraphic() {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.size(76.dp)) {
        val w = size.width
        val h = size.height

        // 1. Tripod Legs
        val tripodApex = Offset(w * 0.48f, h * 0.60f)
        drawLine(Color(0xFF64748B), tripodApex, Offset(w * 0.16f, h * 0.94f), 3.5f, StrokeCap.Round)
        drawLine(Color(0xFF64748B), tripodApex, Offset(w * 0.78f, h * 0.94f), 3.5f, StrokeCap.Round)
        drawLine(Color(0xFF475569), tripodApex, Offset(w * 0.48f, h * 0.96f), 3.5f, StrokeCap.Round)
        
        // Tripod Spreader Plate
        drawLine(Color(0xFF334155), Offset(w * 0.28f, h * 0.82f), Offset(w * 0.66f, h * 0.82f), 2f)

        // 2. Equatorial Mount Head
        drawCircle(Color(0xFF38BDF8), radius = 4f, center = tripodApex)

        // Counterweight Shaft & Counterweight
        val cwEnd = Offset(w * 0.70f, h * 0.74f)
        drawLine(Color(0xFF94A3B8), tripodApex, cwEnd, 3f)
        drawCircle(Color(0xFF818CF8), radius = 5.5f, center = Offset(w * 0.63f, h * 0.70f))

        // 3. HIGHLIGHTED POLAR AXIS (~45 deg angle pointing up-left)
        val polarAxisStart = Offset(w * 0.58f, h * 0.66f)
        val polarAxisEnd = Offset(w * 0.24f, h * 0.35f)

        // Glowing outer stroke for Polar Axis
        drawLine(
            color = Color(0xFFFACC15).copy(alpha = 0.35f),
            start = polarAxisStart,
            end = polarAxisEnd,
            strokeWidth = 9f,
            cap = StrokeCap.Round
        )

        // Solid Yellow Highlight for Polar Axis
        drawLine(
            color = Color(0xFFFACC15),
            start = polarAxisStart,
            end = polarAxisEnd,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Arrow vector pointing towards Celestial Pole direction
        val poleArrowEnd = Offset(w * 0.10f, h * 0.22f)
        drawLine(
            color = Color(0xFFFACC15),
            start = polarAxisEnd,
            end = poleArrowEnd,
            strokeWidth = 2.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f))
        )

        // Arrowhead pointing towards Celestial Pole
        val pPath = Path().apply {
            moveTo(poleArrowEnd.x, poleArrowEnd.y)
            lineTo(poleArrowEnd.x + 5f, poleArrowEnd.y + 6f)
            lineTo(poleArrowEnd.x + 7f, poleArrowEnd.y + 1f)
            close()
        }
        drawPath(pPath, color = Color(0xFFFACC15))

        // Small Celestial Pole Star symbol
        drawCircle(Color(0xFFFEF08A), radius = 2.5f, center = Offset(w * 0.08f, h * 0.15f))

        // 4. Optical Telescope Tube mounted on declination axis
        val tubeStart = Offset(w * 0.18f, h * 0.58f)
        val tubeEnd = Offset(w * 0.74f, h * 0.26f)

        // Main Tube Body
        drawLine(Color.White, tubeStart, tubeEnd, 8.5f, cap = StrokeCap.Round)
        // Primary Mirror Cell (Dark rear cap)
        drawCircle(Color(0xFF1E293B), radius = 4.5f, center = tubeStart)
        // Dew Shield / Aperture Rim (Purple front ring)
        drawCircle(Color(0xFFA855F7), radius = 5.5f, center = tubeEnd)
        // Eyepiece / Focuser
        drawLine(Color(0xFFE2E8F0), Offset(w * 0.32f, h * 0.50f), Offset(w * 0.26f, h * 0.42f), 2.5f)

        // 5. LEGEND LABEL "Eixo polar"
        val legendText = "Eixo polar"
        val legendLayout = textMeasurer.measure(
            text = legendText,
            style = TextStyle(
                color = Color(0xFFFACC15),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        )

        val legendTopLeft = Offset(w * 0.42f, h * 0.38f)
        drawRoundRect(
            color = Color(0xEE0F172A),
            topLeft = Offset(legendTopLeft.x - 3f, legendTopLeft.y - 1f),
            size = Size(legendLayout.size.width + 6f, legendLayout.size.height + 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFFFACC15).copy(alpha = 0.6f),
            topLeft = Offset(legendTopLeft.x - 3f, legendTopLeft.y - 1f),
            size = Size(legendLayout.size.width + 6f, legendLayout.size.height + 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            style = Stroke(width = 0.8f)
        )
        drawText(textLayoutResult = legendLayout, topLeft = legendTopLeft)

        // Pointer line connecting legend to polar axis shaft
        drawLine(
            color = Color(0xFFFACC15).copy(alpha = 0.85f),
            start = Offset(legendTopLeft.x - 3f, legendTopLeft.y + legendLayout.size.height / 2f),
            end = Offset(w * 0.38f, h * 0.48f),
            strokeWidth = 1.2f
        )
    }
}

// Vector Illustration 3: Dobsonian / Altazimuth Telescope
@Composable
private fun DobsonianTelescopeGraphic() {
    Canvas(modifier = Modifier.size(48.dp)) {
        val w = size.width
        val h = size.height

        // Ground base plate
        drawCircle(Color(0xFF3B3353), radius = 18f, center = Offset(w * 0.5f, h * 0.88f))
        // Rocker Box base
        drawRect(
            color = Color(0xFF6B5B95),
            topLeft = Offset(w * 0.32f, h * 0.68f),
            size = Size(w * 0.36f, h * 0.2f)
        )
        // Side Altitude trunnion disc
        drawCircle(Color(0xFFA78BFA), radius = 6f, center = Offset(w * 0.5f, h * 0.65f))

        // Large Dobsonian Tube pointing up at ~60 degrees
        val tubeStart = Offset(w * 0.35f, h * 0.8f)
        val tubeEnd = Offset(w * 0.75f, h * 0.18f)
        drawLine(Color.White, tubeStart, tubeEnd, 10f, StrokeCap.Round)
        // Primary mirror cell end
        drawCircle(Color(0xFF4C4368), radius = 6f, center = tubeStart)
        // Focuser / Eyepiece
        drawLine(Color(0xFFA78BFA), Offset(w * 0.68f, h * 0.25f), Offset(w * 0.8f, h * 0.32f), 3f)
    }
}

@Composable
private fun StepItem(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFA855F7),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun CoordChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1B28))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StepRow(
    number: String,
    icon: String,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(CosmicPurplePrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = CosmicPurplePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.isBlank()
}

private fun formatRightAscension(raDeg: Double): String {
    val normRA = ((raDeg % 360.0) + 360.0) % 360.0
    val totalHours = normRA / 15.0
    val h = totalHours.toInt()
    val m = ((totalHours - h) * 60.0).roundToInt()
    val finalH = if (m == 60) (h + 1) % 24 else h
    val finalM = if (m == 60) 0 else m
    return "${finalH}h ${finalM.toString().padStart(2, '0')}m"
}

private fun formatDeclination(decDeg: Double): String {
    val sign = if (decDeg >= 0) "+" else "-"
    val absDec = abs(decDeg)
    var d = absDec.toInt()
    var m = ((absDec - d) * 60.0).roundToInt()
    if (m == 60) {
        d += 1
        m = 0
    }
    return "$sign${d}° ${m.toString().padStart(2, '0')}′"
}
