package com.fedo.modelpulse.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * @see TopBarBackButton
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPulseTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        actions = actions,
        navigationIcon = navigationIcon,
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )

}