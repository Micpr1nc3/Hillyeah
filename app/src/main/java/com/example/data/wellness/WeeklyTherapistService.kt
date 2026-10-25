package com.example.data.wellness

import com.example.data.model.DailyPulseCheckIn
import com.example.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object WeeklyTherapistService {

    /**
     * Synthesizes the user's daily check-ins over the past week into a comprehensive
     * psychological evaluation and verdict with empathetic, professional clinical guidance.
     */
    fun synthesizeWeeklyReport(
        history: List<DailyPulseCheckIn>,
        user: User?
    ): WeeklyTherapistReport {
        val userName = user?.name?.split(" ")?.firstOrNull() ?: "Friend"

        // Take check-ins from the last 7 days (or up to 7 entries)
        val weeklyCheckIns = history.take(7)
        val checkInCount = weeklyCheckIns.size

        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val dateRangeLabel = if (weeklyCheckIns.isNotEmpty()) {
            val oldest = weeklyCheckIns.last().date
            val newest = weeklyCheckIns.first().date
            try {
                val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val d1 = inFmt.parse(oldest) ?: Date()
                val d2 = inFmt.parse(newest) ?: Date()
                "${dateFormat.format(d1)} – ${dateFormat.format(d2)}"
            } catch (_: Exception) {
                "Past 7 Days"
            }
        } else {
            "Current Week"
        }

        // Tally answers across all 6 questions
        var feelingGreat = 0
        var feelingOkay = 0
        var feelingHeavy = 0
        var feelingOverwhelmed = 0
        var feelingUnsure = 0

        var energyHigh = 0
        var energyModerate = 0
        var energyLow = 0
        var energyExhausted = 0

        val preoccupations = mutableMapOf<String, Int>()
        val connectionStates = mutableMapOf<String, Int>()
        val futureStates = mutableMapOf<String, Int>()
        val helpfulStates = mutableMapOf<String, Int>()

        for (checkIn in weeklyCheckIns) {
            when (checkIn.answers[1]) {
                "Great — feeling good" -> feelingGreat++
                "Okay — taking things as they come" -> feelingOkay++
                "A little heavy emotionally" -> feelingHeavy++
                "Overwhelmed — a lot on my mind" -> feelingOverwhelmed++
                "Not quite sure how I feel" -> feelingUnsure++
            }
            when (checkIn.answers[2]) {
                "High — ready to go" -> energyHigh++
                "Moderate — I can manage" -> energyModerate++
                "Low — I need to take it easy" -> energyLow++
                "Exhausted — I need rest" -> energyExhausted++
            }
            checkIn.answers[3]?.let { preoccupations[it] = (preoccupations[it] ?: 0) + 1 }
            checkIn.answers[4]?.let { connectionStates[it] = (connectionStates[it] ?: 0) + 1 }
            checkIn.answers[5]?.let { futureStates[it] = (futureStates[it] ?: 0) + 1 }
            checkIn.answers[6]?.let { helpfulStates[it] = (helpfulStates[it] ?: 0) + 1 }
        }

        val topPreoccupation = preoccupations.maxByOrNull { it.value }?.key ?: "Personal responsibilities"
        val topWhatHelped = helpfulStates.maxByOrNull { it.value }?.key ?: "Finding peace in nature"

        // Determine dominant state of mind and nervous system pattern
        val totalDepleted = energyLow + energyExhausted + feelingHeavy + feelingOverwhelmed
        val totalBalanced = energyHigh + energyModerate + feelingGreat + feelingOkay

        val primaryMindState: String
        val mindStateBadge: String
        val psychologicalVerdict: String
        val somaticNervousSystemState: String
        val nervousSystemDetail: String
        val weeklyEnergySummary: String
        val suggestions: List<TherapeuticSuggestion>
        val somaticPrescription: SomaticPrescription
        val therapistLetter: String

        if (checkInCount == 0) {
            // Baseline / Intake state when first starting
            primaryMindState = "Intake Baseline · Ready for Weekly Observation"
            mindStateBadge = "New Beginning"
            psychologicalVerdict = "Welcome to your personal wellness sanctuary, $userName. A healthy mind is not an absence of stress, but the ability to listen to what your body and thoughts need each day. As you log your daily check-ins, I will monitor your emotional trajectory, energy reserves, and cognitive patterns to deliver your weekly psychological verdict."
            somaticNervousSystemState = "Baseline Regulatory Observation"
            nervousSystemDetail = "Tracking initial autonomic nervous system balance between active engagement and restorative rest."
            weeklyEnergySummary = "Awaiting your first cycle of daily check-ins to map your natural circadian and weekly energy rhythms."
            suggestions = listOf(
                TherapeuticSuggestion(
                    title = "Establish Your Daily Anchor",
                    category = "Cognitive Unburdening",
                    insight = "Mental clarity doesn't require drastic life changes—it thrives on a predictable 2-minute daily pause.",
                    actionablePractice = "Take your daily check-in at the same time each morning or evening, treating it as a non-judgmental mirror."
                ),
                TherapeuticSuggestion(
                    title = "The 5-4-3-2-1 Sensory Grounding",
                    category = "Somatic Regulation",
                    insight = "When the mind accelerates into hypothetical futures, physical sensations are your fastest bridge back to the present.",
                    actionablePractice = "Notice 5 colors around you, 4 tactile textures, 3 distinct sounds, 2 natural scents, and 1 slow breath."
                )
            )
            somaticPrescription = SomaticPrescription(
                title = "Introductory Forest Bathing",
                practice = "Take a 25-minute gentle walk outdoors without headphones. Allow your gaze to soften into the horizon.",
                duration = "20–25 minutes",
                recommendedTrailType = "Gentle Nature Walk or Botanical Garden"
            )
            therapistLetter = "Dear $userName,\n\nConsider this space your judgment-free container. You do not need to perform, have everything figured out, or pretend to be resilient here. Take each day as it comes, and let nature and self-honesty do the heavy lifting.\n\nWarmly,\nYour HillYeah Therapist"
        } else if (feelingOverwhelmed >= 2 || (topPreoccupation.contains("Work") && (energyLow + energyExhausted) >= 2)) {
            // Case A: High-Functioning Cognitive Strain / Sympathetic Activation
            primaryMindState = "High-Functioning Cognitive Strain & Adaptive Fatigue"
            mindStateBadge = "Boundary Priority"
            psychologicalVerdict = "Your collective check-ins this week reveal a classic pattern of 'functional endurance'. You are effectively managing external obligations—primarily $topPreoccupation—but at the expense of internal somatic recovery. Your mind is operating in sympathetic vigilance, spending energy anticipating what's next before your body has had the opportunity to discharge accumulated stress. This is not a personal shortcoming; it is an autonomic signal that your cognitive load has outpaced your recovery windows."
            somaticNervousSystemState = "Sympathetic Autonomic Dominance"
            nervousSystemDetail = "Prolonged executive focus has maintained elevated cortisol tone, dampening parasympathetic restorative sleep and digestive calm."
            weeklyEnergySummary = "Energy has been drawn primarily on adrenaline and willpower rather than genuine baseline reserves."
            suggestions = listOf(
                TherapeuticSuggestion(
                    title = "Enforce an Information Curfew",
                    category = "Cognitive Unburdening",
                    insight = "Your brain cannot downshift into restorative states while processing notifications and open task loops.",
                    actionablePractice = "Institute a hard phone-free window 60 minutes before bed. Replace screen time with dim lighting and warm rooibos tea."
                ),
                TherapeuticSuggestion(
                    title = "The Physiological Sigh Protocol",
                    category = "Somatic Regulation",
                    insight = "Two quick inhales through the nose followed by an extended, unforced mouth exhale directly triggers the vagus nerve to slow cardiac rhythm.",
                    actionablePractice = "Perform 3 consecutive physiological sighs whenever you transition between tasks or feel mental overwhelm cresting."
                ),
                TherapeuticSuggestion(
                    title = "Clear Non-Essential Demands",
                    category = "Boundaries & Rest",
                    insight = "Saying 'no' to secondary commitments is an act of neurological self-preservation, not selfishness.",
                    actionablePractice = "Identify one task or social expectation this upcoming week that you can postpone or gracefully decline."
                )
            )
            somaticPrescription = SomaticPrescription(
                title = "Vagal Down-Regulation Trail Walk",
                practice = "A level, shaded forest walk. Walk at an easy pace (under 4 km/h), focusing exclusively on prolonged exhalations and observing river sounds.",
                duration = "35–45 minutes",
                recommendedTrailType = "Shaded Waterfall Walk or Stream Trail (e.g. Walter Sisulu or Kirstenbosch)"
            )
            therapistLetter = "Dear $userName,\n\nI want to gently validate the sheer volume of mental energy you have poured into holding things together this week. Your exhaustion is real and warranted. The prescription for you right now is not to work harder on your wellness—it is to give yourself unconditional permission to do less. You have earned your rest.\n\nWith care,\nYour HillYeah Therapist"
        } else if (feelingHeavy >= 2 || energyExhausted >= 2) {
            // Case B: Emotional Heaviness & Dorsal Restorative Need
            primaryMindState = "Emotional Heaviness & Low-Drives Hibernation"
            mindStateBadge = "Gentle Nurturing"
            psychologicalVerdict = "Your reflections across the week highlight emotional tenderness and significant physical fatigue. In psychological practice, we recognize this as an intuitive conservation state: when emotional or systemic pressure becomes sustained, the psyche instinctively pulls inward to protect vital reserves. What you need right now is not forced positivity or aggressive motivation, but compassionate holding. Allow your current low capacity to exist without feeling guilty about it."
            somaticNervousSystemState = "Dorsal Vagal Conservation Response"
            nervousSystemDetail = "The nervous system is seeking shelter to repair cellular energy and process unprocessed emotional weight."
            weeklyEnergySummary = "Depleted baseline requiring gentle warmth, nourishing hydration, and zero-pressure pacing."
            suggestions = listOf(
                TherapeuticSuggestion(
                    title = "Honor Micro-Steps Over Grand Goals",
                    category = "Cognitive Unburdening",
                    insight = "When feeling emotionally heavy, thinking about entire days or weeks creates paralysis. Break life down into the next 15 minutes.",
                    actionablePractice = "Pick just one gentle action at a time: drink a glass of water, step into sunlight for 5 minutes, or rest your eyes."
                ),
                TherapeuticSuggestion(
                    title = "Warm Somatic Self-Touch",
                    category = "Somatic Regulation",
                    insight = "Placing a warm palm over your sternum or wrapping your arms gently around your shoulders signals physical safety directly to the brainstem.",
                    actionablePractice = "Spend 2 minutes with a hand on your chest, feeling the rise and fall of your breath without trying to change it."
                ),
                TherapeuticSuggestion(
                    title = "Low-Friction Nature Contact",
                    category = "Ecotherapy",
                    insight = "You do not need to hike strenuous peaks to gain the therapeutic benefits of nature. Passive immersion is equally healing.",
                    actionablePractice = "Sit on a bench under a tree or near garden plants for 15 minutes. Let nature hold space for you."
                )
            )
            somaticPrescription = SomaticPrescription(
                title = "Passive Green Canopy Immersion",
                practice = "Zero-mileage nature resting. Bring a blanket or sit on a boulder, listen to birdsong, and practice soft belly breathing.",
                duration = "20–30 minutes",
                recommendedTrailType = "Gentle Meadow or Shaded Riverbank"
            )
            therapistLetter = "Dear $userName,\n\nPlease remember that you are allowed to have quiet, heavy seasons. Trees do not bloom all year round; they gather strength in their roots during winter. Treat yourself with the same deep respect you would offer a dear friend who is weary.\n\nAlways here for you,\nYour HillYeah Therapist"
        } else if (totalBalanced > totalDepleted) {
            // Case C: Grounded Clarity, High Resilience & Vital Equilibrium
            primaryMindState = "Grounded Equilibrium & Restorative Vitality"
            mindStateBadge = "Flourishing"
            psychologicalVerdict = "Your weekly wellness aggregated data demonstrates a remarkably coherent and grounded state of mind. You have navigated daily demands with steady emotional regulation, balanced energy expenditure, and healthy coping mechanisms—especially through $topWhatHelped. This indicates strong psychological flexibility and ventral vagal resilience, enabling you to remain engaged with life while preserving inner peace."
            somaticNervousSystemState = "Ventral Vagal Social Engagement & Flow"
            nervousSystemDetail = "Balanced autonomic tone facilitating cognitive creativity, emotional open-heartedness, and optimal physical endurance."
            weeklyEnergySummary = "Steady, sustainable energy reserves with healthy restorative recoveries between active days."
            suggestions = listOf(
                TherapeuticSuggestion(
                    title = "Consolidate Your Resilience Anchors",
                    category = "Cognitive Integration",
                    insight = "When feeling good, we often take wellness for granted. Documenting what supported you locks in lasting behavioral habits.",
                    actionablePractice = "Write down the two habits that most preserved your peace this week (e.g. morning walks, mindful boundaries)."
                ),
                TherapeuticSuggestion(
                    title = "Expand into Social or Community Hiking",
                    category = "Relational Connection",
                    insight = "Your open window of tolerance makes this an ideal time to share positive presence with others.",
                    actionablePractice = "Consider joining a weekend group hike or inviting a friend who might benefit from your calm grounding energy."
                ),
                TherapeuticSuggestion(
                    title = "Explore Elevation & Flow States",
                    category = "Somatic Vitality",
                    insight = "Channel your vibrant energy into a panoramic mountain trail to stimulate dopamine, endorphins, and awe.",
                    actionablePractice = "Plan a moderate trail with panoramic summit views to celebrate your physical and mental strength."
                )
            )
            somaticPrescription = SomaticPrescription(
                title = "Panoramic Ridge Trek for Peak Clarity",
                practice = "A brisk, invigorating ascent with panoramic views. Practice rhythmic box breathing during climbs and savor summit vistas.",
                duration = "60–90 minutes",
                recommendedTrailType = "Lion's Head, Magaliesberg Ridge, or Drakensberg Foothills"
            )
            therapistLetter = "Dear $userName,\n\nIt is deeply gratifying to see you in this space of vitality and balance. You have cultivated genuine harmony between your inner world and outer commitments. Savor this flow, breathe it in, and let this grounded strength carry you forward.\n\nWarmest regards,\nYour HillYeah Therapist"
        } else {
            // Case D: Transition / Adaptive Resilience
            primaryMindState = "Adaptive Navigation & Quiet Re-centering"
            mindStateBadge = "Steady Balance"
            psychologicalVerdict = "Your weekly check-ins reflect an honest, relatable human experience of balancing fluctuating demands. While some days brought uncertainty or mild fatigue regarding $topPreoccupation, your commitment to checking in has kept you anchored. You are in a healthy phase of recalibration—learning to honor both your ambition and your need for personal space."
            somaticNervousSystemState = "Dynamic Homeostatic Adaptation"
            nervousSystemDetail = "Active re-balancing between engagement and recovery, showing healthy adaptive coping mechanisms."
            weeklyEnergySummary = "Intermittent moderate energy responsive to pacing, rest, and outdoor immersion."
            suggestions = listOf(
                TherapeuticSuggestion(
                    title = "Daily Window of Tolerance Check",
                    category = "Cognitive Awareness",
                    insight = "Notice when your thoughts drift toward anxiety (future) or rumination (past), and gently anchor in today.",
                    actionablePractice = "Ask yourself before saying yes to requests: 'Do I have the physiological bandwidth for this today?'"
                ),
                TherapeuticSuggestion(
                    title = "Rhythmic Cadence Walking",
                    category = "Somatic Regulation",
                    insight = "Bilateral stimulation through regular walking strides synchronizes left and right brain hemispheres, reducing cognitive friction.",
                    actionablePractice = "Take a 30-minute walk with consistent footsteps, letting thoughts come and go without judgment."
                )
            )
            somaticPrescription = SomaticPrescription(
                title = "Balanced Nature Trail Immersion",
                practice = "Moderate loop trail with trees and moving water. Pause halfway to sit quietly and drink water mindfully.",
                duration = "40–50 minutes",
                recommendedTrailType = "Forest Loop or Riverbed Trail"
            )
            therapistLetter = "Dear $userName,\n\nYou are navigating life with quiet grace. Even on days when the path ahead feels ambiguous, you have kept your feet firmly on the ground. Keep trusting your own rhythm.\n\nWarmly,\nYour HillYeah Therapist"
        }

        return WeeklyTherapistReport(
            id = UUID.randomUUID().toString(),
            weekTitle = "Weekly Psychological Verdict",
            dateRangeLabel = dateRangeLabel,
            checkInCount = checkInCount,
            primaryMindState = primaryMindState,
            mindStateBadge = mindStateBadge,
            psychologicalVerdict = psychologicalVerdict,
            somaticNervousSystemState = somaticNervousSystemState,
            nervousSystemDetail = nervousSystemDetail,
            weeklyEnergySummary = weeklyEnergySummary,
            dominantPreoccupation = topPreoccupation,
            therapeuticSuggestions = suggestions,
            somaticPrescription = somaticPrescription,
            therapistLetter = therapistLetter
        )
    }
}
