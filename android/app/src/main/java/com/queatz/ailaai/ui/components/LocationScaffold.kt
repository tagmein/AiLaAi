package com.queatz.ailaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.ailaai.R
import com.queatz.ailaai.extensions.goToSettings
import com.queatz.ailaai.helpers.LocationSelector
import com.queatz.ailaai.nav
import com.queatz.ailaai.ui.theme.pad

@Composable
fun LocationScaffold(
    geo: LatLng?,
    locationSelector: LocationSelector,
    appHeader: @Composable ColumnScope.() -> Unit = {},
    rationale: @Composable ColumnScope.() -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val nav = nav

    when {
        !enabled -> {
            content()
        }

        geo == null && !locationSelector.isGranted -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                appHeader()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.pad, Alignment.CenterVertically),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.pad)
                ) {
                    rationale()

                    val showOpenSettings = locationSelector.isPermanentlyDenied
                    Button(
                        {
                            if (showOpenSettings) {
                                nav.goToSettings()
                            } else {
                                locationSelector.launchPermissionRequest()
                            }
                        }
                    ) {
                        Text(if (showOpenSettings) stringResource(R.string.open_settings) else stringResource(R.string.use_my_location))
                    }

                    if (showOpenSettings) {
                        Text(
                            stringResource(R.string.location_disabled_description),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    TextButton({
                        locationSelector.setLocationManually()
                    }) {
                        Text(stringResource(R.string.set_my_location))
                    }
                }
            }
        }

        geo == null -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                appHeader()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.pad, Alignment.CenterVertically),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.pad)
                ) {
                    Text(stringResource(R.string.finding_your_location), color = MaterialTheme.colorScheme.secondary)
                    TextButton({
                        locationSelector.setLocationManually()
                    }) {
                        Text(stringResource(R.string.set_my_location))
                    }
                }
            }
        }

        else -> {
            content()
        }
    }
}
