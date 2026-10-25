package com.example.data.wellness

import com.example.data.model.DailyPulseCheckIn
import com.example.data.model.DailyPulseInterpretation
import com.example.data.model.DailyPulseQuestion
import com.example.data.model.SuggestedActivityType
import org.json.JSONArray
import org.json.JSONObject

object DailyPulseData {

    /**
     * Exact 6 Daily Pulse wellness questions with simple, friendly English.
     */
    val questions: List<DailyPulseQuestion> = listOf(
        DailyPulseQuestion(
            id = 1,
            question = "How are you feeling today?",
            options = listOf(
                "Great — feeling good",
                "Okay — taking things as they come",
                "A little heavy emotionally",
                "Overwhelmed — a lot on my mind",
                "Not quite sure how I feel"
            )
        ),
        DailyPulseQuestion(
            id = 2,
            question = "How is your energy today?",
            options = listOf(
                "High — ready to go",
                "Moderate — I can manage",
                "Low — I need to take it easy",
                "Exhausted — I need rest"
            )
        ),
        DailyPulseQuestion(
            id = 3,
            question = "What's taking up the most space in your mind right now?",
            options = listOf(
                "Work, studies or responsibilities",
                "Relationships or family",
                "My future or uncertainty",
                "Personal growth and challenges",
                "Nothing specific — I just need space",
                "I'd rather not say"
            )
        ),
        DailyPulseQuestion(
            id = 4,
            question = "How connected do you feel to the people around you?",
            options = listOf(
                "Connected and supported",
                "Somewhat connected",
                "A little disconnected",
                "I need some time to myself"
            )
        ),
        DailyPulseQuestion(
            id = 5,
            question = "How are you feeling about what's ahead?",
            options = listOf(
                "Hopeful — things are looking up",
                "Uncertain — taking it one day at a time",
                "Pressured — there's a lot to figure out",
                "Discouraged — things feel difficult"
            )
        ),
        DailyPulseQuestion(
            id = 6,
            question = "What would help you most today?",
            options = listOf(
                "Taking it easy and resting",
                "Getting outside and moving",
                "Finding some peace in nature",
                "Connecting with someone",
                "Reflecting and clearing my mind",
                "Making progress on one small thing"
            )
        )
    )

