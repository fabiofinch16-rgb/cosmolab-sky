package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.Planet
import com.example.astronomy.PlanetObservation
import com.example.ui.imageResId
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

import androidx.compose.runtime.remember
import com.example.ui.components.CosmoLabHeader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PlanetsScreen(
    viewModel: CosmoLabViewModel,
    onNavigateToPlanet: (Planet) -> Unit,
    onOpenChangeContextModal: () -> Unit = {}
) {
    val obsMap = viewModel.observationsMap
    val calendar = viewModel.selectedCalendar

    val nowCal = Calendar.getInstance(calendar.timeZone)
    val isToday = calendar.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    val dateFormat = remember(calendar.timeZone, calendar.timeInMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply {
            timeZone = calendar.timeZone
        }
    }

    val dynamicTitle = if (isToday) {
        "🪐 MELHORES PLANETAS PARA OBSERVAR HOJE"
    } else {
        "🪐 MELHORES PLANETAS PARA OBSERVAR EM ${dateFormat.format(calendar.time)}"
    }

    // Dynamic ranking of all 7 planets based on current observation score
    val sortedPlanets = Planet.entries.sortedByDescending { planet ->
        obsMap[planet]?.score ?: -100.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        // Shared Top Header (Location, Bortle, Date, Time, Sun/Moon, ALTERAR)
        CosmoLabHeader(
            viewModel = viewModel,
            onOpenChangeContextModal = onOpenChangeContextModal
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dynamicTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ranking calculado para este local e data, considerando posição, altitude, horários, condições do céu e proximidade do Sol.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            itemsIndexed(sortedPlanets) { index, planet ->
                val obs = obsMap[planet]
                if (obs != null) {
                    PlanetCardItem(
                        rankNumber = index + 1,
                        observation = obs,
                        onClick = { onNavigateToPlanet(planet) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PlanetCardItem(
    rankNumber: Int,
    observation: PlanetObservation,
    onClick: () -> Unit
) {
    val planet = observation.planet
    val qualityColor = try {
        Color(android.graphics.Color.parseColor(observation.quality.colorHex))
    } catch (e: Exception) {
        CosmicPurplePrimary
    }

    val obsMethodsString = when (planet) {
        Planet.MERCURY, Planet.VENUS, Planet.MARS, Planet.JUPITER, Planet.SATURN -> "👁️ Olho nu  •  🔭 Binóculo  •  🔭 Telescópio"
        Planet.URANUS -> "🔭 Binóculo  •  🔭 Telescópio"
        Planet.NEPTUNE -> "🔭 Telescópio"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SpaceBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SpaceCardSurface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = planet.imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                alpha = 0.15f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = planet.imageResId),
                            contentDescription = planet.portugueseName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(1.dp, SpaceBorder, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#$rankNumber ",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CosmicPurplePrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = planet.portugueseName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = planet.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(qualityColor.copy(alpha = 0.18f))
                                .border(1.dp, qualityColor.copy(alpha = 0.4f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = observation.displayStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = qualityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (observation.solarProximityInfo?.showAlert == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "☀️ Próx. Sol (${observation.solarProximityInfo.angularSeparationDeg.toInt()}°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = obsMethodsString,
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicPurplePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧭", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = observation.directionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⬆️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = observation.heightLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "🌅 Nasce: ${observation.riseTimeStr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = "🌄 Se põe: ${observation.setTimeStr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2A36),
                        contentColor = CosmicPurplePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Ver ${planet.portugueseName}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
