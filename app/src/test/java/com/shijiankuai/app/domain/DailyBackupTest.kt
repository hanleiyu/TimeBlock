package com.shijiankuai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyBackupTest {
    private val shanghai = ZoneId.of("Asia/Shanghai")

    @Test
    fun fileNameIsFixedChineseBackup() {
        assertEquals("时间块-自动备份.txt", DailyBackup.FILE_NAME)
        assertEquals("text/plain", DailyBackup.MIME)
    }

    @Test
    fun delayFromAfternoonIsUntilNextLocalMidnight() {
        val now = ZonedDateTime.of(LocalDate.of(2026, 10, 27), LocalTime.of(15, 30), shanghai)
        val delay = DailyBackup.delayUntilNextMidnight(now)
        assertEquals(Duration.ofHours(8).plusMinutes(30), delay)
    }

    @Test
    fun delayAtMidnightIsFullNextDay() {
        val midnight = ZonedDateTime.of(LocalDate.of(2026, 10, 27), LocalTime.MIDNIGHT, shanghai)
        assertEquals(Duration.ofHours(24), DailyBackup.delayUntilNextMidnight(midnight))
    }

    @Test
    fun writeOverwriteReplacesSameFixedFile() {
        val dir = Files.createTempDirectory("tb-backup").toFile().resolve("Download")
        val first = DailyBackup.writeOverwrite(dir, """{"version":1}""")
        assertEquals(DailyBackup.FILE_NAME, first.name)
        assertEquals("""{"version":1}""", first.readText())
        val second = DailyBackup.writeOverwrite(dir, """{"version":2,"kv":[]}""")
        assertEquals(first.canonicalPath, second.canonicalPath)
        assertEquals("""{"version":2,"kv":[]}""", second.readText())
        assertEquals(1, dir.listFiles()?.size)
    }

    @Test
    fun lastBackupLabelUsesChineseDate() {
        assertEquals("尚未备份", DailyBackup.lastBackupLabel(null, shanghai))
        val at = ZonedDateTime.of(LocalDate.of(2026, 10, 27), LocalTime.of(0, 3), shanghai).toInstant()
        assertEquals("上次备份 10月27日 00:03", DailyBackup.lastBackupLabel(at, shanghai))
    }
}
