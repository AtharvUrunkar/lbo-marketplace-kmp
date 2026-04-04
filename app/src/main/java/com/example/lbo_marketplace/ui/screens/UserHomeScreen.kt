package com.example.lbo_marketplace.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lbo_marketplace.auth.AuthViewModel
import com.example.lbo_marketplace.auth.ProviderViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun UserHomeScreen(
    authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    val providerViewModel: ProviderViewModel = viewModel()
    val user = FirebaseAuth.getInstance().currentUser

    var locationText by remember { mutableStateOf("Location not fetched") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocation(context) { loc ->
                locationText = loc
            }
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("👋 Welcome User")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Email: ${user?.email ?: "N/A"}")

        Spacer(modifier = Modifier.height(16.dp))

        // LOCATION BUTTON
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getLocation(context) { loc ->
                    locationText = loc
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }) {
            Text("Get Location")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("📍 $locationText")

        Spacer(modifier = Modifier.height(16.dp))

        // APPLY BUTTON
        Button(onClick = {
            user?.let {
                providerViewModel.apply(it.uid, it.email ?: "")
            }
        }) {
            Text("Apply as Service Provider")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(providerViewModel.applyState)

        Spacer(modifier = Modifier.height(16.dp))

        // LOGOUT
        Button(onClick = {
            authViewModel.logout()
        }) {
            Text("Logout")
        }
    }
}

/**
 * 🔥 IMPORTANT: This function MUST be OUTSIDE composable
 */
@SuppressLint("MissingPermission")
fun getLocation(context: Context, onResult: (String) -> Unit) {

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                onResult("Lat: $lat, Lng: $lng")
            } else {
                onResult("Location not available")
            }
        }
        .addOnFailureListener {
            onResult("Failed to get location")
        }
}