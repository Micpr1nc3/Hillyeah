package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HillYeahViewModel
import com.example.ui.components.HikeCard
import com.example.ui.navigation.Screen

@Composable
fun HikesScreen(
    viewModel: HillYeahViewModel,
    modifier: Modifier = Modifier
) {
    val filteredHikes by viewModel.filteredHikes.collectAsState()
    val selectedProvince by viewModel.hikeProvinceFilter.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("hikes_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header title
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Explore Group Hikes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Join an outdoor trek, meet hiking buddies, and enjoy nature together.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Province Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PROVINCES_LIST.forEach { province ->
                    val isSelected = selectedProvince.equals(province, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.hikeProvinceFilter.value = province },
                        label = {
                            Text(
                                text = province,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("hike_province_chip_$province")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            var showInviteCodeDialog by remember { mutableStateOf(false) }
            var inviteInput by remember { mutableStateOf("") }
            var inviteError by remember { mutableStateOf<String?>(null) }

            // Invite code / link shortcut banner
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { showInviteCodeDialog = true }
                    .testTag("hike_invite_code_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Received a hike invite link or code?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = "Open →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showInviteCodeDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showInviteCodeDialog = false
                        inviteError = null
                    },
                    title = { Text("Open Hike Invitation") },
                    text = {
                        Column {
                            Text(
                                text = "Paste a shared HillYeah link or enter the Hike ID to view the details and respond.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = inviteInput,
                                onValueChange = {
                                    inviteInput = it
                                    inviteError = null
                                },
                                label = { Text("Invite link or Hike ID") },
                                placeholder = { Text("e.g. hike_01 or paste link") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("invite_code_input")
                            )
                            if (inviteError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = inviteError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmed = inviteInput.trim()
                                val parsedId = when {
                                    trimmed.contains("hikId=") -> trimmed.substringAfter("hikId=").substringBefore("&").trim()
                                    trimmed.contains("id=") -> trimmed.substringAfter("id=").substringBefore("&").trim()
                                    trimmed.contains("hikeld=") -> trimmed.substringAfter("hikeld=").substringBefore("&").trim()
                                    trimmed.contains("hikid=") -> trimmed.substringAfter("hikid=").substringBefore("&").trim()
                                    trimmed.contains("hikeId=") -> trimmed.substringAfter("hikeId=").substringBefore("&").trim()
                                    else -> trimmed
                                }
                                if (parsedId.isNotBlank()) {
                                    showInviteCodeDialog = false
                                    viewModel.navigateTo(Screen.InviteResponse(parsedId))
                                } else {
                                    inviteError = "Please enter a valid link or hike ID"
                                }
                            },
                            modifier = Modifier.testTag("submit_invite_code_btn")
                        ) {
                            Text("Open Invite")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            showInviteCodeDialog = false
                            inviteError = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Hikes List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredHikes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hikes scheduled in this province yet.\nBe the first to host an outing!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(filteredHikes) { hikeDetails ->
                        HikeCard(
                            hikeWithDetails = hikeDetails,
                            onClick = { viewModel.navigateTo(Screen.HikeDetail(hikeDetails.hike.id)) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Host a Hike
        ExtendedFloatingActionButton(
            onClick = { viewModel.navigateTo(Screen.HostHike()) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Host a Hike", fontWeight = FontWeight.Bold) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_host_hike")
        )
    }
}
