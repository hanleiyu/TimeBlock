package com.shijiankuai.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreGuardTest {
    @Test
    fun exceptionsBecomeFalseInsteadOfPropagating() {
        assertTrue(RestoreGuard.run { })
        assertFalse(RestoreGuard.run { error("illegal intensity null") })
        assertFalse(
            RestoreGuard.run {
                throw IllegalArgumentException("No enum constant Intensity.null")
            },
        )
    }
}
