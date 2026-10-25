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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HillYeahViewModel
import com.example.ui.components.TrailCard
import com.example.ui.navigation.Screen

val PROVINCES_LIST = listOf(
    "All",
    "Western Cape",
    "Gauteng",
    "KwaZulu-Natal",
    "Mpumalanga",
    "Eastern Cape",
    "Free State",
    "North West",
    "Limpopo",
    "Northern Cape"
)

val DIFFICULTY_LIST = listOf("All", "Easy", "Moderate", "Challenging")

@Composable
fun TrailsScreen(
    viewModel: HillYeahViewModel,
    modifier: Modifier = Modifier
) {
    val filteredTrails by viewModel.filteredTrails.collectAsState()
    val searchQuery by viewModel.trailSearchQuery.collectAsState()
    val selectedProvince by viewModel.selectedProvinceFilter.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficultyFilter.collectAsState()
    val waterfallFilterOnly by viewModel.waterfallFilterOnly.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("trails_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.trailSearchQuery.value = it },
            placeholder = { Text("Search 180 trails, waterfalls, parks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.trailSearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("trail_search_input")
        )

        // Province Filter Chips Row
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
                    onClick = { viewModel.selectedProvinceFilter.value = province },
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
                    modifier = Modifier.testTag("province_chip_$province")
                )
            }
        }

        // Secondary Filters Row: Difficulty & Waterfall Filter Chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dedicated Waterfall Filter Chip
            FilterChip(
                selected = waterfallFilterOnly,
                onClick = { viewModel.waterfallFilterOnly.value = !waterfallFilterOnly },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (waterfallFilterOnly) Color(0xFF0284C7) else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                },
                label = {
                    Text(
                        text = "Waterfalls 💧",
                        fontSize = 12.sp,
                        fontWeight = if (waterfallFilterOnly) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE0F2FE),
                    selectedLabelColor = Color(0xFF0369A1)
                ),
                modifier = Modifier.testTag("filter_chip_waterfall")
            )

            DIFFICULTY_LIST.forEach { difficulty ->
                val isSelected = selectedDifficulty.equals(difficulty, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedDifficultyFilter.value = difficulty },
                    label = {
                        Text(
                            text = difficulty,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.testTag("difficulty_chip_$difficulty")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Results count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${filteredTrails.size} South African trails found",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            if (waterfallFilterOnly) {
                Text(
                    text = "💧 Waterfall trails active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
            }
        }

        // Trails List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredTrails.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    ) {
                        Text(
                            text = "No trails found matching your filters.\nTry deselecting the Waterfall filter, choosing 'All' provinces, or clearing the search query.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(filteredTrails) { trail ->
                    TrailCard(
                        trail = trail,
                        onClick = { viewModel.navigateTo(Screen.TrailDetail(trail.id)) }
                    )
                }
            }
        }
    }
}
