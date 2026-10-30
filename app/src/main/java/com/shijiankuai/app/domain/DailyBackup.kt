package com.shijiankuai.app.domain

import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DailyBackup {
    const val FILE_NAME = "时间块-自动备份.txt"
    const val WORK_NAME = "daily_auto_backup"
    const val MIME = "text/plain"

    private val hm = DateTimeFormatter.ofPattern("HH:mm")

    fun delayUntilNextMidnight(now: ZonedDateTime): Duration {
        val next = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        return Duration.between(now, next)
    }

    fun lastBackupLabel(at: Instant?, zone: ZoneId): String {
        if (at == null) return "尚未备份"
        val local = at.atZone(zone)
        return "上次备份 ${local.monthValue}月${local.dayOfMonth}日 ${local.toLocalTime().format(hm)}"
    }

    fun writeOverwrite(dir: File, body: String, name: String = FILE_NAME): File {
        dir.mkdirs()
        val file = File(dir, name)
        file.writeText(body)
        return file
    }
}
