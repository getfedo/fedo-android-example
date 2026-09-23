package com.fedo.modelpulse.ui.roadmap

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fedo.modelpulse.R
import com.fedo.modelpulse.ui.theme.ModelPulseTheme
import com.fedo.sdk.ui.FedoFeedbackScreen
import com.fedo.sdk.ui.FedoFeedbackScreenDefaults
import com.fedo.sdk.ui.FedoFeedbackScreenSlots

/**
 * The Roadmap destination. With a key it is the SDK's board, which owns its
 * own list/detail/editor navigation; [onBack] only fires once that internal
 * stack is back at its root. Without a key it teaches instead of vanishing —
 * see specs/fedo-showcase.md.
 */
@Composable
internal fun RoadmapScreen(
    isConfigured: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isConfigured) {
        FedoFeedbackScreen(
            modifier = modifier.fillMaxSize(),
            onDismiss = onBack,
            slots = FedoFeedbackScreenDefaults.slots().copy(
                backButtonIcon = null
            )
        )
    } else {
        NotConfiguredState(modifier)
    }
}

@Composable
private fun NotConfiguredState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.roadmap_not_configured_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.roadmap_not_configured_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(R.string.roadmap_not_configured_snippet),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoadmapNotConfiguredPreview() {
    ModelPulseTheme {
        RoadmapScreen(isConfigured = false, onBack = {})
    }
}
