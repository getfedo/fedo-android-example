package com.fedo.modelpulse.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.junit4.MockWebServerRule
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OpenRouterDataSourceTest {

    @get:Rule
    val serverRule = MockWebServerRule()

    private fun dataSource() = OpenRouterDataSource(
        client = OkHttpClient(),
        json = Json { ignoreUnknownKeys = true },
        baseUrl = serverRule.server.url("/models").toString(),
    )

    @Test
    fun `AC-1 models come back newest first whatever order the wire had`() = runTest {
        serverRule.server.enqueue(MockResponse(body = OUT_OF_ORDER_PAYLOAD))

        val models = dataSource().getModels().getOrThrow()

        assertEquals(listOf("newest", "middle", "oldest"), models.map(AiModel::shortName))
    }

    @Test
    fun `AC-1 the captured OpenRouter response decodes with the production config`() = runTest {
        serverRule.server.enqueue(MockResponse(body = readResource("models.json")))

        val models = dataSource().getModels().getOrThrow()

        assertEquals(6, models.size)
        // Newest first, and every entry survived the awkward fields.
        assertTrue(models.zipWithNext().all { (a, b) -> a.created >= b.created })
    }

    @Test
    fun `AC-2 the fixture's awkward entries map the way the UI expects`() = runTest {
        serverRule.server.enqueue(MockResponse(body = readResource("models.json")))

        val models = dataSource().getModels().getOrThrow().associateBy(AiModel::id)

        // A "~"-prefixed id loses the tilde and still groups by provider.
        val tilde = models.getValue("deepseek/deepseek-pro-latest")
        assertEquals("deepseek", tilde.providerSlug)

        // "0" is free, "-1" is variable.
        assertEquals(Price.Free, models.getValue("inclusionai/ling-3.0-flash-vl:free").promptPrice)
        assertEquals(Price.Variable, models.getValue("openrouter/auto-beta").promptPrice)

        // A null context_length and a missing architecture/pricing block are
        // both survivable: the entry is still there.
        assertNull(models.getValue("mistralai/mistral-medium-3-5").contextLength)
        val noArchitecture = models.getValue("google/gemini-3.8-flash")
        assertTrue(noArchitecture.inputModalities.isEmpty())
        // No pricing block at all: unknown, not "variable pricing".
        assertEquals(Price.Unknown, noArchitecture.promptPrice)
    }

    @Test
    fun `a non-2xx response is a failure, not an exception`() = runTest {
        serverRule.server.enqueue(MockResponse(code = 503))

        val result = dataSource().getModels()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("503") == true)
    }

    @Test
    fun `a body that will not decode is a failure`() = runTest {
        serverRule.server.enqueue(MockResponse(body = "not json"))

        assertTrue(dataSource().getModels().isFailure)
    }

    @Test
    fun `unknown fields do not break decoding`() = runTest {
        serverRule.server.enqueue(
            MockResponse(
                body = """{"data":[{"id":"a/b","name":"A: B","created":1,"brand_new_field":true}]}""",
            ),
        )

        val models = dataSource().getModels().getOrThrow()

        assertEquals(1, models.size)
        assertEquals(Price.Unknown, models.first().promptPrice)
    }

    private fun readResource(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "missing $name" }
            .bufferedReader()
            .use { it.readText() }
}

private val OUT_OF_ORDER_PAYLOAD = """
{"data":[
  {"id":"p/middle","name":"P: middle","created":2000,"context_length":32768,
   "pricing":{"prompt":"0.000015","completion":"0.000075"}},
  {"id":"~p/oldest","name":"P: oldest","created":1000,"context_length":null,
   "pricing":{"prompt":"0","completion":"0"}},
  {"id":"p/newest","name":"P: newest","created":3000,
   "pricing":{"prompt":"-1","completion":"-1"},
   "architecture":{"input_modalities":["text","image"]}}
]}
""".trimIndent()
