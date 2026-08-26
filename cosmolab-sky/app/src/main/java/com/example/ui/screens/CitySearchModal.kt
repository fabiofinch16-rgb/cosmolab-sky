package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.City
import com.example.data.CityRepository
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CitySearchModal(
    viewModel: CosmoLabViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<City>>(CityRepository.SUGGESTED_CITIES) }
    var isSearching by remember { mutableStateOf(false) }

    val currentCity = viewModel.selectedCity
    val recentCities = viewModel.recentCities

    // Debounced async search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = CityRepository.SUGGESTED_CITIES
            isSearching = false
        } else {
            isSearching = true
            delay(300) // debounce typing
            val results = CityRepository.searchCitiesAsync(searchQuery, context)
            searchResults = results
            isSearching = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = SpaceCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📍 PESQUISAR LOCALIZAÇÃO",
                            style = MaterialTheme.typography.titleMedium,
                            color = CosmicPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Pesquise qualquer cidade ou localidade do mundo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF262338), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Search Input Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Ex: Pomerode, Delfim Moreira, Atins, Paris...", color = TextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CosmicPurplePrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar", tint = TextMuted)
                            }
                        } else if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CosmicPurplePrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPurplePrimary,
                        unfocusedBorderColor = Color(0xFF3D3846),
                        focusedContainerColor = Color(0xFF13131A),
                        unfocusedContainerColor = Color(0xFF13131A),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Main Content List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Show Recentes and Sugestões when query is blank
                    if (searchQuery.isBlank()) {
                        if (recentCities.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                    Text(
                                        text = "📍 LOCALIDADES RECENTES",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CosmicPurplePrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(recentCities) { recent ->
                                            val isSelected = recent.displayName == currentCity.displayName
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .clickable {
                                                        viewModel.setCity(recent)
                                                        onDismiss()
                                                    },
                                                color = if (isSelected) CosmicPurplePrimary else Color(0xFF262338),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isSelected) CosmicPurplePrimary else SpaceBorder
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = if (isSelected) CosmicPurpleOnPrimary else CosmicPurplePrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = recent.name,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) CosmicPurpleOnPrimary else TextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "💡 SUGESTÕES PARA OBSERVAÇÃO",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }

                    if (isSearching) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = CosmicPurplePrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Pesquise qualquer cidade no mapa global...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    if (!isSearching && searchResults.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma localidade encontrada para '$searchQuery'.\nVerifique a grafia ou tente incluir o estado/país.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    items(searchResults) { city ->
                        val isSelected = city.displayName == currentCity.displayName &&
                                Math.abs(city.latitude - currentCity.latitude) < 0.05 &&
                                Math.abs(city.longitude - currentCity.longitude) < 0.05

                        CityListItem(
                            city = city,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.setCity(city)
                                onDismiss()
                            }
                        )
                    }
                }

                // Footer button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPurplePrimary,
                        contentColor = CosmicPurpleOnPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Fechar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CityListItem(
    city: City,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF2E1A52) else Color(0xFF181722))
            .border(
                width = 1.dp,
                color = if (isSelected) CosmicPurplePrimary else SpaceBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) CosmicPurplePrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (city.stateOrCountry.isNotBlank()) {
                    Text(
                        text = city.stateOrCountry,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(start = 22.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "Lat: %.3f, Lon: %.3f", city.latitude, city.longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    // Bortle Badge
                    if (city.bortleClass != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2C223C)
                        ) {
                            Text(
                                text = "🌌 Bortle ${city.bortleClass.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD0BCFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "🌌 Bortle não disp.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = CosmicPurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
