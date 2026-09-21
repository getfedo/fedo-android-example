# Architecture Guide

ModelPulse browses the public OpenRouter model catalogue
(`GET https://openrouter.ai/api/v1/models`, no API key) and showcases the
Fedo SDK. The app is documentation for SDK integrators: keep it small and
readable. Structure follows Google's Android architecture guidance,
trimmed to what a single-module, online-only app actually needs.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Data Layer](#data-layer)
3. [Domain Layer](#domain-layer)
4. [UI Layer](#ui-layer)
5. [Data Flow Example](#data-flow-example)

## Architecture Overview

Three layers, unidirectional data flow:
- **Events flow DOWN** (UI → Domain → Data)
- **Data flows UP** (Data → Domain → UI)
- **Remote is the source of truth**; fetched data is cached in memory for
  the lifetime of the process only

```
┌────────────────────────────────────────────────────┐
│                    UI Layer                         │
│  ┌──────────────┐    ┌─────────────────────────┐   │
│  │   Screen     │◄───│      ViewModel          │   │
│  │  (Compose)   │    │  (StateFlow<UiState>)   │   │
│  └──────────────┘    └───────────┬─────────────┘   │
├──────────────────────────────────┼─────────────────┤
│         Domain Layer (optional)  │                  │
│              ┌───────────────────▼──────┐          │
│              │       Use Cases          │          │
│              │  (combine/transform)     │          │
│              └───────────┬──────────────┘          │
├──────────────────────────┼─────────────────────────┤
│                  Data Layer                         │
│  ┌───────────────────────▼──────────────────────┐  │
│  │            ModelsRepository                   │  │
│  │   (in-memory cache + fetch orchestration)     │  │
│  └───────────────────────┬──────────────────────┘  │
│                          │                          │
│              ┌───────────▼──────────────┐          │
│              │   Remote DataSource      │          │
│              │ (OkHttp + kotlinx.ser.)  │          │
│              └──────────────────────────┘          │
└────────────────────────────────────────────────────┘
```

## Data Layer

### Principles
- **No offline support.** Nothing is persisted to disk. Cold start always
  hits the network; no Room, no DataStore, no WorkManager.
- **In-memory cache.** The repository holds the last successful response so
  navigation, rotation and a failed refresh do not lose the list.
- **Repository pattern**: single public API for data access.
- **Reactive streams**: data exposed as `Flow<T>` / `StateFlow<T>`.
- **No snapshots**: never expose `getModels(): List<Model>`, always a flow.
- **Failed refresh never wipes data.** Refresh errors surface as a separate
  signal; the cached list stays.
- **One load at a time.** A refresh started while one is already running
  joins it and shares its result, so the initial load, a pull to refresh and
  a retry cannot race or double-fetch. The catalogue is one unparameterised
  GET: a second caller wants exactly what the first is already fetching.
  Cancelling the first load instead would hand its caller a
  `CancellationException` and no result.

### Repository

```kotlin
interface ModelsRepository {
    /** Last known models. Empty until the first successful load. */
    val models: StateFlow<List<AiModel>>

    /** Loads from the network, joining a load already in flight. */
    suspend fun refresh(): Result<Unit>
}

internal class DefaultModelsRepository(
    private val remote: OpenRouterDataSource,
    private val scope: CoroutineScope,
) : ModelsRepository {

    private val _models = MutableStateFlow(emptyList<AiModel>())
    override val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    private var inFlight: Deferred<Result<Unit>>? = null

    private val lock = Mutex()

    override suspend fun refresh(): Result<Unit> = currentOrNewLoad().await()

    private suspend fun currentOrNewLoad(): Deferred<Result<Unit>> = lock.withLock {
        inFlight?.takeIf { it.isActive }?.let { return it }

        scope.async {
            remote.getModels()
                .onSuccess { models -> _models.value = models }
                .map { }
        }.also { inFlight = it }
    }
}
```

`scope` is an application-scoped `CoroutineScope` provided by Koin, so a
refresh survives the ViewModel that triggered it.

### Data Sources

| Type | Implementation | Purpose |
|------|----------------|---------|
| Remote | OkHttp + kotlinx.serialization | Fetch the OpenRouter catalogue |
| Memory | `MutableStateFlow` in the repository | Survive navigation and failed refreshes within one session |

There is no local database and no preferences store. Add one only when a
bead requires state that must outlive the process.

### Remote DataSource

```kotlin
internal class OpenRouterDataSource(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: String = OPEN_ROUTER_MODELS_URL,
) {
    /** Models, newest first. Never throws; every failure comes back as one. */
    suspend fun getModels(): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(baseUrl).build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                json.decodeFromString<ModelsResponse>(response.body.string())
                    .data
                    .map(NetworkModel::asExternalModel)
                    .sortedByDescending(AiModel::created)
            }
        }
    }
}
```

`runCatching` is what keeps the promise in [Errors](#errors): a non-2xx
response, a dropped connection and a body that will not decode all leave as
`Result.failure`. Sorting happens here, so everything downstream can treat
the list as newest first. `baseUrl` is a constructor parameter only so tests
can point it at a local server — see [testing.md](testing.md).

`Json` is configured once in the Koin module with `ignoreUnknownKeys = true`
— OpenRouter adds fields without warning.

### Network Models & Mapping

Network DTOs are `@Serializable`, mirror the wire format exactly, and stay
internal to the data layer. Domain models are what the UI sees.

```kotlin
@Serializable
internal data class ModelsResponse(val data: List<NetworkModel>)

// Only the id is required. OpenRouter has shipped entries with no pricing
// block and no architecture, and one missing field must not cost the other
// 445 models.
@Serializable
internal data class NetworkModel(
    val id: String,
    val name: String = "",
    val created: Long = 0L,                  // unix seconds
    val description: String? = null,
    @SerialName("context_length") val contextLength: Int? = null,
    val pricing: NetworkPricing? = null,
    val architecture: NetworkArchitecture? = null,
)

@Serializable
internal data class NetworkPricing(
    val prompt: String? = null,              // USD per token, as a STRING
    val completion: String? = null,          // "0" = free, "-1" = variable
)

@Serializable
internal data class NetworkArchitecture(
    @SerialName("input_modalities") val inputModalities: List<String> = emptyList(),
)

// Network model → domain model
internal fun NetworkModel.asExternalModel(): AiModel {
    val cleanId = id.removePrefix("~")       // ids are sometimes "~" prefixed
    return AiModel(
        id = cleanId,
        // Lowercased so the grouping key survives a provider renaming its
        // casing; "Anthropic/…" and "anthropic/…" are one provider.
        providerSlug = cleanId.substringBefore('/').lowercase(),
        shortName = name.substringAfter(": ", name).ifBlank { cleanId },
        // Names are usually "Provider: Model". When one is not, the id prefix
        // is the next best label — never an empty provider.
        providerName = name.substringBefore(": ", "").ifBlank { cleanId.substringBefore('/') },
        created = Instant.ofEpochSecond(created),
        description = description.orEmpty(),
        contextLength = contextLength,
        promptPrice = Price.parse(pricing?.prompt.orEmpty()),
        completionPrice = Price.parse(pricing?.completion.orEmpty()),
        inputModalities = architecture?.inputModalities.orEmpty(),
    )
}
```

Parsing and formatting (`Price.parse`, relative dates, context labels,
price per 1M tokens) live in plain functions, not in composables and not in
the DTOs. They are the cheapest things to unit test — see
[testing.md](testing.md).

### Errors

The data layer returns `Result`. It does not throw across the layer
boundary and it does not map errors to user-facing strings; the ViewModel
decides what the user sees.

## Domain Layer

### Purpose
- Encapsulate business logic that more than one ViewModel needs
- Combine and transform data from multiple repositories
- **Optional layer** — ModelPulse currently has none. Do not add a use case
  for a single pass-through call.

### When to Create a Use Case
- Logic is reused across multiple ViewModels
- Complex data transformations
- Combining data from multiple repositories
- Business rules that belong neither in UI nor in Data

Search and provider filtering are pure functions over `List<AiModel>`; they
live next to the model, not behind a use case.

## UI Layer

### Components
- **Screen**: stateless composable, takes `state` + callbacks
- **ViewModel**: holds and manages UI state, exposes `StateFlow<UiState>`
- **UiState**: sealed interface covering every state the screen can be in

See [compose-pattern.md](compose-pattern.md) for the full UI conventions.

### UiState Modeling

```kotlin
sealed interface ModelsUiState {
    data object Loading : ModelsUiState

    data class Error(val message: String) : ModelsUiState

    data class Success(
        val models: List<AiModel>,
        val isRefreshing: Boolean = false,
        /** Set when a refresh failed while data was already on screen. */
        val refreshError: String? = null,
    ) : ModelsUiState
}
```

`Error` means "nothing to show". A failed refresh with data present is
`Success(refreshError = ...)` — an inline notice, never a wiped list.

### ViewModel Pattern

```kotlin
class ModelsViewModel(
    private val repository: ModelsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val provider = MutableStateFlow<String?>(null)
    private val loadState = MutableStateFlow<LoadState>(LoadState.Loading)

    val uiState: StateFlow<ModelsUiState> =
        combine(repository.models, query, provider, loadState, ::toUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ModelsUiState.Loading,
            )

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            loadState.value = LoadState.Refreshing
            loadState.value = repository.refresh().fold(
                onSuccess = { LoadState.Idle },
                onFailure = { LoadState.Failed(it.userMessage()) },
            )
        }
    }

    fun onQueryChange(value: String) { query.value = value }
    fun onProviderChange(value: String?) { provider.value = value }
}
```

`toUiState` is a pure function — it is the thing the unit tests call.

### DI

Koin. Modules live in `di/`, one `module { }` per layer, all started from
`Application.onCreate`:

```kotlin
val dataModule = module {
    single { Json { ignoreUnknownKeys = true } }
    single { OkHttpClient.Builder().build() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::OpenRouterDataSource)
    singleOf(::DefaultModelsRepository) bind ModelsRepository::class
}

val uiModule = module {
    viewModelOf(::ModelsViewModel)
    viewModelOf(::ModelDetailViewModel)
}
```

## Data Flow Example

**Scenario**: show the models list

1. `ModelsViewModel` is created; `uiState` starts as `Loading`, `init` calls `refresh()`
2. `ModelsRepository.refresh()` joins the load already in flight, or starts one
3. `OpenRouterDataSource` performs the OkHttp call
4. kotlinx.serialization decodes the body into `NetworkModel`s
5. DTOs map to `AiModel`s and the data source sorts them newest first
6. The repository writes the list to its in-memory `StateFlow` as it comes
7. The ViewModel combines models + query + filter + load state
8. `toUiState` produces `Success`
9. The screen recomposes
10. A later failed refresh sets `refreshError`; the list stays on screen
