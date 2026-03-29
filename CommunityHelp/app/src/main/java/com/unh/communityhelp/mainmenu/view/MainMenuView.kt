package com.unh.communityhelp.mainmenu.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuView(onLogout: () -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }

    // Updated lists to include "My Tasks"
    val titles = listOf("Feed", "Post", "My Tasks", "Rewards", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.AddCircle,
        Icons.AutoMirrored.Filled.FormatListBulleted,
        Icons.Default.EmojiEvents,
        Icons.Default.AccountCircle
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = titles[selectedItem],
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (selectedItem == 4) {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                titles.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeView()
                1 -> CreateHelpView()
                2 -> MyTasksView()
                3 -> RewardsView()
                4 -> ProfileView(onLogout = onLogout)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    CommunityHelpTheme {
        MainMenuView(onLogout = {})
    }
}