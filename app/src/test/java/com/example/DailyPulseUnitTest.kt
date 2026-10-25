package com.example

import com.example.data.model.DailyPulseCheckIn
import com.example.data.model.SuggestedActivityType
import com.example.data.wellness.DailyPulseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyPulseUnitTest {

    @Test
    fun testAllSixQuestionsPresentWithExactOptions() {
        val questions = DailyPulseData.questions
        assertEquals(6, questions.size)

        // Q1
        assertEquals("How are you feeling today?", questions[0].question)
        assertEquals(
            listOf(
                "Great — feeling good",
                "Okay — taking things as they come",
                "A little heavy emotionally",
                "Overwhelmed — a lot on my mind",
                "Not quite sure how I feel"
            ),
            questions[0].options
        )

        // Q2
        assertEquals("How is your energy today?", questions[1].question)
        assertEquals(
            listOf(
                "High — ready to go",
                "Moderate — I can manage",
                "Low — I need to take it easy",
                "Exhausted — I need rest"
            ),
            questions[1].options
        )

        // Q3
        assertEquals("What's taking up the most space in your mind right now?", questions[2].question)
        assertEquals(
            listOf(
                "Work, studies or responsibilities",
                "Relationships or family",
                "My future or uncertainty",
                "Personal growth and challenges",
                "Nothing specific — I just need space",
                "I'd rather not say"
            ),
            questions[2].options
        )

        // Q4
        assertEquals("How connected do you feel to the people around you?", questions[3].question)
        assertEquals(
            listOf(
                "Connected and supported",
                "Somewhat connected",
                "A little disconnected",
                "I need some time to myself"
            ),
            questions[3].options
        )

        // Q5
        assertEquals("How are you feeling about what's ahead?", questions[4].question)
        assertEquals(
            listOf(
                "Hopeful — things are looking up",
                "Uncertain — taking it one day at a time",
                "Pressured — there's a lot to figure out",
                "Discouraged — things feel difficult"
            ),
            questions[4].options
        )

        // Q6
        assertEquals("What would help you most today?", questions[5].question)
        assertEquals(
            listOf(
                "Taking it easy and resting",
                "Getting outside and moving",
                "Finding some peace in nature",
                "Connecting with someone",
                "Reflecting and clearing my mind",
                "Making progress on one small thing"
            ),
            questions[5].options
        )
    }

    @Test
    fun testOverwhelmedWithGoodEnergySuggestsSteppingOutside() {
        val answers = mapOf(
            1 to "Overwhelmed — a lot on my mind",
            2 to "High — ready to go",
            3 to "Work, studies or responsibilities",
            4 to "Connected and supported",
            5 to "Pressured — there's a lot to figure out",
            6 to "Getting outside and moving"
        )

        val interpretation = DailyPulseData.interpret(answers)
        assertTrue(interpretation.reflection.contains("You may have a lot on your mind, even though your energy is still good"))
        assertTrue(interpretation.practicalNextStep.contains("outdoors") || interpretation.practicalNextStep.contains("walk"))
    }

    @Test
    fun testExhaustedUserRespectsRestAndNeverPushesDifficultHikes() {
        val answers = mapOf(
            1 to "A little heavy emotionally",
            2 to "Exhausted — I need rest",
            3 to "Personal growth and challenges",
            4 to "I need some time to myself",
            5 to "Discouraged — things feel difficult",
            6 to "Taking it easy and resting"
        )

        val interpretation = DailyPulseData.interpret(answers)
        assertEquals(SuggestedActivityType.REST_AND_RECHARGE, interpretation.suggestedActivityType)
        assertTrue(interpretation.hillYeahSuggestion.contains("No mountain needs to be climbed today"))
        assertTrue(interpretation.reflection.contains("recharge") || interpretation.reflection.contains("rest"))
    }

    @Test
    fun testUncertainFutureAcknowledgesOneDayAtATime() {
        val answers = mapOf(
            1 to "Okay — taking things as they come",
            2 to "Moderate — I can manage",
            3 to "My future or uncertainty",
            4 to "Somewhat connected",
            5 to "Uncertain — taking it one day at a time",
            6 to "Finding some peace in nature"
        )

        val interpretation = DailyPulseData.interpret(answers)
        assertTrue(interpretation.reflection.contains("one day at a time") || interpretation.reflection.contains("uncertain"))
        assertEquals(SuggestedActivityType.GENTLE_NATURE_WALK, interpretation.suggestedActivityType)
    }

    @Test
    fun testSerializationAndDeserialization() {
        val answers = mapOf(
            1 to "Great — feeling good",
            2 to "High — ready to go",
            3 to "Nothing specific — I just need space",
            4 to "Connected and supported",
            5 to "Hopeful — things are looking up",
            6 to "Getting outside and moving"
        )

        val interpretation = DailyPulseData.interpret(answers)
        val checkIn = DailyPulseCheckIn(
            id = "test-123",
            userId = "user-abc",
            date = "2026-10-22",
            timestamp = 1700000000000L,
            answers = answers,
            interpretation = interpretation
        )

        val serialized = DailyPulseData.serializeCheckIn(checkIn)
        val deserialized = DailyPulseData.deserializeCheckIn(serialized)

        assertNotNull(deserialized)
        assertEquals("test-123", deserialized?.id)
        assertEquals("user-abc", deserialized?.userId)
        assertEquals("2026-10-22", deserialized?.date)
        assertEquals("Great — feeling good", deserialized?.answers?.get(1))
        assertEquals(interpretation.reflection, deserialized?.interpretation?.reflection)
    }
}
