package com.unh.communityhelp.mainmenu.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepository(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCity(): String {
        return try {
            // 1. Get the raw coordinates from the GPS chip
            val location = fusedLocationClient.lastLocation.await() ?: return "Unknown"

            // 2. Use the modern SDK 33+ Async Geocoder
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ) { addresses ->
                    // locality is the standard "City" field
                    val city = addresses.firstOrNull()?.locality ?: "Unknown"
                    continuation.resume(city)
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}