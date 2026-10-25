package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.HillYeahViewModel

@Composable
fun CommunityScreen(
    viewModel: HillYeahViewModel,
    modifier: Modifier = Modifier
) {
    // Replaced old community hub with the advanced, premium Digital Somatic Psychologist Wellness Tab
    WellnessScreen(viewModel = viewModel, modifier = modifier)
}
