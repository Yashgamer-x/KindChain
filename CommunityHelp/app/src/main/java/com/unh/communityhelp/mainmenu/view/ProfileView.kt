package com.unh.communityhelp.mainmenu.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.mainmenu.model.UserProfile
import com.unh.communityhelp.mainmenu.state.ProfileState
import com.unh.communityhelp.mainmenu.viewmodel.ProfileViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun ProfileView(
    viewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileViewContent(
        uiState = uiState,
        onLogout = { viewModel.logout(onLogout) }
    )
}

@Composable
fun ProfileViewContent(
    uiState: ProfileState,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (uiState) {
            is ProfileState.Loading -> CircularProgressIndicator()
            is ProfileState.Error -> Text(uiState.message)
            is ProfileState.Success -> ProfileContent(uiState.profile, onLogout)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileContent(profile: UserProfile, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(Modifier.size(100.dp), CircleShape, MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Default.Person, null, Modifier.padding(20.dp))
        }
        Text(profile.fullName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("@${profile.username}", color = Color.Gray)

        Spacer(Modifier.height(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${profile.points} Points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp)) {
                Icon(Icons.Default.Phone, null); Spacer(Modifier.width(12.dp)); Text(profile.phoneNumber)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Expertise", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold)
        FlowRow(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            profile.expertiseList.forEach { SuggestionChip(onClick = {}, label = { Text(it) }) }
        }

        Spacer(Modifier.height(40.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
            Text("Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileViewPreview(){
    CommunityHelpTheme{
        ProfileViewContent(
            uiState = ProfileState.Success(
                UserProfile(
                    fullName = "John Doe",
                    username = "johndoe",
                    phoneNumber = "123-456-7890",
                    expertiseList = listOf("Plumbing", "Electrical", "Gardening")
                )
            ),
            onLogout = {}
        )
    }
}