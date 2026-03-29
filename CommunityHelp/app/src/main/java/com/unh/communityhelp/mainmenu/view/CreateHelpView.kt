package com.unh.communityhelp.mainmenu.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.unh.communityhelp.auth.signup.category.Expertise
import com.unh.communityhelp.mainmenu.view.asset.LocationSection
import com.unh.communityhelp.mainmenu.viewmodel.CreateHelpViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@OptIn(ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun CreateHelpView(viewModel: CreateHelpViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Request Help", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        CameraActionSection(
            capturedImage = viewModel.capturedImage,
            onImageCaptured = { viewModel.capturedImage = it },
            onRemoveImage = { viewModel.clearImage() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Category Selection ---
        Text(
            text = "Select Category",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Expertise.entries.forEach { expertise ->
                val isSelected = viewModel.selectedCategory == expertise
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCategory = expertise },
                    label = { Text(expertise.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LocationSection(viewModel = viewModel, context = context)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.createHelpRequest(
                    image = viewModel.capturedImage,
                    title = title,
                    description = description,
                    onSuccess = {
                        Toast.makeText(context, "Post successful!", Toast.LENGTH_SHORT).show()
                        title = ""
                        description = ""
                        viewModel.resetForm()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && description.isNotBlank() &&
                    viewModel.cityName.isNotBlank() && !viewModel.isSubmitting
        ) {
            if (viewModel.isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text("Post Help Request")
            }
        }

        if (viewModel.statusMessage.isNotEmpty()) {
            Text(
                text = viewModel.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraActionSection(
    capturedImage: android.graphics.Bitmap?,
    onImageCaptured: (android.graphics.Bitmap?) -> Unit,
    onRemoveImage: () -> Unit
) {
    val cameraPermissionState = if (LocalInspectionMode.current) null else rememberPermissionState(android.Manifest.permission.CAMERA)
    var showRationale by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> onImageCaptured(bitmap) }

    if (showRationale) {
        PermissionRationaleDialog(
            onDismiss = { showRationale = false },
            onConfirm = {
                showRationale = false
                cameraPermissionState?.launchPermissionRequest()
            }
        )
    }

    if (capturedImage != null) {
        ImagePreview(
            bitmap = capturedImage.asImageBitmap(),
            onRemove = onRemoveImage
        )
    } else {
        CameraPlaceholder(onLaunchCamera = {
            when {
                cameraPermissionState?.status?.isGranted == true -> cameraLauncher.launch()
                cameraPermissionState?.status?.shouldShowRationale == true -> showRationale = true
                else -> cameraPermissionState?.launchPermissionRequest()
            }
        })
    }
}


/**
 * Friendly dialog explaining why we need the camera.
 */
@Composable
fun PermissionRationaleDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Camera Permission Needed") },
        text = { Text("To show helpers what you need, we need access to your camera to take a photo of the issue.") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Grant Permission") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Displays the captured image with an overlay button to remove/retake it.
 */
@Composable
fun ImagePreview(bitmap: ImageBitmap, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium),
        contentAlignment = Alignment.TopEnd
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = "Help request photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Small floating 'X' button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.padding(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                tonalElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Photo",
                    modifier = Modifier.padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * The initial button shown when no photo has been taken.
 */
@Composable
fun CameraPlaceholder(onLaunchCamera: () -> Unit) {
    OutlinedButton(
        onClick = onLaunchCamera,
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Take a Photo", style = MaterialTheme.typography.labelLarge)
        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun CreateHelpPreview() {
    CommunityHelpTheme {
        CreateHelpView()
    }
}