package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.astronomy.ObservationWindow
import com.example.astronomy.Planet
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun BestTimeFinderDialog(
    planet: Planet,
    viewModel: CosmoLabViewModel,
    onDismiss: () -> Unit,
    onApplyWindowTime: (Calendar) -> Unit
) {
    val windows = viewModel.findBestWindowsForPlanet(planet)
    val heightChoices = listOf(
        "Próximo do horizonte",
        "Baixo",
        "Bem posicionado",
        "Alto no céu",
        "O mais alto possível"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xFF3D3846), RoundedCornerShape(28.dp)),
            color = SpaceCardSurface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔭 ENCONTRAR MELHOR HORÁRIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = planet.portugueseName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextMuted
                        )
                    }
                }

                Text(
                    text = "Como você quer observar?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                // Height Choice Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    heightChoices.forEach { option ->
                        val isSelected = viewModel.minHeightChoice == option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF381E72) else Color(0xFF13131A))
                                .border(
                                    1.dp,
                                    if (isSelected) CosmicPurplePrimary else Color(0xFF2A2933),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.minHeightChoice = option
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Dark Sky Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.darkSkyOnlyChoice = !viewModel.darkSkyOnlyChoice }
                ) {
                    Checkbox(
                        checked = viewModel.darkSkyOnlyChoice,
                        onCheckedChange = { viewModel.darkSkyOnlyChoice = it },
                        colors = CheckboxDefaults.colors(checkedColor = CosmicPurplePrimary)
                    )
                    Text(
                        text = "Considerar apenas o céu escuro (noite)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "⭐ PERÍODOS RECOMENDADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                if (windows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum período atende estritamente a esses critérios para a data selecionada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(windows) { win ->
                            WindowResultCard(
                                window = win,
                                onSelect = {
                                    onApplyWindowTime(win.startCal)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WindowResultCard(
    window: ObservationWindow,
    onSelect: () -> Unit
) {
    val qualityColor = try {
        Color(android.graphics.Color.parseColor(window.quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF13131A))
            .border(1.dp, qualityColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${window.startTimeStr} → ${window.endTimeStr}",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.ExtraBold
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(qualityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = window.quality.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = qualityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = window.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "🧭 Direção: ${window.startDirection} → ${window.peakDirection}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicPurplePrimary,
                    contentColor = CosmicPurpleOnPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Aplicar este horário",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
