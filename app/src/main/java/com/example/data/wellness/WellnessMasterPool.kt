package com.example.data.wellness

import com.example.data.model.ArchetypeResult
import com.example.data.model.WellnessOption
import com.example.data.model.WellnessQuestion

object WellnessMasterPool {

    val archetypes = mapOf(
        "charger" to ArchetypeResult(
            key = "charger",
            title = "The Kinetic Peak Seeker",
            badge = "⚡ THE CHARGER",
            somaticState = "Metabolic Elevation & High-Intensity Catharsis",
            description = "Your nervous system holds a reservoir of dormant, restless energy that gentle strolls will fail to disperse. You require a physical mountain summit to trigger a full neurobiological reset.",
            nervousSystemNarrative = "Your current somatic state indicates heightened motor readiness. Passive rest at this juncture will likely perpetuate mental restlessness. By demanding intense cardiovascular expenditure—scrambling steep granite and conquering gradient—your system releases endorphins and naturally resets systemic cortisol.",
            recommendedTrailCriteria = "Steep ascents, technical rock scrambling, high elevation gain (>500m), and dramatic summit horizons.",
            sensoryCalmingPrompt = "Take 3 rapid forceful exhalations through the mouth, followed by a long deep inhalation through the nose into the diaphragm. Channel your kinetic focus.",
            accentColorHex = 0xFFD97706 // Warm Amber Gold
        ),
        "cruiser" to ArchetypeResult(
            key = "cruiser",
            title = "The Rhythmic Rhythm Weaver",
            somaticState = "Bilateral Motor Flow & Sustained Endurance",
            badge = "🧭 THE STEADY CRUISER",
            description = "Your cognitive bandwidth is craving bilateral stimulation and uninterrupted forward momentum across rolling contours. Neither extreme sprint nor complete stillness matches your frequency.",
            nervousSystemNarrative = "Your internal cadence thrives when mind and feet synchronize into a steady, uninterrupted flow-state. Rhythmic pacing across undulating terrain gently uncouples circular rumination, fostering bilateral brain hemisphere balance and grounded emotional clarity.",
            recommendedTrailCriteria = "Extended distance (7-14 km), undulating ridge traverses, coastal contours, and panoramic river corridors.",
            sensoryCalmingPrompt = "Match your walking strides to your breathing cadence: inhale for 4 steps, exhale smoothly for 4 steps. Find your unbroken rhythm.",
            accentColorHex = 0xFF264634 // Rich Forest Green
        ),
        "zen" to ArchetypeResult(
            key = "zen",
            title = "The Sanctuary Forest Wanderer",
            somaticState = "Parasympathetic Immersion & Negative-Ion Bathing",
            badge = "🌿 THE ZEN SEEKER",
            description = "Your sympathetic nervous system is operating near its capacity ceiling. Strenuous physical exertion would only amplify exhaustion; your medicine today is deep sanctuary and negative-ion bathing.",
            nervousSystemNarrative = "Your telemetry reveals acute cognitive and sensory overstimulation. Pushing yourself up a grueling mountain would simply flood your system with adrenaline. What you need is restorative canopy cover, rushing stream frequencies, mossy stones, and permission to sit quietly without counting kilometers.",
            recommendedTrailCriteria = "Afro-montane forest canopies, cascading waterfalls, crystal rock pools, and shaded river gorges.",
            sensoryCalmingPrompt = "Physically touch an ancient tree trunk or dip your fingertips into fresh mountain water. Close your eyes and inhale the damp forest humus.",
            accentColorHex = 0xFF5A7254 // Soothing Olive Sage
        )
    )

