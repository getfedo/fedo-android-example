package com.fedo.modelpulse.data

import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/** The public catalogue. No API key, no auth header. */
const val OPEN_ROUTER_MODELS_URL = "https://openrouter.ai/api/v1/models"

/**
 * Fetches the OpenRouter catalogue. [baseUrl] is a parameter so tests can point
 * it at a local server; production uses the default.
 */
class OpenRouterDataSource(
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val baseUrl: String = OPEN_ROUTER_MODELS_URL,
) {
    /**
     * Models, newest first. Never throws: a non-2xx response, a dropped
     * connection and a body that will not decode all come back as a failure.
     */
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

@Serializable
internal data class ModelsResponse(val data: List<NetworkModel>)

@Serializable
internal data class NetworkModel(
    val id: String,
    val name: String = "",
    val created: Long = 0L,
    val description: String? = null,
    @SerialName("context_length") val contextLength: Int? = null,
    val pricing: NetworkPricing? = null,
    val architecture: NetworkArchitecture? = null,
)

@Serializable
internal data class NetworkPricing(
    val prompt: String? = null,
    val completion: String? = null,
)

@Serializable
internal data class NetworkArchitecture(
    @SerialName("input_modalities") val inputModalities: List<String> = emptyList(),
)

/**
 * Wire format to domain. Ids are sometimes "~" prefixed and names are usually
 * "Provider: Model"; when the name carries no provider the id prefix is the
 * next best thing.
 */
internal fun NetworkModel.asExternalModel(): AiModel {
    val cleanId = id.removePrefix("~")
    val slug = cleanId.substringBefore('/').lowercase()
    return AiModel(
        id = cleanId,
        providerSlug = slug,
        shortName = name.substringAfter(": ", name).ifBlank { cleanId },
        providerName = name.substringBefore(": ", "").ifBlank { cleanId.substringBefore('/') },
        created = Instant.ofEpochSecond(created),
        description = description.orEmpty(),
        contextLength = contextLength,
        promptPrice = Price.parse(pricing?.prompt.orEmpty()),
        completionPrice = Price.parse(pricing?.completion.orEmpty()),
        inputModalities = architecture?.inputModalities.orEmpty(),
    )
}
