package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HillYeahConstants
import com.example.ui.HillYeahViewModel
import com.example.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    viewModel: HillYeahViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading account details...")
        }
        return
    }

    val user = currentUser!!

    var name by remember(user) { mutableStateOf(user.name) }
    var city by remember(user) { mutableStateOf(user.city) }
    var selectedProvince by remember(user) { mutableStateOf(user.province.ifBlank { "Western Cape" }) }
    var experienceLevel by remember(user) { mutableStateOf(user.experienceLevel.ifBlank { "Intermediate" }) }
    var bio by remember(user) { mutableStateOf(user.bio) }
    var selectedAvatar by remember(user) {
        mutableStateOf(user.avatarUrl.ifBlank { HillYeahConstants.PresetAvatars.first() })
    }

    val selectedInterests = remember(user) {
        mutableStateListOf<String>().apply {
            addAll(if (user.interests.isNotEmpty()) user.interests else listOf("Sunrise Hikes", "Fynbos & Flora"))
        }
    }

    var provinceMenuExpanded by remember { mutableStateOf(false) }
    var expMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("profile_setup_screen")
    ) {
        Text(
            text = "Personalise Your Hiking Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Tell the HillYeah community about your hiking style, favourite trails, and outdoor goals.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Avatar Preview & Presets
        Text(
            text = "Profile Photo & Avatar",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = selectedAvatar,
                contentDescription = "Profile preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Selected Photo",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any preset below or enter a custom link",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HillYeahConstants.PresetAvatars.forEach { avatarUrl ->
                val isSelected = selectedAvatar == avatarUrl
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { selectedAvatar = avatarUrl }
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar preset",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x55000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_name_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // City / Area
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City or Area") },
            placeholder = { Text("e.g. Cape Town, Johannesburg, Durban") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_city_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Province Dropdown (All 9 SA provinces)
        ExposedDropdownMenuBox(
            expanded = provinceMenuExpanded,
            onExpandedChange = { provinceMenuExpanded = !provinceMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedProvince,
                onValueChange = {},
                readOnly = true,
                label = { Text("Province (South Africa)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceMenuExpanded) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .testTag("setup_province_dropdown")
            )

            ExposedDropdownMenu(
                expanded = provinceMenuExpanded,
                onDismissRequest = { provinceMenuExpanded = false }
            ) {
                HillYeahConstants.SouthAfricanProvinces.forEach { prov ->
                    DropdownMenuItem(
                        text = { Text(prov) },
                        onClick = {
                            selectedProvince = prov
                            provinceMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Experience Level Dropdown
        ExposedDropdownMenuBox(
            expanded = expMenuExpanded,
            onExpandedChange = { expMenuExpanded = !expMenuExpanded }
        ) {
            OutlinedTextField(
                value = experienceLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Hiking Experience Level") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expMenuExpanded) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .testTag("setup_experience_dropdown")
            )

            ExposedDropdownMenu(
                expanded = expMenuExpanded,
                onDismissRequest = { expMenuExpanded = false }
            ) {
                HillYeahConstants.ExperienceLevels.forEach { lvl ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(lvl, fontWeight = FontWeight.Bold)
                                Text(
                                    when (lvl) {
                                        "Beginner" -> "Gentle walks, nature trails, family-friendly routes"
                                        "Intermediate" -> "Half-day mountain ascents, moderate rocky slopes"
                                        else -> "Technical scrambles, high-altitude ridges, multi-day treks"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        onClick = {
                            experienceLevel = lvl
                            expMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hiking Interests Multi-Selector
        Text(
            text = "Hiking Interests & Preferences",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select all that match your passions in nature:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HillYeahConstants.HikingInterests.forEach { interest ->
                val isSelected = selectedInterests.contains(interest)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedInterests.remove(interest)
                        } else {
                            selectedInterests.add(interest)
                        }
                    },
                    label = { Text(interest, fontSize = 12.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Short Biography
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Short Biography & Wellness Goals") },
            placeholder = { Text("Share what you love about South African nature and your dream peaks...") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_bio_input")
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Save & Finish Button
        Button(
            onClick = {
                viewModel.updateProfile(
                    name = name,
                    city = city,
                    province = selectedProvince,
                    experienceLevel = experienceLevel,
                    interests = selectedInterests.toList(),
                    bio = bio,
                    avatarUrl = selectedAvatar
                )
                viewModel.navigateRoot(Screen.Home)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("setup_save_btn")
        ) {
            Text("Save Profile & Explore Trails", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
