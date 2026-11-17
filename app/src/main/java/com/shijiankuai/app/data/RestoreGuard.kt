package com.shijiankuai.app.data

object RestoreGuard {
    inline fun run(block: () -> Unit): Boolean = try {
        block()
        true
    } catch (_: Exception) {
        false
    }

    suspend fun runSuspend(block: suspend () -> Unit): Boolean = try {
        block()
        true
    } catch (_: Exception) {
        false
    }
}
