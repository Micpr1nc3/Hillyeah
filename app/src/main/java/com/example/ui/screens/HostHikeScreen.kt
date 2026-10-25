package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HillYeahViewModel
import com.example.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostHikeScreen(
    viewModel: HillYeahViewModel,
    prefillTrailId: String? = null,
    modifier: Modifier = Modifier
) {
    val trails by viewModel.trails.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sign in to Host a Hike",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Only registered and verified community members can host hikes. Log in or create your profile to organize group trail adventures.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.navigateTo(Screen.Login(Screen.HostHike(prefillTrailId))) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
            ) {
                Text("Log In", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.Register(Screen.HostHike(prefillTrailId))) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
            ) {
                Text("Register New Account", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var selectedTrailId by remember { mutableStateOf(prefillTrailId) }
    var customTrailName by remember { mutableStateOf("") }
    var trailNameError by remember { mutableStateOf<String?>(null) }
    var province by remember { mutableStateOf("Western Cape") }
    var location by remember { mutableStateOf("Table Mountain NP") }
    var date by remember { mutableStateOf("2026-10-31") }
    var time by remember { mutableStateOf("07:00") }
    var meetingPoint by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Moderate") }
    var distanceKmText by remember { mutableStateOf("6.0") }
    var capacityText by remember { mutableStateOf("12") }
    var description by remember { mutableStateOf("") }

    var trailDropdownExpanded by remember { mutableStateOf(false) }
    var provinceDropdownExpanded by remember { mutableStateOf(false) }
    var difficultyDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-prefill if trail is chosen
    LaunchedEffect(prefillTrailId, trails) {
        if (!prefillTrailId.isNullOrBlank()) {
            val matched = trails.find { it.id == prefillTrailId }
            if (matched != null) {
                selectedTrailId = matched.id
                title = "${matched.name} Group Trek"
                customTrailName = matched.name
                province = matched.province
                location = matched.location
                meetingPoint = matched.meetingPoint
                difficulty = matched.difficulty
                distanceKmText = matched.distanceKm.toString()
                description = "Join us for a mindful outing on ${matched.name}. We'll take an easy pace, share trail moments, and enjoy nature's restorative peace."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("host_hike_screen")
    ) {
        Text(
            text = "Host a Community Hike",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Organize a group hike, welcome newcomers, and help heal the world through nature.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Hike Title
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Hike Title *") },
            placeholder = { Text("e.g. Mindful Sunrise at Hennops") },
            isError = titleError != null,
            supportingText = {
                if (titleError != null) {
                    Text(titleError!!, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Provide a full, descriptive title (e.g. 'Mindful Sunrise at Hennops').")
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("input_hike_title")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Select Verified Trail Dropdown
        ExposedDropdownMenuBox(
            expanded = trailDropdownExpanded,
            onExpandedChange = { trailDropdownExpanded = !trailDropdownExpanded }
        ) {
            val selectedTrail = trails.find { it.id == selectedTrailId }
            OutlinedTextField(
                value = selectedTrail?.let { "${it.name} (${it.province})" } ?: "Custom Trail / Reserve",
                onValueChange = {},
                readOnly = true,
                label = { Text("Verified 9-Province Trail") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trailDropdownExpanded) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = trailDropdownExpanded,
                onDismissRequest = { trailDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Custom Trail / Enter My Own") },
                    onClick = {
                        selectedTrailId = null
                        trailDropdownExpanded = false
                    }
                )
                trails.forEach { t ->
                    DropdownMenuItem(
                        text = { Text("${t.name} (${t.province}) — ${t.difficulty}") },
                        onClick = {
                            selectedTrailId = t.id
                            title = "${t.name} Group Trek"
                            customTrailName = t.name
                            province = t.province
                            location = t.location
                            meetingPoint = t.meetingPoint
                            difficulty = t.difficulty
                            distanceKmText = t.distanceKm.toString()
                            description = "Join us for a scenic, mindful trek on ${t.name}. ${t.description}"
                            trailDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trail Name
        OutlinedTextField(
            value = customTrailName,
            onValueChange = {
                customTrailName = it
                trailNameError = null
            },
            label = { Text("Trail / Mountain Name *") },
            placeholder = { Text("e.g. Hennops Krokodilberg Trail") },
            isError = trailNameError != null,
            supportingText = {
                if (trailNameError != null) {
                    Text(trailNameError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Province Picker Dropdown
        ExposedDropdownMenuBox(
            expanded = provinceDropdownExpanded,
            onExpandedChange = { provinceDropdownExpanded = !provinceDropdownExpanded }
        ) {
            OutlinedTextField(
                value = province,
                onValueChange = {},
                readOnly = true,
                label = { Text("Province *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceDropdownExpanded) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = provinceDropdownExpanded,
                onDismissRequest = { provinceDropdownExpanded = false }
            ) {
                PROVINCES_LIST.filter { it != "All" }.forEach { prov ->
                    DropdownMenuItem(
                        text = { Text(prov) },
                        onClick = {
                            province = prov
                            provinceDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Location / City
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location / Reserve / Town *") },
            placeholder = { Text("e.g. Table Mountain National Park, Cape Town") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Date and Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD) *") },
                placeholder = { Text("2026-10-31") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_hike_date")
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Start Time *") },
                placeholder = { Text("07:00") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_hike_time")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Meeting Point
        OutlinedTextField(
            value = meetingPoint,
            onValueChange = { meetingPoint = it },
            label = { Text("Specific Meeting Point & Parking Instructions *") },
            placeholder = { Text("e.g. In front of the information kiosk by 06:45 AM") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("input_hike_meeting")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Difficulty, Distance, Capacity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = difficultyDropdownExpanded,
                onExpandedChange = { difficultyDropdownExpanded = !difficultyDropdownExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Difficulty") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyDropdownExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )

                ExposedDropdownMenu(
                    expanded = difficultyDropdownExpanded,
                    onDismissRequest = { difficultyDropdownExpanded = false }
                ) {
                    listOf("Easy", "Moderate", "Challenging").forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = {
                                difficulty = d
                                difficultyDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = distanceKmText,
                onValueChange = { distanceKmText = it },
                label = { Text("Dist (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = capacityText,
                onValueChange = { capacityText = it },
                label = { Text("Max Spots") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_hike_capacity")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Trail Expectations") },
            placeholder = { Text("Describe the intended hiking pace, water stops, dog-friendliness, and general vibe...") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Publish Button
        Button(
            onClick = {
                val trimmedTitle = title.trim()
                if (trimmedTitle.isBlank() || trimmedTitle.equals("the", ignoreCase = true) || trimmedTitle.length < 3) {
                    titleError = "Please enter a proper, full trail/hike name (e.g., 'Mindful Sunrise at Hennops')."
                    return@Button
                }

                val trimmedTrail = customTrailName.trim()
                if (trimmedTrail.isBlank() || trimmedTrail.equals("the", ignoreCase = true)) {
                    trailNameError = "Please specify the full trail name (e.g. 'Hennops Krokodilberg Trail')."
                    return@Button
                }

                val dist = distanceKmText.toDoubleOrNull() ?: 5.0
                val cap = capacityText.toIntOrNull() ?: 12
                viewModel.createHike(
                    title = trimmedTitle,
                    trailId = selectedTrailId,
                    customTrailName = trimmedTrail,
                    province = province,
                    location = if (location.isBlank()) "South Africa" else location,
                    date = if (date.isBlank()) "2026-10-31" else date,
                    time = if (time.isBlank()) "07:00" else time,
                    meetingPoint = if (meetingPoint.isBlank()) "Main Trailhead Car Park" else meetingPoint,
                    difficulty = difficulty,
                    distanceKm = dist,
                    capacity = cap,
                    description = if (description.isBlank()) "Join our friendly group hike. Enjoy the fresh air, connect with fellow hikers, and leave no trace." else description,
                    onSuccess = { createdHike ->
                        viewModel.navigateTo(Screen.HikeDetail(createdHike.id))
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_hike_button")
        ) {
            Text("Publish Group Hike", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.navigateBack() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}
