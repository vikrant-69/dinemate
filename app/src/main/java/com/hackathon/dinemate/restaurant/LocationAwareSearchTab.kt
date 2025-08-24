package com.hackathon.dinemate.restaurant

import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White
import kotlinx.coroutines.tasks.await
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAwareSearchTab(
    padding: PaddingValues,
    baseURL: String,
    viewModel: SearchViewModel = viewModel(),
    onRestaurantClick: (Restaurant) -> Unit = {}
) {
    val context = LocalContext.current

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            getCurrentLocation(fusedLocationClient, context, viewModel)
        }
    }

    // Auto-request permissions when tab opens
    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    when {
        locationPermissionState.allPermissionsGranted -> {
            SearchTab(
                padding = padding,
                baseURL = baseURL,
                viewModel = viewModel,
                onRestaurantClick = onRestaurantClick
            )
        }

        locationPermissionState.shouldShowRationale -> {
            LocationPermissionRationale(
                onRequestPermission = { locationPermissionState.launchMultiplePermissionRequest() },
                padding = padding
            )
        }

        else -> {
            LocationPermissionDenied(
                onRequestPermission = { locationPermissionState.launchMultiplePermissionRequest() },
                padding = padding
            )
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    context: android.content.Context,
    viewModel: SearchViewModel
) {
    try {
        val location = fusedLocationClient.lastLocation.await()
        if (location != null) {
            val address = getAddressFromLocation(context, location.latitude, location.longitude)
            viewModel.setCurrentLocation(location.latitude, location.longitude, address)
        }
    } catch (e: Exception) {
        // Handle location fetch error
    }
}

private fun getAddressFromLocation(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            "${address.locality ?: ""}, ${address.adminArea ?: ""}"
        } else {
            "Current Location"
        }
    } catch (e: Exception) {
        "Current Location"
    }
}

@Composable
private fun LocationPermissionRationale(
    onRequestPermission: () -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LightGrey),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📍",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Location Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Charcoal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We need location access to find restaurants near you and provide accurate search results.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGrey
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Charcoal,
                        contentColor = White
                    )
                ) {
                    Text("Grant Location Permission")
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionDenied(
    onRequestPermission: () -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LightGrey),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚫",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Location Permission Needed",
                    style = MaterialTheme.typography.titleLarge,
                    color = Charcoal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please enable location access from settings to search for nearby restaurants.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
