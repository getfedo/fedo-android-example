# Testing Patterns

Test doubles, no mocking libraries.

## Table of Contents
1. [Testing Philosophy](#testing-philosophy)
2. [Test Doubles](#test-doubles)
3. [Pure Function Tests](#pure-function-tests)
4. [ViewModel Tests](#viewmodel-tests)
5. [Repository & Parsing Tests](#repository--parsing-tests)
6. [UI Tests](#ui-tests)
7. [Test Utilities](#test-utilities)

## Testing Philosophy

### No Mocking Libraries

No Mockito, no MockK. Write a test double that implements the same
interface. Doubles are realistic implementations with test hooks; tests
stay readable and exercise more production code.

### Acceptance criteria drive tests

Constitution rule: **every acceptance criterion maps to ≥1 test named after
its ID.** Beads acceptance criteria are written as `AC-1: …`, `AC-2: …`;
the test carries the ID in its name:

```kotlin
@Test
fun `AC-1 list shows newest models first`() { … }

@Test
fun `AC-3 failed refresh with data present keeps the list and reports the error`() { … }
```

### Test Types

| Type | Location | Runner | Purpose |
|------|----------|--------|---------|
| Unit tests | `app/src/test/` | JVM | Formatters, parsing, filtering, ViewModels |
| UI tests | `app/src/androidTest/` | Device/Emulator | Compose screens |

Most of ModelPulse's logic is pure functions. Prefer a unit test over an
instrumented test whenever the behaviour can be reached from the JVM.

## Test Doubles

### Test Repository

```kotlin
class TestModelsRepository : ModelsRepository {

    private val _models = MutableStateFlow(emptyList<AiModel>())
    override val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    // Test hooks
    var refreshResult: Result<Unit> = Result.success(Unit)
    var refreshCount = 0
        private set

    fun sendModels(models: List<AiModel>) { _models.value = models }

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        return refreshResult.onSuccess { /* caller decides via sendModels */ }
    }
}
```

A failing double keeps whatever `sendModels` already emitted — that is the
behaviour the "failed refresh keeps data" criterion depends on.

### Fake HTTP responses

The remote data source is tested against OkHttp's `MockWebServer`, not a
hand-written double: it is the only way to cover real decoding, status
codes and malformed bodies.

```kotlin
class OpenRouterDataSourceTest {

    @get:Rule
    val server = MockWebServerRule()

    private val dataSource = OpenRouterDataSource(
        client = OkHttpClient(),
        json = Json { ignoreUnknownKeys = true },
        baseUrl = server.url("/"),
    )

    @Test
    fun `decodes the models payload`() = runTest {
        server.enqueue(MockResponse(body = readResource("models.json")))

        val models = dataSource.getModels()

        assertEquals("anthropic/claude-opus-5", models.first().id)
    }

    @Test
    fun `unknown fields do not break decoding`() = runTest {
        server.enqueue(MockResponse(body = """{"data":[{"id":"a/b","name":"A: B","created":1,"pricing":{"prompt":"0","completion":"0"},"brand_new_field":true}]}"""))

        assertEquals(1, dataSource.getModels().size)
    }
}
```

The data source takes its `baseUrl` as a constructor parameter so tests can
point it at the local server. Production passes the OpenRouter URL from the
Koin module.

Keep a trimmed real response at `app/src/test/resources/models.json` — a
handful of entries covering the ugly cases: `"~"`-prefixed id, `null`
`context_length`, `"0"` pricing, `"-1"` pricing, missing `architecture`.

## Pure Function Tests

The highest-value tests in this project. No rules, no coroutines, no
Android.

```kotlin
class PriceTest {

    @Test
    fun `zero price is free`() {
        assertEquals(Price.Free, Price.parse("0"))
    }

    @Test
    fun `negative one price is variable`() {
        assertEquals(Price.Variable, Price.parse("-1"))
    }

    @Test
    fun `per token price renders per million`() {
        assertEquals("$15.00 / 1M", Price.parse("0.000015").perMillionLabel())
    }

    @Test
    fun `unparseable price is unknown`() {
        assertEquals(Price.Unknown, Price.parse("cheap"))
    }
}

class FilterTest {

    @Test
    fun `query matches name and id case-insensitively`() {
        val results = models.filterBy(query = "OPUS", provider = null)

        assertEquals(listOf("anthropic/claude-opus-5"), results.map(AiModel::id))
    }

    @Test
    fun `provider filter and query combine`() {
        val results = models.filterBy(query = "gpt", provider = "openai")

        assertTrue(results.all { it.providerSlug == "openai" })
    }
}
```

## ViewModel Tests

### Test Dispatcher Rule

```kotlin
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

### Setup

```kotlin
class ModelsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = TestModelsRepository()
    private lateinit var viewModel: ModelsViewModel

    @Before
    fun setup() {
        viewModel = ModelsViewModel(repository)
    }

    @Test
    fun `AC-1 uiState is Loading before the first load completes`() = runTest {
        assertEquals(ModelsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `AC-1 models are shown newest first`() = runTest {
        repository.sendModels(testModels)

        val state = viewModel.uiState.first { it is ModelsUiState.Success } as ModelsUiState.Success

        assertEquals(testModels.sortedByDescending(AiModel::created), state.models)
    }

    @Test
    fun `AC-2 offline first load shows an error state with retry`() = runTest {
        repository.refreshResult = Result.failure(IOException("offline"))

        viewModel.refresh()

        assertTrue(viewModel.uiState.first() is ModelsUiState.Error)
    }

    @Test
    fun `AC-3 failed refresh keeps the list and reports inline`() = runTest {
        repository.sendModels(testModels)
        viewModel.uiState.first { it is ModelsUiState.Success }

        repository.refreshResult = Result.failure(IOException("offline"))
        viewModel.refresh()

        val state = viewModel.uiState.first {
            it is ModelsUiState.Success && it.refreshError != null
        } as ModelsUiState.Success

        assertEquals(testModels.size, state.models.size)
        assertNotNull(state.refreshError)
    }
}
```

### Testing StateFlow with Turbine

```kotlin
@Test
fun `uiState emits Loading then Success`() = runTest {
    viewModel.uiState.test {
        assertEquals(ModelsUiState.Loading, awaitItem())

        repository.sendModels(testModels)

        assertTrue(awaitItem() is ModelsUiState.Success)
        cancelAndIgnoreRemainingEvents()
    }
}
```

## Repository & Parsing Tests

```kotlin
class DefaultModelsRepositoryTest {

    private val remote = TestOpenRouterDataSource()
    private val scope = TestScope()
    private val repository = DefaultModelsRepository(remote, scope)

    @Test
    fun `refresh publishes models newest first`() = runTest {
        remote.response = Result.success(testModels)

        repository.refresh()

        assertEquals(
            testModels.sortedByDescending(AiModel::created),
            repository.models.value,
        )
    }

    @Test
    fun `failed refresh keeps the previous models`() = runTest {
        remote.response = Result.success(testModels)
        repository.refresh()

        remote.response = Result.failure(IOException("offline"))
        val result = repository.refresh()

        assertTrue(result.isFailure)
        assertEquals(testModels.size, repository.models.value.size)
    }

    @Test
    fun `a second refresh joins the in-flight one instead of fetching twice`() = runTest {
        // remote.gate suspends the first call until released
        …
    }
}
```

## UI Tests

Screens are stateless, so Compose tests drive them directly — no ViewModel,
no Koin.

```kotlin
class ModelsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `loading state shows progress indicator`() {
        composeTestRule.setContent {
            ModelPulseTheme {
                ModelsScreen(
                    uiState = ModelsUiState.Loading,
                    onModelClick = {},
                    onQueryChange = {},
                    onProviderChange = {},
                    onRefresh = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun `AC-2 error state retry triggers the callback`() {
        var retried = false

        composeTestRule.setContent {
            ModelPulseTheme {
                ModelsScreen(
                    uiState = ModelsUiState.Error("No internet connection"),
                    onModelClick = {},
                    onQueryChange = {},
                    onProviderChange = {},
                    onRefresh = { retried = true },
                )
            }
        }

        // Resolved from the same resource the screen uses — never a copied
        // literal, so renaming the string cannot leave a test asserting stale
        // copy (constitution).
        val retry = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getString(R.string.models_retry)

        composeTestRule.onNodeWithText(retry).performClick()

        assertTrue(retried)
    }
}
```

## Test Utilities

### Test Data Factory

```kotlin
// app/src/test/java/com/fedo/modelpulse/TestData.kt
object TestData {
    val claudeOpus = AiModel(
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
    )

    val freeModel = claudeOpus.copy(
        id = "meta-llama/llama-3-8b:free",
        providerSlug = "meta-llama",
        shortName = "Llama 3 8B",
        providerName = "Meta",
        created = Instant.ofEpochSecond(1_713_000_000),
        contextLength = null,
        promptPrice = Price.Free,
        completionPrice = Price.Free,
    )

    val testModels = listOf(claudeOpus, freeModel)
}
```

### Gradle Test Configuration

```kotlin
// app/build.gradle.kts
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

Koin is not needed in unit tests — ViewModels and repositories take their
dependencies as constructor parameters. If a test ever needs the graph,
`koin-test-junit4` is in the catalog; verifying module definitions with
`checkModules()` is the only good reason to reach for it.

## Running Tests

```bash
# Unit tests (the default gate after every bead)
./gradlew :app:testDebugUnitTest

# Lint
./gradlew :app:lintDebug

# Instrumented tests (device or emulator required)
./gradlew :app:connectedDebugAndroidTest

# Everything the constitution calls "done"
./gradlew build test lint
```
