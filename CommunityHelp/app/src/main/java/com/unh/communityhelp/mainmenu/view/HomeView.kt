package com.unh.communityhelp.mainmenu.view

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.unh.communityhelp.mainmenu.model.HelpRequest
import com.unh.communityhelp.mainmenu.model.decodeImage
import com.unh.communityhelp.mainmenu.viewmodel.HomeViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeView(
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    
    // Accompanist's rememberPermissionState requires an Activity context, which is not available in Previews.
    // We provide a mock state when in inspection mode to prevent crashes.
    val locationPermissionState = if (isInspectionMode) {
        remember {
            object : PermissionState {
                override val permission: String = Manifest.permission.ACCESS_FINE_LOCATION
                override val status: PermissionStatus = PermissionStatus.Granted
                override fun launchPermissionRequest() {}
            }
        }
    } else {
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // FusedLocationProviderClient might also have issues in Preview environments.
    val fusedLocationClient = remember { 
        if (isInspectionMode) null else LocationServices.getFusedLocationProviderClient(context) 
    }

    // Helper function to trigger refresh
    val onRefresh = {
        fusedLocationClient?.let { viewModel.refreshHomeData(context, it) }
    }

    // Safely get the current user ID only if not in inspection mode to avoid Firebase initialization errors
    val currentUserId = if (isInspectionMode) null else remember { Firebase.auth.currentUser?.uid }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        // Avoid triggering VM data refresh in Preview to prevent Firebase initialization errors
        if (locationPermissionState.status.isGranted && !isInspectionMode) {
            onRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            locationPermissionState.status.isGranted -> {
                // Pass the refresh function and currentUserId to the content
                HomeContent(viewModel, onRefresh as () -> Unit, currentUserId)
            }
            else -> {
                LocationPermissionPrompt(
                    onGrantClick = { locationPermissionState.launchPermissionRequest() }
                )
            }
        }
    }
}

@Composable
private fun LocationPermissionPrompt(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Find Tasks Near You",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We need your location permission to show help requests in your current city.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGrantClick) {
            Text("Enable Location Access")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    viewModel: HomeViewModel,
    onRefresh: () -> Unit,
    currentUserId: String?
) {
    val context = LocalContext.current
    val requests = viewModel.helpRequests
    // PullToRefreshBox handles its own state based on the viewModel's isLoading

    PullToRefreshBox(
        isRefreshing = viewModel.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (requests.isEmpty() && !viewModel.isLoading) {
            // Use a verticalScroll modifier here so the pull-to-refresh gesture works
            // even when the list is empty
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No help requests found in ${viewModel.currentCity}.\nSwipe down to refresh.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(requests) { request ->
                    HelpTaskCard(
                        request = request,
                        currentUserId = currentUserId,
                        onAcceptClick = {
                            viewModel.acceptTask(request) {
                                Toast.makeText(context, "Task Accepted!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HelpTaskCard(
    request: HelpRequest,
    currentUserId: String?,
    onAcceptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(request.image) { request.decodeImage() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            UserHeader(userName = request.authorName)
            TaskImage(bitmap = imageBitmap)
            TaskDetails(
                title = request.title,
                location = request.location,
                description = request.description
            )

            // Only show the Accept button if:
            // 1. The user is NOT the author
            // 2. The task status is "open"
            if (request.authorId != currentUserId && request.status == "open") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onAcceptClick,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("Accept Task")
                    }
                }
            } else if (request.authorId == currentUserId) {
                Text(
                    "Your Post",
                    modifier = Modifier.padding(16.dp).alpha(0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun UserHeader(userName: String) {
    Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(userName, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TaskImage(bitmap: ImageBitmap?) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Task visual",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.alpha(0.3f)
                    )
                    Text(
                        "No Image",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetails(title: String, location: String, description: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = location,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    CommunityHelpTheme {
        // Mock data for previewing the UI without Firebase
        val mockRequest = HelpRequest(
            title = "Help with Trash Removal",
            description = "Need someone to help me remove trash from the backyard.",
            location = "West Haven, CT",
            image = ""
        )

        Column(Modifier.padding(16.dp)) {
            HelpTaskCard(
                request = mockRequest,
                onAcceptClick = {},
                currentUserId = "",
            )
        }
    }
}