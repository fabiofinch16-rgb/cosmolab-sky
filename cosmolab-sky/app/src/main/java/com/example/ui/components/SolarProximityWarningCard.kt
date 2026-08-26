package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.SolarProximityInfo

/**
 * Indicador visual compacto para cards e listas (ex.: "O QUE OBSERVAR AGORA").
 */
@Composable
fun SolarProximityBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF59E0B).copy(alpha = 0.22f))
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = "⚠️ Próximo ao Sol",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFFBBF24),
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp
        )
    }
}

/**
 * Alerta completo de proximidade solar para telas de detalhes astronômicos e modais.
 */
@Composable
fun SolarProximityWarningCard(
    solarProximityInfo: SolarProximityInfo?,
    modifier: Modifier = Modifier
) {
    if (solarProximityInfo == null || !solarProximityInfo.isWarningActive) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.75f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF261A0C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ ATENÇÃO: PROXIMIDADE DO SOL",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "Separação angular do Sol: ${solarProximityInfo.formattedSeparation}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFEF08A),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${solarProximityInfo.targetName} está a ${solarProximityInfo.formattedSeparation} do Sol. O brilho do céu pode dificultar a observação.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF1F5F9),
                lineHeight = 18.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF381E08))
                    .border(1.dp, Color(0xFFD97706).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = solarProximityInfo.safetyGuidance,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFDE68A),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                    lineHeight = 16.5.sp
                )
            }
        }
    }
}