    val questions: List<WellnessQuestion> = listOf(
        WellnessQuestion(
            id = "q1_somatic_energy",
            category = "Somatic Energy",
            question = "When you scan your physical body right now, what sensations are demanding your attention?",
            options = listOf(
                WellnessOption("q1_opt1", "A surge of restless, kinetic charge looking for rigorous physical release", 4, 1, 0),
                WellnessOption("q1_opt2", "Steady, dependable stamina ready for sustained, rhythmic movement", 1, 4, 1),
                WellnessOption("q1_opt3", "Heavy shoulders and desk fatigue seeking gentle muscular decompression", 0, 1, 4),
                WellnessOption("q1_opt4", "Exhaustion craving quiet stillness, shaded canopy, and cool mountain air", 0, 0, 5)
            )
        ),
        WellnessQuestion(
            id = "q2_cognitive_chatter",
            category = "Cognitive Bandwidth",
            question = "How would you characterize your internal mental chatter this morning?",
            options = listOf(
                WellnessOption("q2_opt1", "Hyperactive and scattered between countless open tabs and urgent demands", 1, 1, 4),
                WellnessOption("q2_opt2", "Alert, sharp, and eager to test myself against a difficult challenge", 4, 2, 0),
                WellnessOption("q2_opt3", "Sluggish mental fog that needs an expansive open horizon to dissolve", 1, 4, 1),
                WellnessOption("q2_opt4", "Deeply overstimulated and desperate for total ambient silence", 0, 0, 5)
            )
        ),
        WellnessQuestion(
            id = "q3_emotional_landscape",
            category = "Emotional Climate",
            question = "Which landscape metaphor best mirrors your emotional climate right now?",
            options = listOf(
                WellnessOption("q3_opt1", "A windswept crag - turbulent and intense, but yearning for triumph", 4, 1, 0),
                WellnessOption("q3_opt2", "A quiet mist-veiled forest floor - soft, tender, and introverted", 0, 1, 5),
                WellnessOption("q3_opt3", "A sun-warmed plateau - balanced, grounded, and peacefully curious", 1, 4, 1),
                WellnessOption("q3_opt4", "A cascading mountain stream - dynamic, refreshing, and washing away residue", 2, 2, 3)
            )
        ),
        WellnessQuestion(
            id = "q4_breath_pattern",
            category = "Somatic Regulation",
            question = "Tune into your natural breathing rhythm without forcing a change. What is it revealing?",
            options = listOf(
                WellnessOption("q4_opt1", "Shallow and held high in the chest; heart rate subtly elevated", 0, 1, 4),
                WellnessOption("q4_opt2", "Deep, grounded belly breaths feeling balanced and ready", 1, 4, 1),
                WellnessOption("q4_opt3", "Quick, invigorating inhalation ready for steep elevation gain", 4, 1, 0),
                WellnessOption("q4_opt4", "Constrained and tight from prolonged sitting and posture fatigue", 1, 2, 3)
            )
        ),
        WellnessQuestion(
            id = "q5_social_battery",
            category = "Social Dynamics",
            question = "What relational dynamic would feel most nourishing for your spirit today?",
            options = listOf(
                WellnessOption("q5_opt1", "Solitary pilgrimage - just my boots, the mountain breeze, and my own mind", 1, 1, 4),
                WellnessOption("q5_opt2", "Side-by-side companionship where silence is comfortable and shared", 1, 4, 2),
                WellnessOption("q5_opt3", "Lively collective camaraderie with shared summits and celebratory high-fives", 4, 2, 0),
                WellnessOption("q5_opt4", "Observing quiet wildlife and listening to birdsong far from crowds", 0, 2, 4)
            )
        ),
        WellnessQuestion(
            id = "q6_dose_craving",
            category = "Therapeutic Dose",
            question = "If the South African wild were a natural apothecary, what prescription do you instinctively seek?",
            options = listOf(
                WellnessOption("q6_opt1", "A heart-pounding summit ascent that demands every ounce of focus and grit", 5, 1, 0),
                WellnessOption("q6_opt2", "A rhythmic 10km contour traverse that induces a moving meditation", 1, 5, 1),
                WellnessOption("q6_opt3", "A shaded indigenous forest walk leading to an icy waterfall rock pool", 0, 1, 5),
                WellnessOption("q6_opt4", "Sitting beneath an ancient yellowwood with a notebook and hot rooibos", 0, 0, 5)
            )
        ),
        WellnessQuestion(
            id = "q7_soundscape",
            category = "Sensory Seeking",
            question = "What soundscape would most effectively regulate your nervous system right now?",
            options = listOf(
                WellnessOption("q7_opt1", "The thunderous roar of a mountain waterfall drowning out all mental noise", 2, 1, 4),
                WellnessOption("q7_opt2", "The rhythmic crunch of trail gravel underfoot and steady breathing", 1, 4, 1),
                WellnessOption("q7_opt3", "Whispering mountain fynbos, gentle wind, and distant Cape sugarbirds", 0, 1, 5),
                WellnessOption("q7_opt4", "Howling gusts on an exposed peak where nothing else can survive", 4, 1, 0)
            )
        ),
        WellnessQuestion(
            id = "q8_stress_origin",
            category = "Stress Vector",
            question = "What has been the primary drain on your vitality over the past 48 hours?",
            options = listOf(
                WellnessOption("q8_opt1", "Endless digital notifications, screens, and sedentary indoor confinement", 3, 3, 1),
                WellnessOption("q8_opt2", "Emotional heaviness, complex conversations, or relational fatigue", 0, 1, 5),
                WellnessOption("q8_opt3", "Under-stimulation, routine stagnation, and feeling uninspired", 4, 2, 0),
                WellnessOption("q8_opt4", "Decision fatigue from juggling relentless competing responsibilities", 1, 2, 4)
            )
        ),
        WellnessQuestion(
            id = "q9_elevation_desire",
            category = "Spatial Horizon",
            question = "When you look at a towering mountain ridge on the horizon, what is your visceral reaction?",
            options = listOf(
                WellnessOption("q9_opt1", "An urge to stand on the highest rocky pinnacle and look down on the clouds", 5, 1, 0),
                WellnessOption("q9_opt2", "To trace the gentle contour paths around its curves and explore its valleys", 1, 4, 2),
                WellnessOption("q9_opt3", "To nestle into its deep shaded ravines and rest beside clear water", 0, 1, 5),
                WellnessOption("q9_opt4", "To sit quietly at its base with reverence for its timeless peace", 0, 0, 5)
            )
        ),
        WellnessQuestion(
            id = "q10_reintegration_outcome",
            category = "Integration Outcome",
            question = "When you return to your car, take off your boots, and exhale, how do you want to feel?",
            options = listOf(
                WellnessOption("q10_opt1", "Physically exhausted, dopamine-rich, and triumphant in my body", 5, 1, 0),
                WellnessOption("q10_opt2", "Grounded, mentally organized, and in effortless harmony with myself", 1, 4, 1),
                WellnessOption("q10_opt3", "Emotionally softened, deeply calm, and emptied of internal tension", 0, 1, 5),
                WellnessOption("q10_opt4", "Awakened with reverence for the ancient, healing beauty of Mzansi", 1, 2, 4)
            )
        )
    )
}
