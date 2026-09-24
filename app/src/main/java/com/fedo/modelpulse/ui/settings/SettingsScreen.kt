package com.fedo.modelpulse.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fedo.modelpulse.R
import com.fedo.modelpulse.ui.common.ModelPulseTopBar
import com.fedo.modelpulse.ui.mergePaddingValues
import com.fedo.modelpulse.ui.theme.ModelPulseTheme
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SettingsRoute(
    onRoadmapClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onSignIn = viewModel::signIn,
        onSignOut = viewModel::signOut,
        onRoadmapClick = onRoadmapClick,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onRoadmapClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            ModelPulseTopBar(
                title = stringResource(R.string.settings_title),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val mergedContentPadding = mergePaddingValues(innerPadding, contentPadding)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(mergedContentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SdkStatusCard(isConfigured = uiState.isConfigured)

            DemoAccountCard(
                uiState = uiState,
                onNameChange = onNameChange,
                onEmailChange = onEmailChange,
                onSignIn = onSignIn,
                onSignOut = onSignOut,
            )

            Button(
                onClick = onRoadmapClick
            ) {
                Text(stringResource(R.string.nav_roadmap))
            }
        }
    }

}

@Composable
private fun SdkStatusCard(isConfigured: Boolean, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_sdk_status),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    if (isConfigured) R.string.settings_sdk_configured
                    else R.string.settings_sdk_not_configured,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!isConfigured) {
                Text(
                    text = stringResource(R.string.roadmap_not_configured_snippet),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun DemoAccountCard(
    uiState: SettingsUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_demo_account),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_demo_explainer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val signedIn = uiState.signedInAs
            if (signedIn == null) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    enabled = uiState.isConfigured,
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    enabled = uiState.isConfigured,
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_email)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = onSignIn, enabled = uiState.canSignIn) {
                    Text(stringResource(R.string.settings_sign_in))
                }
            } else {
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.settings_signed_in_as, signedIn.name),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(signedIn.email, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = signedIn.id,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onSignOut) {
                    Text(stringResource(R.string.settings_sign_out))
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsUiStateProvider::class) uiState: SettingsUiState,
) {
    ModelPulseTheme {
        SettingsScreen(
            uiState = uiState,
            onNameChange = {},
            onEmailChange = {},
            onSignIn = {},
            onSignOut = {},
            onRoadmapClick = {}
        )
    }
}

private class SettingsUiStateProvider : PreviewParameterProvider<SettingsUiState> {
    override val values = sequenceOf(
        SettingsUiState(isConfigured = true),
        SettingsUiState(
            isConfigured = true,
            signedInAs = DemoUser("demo-4f2c", DEFAULT_NAME, DEFAULT_EMAIL),
        ),
        SettingsUiState(isConfigured = false),
    )
}
