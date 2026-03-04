package com.newhanchat.v1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage // <-- Coil 3 Import
import com.newhanchat.v1.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel() // Fetches data immediately on tab click
) {
    val profile by viewModel.profile.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (profile == null) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
        } else {
            // Profile Picture using Coil 3
            AsyncImage(
                model = profile?.profilePictureUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            Text("Name: ${profile?.fname} ${profile?.lname}", style = MaterialTheme.typography.titleLarge)
            Text("Username: @${profile?.username}", style = MaterialTheme.typography.bodyLarge)

            // If you have status in the response:
            Text("Status: ${profile?.userStatus}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = { viewModel.logout { onLogout() } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}