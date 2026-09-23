package com.fedo.modelpulse.ui.navigation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.fedo.modelpulse.FedoIntegration
import com.fedo.modelpulse.ui.detail.ModelDetailRoute
import com.fedo.modelpulse.ui.models.ModelsRoute
import com.fedo.modelpulse.ui.roadmap.RoadmapScreen
import com.fedo.modelpulse.ui.settings.SettingsRoute
import com.fedo.modelpulse.ui.theme.ModelPulseTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelPulseNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(ModelsKey)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                currentNavKey = backStack.lastOrNull(),
                onSelect = { destination ->
                    backStack.clear()
                    backStack.addAll(backStackFor(destination))
                }
            )
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
                entry<ModelsKey> {
                    ModelsRoute(onModelClick = { id -> backStack.add(ModelDetailKey(id)) })
                }
                entry<ModelDetailKey> { key ->
                    ModelDetailRoute(
                        onBackClick = { backStack.removeLastOrNull() },
                        viewModel = koinViewModel { parametersOf(key) },
                    )
                }
                // ponytail: placeholders until uyb.1 and uyb.3 fill them in.
                entry<RoadmapKey> {
                    RoadmapScreen(
                        isConfigured = FedoIntegration.isConfigured,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<SettingsKey> { SettingsRoute() }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}


/** Kept so the keys file is the only place that knows the destination list. */
internal val topLevelKeys: List<NavKey> = TopLevelDestination.entries.map(TopLevelDestination::key)


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomBar(
    currentNavKey: NavKey?,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val wide = remember {
        windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        )
    }
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                toolbarContainerColor = colorScheme.primaryContainer,
                toolbarContentColor = colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .padding(
                    top = ScreenOffset,
                    bottom = systemBarsInsets.calculateBottomPadding()
                            + ScreenOffset
                )
                .zIndex(1f)
        ) {
            TopLevelDestination.entries.forEachIndexed { index, destination ->
                val selected by remember(currentNavKey) { derivedStateOf { currentNavKey == destination.key } }
                ToggleButton(
                    checked = selected,
                    onCheckedChange = {
                        if (it) onSelect(destination)
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        checkedContainerColor = colorScheme.primary,
                        checkedContentColor = colorScheme.onPrimary
                    ),
                    shapes = ToggleButtonDefaults.shapes(
                        CircleShape,
                        CircleShape,
                        CircleShape
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Crossfade(selected) {
                            if (it) Icon(
                                destination.selectedIcon,
                                stringResource(destination.labelRes),
                                modifier = Modifier.size(24.dp)
                            )
                            else Icon(
                                destination.unselectedIcon,
                                stringResource(destination.labelRes),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = selected || wide,
                            enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                            exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                        ) {
                            Text(
                                text = stringResource(destination.labelRes),
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240", name = "Tablet")
private annotation class ThemePreviews

class BottomSheetStateProvider : PreviewParameterProvider<NavKey?> {
    override val values = sequenceOf(
        null,
        TopLevelDestination.MODELS.key,
        TopLevelDestination.ROADMAP.key,
        TopLevelDestination.SETTINGS.key,
    )
}

@ThemePreviews
@Composable
private fun BottomBarPreview(
    @PreviewParameter(BottomSheetStateProvider::class) navKey: NavKey?
) {
    ModelPulseTheme {
        Surface {
            BottomBar(
                currentNavKey = navKey,
                onSelect = {}
            )
        }
    }
}
