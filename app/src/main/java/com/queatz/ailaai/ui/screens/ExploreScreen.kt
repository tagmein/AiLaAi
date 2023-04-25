package com.queatz.ailaai.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import at.bluesource.choicesdk.core.Outcome
import at.bluesource.choicesdk.location.common.LocationRequest
import at.bluesource.choicesdk.location.factory.FusedLocationProviderFactory
import at.bluesource.choicesdk.maps.common.LatLng
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.huawei.hms.hmsscankit.ScanKitActivity
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.queatz.ailaai.*
import com.queatz.ailaai.R
import com.queatz.ailaai.extensions.distance
import com.queatz.ailaai.extensions.toggle
import com.queatz.ailaai.ui.components.CardParentSelector
import com.queatz.ailaai.ui.components.CardsList
import com.queatz.ailaai.ui.components.horizontalFadingEdge
import com.queatz.ailaai.ui.dialogs.SetLocationDialog
import com.queatz.ailaai.ui.state.jsonSaver
import com.queatz.ailaai.ui.state.latLngSaver
import com.queatz.ailaai.ui.theme.ElevationDefault
import com.queatz.ailaai.ui.theme.PaddingDefault
import io.ktor.utils.io.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val geoKey = stringPreferencesKey("geo")
private val geoManualKey = booleanPreferencesKey("geo-manual")

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExploreScreen(navController: NavController, me: () -> Person?) {
    val context = LocalContext.current
    val locationClient = FusedLocationProviderFactory.getFusedLocationProviderClient(
        navController.context as Activity
    )
    var value by rememberSaveable { mutableStateOf("") }
    var geo: LatLng? by rememberSaveable(stateSaver = latLngSaver()) { mutableStateOf(null) }
    var shownGeo: LatLng? by rememberSaveable(stateSaver = latLngSaver()) { mutableStateOf(null) }
    var shownValue by rememberSaveable { mutableStateOf("") }
    var geoManual by remember { mutableStateOf(false) }
    var showSetMyLocation by remember { mutableStateOf(false) }
    var cards by rememberSaveable(stateSaver = jsonSaver<List<Card>>()) { mutableStateOf(listOf()) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val initialCameraPermissionState by remember { mutableStateOf(cameraPermissionState.status.isGranted) }
    var showCameraRationale by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var hasInitialCards by remember { mutableStateOf(cards.isNotEmpty()) }
    var isLoading by remember { mutableStateOf(cards.isEmpty()) }
    var isError by remember { mutableStateOf(false) }

    fun goToSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${navController.context.packageName}")
        )
        (navController.context as Activity).startActivity(intent)
    }

    LaunchedEffect(Unit) {
        geoManual = !locationPermissionState.status.isGranted || context.dataStore.data.first()[geoManualKey] == true
    }

    LaunchedEffect(geoManual) {
        context.dataStore.edit {
            if (geoManual) {
                it[geoManualKey] = true
            } else {
                it.remove(geoManualKey)
            }
        }
    }

    LaunchedEffect(geo) {
        if (geo == null) {
            val savedGeo = context.dataStore.data.first()[geoKey]?.split(",")?.map { it.toDouble() }
            if (savedGeo != null)
                geo = LatLng.getFactory().create(savedGeo[0], savedGeo[1])
        } else {
            context.dataStore.edit {
                if (geo == null) {
                    it.remove(geoKey)
                } else {
                    it[geoKey] = "${geo!!.latitude},${geo!!.longitude}"
                }
            }
        }
    }

    LaunchedEffect(geo, value) {
        if (geo == null) {
            return@LaunchedEffect
        }

        if (hasInitialCards) {
            hasInitialCards = false

            if (cards.isNotEmpty()) {
                return@LaunchedEffect
            }
        }

        // Don't reload if moving < 100m
        if (shownGeo != null && geo!!.distance(shownGeo!!) < 100 && shownValue == value) {
            return@LaunchedEffect
        }

        try {
            isLoading = true
            cards = api.cards(geo!!, value.takeIf { it.isNotBlank() })
                .filter { it.equipped != true || it.person != me()?.id }

            shownGeo = geo
            shownValue = value
            isError = false
            isLoading = false
        } catch (ex: Exception) {
            if (ex is CancellationException || ex is InterruptedException) {
                // Ignore, probably geo or search value changed
            } else {
                isError = true
                isLoading = false
                ex.printStackTrace()
            }
        }
    }

    val scanQrLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getParcelableExtra<HmsScan?>(ScanUtil.RESULT)
                ?.let {
                    it.linkUrl?.linkValue?.takeIf { it.startsWith(appDomain) }?.split("/card/")?.last()
                }
                ?.let { cardId ->
                    navController.navigate("card/$cardId")
                } ?: run {
                Toast.makeText(
                    context,
                    context.getString(R.string.didnt_work),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun scan() {
        if (cameraPermissionState.status.isGranted) {
            scanQrLauncher.launch(
                // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-parsing-result-codes-0000001050043969
                // Extracted from ScanUtil.java (startScan)
                Intent(navController.context as Activity, ScanKitActivity::class.java).apply {
                    putExtra("ScanFormatValue", HmsScan.QRCODE_SCAN_TYPE)
                    putExtra("ScanViewValue", 1)
                }
            )
        } else {
            if (cameraPermissionState.status.shouldShowRationale) {
                showCameraRationale = true
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    if (!initialCameraPermissionState) {
        LaunchedEffect(cameraPermissionState.status.isGranted) {
            if (cameraPermissionState.status.isGranted) {
                scan()
            }
        }
    }

    if (geo == null && !locationPermissionState.status.isGranted) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefault * 2, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingDefault)
        ) {
            val showOpenSettings = locationPermissionState.status.shouldShowRationale

            Button(
                {
                    if (showOpenSettings) {
                        goToSettings()
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                Text(if (showOpenSettings) stringResource(R.string.open_settings) else stringResource(R.string.find_my_location))
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
                showSetMyLocation = true
            }) {
                Text(stringResource(R.string.set_my_location))
            }
        }
    } else if (geo == null) {
        LaunchedEffect(Unit) {
            locationClient.observeLocation(LocationRequest.createDefault())
                .filter { it is Outcome.Success && it.value.lastLocation != null }
                .takeWhile { coroutineScope.isActive }
                // todo dispose on close
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    geoManual = false
                    geo = (it as Outcome.Success).value.lastLocation!!.toLatLng()
                }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefault, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingDefault)
        ) {
            Text(stringResource(R.string.finding_your_location), color = MaterialTheme.colorScheme.secondary)
            TextButton({
                showSetMyLocation = true
            }) {
                Text(stringResource(R.string.set_my_location))
            }
        }
    } else {
        CardsList(
            cards = cards,
            isMine = { it.person == me()?.id },
            geo = geo,
            isLoading = isLoading,
            isError = isError,
            value = value,
            valueChange = { value = it },
            navController = navController,
            useDistance = true,
            action = {
                Icon(Icons.Outlined.QrCodeScanner, stringResource(R.string.scan))
            },
            onAction = {
                scan()
            }
        ) {
            if (geoManual) {
                ElevatedButton(
                    elevation = ButtonDefaults.elevatedButtonElevation(ElevationDefault * 2),
                    onClick = {
                        coroutineScope.launch {
                            context.dataStore.edit {
                                it.remove(geoKey)
                                it.remove(geoManualKey)
                            }
                            geo = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.reset_location), modifier = Modifier.padding(end = PaddingDefault))
                    Icon(Icons.Outlined.Clear, "")
                }
            }
            if ("show categories".isEmpty()) {
                var viewport by remember { mutableStateOf(Size(0f, 0f)) }
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)

                        .onPlaced { viewport = it.boundsInParent().size }
                        .horizontalFadingEdge(viewport, scrollState)
                ) {
                    listOf(
                        "Classes",
                        "Photography",
                        "Food Delivery",
                        "Arts & Crafts",
                        "Pets",
                        "Home Services",
                        "Goods",
                        "Secret Rooms",
                        "Philosophy",
                    ).forEachIndexed { index, category ->
                        OutlinedButton(
                            {
                                // select category
                            },
                            border = IconButtonDefaults.outlinedIconToggleButtonBorder(true, index == 1),
                            colors = if (index != 1) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ) else ButtonDefaults.buttonColors(),
                            modifier = Modifier.padding(end = PaddingDefault)
                        ) {
                            Text(category)
                        }
                    }
                }
            }
        }
    }

    if (showSetMyLocation) {
        SetLocationDialog({ showSetMyLocation = false }) {
            geoManual = true
            geo = it
        }
    }

    if (showCameraRationale) {
        AlertDialog(
            { showCameraRationale = false },
            text = {
                Text(stringResource(R.string.camera_disabled_description))
            },
            confirmButton = {
                TextButton(
                    {
                        showCameraRationale = false
                        goToSettings()
                    }
                ) {
                    Text(stringResource(R.string.open_settings))
                }
            }
        )
    }
}
