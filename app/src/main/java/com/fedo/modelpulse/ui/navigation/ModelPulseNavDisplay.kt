package com.fedo.modelpulse.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.fedo.modelpulse.R
import com.fedo.modelpulse.ui.models.ModelsRoute

@Composable
fun ModelPulseNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(ModelsKey)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = backStack.lastOrNull() == destination.key,
                        onClick = {
                            backStack.clear()
                            backStack.addAll(backStackFor(destination))
                        },
                        icon = {},
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            // Re-declaring the defaults is the price of adding the ViewModel one.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<ModelsKey> { ModelsRoute() }
                // ponytail: placeholders until uyb.1 and uyb.3 fill them in.
                entry<RoadmapKey> { Placeholder(stringResource(R.string.nav_roadmap)) }
                entry<SettingsKey> { Placeholder(stringResource(R.string.nav_settings)) }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun Placeholder(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall)
    }
}

/** Kept so the keys file is the only place that knows the destination list. */
internal val topLevelKeys: List<NavKey> = TopLevelDestination.entries.map(TopLevelDestination::key)
