package com.unh.communityhelp.mainmenu.view.asset

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.unh.communityhelp.mainmenu.viewmodel.CreateHelpViewModel
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationSection(
    viewModel: CreateHelpViewModel,
    context: Context
) {
    val isInspectionMode = LocalInspectionMode.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionState = if (isInspectionMode) null else rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var showRationale by remember { mutableStateOf(false) }

    // Rationale Dialog
    if (showRationale) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location Permission Needed") },
            text = { Text("We need your location to show helpers exactly where you are. If you denied this previously, you may need to enable it in Settings.") },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    // If denied twice, launchPermissionRequest() does nothing,
                    // so we provide a way to jump to settings.
                    locationPermissionState?.let { state ->
                        if (!state.status.shouldShowRationale && !state.status.isGranted) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            state.launchPermissionRequest()
                        }
                    }
                }) { Text("Grant / Open Settings") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showRationale = false }) { Text("Cancel") }
            }
        )
    }

    OutlinedButton(
        onClick = {
            when {
                locationPermissionState?.status?.isGranted == true -> {
                    fetchCurrentLocation(context, fusedLocationClient, viewModel)
                }
                locationPermissionState?.status?.shouldShowRationale == true -> {
                    showRationale = true
                }
                else -> {
                    locationPermissionState?.launchPermissionRequest()
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !viewModel.isFetchingLocation
    ) {
        if (viewModel.isFetchingLocation) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.MyLocation, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = viewModel.locationAddress.ifEmpty { "Use Current Location" },
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 2000
                    ),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

private fun fetchCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: CreateHelpViewModel
) {
    viewModel.isFetchingLocation = true

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())

                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]

                        // Extracting the street details
                        val streetNumber = address.subThoroughfare ?: "" // e.g., "123"
                        val streetName = address.thoroughfare ?: ""     // e.g., "Main St"
                        val city = address.locality ?: ""               // e.g., "West Haven"
                        // Cleanly formatting the string
                        val fullAddress = if (streetName.isNotEmpty()) {
                            "$streetNumber $streetName, $city".trim()
                        } else {
                            // Fallback if street name isn't found (e.g., rural areas/parks)
                            address.getAddressLine(0) ?: "$city, ${address.adminArea}"
                        }
                        viewModel.cityName = city
                        viewModel.locationAddress = fullAddress
                    }
                    viewModel.isFetchingLocation = false
                }
            } else {
                viewModel.isFetchingLocation = false
                Toast.makeText(context, "Please turn on your GPS/Location services", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: SecurityException) {
        viewModel.isFetchingLocation = false
    }
}

// Add this helper function at the bottom of your file or in a Utils file
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}