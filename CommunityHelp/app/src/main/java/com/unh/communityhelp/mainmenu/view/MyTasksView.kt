package com.unh.communityhelp.mainmenu.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.mainmenu.model.HelpRequest
import com.unh.communityhelp.mainmenu.model.decodeImage
import com.unh.communityhelp.mainmenu.view.asset.RatingDialog
import com.unh.communityhelp.mainmenu.viewmodel.MyTasksViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun MyTasksView(viewModel: MyTasksViewModel = viewModel()) {

    val pullToRefreshState = rememberPullToRefreshState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Created", "Accepted")

    // Automatically refresh data when screen is opened
    LaunchedEffect(Unit) {
        viewModel.fetchUserTasks()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                    width = 64.dp,
                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = viewModel.isLoading, // Syncs visual spinner with ViewModel state
            onRefresh = { viewModel.fetchUserTasks() }, // Action to perform on pull
            modifier = Modifier.fillMaxSize()
        ) {
            // Determine which list to show
            val tasks = when (selectedTabIndex) {
                0 -> viewModel.createdTasks
                else -> viewModel.acceptedTasks
            }

            if (tasks.isEmpty() && !viewModel.isLoading) {
                EmptyStateMessage(tabs[selectedTabIndex])
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            tabIndex = selectedTabIndex,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemRow(
    task: HelpRequest,
    tabIndex: Int,
    viewModel: MyTasksViewModel
) {
    var showRatingDialog by remember { mutableStateOf(false) }

    if (showRatingDialog) {
        RatingDialog(
            // In a production app, you'd fetch the helper's username
            // from the helperId just like you do for the authorName.
            helperName = "the helper",
            onDismiss = { showRatingDialog = false },
            onConfirm = { rating, comment ->
                viewModel.verifyTaskCompletion(task, rating, comment) {
                    showRatingDialog = false
                }
            }
        )
    }

    HelpTaskCard(
        request = task,
        actionButton = {
            when (tabIndex) {
                0 -> { // Created Tab
                    if (!task.helperId.isNullOrEmpty() && task.status != "completed") {
                        Button(
                            onClick = { showRatingDialog = true },
                            modifier = Modifier.padding(bottom = 12.dp, end = 16.dp)
                        ) {
                            Text("Verify Completion")
                        }
                    }
                }
                1 -> {
                    if (task.status != "completed") {
                        OutlinedButton(
                            onClick = { viewModel.performDrop(task) },
                            modifier = Modifier.padding(bottom = 12.dp, end = 16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Drop Task")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun BoxScope.EmptyStateMessage(tabName: String) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No ${tabName.lowercase()} tasks found.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
private fun HelpTaskCard(
    modifier: Modifier = Modifier,
    request: HelpRequest,
    actionButton: @Composable () -> Unit = {},
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
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                actionButton()
            }
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

@Preview
@Composable
fun MyTasksViewPreview() {
    CommunityHelpTheme{
        MyTasksView()
    }
}