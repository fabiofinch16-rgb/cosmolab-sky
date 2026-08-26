package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.City
import com.example.data.CityRepository
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ChangeContextModal(
    viewModel: CosmoLabViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentCal = viewModel.selectedCalendar
    var showCitySearchModal by remember { mutableStateOf(false) }

    val dateFormat = remember(currentCal.timeZone, currentCal.timeInMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply {
            timeZone = currentCal.timeZone
        }
    }
    val timeFormat = remember(currentCal.timeZone, currentCal.timeInMillis) {
        SimpleDateFormat("HH:mm", Locale("pt", "BR")).apply {
            timeZone = currentCal.timeZone
        }
    }

    val showDatePicker = {
        val year = currentCal.get(Calendar.YEAR)
        val month = currentCal.get(Calendar.MONTH)
        val day = currentCal.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, y, m, d ->
            viewModel.setDate(y, m, d)
        }, year, month, day).show()
    }

    val showTimePicker = {
        val hour = currentCal.get(Calendar.HOUR_OF_DAY)
        val minute = currentCal.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, h, m ->
            viewModel.setTime(h, m)
        }, hour, minute, true).show()
    }

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
                    Text(
                        text = "⚙️ ALTERAR PARÂMETROS",
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }

                // Data e Horário de Observação Section
                Text(
                    text = "📅 DATA E HORÁRIO DE OBSERVAÇÃO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Data de observação: [09/08/2026 ▼]
                    Surface(
                        onClick = showDatePicker,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF25232A),
                        border = BorderStroke(1.dp, CosmicPurplePrimary.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(
                                text = "Data de observação",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateFormat.format(currentCal.time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("▼", color = CosmicPurplePrimary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Horário de observação: [15:51]
                    Surface(
                        onClick = showTimePicker,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF25232A),
                        border = BorderStroke(1.dp, CosmicPurplePrimary.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(
                                text = "Horário",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = timeFormat.format(currentCal.time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✏️", fontSize = 10.sp)
                            }
                        }
                    }

                    // [Agora]
                    Surface(
                        onClick = { viewModel.resetToCurrentTime() },
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicPurplePrimary
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Agora",
                                style = MaterialTheme.typography.labelMedium,
                                color = CosmicPurpleOnPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Quick Nudge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25232A))
                            .clickable { viewModel.updateDateOffset(-1) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-1 dia", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25232A))
                            .clickable { viewModel.updateDateOffset(1) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1 dia", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25232A))
                            .clickable { viewModel.updateTimeOffset(-60) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-1h", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25232A))
                            .clickable { viewModel.updateTimeOffset(60) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1h", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                // Bortle Dark-Sky selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌌 Qualidade do Céu (Escala Bortle)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (viewModel.isBortleAuto) CosmicPurplePrimary else Color(0xFF25232A))
                                .clickable { viewModel.setBortleAutoMode() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Auto",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (viewModel.isBortleAuto) CosmicPurpleOnPrimary else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!viewModel.isBortleAuto) Color(0xFF334155) else Color(0xFF25232A))
                                .clickable { viewModel.updateBortleScale(viewModel.bortleScale, isManual = true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Manual",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!viewModel.isBortleAuto) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.example.astronomy.BortleScale.entries.take(9).forEach { bortle ->
                        val isSelected = bortle == viewModel.bortleScale
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CosmicPurplePrimary else Color(0xFF25232A))
                                .clickable { viewModel.updateBortleScale(bortle, isManual = true) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "B${bortle.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CosmicPurpleOnPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = if (viewModel.isBortleAuto) {
                        if (viewModel.selectedCity.bortleClass != null) {
                            "🌌 ${viewModel.selectedCity.bortleClass!!.displayLabel} (Automático para ${viewModel.selectedCity.name}): ${viewModel.bortleScale.description}"
                        } else {
                            "🌌 Bortle não disponível para ${viewModel.selectedCity.name} (Ajuste manualmente se desejar)"
                        }
                    } else {
                        "🌌 ${viewModel.bortleScale.displayLabel} (Ajustado Manualmente): ${viewModel.bortleScale.description}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )

                // City selector
                Text(
                    text = "📍 Localização de Observação",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showCitySearchModal = true },
                    color = Color(0xFF181722),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.selectedCity.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "Lat: %.3f, Lon: %.3f • %s", viewModel.selectedCity.latitude, viewModel.selectedCity.longitude, viewModel.selectedCity.timezoneId),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Button(
                            onClick = { showCitySearchModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CosmicPurplePrimary,
                                contentColor = CosmicPurpleOnPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Pesquisar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPurplePrimary,
                        contentColor = CosmicPurpleOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Concluir", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCitySearchModal) {
        CitySearchModal(
            viewModel = viewModel,
            onDismiss = { showCitySearchModal = false }
        )
    }
}
