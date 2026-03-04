package com.lbo.app.presentation.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lbo.app.presentation.components.LBOButton
import com.lbo.app.presentation.components.LBOTextField
import com.lbo.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileSetupScreen(
    profileState: ProviderProfileState,
    onSaveProfile: (String, String, String, String, Int, Uri?, List<Uri>) -> Unit,
    onBack: () -> Unit
) {
    val provider = profileState.provider
    var name by remember { mutableStateOf(provider?.name ?: "") }
    var category by remember { mutableStateOf(provider?.category ?: "") }
    var location by remember { mutableStateOf(provider?.location ?: "") }
    var description by remember { mutableStateOf(provider?.description ?: "") }
    var experience by remember { mutableStateOf(provider?.experience?.toString() ?: "") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var documentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { profileImageUri = it } }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> documentUris = uris }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (provider != null) "Edit Profile" else "Setup Profile")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(DeepBlue100)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    profileImageUri != null -> {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    provider?.profileImage?.isNotEmpty() == true -> {
                        AsyncImage(
                            model = provider.profileImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.CameraAlt, null,
                                tint = DeepBlue600,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "Add Photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepBlue600
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LBOTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = Icons.Filled.Person
            )

            Spacer(modifier = Modifier.height(12.dp))

            LBOTextField(
                value = category,
                onValueChange = { category = it },
                label = "Service Category",
                leadingIcon = Icons.Outlined.Category
            )

            Spacer(modifier = Modifier.height(12.dp))

            LBOTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                leadingIcon = Icons.Outlined.LocationOn
            )

            Spacer(modifier = Modifier.height(12.dp))

            LBOTextField(
                value = experience,
                onValueChange = { experience = it },
                label = "Experience (years)",
                leadingIcon = Icons.Outlined.WorkOutline,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(12.dp))

            LBOTextField(
                value = description,
                onValueChange = { description = it },
                label = "About your services",
                leadingIcon = Icons.Outlined.Description,
                singleLine = false,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Upload documents
            Text(
                "Verification Documents",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { documentPickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.CloudUpload, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (documentUris.isNotEmpty()) "${documentUris.size} files selected"
                    else "Upload Documents"
                )
            }

            if (provider?.documentsUrl?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${provider.documentsUrl.size} existing document(s) uploaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Approval status
            provider?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.isApproved) SuccessLight else WarningLight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (it.isApproved) Icons.Filled.Verified else Icons.Filled.HourglassTop,
                            null,
                            tint = if (it.isApproved) Success else Warning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (it.isApproved) "Profile Approved ✓"
                            else "Pending Admin Approval",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Error
            AnimatedVisibility(visible = profileState.error != null) {
                profileState.error?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = it, color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LBOButton(
                text = if (provider != null) "Update Profile" else "Submit for Approval",
                onClick = {
                    onSaveProfile(
                        name, category, location, description,
                        experience.toIntOrNull() ?: 0,
                        profileImageUri, documentUris
                    )
                },
                isLoading = profileState.isLoading,
                enabled = name.isNotBlank() && category.isNotBlank() && location.isNotBlank(),
                icon = if (provider != null) Icons.Filled.Save else Icons.Filled.Send
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
