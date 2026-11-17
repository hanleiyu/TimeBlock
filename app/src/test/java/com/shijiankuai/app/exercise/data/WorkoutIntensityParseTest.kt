package com.shijiankuai.app.exercise.data

import com.shijiankuai.app.exercise.domain.Intensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class WorkoutIntensityParseTest {
    @Test
    fun androidOptStringNullDoesNotCrashToDomain() {
        assertNull(parseIntensity("null"))
        assertNull(parseIntensity(""))
        assertNull(parseIntensity("  "))
        assertNull(parseIntensity(null))
        assertEquals(Intensity.MID, parseIntensity("MID"))
        val poisoned = WorkoutEntity(
            id = 1,
            category = "PERIOD",
            activityName = "经期",
            durationMinutes = 0,
            intensity = "null",
            dateEpochDay = LocalDate.of(2026, 11, 16).toEpochDay(),
            note = "null",
            createdAt = 1L,
            isFree = false,
        )
        val domain = poisoned.toDomain()
        assertNull(domain.intensity)
        assertNull(domain.note)
        assertEquals("PERIOD", domain.category.name)
    }
}
