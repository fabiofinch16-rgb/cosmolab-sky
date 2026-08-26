package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.CosmicPurpleContainer
import com.example.ui.theme.CosmicPurpleOnPrimary
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: CosmoLabViewModel
) {
    val currentCity = viewModel.selectedCity
    val calendar = viewModel.selectedCalendar
    val context = LocalContext.current

    val dateFormat = remember(calendar.timeZone, calendar.timeInMillis) {
        SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")).apply {
            timeZone = calendar.timeZone
        }
    }
    val timeFormat = remember(calendar.timeZone, calendar.timeInMillis) {
        SimpleDateFormat("HH:mm", Locale("pt", "BR")).apply {
            timeZone = calendar.timeZone
        }
    }

    val showDatePicker = {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, y, m, d ->
            viewModel.setDate(y, m, d)
        }, year, month, day).show()
    }

    val showTimePicker = {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, h, m ->
            viewModel.setTime(h, m)
        }, hour, minute, true).show()
    }

    var showCitySearchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CONFIGURAÇÕES E LOCALIZAÇÃO",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        // Location Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CosmicPurplePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOCALIZAÇÃO DE OBSERVAÇÃO",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentCity.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lat: ${String.format("%.4f", currentCity.latitude)} • Lon: ${String.format("%.4f", currentCity.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCitySearchDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicPurplePrimary,
                            contentColor = CosmicPurpleOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Escolher Cidade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bortle Dark-Sky Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌌 QUALIDADE DO CÉU (ESCALA BORTLE)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewModel.isBortleAuto) CosmicPurpleContainer else Color(0xFF33291A))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (viewModel.isBortleAuto) "⚡ AUTOMÁTICO" else "✋ MANUAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (viewModel.isBortleAuto) CosmicPurplePrimary else Color(0xFFFACC15),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🌌 ${viewModel.bortleScale.displayLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (viewModel.isBortleAuto) {
                        if (viewModel.selectedCity.bortleClass != null) {
                            "Obtido automaticamente para ${viewModel.selectedCity.name} (Bortle ${viewModel.selectedCity.bortleClass!!.level}). ${viewModel.bortleScale.description}"
                        } else {
                            "Classificação Bortle não disponível para ${viewModel.selectedCity.name}. (Ajuste manualmente se desejar)"
                        }
                    } else {
                        "Ajustado manualmente. ${viewModel.bortleScale.description}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle buttons for Auto vs Manual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewModel.isBortleAuto) CosmicPurplePrimary else Color.Transparent)
                            .clickable { viewModel.setBortleAutoMode() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Automático (${viewModel.selectedCity.name})",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (viewModel.isBortleAuto) CosmicPurpleOnPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!viewModel.isBortleAuto) Color(0xFF334155) else Color.Transparent)
                            .clickable { viewModel.updateBortleScale(viewModel.bortleScale, isManual = true) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ajustar Manualmente",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (!viewModel.isBortleAuto) TextPrimary else TextMuted,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Nível Bortle (1 a 9):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            }
        }

        // Quick Date & Time Presets Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📅 DATA E HORÁRIO SELECIONADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25232A))
                            .clickable { showDatePicker() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📅 ${dateFormat.format(calendar.time)} ▼",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF25232A))
                            .clickable { showTimePicker() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🕐 ${timeFormat.format(calendar.time)} ✏️",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Atalhos rápidos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton(text = "Agora", modifier = Modifier.weight(1f)) {
                        viewModel.resetToCurrentTime()
                    }
                    PresetButton(text = "Hoje 21:00", modifier = Modifier.weight(1f)) {
                        val tz = java.util.TimeZone.getTimeZone(viewModel.selectedCity.timezoneId)
                        val c = Calendar.getInstance(tz)
                        c.set(Calendar.HOUR_OF_DAY, 21)
                        c.set(Calendar.MINUTE, 0)
                        c.set(Calendar.SECOND, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        viewModel.setCalendar(c)
                    }
                    PresetButton(text = "Madrugada 02:00", modifier = Modifier.weight(1f)) {
                        val tz = java.util.TimeZone.getTimeZone(viewModel.selectedCity.timezoneId)
                        val c = Calendar.getInstance(tz)
                        c.set(Calendar.HOUR_OF_DAY, 2)
                        c.set(Calendar.MINUTE, 0)
                        c.set(Calendar.SECOND, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        viewModel.setCalendar(c)
                    }
                }
            }
        }

        // About CosmoLab Sky Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌌 SOBRE O COSMOLAB SKY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "CosmoLab Sky v1.0.0",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Guia astronômico inteligente focado na observação dos sete planetas visíveis: Mercúrio, Vênus, Marte, Júpiter, Saturno, Urano e Netuno. Todos os cálculos de posição, nascer, ocaso e visibilidade funcionam 100% offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }

    if (showCitySearchDialog) {
        CitySearchModal(
            viewModel = viewModel,
            onDismiss = { showCitySearchDialog = false }
        )
    }
}

@Composable
fun PresetButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF25232A))
            .border(1.dp, SpaceBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

