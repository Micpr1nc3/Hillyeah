package com.example.data.wellness

/**
 * Modern Therapist Simulation & Weekly Intelligence Data Models.
 */
data class WeeklyTherapistReport(
    val id: String,
    val weekTitle: String,
    val dateRangeLabel: String,
    val checkInCount: Int,
    val primaryMindState: String,
    val mindStateBadge: String,
    val psychologicalVerdict: String,
    val somaticNervousSystemState: String,
    val nervousSystemDetail: String,
    val weeklyEnergySummary: String,
    val dominantPreoccupation: String,
    val therapeuticSuggestions: List<TherapeuticSuggestion>,
    val somaticPrescription: SomaticPrescription,
    val therapistLetter: String
)

data class TherapeuticSuggestion(
    val title: String,
    val category: String, // "Cognitive Unburdening", "Somatic Regulation", "Boundaries & Rest", "Relational Connection"
    val insight: String,
    val actionablePractice: String
)

data class SomaticPrescription(
    val title: String,
    val practice: String,
    val duration: String,
    val recommendedTrailType: String
)