    /**
     * Interprets the user's answers collectively like a supportive friend checking in.
     * Never diagnoses, never makes users feel guilty, never forces positivity, and always respects rest.
     */
    fun interpret(answers: Map<Int, String?>): DailyPulseInterpretation {
        val feeling = answers[1]
        val energy = answers[2]
        val mind = answers[3]
        val connection = answers[4]
        val future = answers[5]
        val whatHelps = answers[6]

        val isExhausted = energy == "Exhausted — I need rest" || whatHelps == "Taking it easy and resting"
        val isLowEnergy = energy == "Low — I need to take it easy"
        val isHighEnergy = energy == "High — ready to go"
        val isOverwhelmed = feeling == "Overwhelmed — a lot on my mind"
        val isHeavy = feeling == "A little heavy emotionally"
        val isGreat = feeling == "Great — feeling good"
        val isDisconnected = connection == "A little disconnected" || connection == "I need some time to myself"

        // A. A gentle reflection
        val reflection = when {
            isExhausted -> {
                if (isHeavy || isOverwhelmed) {
                    "It sounds like you may be carrying quite a lot right now and your body is asking for a pause. Needing rest or feeling low energy isn't a failure—it's simply your body letting you know it needs a breather. Giving yourself permission to rest and recharge is the kindest thing you can do today."
                } else {
                    "You may be feeling depleted today, and your system is asking for gentle recovery. It's completely okay to step back, take off the pressure, and allow your body and mind to catch up."
                }
            }
            isOverwhelmed && (isHighEnergy || energy == "Moderate — I can manage") -> {
                "You may have a lot on your mind, even though your energy is still good. You don't necessarily need to push harder today. A change of environment might help."
            }
            isOverwhelmed && isLowEnergy -> {
                "It sounds like there is a lot on your plate while your energy is running low. When your mind is full and your battery is modest, keeping today's expectations light can help create room to breathe."
            }
            isOverwhelmed -> {
                "It sounds like there is a lot competing for your attention today. When things feel full like this, stepping out of the noise for even a little while can help bring things into gentler focus."
            }
            isHeavy && isDisconnected -> {
                "You might be feeling a bit heavy emotionally, and things around you might feel a little distant today. Giving yourself quiet compassion and taking things slowly is more than enough."
            }
            isHeavy && connection == "Connected and supported" -> {
                "Things may feel a little heavy inside today, but it is comforting that you have supportive people in your corner. You don't have to carry everything alone."
            }
            isHeavy -> {
                "You may be feeling a little heavy emotionally today. There is no need to force yourself to feel positive or cheerful—meeting yourself right where you are is always the right place to begin."
            }
            future == "Uncertain — taking it one day at a time" || future == "Pressured — there's a lot to figure out" -> {
                "It sounds like the path ahead feels uncertain or demanding right now. Remember that you do not need to have everything figured out today; taking things one day at a time is real progress."
            }
            future == "Discouraged — things feel difficult" -> {
                "It sounds like things are feeling particularly difficult right now. Please be gentle with yourself today. You don't have to conquer anything today—just take small, steady breaths."
            }
            isGreat && isHighEnergy -> {
                "You're in a great space with high energy and a bright outlook! It sounds like you have good momentum to enjoy whatever comes your way today."
            }
            isGreat -> {
                "It sounds like you're feeling good today and taking things in your stride with a light heart."
            }
            feeling == "Okay — taking things as they come" -> {
                "It sounds like you're in a grounded, steady rhythm today, ready to navigate whatever unfolds one step at a time."
            }
            else -> {
                "It sounds like today is an open page, and you might still be settling into how you feel. Pausing to check in with yourself is a wonderful and gentle practice in itself."
            }
        }

        // B. One practical next step
        val practicalNextStep = when {
            whatHelps == "Taking it easy and resting" || isExhausted -> {
                "Give yourself permission to do less today. Put your feet up, drink some water or warm tea, and let non-essential chores wait for tomorrow."
            }
            whatHelps == "Getting outside and moving" -> {
                if (isHighEnergy) {
                    "Lace up your trainers and get outdoors for an active walk or hike where you can stretch your legs and move freely."
                } else {
                    "Try a gentle walk outdoors without the pressure to achieve anything."
                }
            }
            whatHelps == "Finding some peace in nature" -> {
                "Step outside into a garden, park, or quiet green corner. Sit quietly for 5 minutes, close your eyes, and listen to the birds and the breeze."
            }
            whatHelps == "Connecting with someone" -> {
                "Send a short, relaxed message to someone you feel comfortable with, or invite a friend for an easy catch-up without any expectations."
            }
            whatHelps == "Reflecting and clearing my mind" -> {
                "Jot down whatever thoughts are swirling around on a piece of paper to get them out of your head, or take an unhurried stroll without your phone."
            }
            whatHelps == "Making progress on one small thing" -> {
                "Pick one small, realistic task you can finish in 10 minutes. Complete it, cross it off, and give yourself credit for taking a step forward."
            }
            isLowEnergy -> {
                "Take a few slow, unhurried breaths, keep your schedule light, and give yourself extra time between tasks today."
            }
            else -> {
                "Step outside for a few minutes of fresh air and sunshine to give your senses a simple, natural lift."
            }
        }

        // C. A relevant HillYeah suggestion
        val (hillYeahSuggestion, activityType) = when {
            whatHelps == "Taking it easy and resting" || isExhausted -> {
                Pair(
                    "Taking time to rest: No mountain needs to be climbed today. If you want some fresh air, relax on a lawn, garden bench, or sunny veranda with a warm drink. Rest is an essential part of the journey.",
                    SuggestedActivityType.REST_AND_RECHARGE
                )
            }
            whatHelps == "Finding some peace in nature" -> {
                Pair(
                    "A gentle nature walk: Explore a shaded botanical garden, quiet forest path, or river bank where you can walk at your own gentle pace surrounded by green trees and running water.",
                    SuggestedActivityType.GENTLE_NATURE_WALK
                )
            }
            whatHelps == "Connecting with someone" -> {
                if (isHighEnergy || energy == "Moderate — I can manage") {
                    Pair(
                        "Connecting with a hiking buddy or joining a group: Check out an upcoming social group hike on HillYeah or invite a friendly hiking buddy to share a scenic trail together.",
                        SuggestedActivityType.GROUP_OR_BUDDY_HIKE
                    )
                } else {
                    Pair(
                        "Connecting quietly: Invite a close friend or family member for a relaxed stroll in a nearby park or an unhurried tea rather than a strenuous hike.",
                        SuggestedActivityType.GROUP_OR_BUDDY_HIKE
                    )
                }
            }
            whatHelps == "Getting outside and moving" -> {
                if (isHighEnergy) {
                    Pair(
                        "An invigorating hike: Your body has good energy to burn. Pick a scenic trail with wide open horizons or a rewarding view to move and feel alive.",
                        SuggestedActivityType.SOLO_PEACEFUL_HIKE
                    )
                } else {
                    Pair(
                        "A gentle nature walk: Choose a flat or easy contour path where you can stretch your legs and enjoy the scenery without pushing your limits.",
                        SuggestedActivityType.GENTLE_NATURE_WALK
                    )
                }
            }
            whatHelps == "Reflecting and clearing my mind" -> {
                Pair(
                    "A solo hike or quiet stroll: Take an easy solo walk on an open contour path where the wide sky and nature can give your mind room to settle.",
                    SuggestedActivityType.MIND_CLEARING_STROLL
                )
            }
            whatHelps == "Making progress on one small thing" -> {
                Pair(
                    "Reflecting on one small personal goal: Mark a trail on HillYeah that you would love to visit in the future, or set a gentle intention to take 10 mindful steps outside today.",
                    SuggestedActivityType.SMALL_GOAL_FOCUS
                )
            }
            isLowEnergy -> {
                Pair(
                    "Spending a few quiet minutes outdoors: Sit by a quiet viewpoint, garden lawn, or shady tree for 10 minutes of still, grounded peace.",
                    SuggestedActivityType.QUIET_OUTDOOR_MOMENT
                )
            }
            else -> {
                Pair(
                    "A gentle nature walk: A calm, scenic stroll along an easy trail to refresh your perspective.",
                    SuggestedActivityType.GENTLE_NATURE_WALK
                )
            }
        }

        val gentleHeadline = when {
            isExhausted -> "Time to recharge and be kind to yourself"
            isOverwhelmed -> "Giving your mind space to breathe"
            isHeavy -> "Meeting yourself with patience today"
            isGreat -> "Enjoying your good momentum"
            else -> "Moving gently with today's flow"
        }

        return DailyPulseInterpretation(
            reflection = reflection,
            practicalNextStep = practicalNextStep,
            hillYeahSuggestion = hillYeahSuggestion,
            suggestedActivityType = activityType,
            gentleHeadline = gentleHeadline
        )
    }

