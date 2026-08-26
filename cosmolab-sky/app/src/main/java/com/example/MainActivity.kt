package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astronomy.DeepSkyObject
import com.example.astronomy.MeteorShower
import com.example.astronomy.Planet
import com.example.ui.model.CosmoLabViewModel
import com.example.ui.screens.BestTimeFinderDialog
import com.example.ui.screens.ChangeContextModal
import com.example.ui.screens.DeepSkyDetailScreen
import com.example.ui.screens.MeteorShowerDetailScreen
import com.example.ui.screens.MoonDetailScreen
import com.example.ui.screens.PlanetDetailScreen
import com.example.ui.screens.PlanetsScreen
import com.example.ui.screens.SkyScreen
import com.example.ui.screens.TelescopeRecommendationScreen
import com.example.ui.theme.CosmoLabSkyTheme
import com.example.ui.theme.CosmicPurpleContainer
import com.example.ui.theme.CosmicPurplePrimary
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: CosmoLabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CosmoLabSkyTheme {
                CosmoLabSkyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CosmoLabSkyApp(viewModel: CosmoLabViewModel) {
    var activeDetailPlanet by remember { mutableStateOf<Planet?>(null) }
    var activeDetailDso by remember { mutableStateOf<DeepSkyObject?>(null) }
    var activeDetailMeteorShower by remember { mutableStateOf<MeteorShower?>(null) }
    var isMoonDetailOpen by remember { mutableStateOf(false) }
    var showBestTimeFinderForPlanet by remember { mutableStateOf<Planet?>(null) }
    var showChangeContextModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpaceBackground,
        bottomBar = {
            if (activeDetailPlanet == null && activeDetailDso == null && activeDetailMeteorShower == null && !isMoonDetailOpen) {
                CosmoLabBottomNavigationBar(
                    selectedTab = viewModel.activeTab,
                    onTabSelected = { tab ->
                        viewModel.activeTab = tab
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isMoonDetailOpen) {
                MoonDetailScreen(
                    viewModel = viewModel,
                    onBack = { isMoonDetailOpen = false }
                )
            } else if (activeDetailPlanet != null) {
                PlanetDetailScreen(
                    planet = activeDetailPlanet!!,
                    viewModel = viewModel,
                    onBack = { activeDetailPlanet = null },
                    onOpenBestTimeFinder = {
                        showBestTimeFinderForPlanet = activeDetailPlanet
                    }
                )
            } else if (activeDetailDso != null) {
                DeepSkyDetailScreen(
                    dso = activeDetailDso!!,
                    viewModel = viewModel,
                    onBack = { activeDetailDso = null }
                )
            } else if (activeDetailMeteorShower != null) {
                MeteorShowerDetailScreen(
                    shower = activeDetailMeteorShower!!,
                    viewModel = viewModel,
                    onBack = { activeDetailMeteorShower = null }
                )
            } else {
                when (viewModel.activeTab) {
                    0 -> SkyScreen(
                        viewModel = viewModel,
                        onNavigateToPlanet = { planet -> activeDetailPlanet = planet },
                        onNavigateToDso = { dso -> activeDetailDso = dso },
                        onNavigateToMeteorShower = { shower -> activeDetailMeteorShower = shower },
                        onNavigateToMoon = { isMoonDetailOpen = true },
                        onOpenChangeContextModal = { showChangeContextModal = true }
                    )
                    1 -> PlanetsScreen(
                        viewModel = viewModel,
                        onNavigateToPlanet = { planet -> activeDetailPlanet = planet },
                        onOpenChangeContextModal = { showChangeContextModal = true }
                    )
                    2 -> TelescopeRecommendationScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showChangeContextModal) {
        ChangeContextModal(
            viewModel = viewModel,
            onDismiss = { showChangeContextModal = false }
        )
    }

    if (showBestTimeFinderForPlanet != null) {
        BestTimeFinderDialog(
            planet = showBestTimeFinderForPlanet!!,
            viewModel = viewModel,
            onDismiss = { showBestTimeFinderForPlanet = null },
            onApplyWindowTime = { cal ->
                viewModel.setCalendar(cal)
                showBestTimeFinderForPlanet = null
            }
        )
    }
}

@Composable
fun CosmoLabBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaceCardSurface)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = "🌌",
                label = "CÉU",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            NavItem(
                icon = "🪐",
                label = "PLANETAS",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )

            NavItem(
                icon = "🔭",
                label = "TELESCÓPIOS",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) CosmicPurpleContainer else Color.Transparent)
                .padding(horizontal = 18.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) CosmicPurplePrimary else TextMuted,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
        )
    }
}
