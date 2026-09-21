package com.fedo.modelpulse.ui.models

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.fedo.modelpulse.data.AiModel
import com.fedo.modelpulse.data.Price
import com.fedo.modelpulse.data.providerFilters
import com.fedo.modelpulse.ui.theme.ModelPulseTheme
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ModelsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val models = listOf(
        AiModel(
            id = "anthropic/claude-opus-5",
            providerSlug = "anthropic",
            shortName = "Claude Opus 5",
            providerName = "Anthropic",
            created = Instant.ofEpochSecond(1_756_000_000),
            description = "Most capable Claude model.",
            contextLength = 200_000,
            promptPrice = Price.PerToken(BigDecimal("0.000015")),
            completionPrice = Price.PerToken(BigDecimal("0.000075")),
            inputModalities = listOf("text"),
        ),
    )

    private fun setScreen(state: ModelsUiState, onRefresh: () -> Unit = {}) {
        composeTestRule.setContent {
            ModelPulseTheme {
                ModelsScreen(
                    uiState = state,
                    onModelClick = {},
                    onQueryChange = {},
                    onProviderChange = {},
                    onRefresh = onRefresh,
                )
            }
        }
    }

    @Test
    fun aC1_failedRefreshShowsASnackbarWithRetry() {
        var retried = false
        setScreen(
            ModelsUiState.Success(
                models = models,
                providers = models.providerFilters(),
                refreshError = "Couldn't reach OpenRouter.",
            ),
            onRefresh = { retried = true },
        )

        composeTestRule
            .onNodeWithText("Showing the models loaded earlier. Couldn't reach OpenRouter.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue(retried)
    }

    @Test
    fun aC2_theListKeepsItsDataAndHasNoInlineNotice() {
        setScreen(
            ModelsUiState.Success(
                models = models,
                providers = models.providerFilters(),
                refreshError = "Couldn't reach OpenRouter.",
            ),
        )

        composeTestRule.onNodeWithText("Claude Opus 5").assertIsDisplayed()
        // The old inline notice lived in the list; only the snackbar says this now.
        composeTestRule
            .onAllNodesWithText("Showing the models loaded earlier. Couldn't reach OpenRouter.")
            .assertCountEquals(1)
    }
}
