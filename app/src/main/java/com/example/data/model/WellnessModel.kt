package com.example.data.model

// ---------------------------------------------------------------------------
// HILL YEAH — DAILY PULSE WELLNESS MODEL
// ---------------------------------------------------------------------------

data class DailyPulseQuestion(
    val id: Int, // 1..6
    val question: String,
    val options: List<String>,
    val allowsSkip: Boolean = true
)

enum class SuggestedActivityType {
    REST_AND_RECHARGE,
    GENTLE_NATURE_WALK,
    SOLO_PEACEFUL_HIKE,
    GROUP_OR_BUDDY_HIKE,
    QUIET_OUTDOOR_MOMENT,
    MIND_CLEARING_STROLL,
    SMALL_GOAL_FOCUS
}

data class DailyPulseInterpretation(
    val reflection: String,
    val practicalNextStep: String,
    val hillYeahSuggestion: String,
    val suggestedActivityType: SuggestedActivityType,
    val gentleHeadline: String
)

data class DailyPulseCheckIn(
    val id: String,
    val userId: String,
    val date: String, // yyyy-MM-dd
    val timestamp: Long,
    val answers: Map<Int, String?>, // questionId 1..6 -> answer text or null if skipped
    val interpretation: DailyPulseInterpretation
)

// Legacy / Compatibility definitions
data class WellnessQuestion(
    val id: String,
    val category: String,
    val question: String,
    val options: List<WellnessOption>
)

data class WellnessOption(
    val id: String,
    val label: String,
    val chargerWeight: Int,
    val cruiserWeight: Int,
    val zenWeight: Int
)

data class ArchetypeResult(
    val key: String,
    val title: String,
    val badge: String,
    val somaticState: String,
    val description: String,
    val nervousSystemNarrative: String,
    val recommendedTrailCriteria: String,
    val sensoryCalmingPrompt: String,
    val accentColorHex: Long
)
