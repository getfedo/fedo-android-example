package com.fedo.modelpulse.ui.models

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fedo.modelpulse.R
import com.fedo.modelpulse.data.AiModel
import com.fedo.modelpulse.data.Price
import com.fedo.modelpulse.data.ProviderFilter
import com.fedo.modelpulse.data.contextLabel
import com.fedo.modelpulse.data.perMillionLabel
import com.fedo.modelpulse.data.providerFilters
import com.fedo.modelpulse.data.relativeLabel
import com.fedo.modelpulse.ui.theme.ModelPulseTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ModelsRoute(
    onModelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ModelsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ModelsScreen(
        uiState = uiState,
        onModelClick = onModelClick,
        onRefresh = viewModel::refresh,
        onQueryChange = viewModel::onQueryChange,
        onProviderChange = viewModel::onProviderChange,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ModelsScreen(
    uiState: ModelsUiState,
    onModelClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onProviderChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.models_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ModelsUiState.Loading -> LoadingState(Modifier.padding(innerPadding))

            is ModelsUiState.Error -> MessageState(
                message = uiState.message,
                actionLabel = stringResource(R.string.models_retry),
                onAction = onRefresh,
                modifier = Modifier.padding(innerPadding),
            )

            // providers is empty only when the catalogue itself is; a filter
            // that matches nothing still has providers to clear.
            is ModelsUiState.Success -> if (uiState.providers.isEmpty()) {
                MessageState(
                    message = stringResource(R.string.models_empty_body),
                    actionLabel = stringResource(R.string.models_refresh),
                    onAction = onRefresh,
                    title = stringResource(R.string.models_empty_title),
                    modifier = Modifier.padding(innerPadding),
                )
            } else {
                ModelsContent(
                    state = uiState,
                    onModelClick = onModelClick,
                    onRefresh = onRefresh,
                    onQueryChange = onQueryChange,
                    onProviderChange = onProviderChange,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModelsContent(
    state: ModelsUiState.Success,
    onModelClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onProviderChange: (String?) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        state = pullState,
        indicator = {
            // The expressive indicator, not the arrow from the stable set.
            PullToRefreshDefaults.LoadingIndicator(
                state = pullState,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = SEARCH_KEY) {
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item(key = PROVIDERS_KEY) {
                ProviderFilters(
                    providers = state.providers,
                    selected = state.selectedProvider,
                    onProviderChange = onProviderChange,
                )
            }

            // A failed refresh keeps the list and says so here, above it.
            state.refreshError?.let { message ->
                item(key = REFRESH_ERROR_KEY) {
                    RefreshErrorNotice(
                        message = message,
                        onRetry = onRefresh,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            if (state.isNoResults) {
                item(key = NO_RESULTS_KEY) {
                    MessageState(
                        message = stringResource(R.string.models_no_results_body),
                        actionLabel = stringResource(R.string.models_clear_filters),
                        onAction = {
                            onQueryChange("")
                            onProviderChange(null)
                        },
                        title = stringResource(R.string.models_no_results_title),
                        modifier = Modifier.padding(top = 48.dp),
                    )
                }
            } else {
                items(items = state.models, key = AiModel::id) { model ->
                    ModelCard(
                        model = model,
                        onClick = { onModelClick(model.id) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        placeholder = { Text(stringResource(R.string.models_search_hint)) },
        // ponytail: two vector drawables in res/, not the material-icons
        // artifact — it is not a dependency of this project.
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null)
        },
        trailingIcon = if (query.isEmpty()) {
            null
        } else {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.models_search_clear),
                    )
                }
            }
        },
    )
}

@Composable
private fun ProviderFilters(
    providers: List<ProviderFilter>,
    selected: String?,
    onProviderChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = ALL_PROVIDERS_KEY) {
            FilterChip(
                selected = selected == null,
                onClick = { onProviderChange(null) },
                label = { Text(stringResource(R.string.models_provider_all)) },
            )
        }

        items(items = providers, key = ProviderFilter::slug) { provider ->
            FilterChip(
                selected = provider.slug == selected,
                onClick = {
                    onProviderChange(provider.slug.takeIf { it != selected })
                },
                label = {
                    Text(
                        stringResource(
                            R.string.models_provider_chip,
                            provider.name,
                            provider.count,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun ModelCard(model: AiModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(model.shortName, style = MaterialTheme.typography.titleMediumEmphasized)
            Text(
                text = model.providerName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetaText(model.created.relativeLabel())
                MetaText(stringResource(R.string.models_context, model.contextLength.contextLabel()))
                MetaText(
                    stringResource(R.string.models_input_price, model.promptPrice.perMillionLabel()),
                )
            }
        }
    }
}

@Composable
private fun MetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun RefreshErrorNotice(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.models_refresh_failed, message),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.models_retry))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val label = stringResource(R.string.models_loading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ContainedLoadingIndicator()
    }
}

@Composable
private fun MessageState(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.headlineSmallEmphasized)
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

private const val REFRESH_ERROR_KEY = "refresh-error"
private const val SEARCH_KEY = "search"
private const val PROVIDERS_KEY = "providers"
private const val NO_RESULTS_KEY = "no-results"
private const val ALL_PROVIDERS_KEY = "all-providers"

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private annotation class ThemePreviews

@ThemePreviews
@Composable
private fun ModelsScreenPreview(
    @PreviewParameter(ModelsUiStateProvider::class) uiState: ModelsUiState,
) {
    ModelPulseTheme {
        ModelsScreen(
            uiState = uiState,
            onModelClick = {},
            onRefresh = {},
            onQueryChange = {},
            onProviderChange = {},
        )
    }
}

private class ModelsUiStateProvider : PreviewParameterProvider<ModelsUiState> {
    override val values = sequenceOf(
        ModelsUiState.Loading,
        ModelsUiState.Error("Couldn't reach OpenRouter. Check your connection."),
        ModelsUiState.Success(previewModels, providers = previewProviders),
        ModelsUiState.Success(previewModels, providers = previewProviders, isRefreshing = true),
        ModelsUiState.Success(
            models = previewModels,
            providers = previewProviders,
            refreshError = "Couldn't reach OpenRouter.",
        ),
        ModelsUiState.Success(
            models = emptyList(),
            query = "gpt",
            providers = previewProviders,
        ),
        ModelsUiState.Success(models = emptyList()),
    )
}

private val previewModels = listOf(
    AiModel(
        id = "anthropic/claude-opus-5",
        providerSlug = "anthropic",
        shortName = "Claude Opus 5",
        providerName = "Anthropic",
        created = Instant.now().minus(2, ChronoUnit.DAYS),
        description = "Most capable Claude model.",
        contextLength = 200_000,
        promptPrice = Price.PerToken(BigDecimal("0.000015")),
        completionPrice = Price.PerToken(BigDecimal("0.000075")),
        inputModalities = listOf("text", "image"),
    ),
    AiModel(
        id = "meta-llama/llama-4-8b:free",
        providerSlug = "meta-llama",
        shortName = "Llama 4 8B",
        providerName = "Meta",
        created = Instant.now().minus(120, ChronoUnit.DAYS),
        description = "Small open model.",
        contextLength = null,
        promptPrice = Price.Free,
        completionPrice = Price.Free,
        inputModalities = listOf("text"),
    ),
)

private val previewProviders = previewModels.providerFilters()
