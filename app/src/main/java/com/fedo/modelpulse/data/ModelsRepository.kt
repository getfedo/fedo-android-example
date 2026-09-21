package com.fedo.modelpulse.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The only way into the catalogue. Remote is the source of truth. */
interface ModelsRepository {

    /** Last known models, newest first. Empty until the first load succeeds. */
    val models: StateFlow<List<AiModel>>

    /**
     * Loads from the network. A call made while a load is already running
     * joins it and gets that load's result, rather than starting a second one.
     */
    suspend fun refresh(): Result<Unit>
}

/**
 * Keeps the last successful response in memory for the session. Nothing is
 * persisted: a cold start always hits the network.
 */
internal class DefaultModelsRepository(
    private val remote: OpenRouterDataSource,
    private val scope: CoroutineScope,
) : ModelsRepository {

    private val _models = MutableStateFlow(emptyList<AiModel>())
    override val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    private var inFlight: Deferred<Result<Unit>>? = null
    private val lock = Mutex()

    /**
     * A failure leaves [models] untouched, so a refresh that fails can never
     * wipe what is already on screen.
     *
     * The catalogue is one unparameterised GET, so a second caller wants
     * exactly what the first one is already fetching: it joins that load and
     * shares its result. Cancelling the first load instead would leave its
     * caller with a CancellationException and no result at all.
     */
    override suspend fun refresh(): Result<Unit> = currentOrNewLoad().await()

    private suspend fun currentOrNewLoad(): Deferred<Result<Unit>> = lock.withLock {
        inFlight?.takeIf { it.isActive }?.let { return it }

        // Runs on an application-scoped CoroutineScope, so the load outlives
        // the ViewModel that asked for it.
        scope.async {
            remote.getModels()
                .onSuccess { models -> _models.value = models }
                .map { }
        }.also { inFlight = it }
    }
}
