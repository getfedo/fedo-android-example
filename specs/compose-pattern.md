# Jetpack Compose Patterns

UI conventions for ModelPulse: Material 3 expressive, Koin, Navigation 3.

## Table of Contents
1. [Screen Architecture](#screen-architecture)
2. [State Management](#state-management)
3. [Component Patterns](#component-patterns)
4. [Navigation](#navigation)
5. [Theming](#theming)
6. [Previews](#previews)

## Screen Architecture

### Route-Screen Pattern

Separate navigation and DI concerns from UI:

```kotlin
// Route: owns the ViewModel and navigation callbacks
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
        onQueryChange = viewModel::onQueryChange,
        onProviderChange = viewModel::onProviderChange,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

// Screen: pure UI, every input is a parameter
@Composable
internal fun ModelsScreen(
    uiState: ModelsUiState,
    onModelClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onProviderChange: (String?) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ModelsUiState.Loading -> LoadingState(modifier)
        is ModelsUiState.Error -> ErrorState(
            message = uiState.message,
            onRetry = onRefresh,
            modifier = modifier,
        )
        is ModelsUiState.Success -> ModelsContent(
            state = uiState,
            onModelClick = onModelClick,
            onQueryChange = onQueryChange,
            onProviderChange = onProviderChange,
            onRefresh = onRefresh,
            modifier = modifier,
        )
    }
}
```

### Benefits
- Screen is testable without a ViewModel or Koin
- Previews work without Koin
- Navigation stays in one place

## State Management

### Sealed Interface for UI State

```kotlin
sealed interface ModelsUiState {
    data object Loading : ModelsUiState

    data class Error(val message: String) : ModelsUiState

    data class Success(
        val models: List<AiModel>,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null,
    ) : ModelsUiState
}
```

`Error` = nothing to show. A failed refresh with data already loaded is
`Success(refreshError = ...)`: inline notice, list untouched.

### StateFlow in ViewModel

```kotlin
class ModelDetailViewModel(
    private val navKey: ModelDetailKey,
    repository: ModelsRepository,
) : ViewModel() {

    val uiState: StateFlow<ModelDetailUiState> =
        repository.models
            .map { models -> models.firstOrNull { it.id == navKey.id } }
            .map { model ->
                if (model == null) ModelDetailUiState.NotFound
                else ModelDetailUiState.Success(model)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ModelDetailUiState.Loading,
            )
}
```

Navigation arguments arrive as the nav key itself, injected by Koin — see
[Navigation](#navigation). There is no `SavedStateHandle.toRoute()` in
Navigation 3.

### Collecting State in Compose

```kotlin
// Lifecycle-aware collection, always
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

## Component Patterns

### Stateless Components

```kotlin
@Composable
fun ProviderChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(name) },
        modifier = modifier,
        leadingIcon = if (selected) {
            { Icon(ModelPulseIcons.Check, contentDescription = null) }
        } else null,
    )
}
```

### List Items

```kotlin
@Composable
fun ModelCard(
    model: AiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(model.shortName, style = MaterialTheme.typography.titleMedium)
            Text(model.providerName, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(model.created.relativeLabel())
                Text(model.contextLength.contextLabel())
                Text(model.promptPrice.perMillionLabel())
            }
        }
    }
}
```

The `*Label()` functions are pure formatters defined in the data/model
package and unit tested. Composables never format.

### Lazy Lists

```kotlin
@Composable
fun ModelList(
    models: List<AiModel>,
    onModelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = models, key = { it.id }) { model ->
            ModelCard(model = model, onClick = { onModelClick(model.id) })
        }
    }
}
```

## Navigation

Navigation 3: the back stack is a list you own, `NavDisplay` renders the
top of it, `entryProvider` maps keys to content. There is no `NavHost`, no
`NavController` and no `NavGraphBuilder`.

### Keys

Keys are `@Serializable` and implement `NavKey` so `rememberNavBackStack`
can save and restore them.

```kotlin
@Serializable
data object ModelsKey : NavKey

