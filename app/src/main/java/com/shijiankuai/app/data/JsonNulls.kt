package com.shijiankuai.app.data

import org.json.JSONObject

/** Android's JSONObject.optString(JSON null) returns the literal "null". */
object JsonNulls {
    fun text(obj: JSONObject, key: String): String? {
        if (!obj.has(key) || obj.isNull(key)) return null
        val value = obj.opt(key) ?: return null
        if (value == JSONObject.NULL) return null
        val text = value as? String ?: value.toString()
        return text.takeUnless { it.isEmpty() }
    }
}