    // -----------------------------------------------------------------------
    // JSON Serialization Helpers for User Check-In Persistence
    // -----------------------------------------------------------------------

    fun serializeCheckIn(checkIn: DailyPulseCheckIn): String {
        val root = JSONObject()
        root.put("id", checkIn.id)
        root.put("userId", checkIn.userId)
        root.put("date", checkIn.date)
        root.put("timestamp", checkIn.timestamp)

        val answersObj = JSONObject()
        checkIn.answers.forEach { (qId, ans) ->
            answersObj.put(qId.toString(), ans ?: JSONObject.NULL)
        }
        root.put("answers", answersObj)

        val interpObj = JSONObject()
        interpObj.put("reflection", checkIn.interpretation.reflection)
        interpObj.put("practicalNextStep", checkIn.interpretation.practicalNextStep)
        interpObj.put("hillYeahSuggestion", checkIn.interpretation.hillYeahSuggestion)
        interpObj.put("suggestedActivityType", checkIn.interpretation.suggestedActivityType.name)
        interpObj.put("gentleHeadline", checkIn.interpretation.gentleHeadline)
        root.put("interpretation", interpObj)

        return root.toString()
    }

    fun deserializeCheckIn(jsonStr: String): DailyPulseCheckIn? {
        return try {
            val root = JSONObject(jsonStr)
            val id = root.getString("id")
            val userId = root.optString("userId", "guest")
            val date = root.getString("date")
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())

            val answersMap = mutableMapOf<Int, String?>()
            val answersObj = root.optJSONObject("answers")
            if (answersObj != null) {
                val keys = answersObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val qId = k.toIntOrNull() ?: continue
                    val opt = if (answersObj.isNull(k)) null else answersObj.getString(k)
                    answersMap[qId] = opt
                }
            }

            val interpObj = root.getJSONObject("interpretation")
            val actTypeName = interpObj.optString("suggestedActivityType", SuggestedActivityType.GENTLE_NATURE_WALK.name)
            val actType = try {
                SuggestedActivityType.valueOf(actTypeName)
            } catch (e: Exception) {
                SuggestedActivityType.GENTLE_NATURE_WALK
            }

            val interp = DailyPulseInterpretation(
                reflection = interpObj.getString("reflection"),
                practicalNextStep = interpObj.getString("practicalNextStep"),
                hillYeahSuggestion = interpObj.getString("hillYeahSuggestion"),
                suggestedActivityType = actType,
                gentleHeadline = interpObj.optString("gentleHeadline", "Today's Gentle Direction")
            )

            DailyPulseCheckIn(
                id = id,
                userId = userId,
                date = date,
                timestamp = timestamp,
                answers = answersMap,
                interpretation = interp
            )
        } catch (e: Exception) {
            null
        }
    }

    fun serializeCheckInList(list: List<DailyPulseCheckIn>): String {
        val array = JSONArray()
        list.forEach { item ->
            try {
                array.put(JSONObject(serializeCheckIn(item)))
            } catch (e: Exception) {
                // ignore
            }
        }
        return array.toString()
    }

    fun deserializeCheckInList(jsonStr: String): List<DailyPulseCheckIn> {
        if (jsonStr.isBlank()) return emptyList()
        val result = mutableListOf<DailyPulseCheckIn>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val checkIn = deserializeCheckIn(obj.toString())
                if (checkIn != null) {
                    result.add(checkIn)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return result
    }
}