@Serializable
data class ModelDetailKey(val id: String) : NavKey

@Serializable
data object SettingsKey : NavKey
```

### NavDisplay

```kotlin
@Composable
fun ModelPulseNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(ModelsKey)

    NavDisplay(
        backStack = backStack,
        onBack = { count -> repeat(count) { backStack.removeLastOrNull() } },
        // rememberViewModelStoreNavEntryDecorator scopes a ViewModel to each
        // entry; adding it means re-declaring the default decorators too.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<ModelsKey> {
                ModelsRoute(
                    onModelClick = { id -> backStack.add(ModelDetailKey(id)) },
                )
            }
            entry<ModelDetailKey> { key ->
                ModelDetailRoute(
                    viewModel = koinViewModel { parametersOf(key) },
                    onBackClick = { backStack.removeLastOrNull() },
                )
            }
        },
        modifier = modifier,
    )
}
```

### Passing arguments

The nav key *is* the argument holder. Inject it with Koin
`parametersOf(key)` and declare the ViewModel as taking it:

```kotlin
val uiModule = module {
    viewModelOf(::ModelsViewModel)
    viewModel { (key: ModelDetailKey) -> ModelDetailViewModel(key, get()) }
}
```

`rememberViewModelStoreNavEntryDecorator()` gives every key instance its
own `ViewModelStore`, so two detail entries never share a ViewModel.

Imports:

```kotlin
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
```

## Theming

Single Material 3 theme, light and dark, no dynamic color toggle screen.

```kotlin
// ui/theme/Theme.kt
@Composable
fun ModelPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModelPulseTypography,
        content = content,
    )
}
```

`minSdk` is 29 and dynamic color needs API 31, so the `dynamicColor` branch
still needs the `Build.VERSION.SDK_INT >= S` check when it is wired up.

### Icons

```kotlin
// ui/theme/ModelPulseIcons.kt
object ModelPulseIcons {
    val ArrowBack = Icons.AutoMirrored.Rounded.ArrowBack
    val Check = Icons.Rounded.Check
    val Feedback = Icons.Rounded.Feedback
    val Search = Icons.Rounded.Search
    val Settings = Icons.Rounded.Settings
}
```

## Previews

Constitution rule: **every screen composable has a `@Preview` per
`UiState`.** A `PreviewParameterProvider` over the sealed interface is the
cheapest way to satisfy it.

### Preview Annotations

```kotlin
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews
```

### Preview Provider

```kotlin
class ModelsUiStateProvider : PreviewParameterProvider<ModelsUiState> {
    override val values = sequenceOf(
        ModelsUiState.Loading,
        ModelsUiState.Error("No internet connection"),
        ModelsUiState.Success(previewModels),
        ModelsUiState.Success(previewModels, refreshError = "Couldn't refresh"),
        ModelsUiState.Success(models = emptyList()),
    )
}

@ThemePreviews
@Composable
private fun ModelsScreenPreview(
    @PreviewParameter(ModelsUiStateProvider::class) uiState: ModelsUiState,
) {
    ModelPulseTheme {
        ModelsScreen(
            uiState = uiState,
            onModelClick = {},
            onQueryChange = {},
            onProviderChange = {},
            onRefresh = {},
        )
    }
}

private val previewModels = listOf(
    AiModel(
        id = "anthropic/claude-opus-5",
        providerSlug = "anthropic",
        shortName = "Claude Opus 5",
        providerName = "Anthropic",
        created = Instant.ofEpochSecond(1_756_000_000),
        description = "Most capable Claude model.",
        contextLength = 200_000,
        promptPrice = Price.PerToken(0.000015),
        completionPrice = Price.PerToken(0.000075),
        inputModalities = listOf("text", "image"),
    ),
)
```

Preview data lives beside the preview, never in `src/main` production code
paths that ship.
