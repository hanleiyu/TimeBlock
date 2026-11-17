package com.shijiankuai.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonNullsTest {
    @Test
    fun jsonNullAndMissingAreNullNotTheWordNull() {
        val obj = JSONObject().put("intensity", JSONObject.NULL).put("note", JSONObject.NULL)
        assertNull(JsonNulls.text(obj, "intensity"))
        assertNull(JsonNulls.text(obj, "note"))
        assertNull(JsonNulls.text(obj, "absent"))
        assertEquals("MID", JsonNulls.text(JSONObject().put("intensity", "MID"), "intensity"))
    }
}
