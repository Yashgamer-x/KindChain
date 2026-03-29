package com.unh.communityhelp.mainmenu.view

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.mainmenu.model.Reward
import com.unh.communityhelp.mainmenu.viewmodel.RewardsViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun RewardsView(viewModel: RewardsViewModel = viewModel()) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastRedeemedShop by remember { mutableStateOf("") }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) { viewModel.fetchRewardsData() }

    // Success Dialog
    if (showSuccessDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Reward Redeemed!") },
            text = { Text("Show this screen to the staff at $lastRedeemedShop to claim your reward.") }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your Balance", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${viewModel.userPoints} pts",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Local Partnerships",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Redeem your points at these local spots",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(Modifier.height(12.dp))


        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = viewModel.isLoading,
            onRefresh = { viewModel.fetchRewardsData() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (viewModel.availableRewards.isEmpty() && !viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No rewards available right now.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.availableRewards) { reward ->
                        RewardItemCard(
                            reward = reward,
                            currentPoints = viewModel.userPoints,
                            onRedeem = {
                                viewModel.redeemReward(reward) {
                                    lastRedeemedShop = reward.businessName
                                    showSuccessDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardItemCard(
    reward: Reward,
    currentPoints: Int,
    onRedeem: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val progress = (currentPoints.toFloat() / reward.pointsRequired).coerceIn(0f, 1f)
    val canRedeem = currentPoints >= reward.pointsRequired
    val pointsLeft = (reward.pointsRequired - currentPoints).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canRedeem) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        color = if (canRedeem) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = if (canRedeem) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.businessName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = reward.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (canRedeem) "Goal Reached!" else "$pointsLeft pts to go",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (canRedeem) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = reward.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Map Button
                androidx.compose.material3.IconButton(
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(reward.mapUrl)
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Open Maps",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (canRedeem) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRedeem,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Redeem Reward", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RewardsViewPreview(){
    CommunityHelpTheme{
        RewardsView()
    }
}