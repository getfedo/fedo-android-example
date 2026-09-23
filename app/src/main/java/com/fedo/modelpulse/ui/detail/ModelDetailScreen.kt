package com.fedo.modelpulse.ui.detail

import android.content.ClipData
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fedo.modelpulse.R
import com.fedo.modelpulse.data.AiModel
import com.fedo.modelpulse.data.Price
import com.fedo.modelpulse.data.contextLabel
import com.fedo.modelpulse.data.perMillionLabel
import com.fedo.modelpulse.data.relativeLabel
import com.fedo.modelpulse.ui.mergePaddingValues
import com.fedo.modelpulse.ui.theme.ModelPulseTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ModelDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ModelDetailViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    ModelDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCopyId = { id ->
            // LocalClipboard, not the deprecated LocalClipboardManager: the
            // write is a suspend call now.
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(CLIP_LABEL, id)))
            }
        },
        modifier = modifier,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelDetailScreen(
    uiState: ModelDetailUiState,
    onBackClick: () -> Unit,
    onCopyId: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? ModelDetailUiState.Success)
                            ?.model
                            ?.shortName
                            .orEmpty(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val mergedContentPadding = mergePaddingValues(innerPadding, contentPadding)

        when (uiState) {
            ModelDetailUiState.Loading -> LoadingState(Modifier.padding(mergedContentPadding))

            ModelDetailUiState.NotFound -> Text(
                text = stringResource(R.string.detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(mergedContentPadding)
                    .padding(32.dp),
            )

            is ModelDetailUiState.Success -> ModelDetailContent(
                model = uiState.model,
                onCopyId = onCopyId,
                contentPadding = mergedContentPadding,
            )
        }
    }
}

@Composable
private fun ModelDetailContent(
    model: AiModel,
    onCopyId: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // A long description scrolls rather than clipping.
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(model.providerName, style = MaterialTheme.typography.titleMedium)

        ModelIdRow(id = model.id, onCopyId = onCopyId)

        HorizontalDivider()

        DetailRow(
            label = stringResource(R.string.detail_input_price),
            value = model.promptPrice.perMillionLabel(),
        )
        DetailRow(
            label = stringResource(R.string.detail_output_price),
            value = model.completionPrice.perMillionLabel(),
        )
        DetailRow(
            label = stringResource(R.string.detail_context),
            value = model.contextLength.contextLabel(),
        )
        DetailRow(
            label = stringResource(R.string.detail_released),
            value = model.created.relativeLabel(),
        )
        DetailRow(
            label = stringResource(R.string.detail_modalities),
            value = model.inputModalities.takeIf { it.isNotEmpty() }?.joinToString()
                ?: stringResource(R.string.detail_modalities_unknown),
        )

        if (model.description.isNotBlank()) {
            HorizontalDivider()
            Text(model.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ModelIdRow(id: String, onCopyId: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = id,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onCopyId(id) }) {
                Text(stringResource(R.string.detail_copy_id))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ContainedLoadingIndicator()
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private annotation class ThemePreviews

@ThemePreviews
@Composable
private fun ModelDetailScreenPreview(
    @PreviewParameter(ModelDetailUiStateProvider::class) uiState: ModelDetailUiState,
) {
    ModelPulseTheme {
        ModelDetailScreen(uiState = uiState, onBackClick = {}, onCopyId = {})
    }
}

private class ModelDetailUiStateProvider : PreviewParameterProvider<ModelDetailUiState> {
    override val values = sequenceOf(
        ModelDetailUiState.Loading,
        ModelDetailUiState.NotFound,
        ModelDetailUiState.Success(previewModel),
    )
}

private const val CLIP_LABEL = "model id"

private val previewModel = AiModel(
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
)
