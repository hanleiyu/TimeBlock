package com.shijiankuai.app.domain

import java.time.Instant

object Prefs {
    const val SHOW_ALL_TIME = "show_all_time"
    const val AUTO_BACKUP = "auto_backup"
    const val LAST_AUTO_BACKUP_AT = "last_auto_backup_at"
    const val DARK_THEME = "dark_theme"

    fun showAllTimeEnabled(raw: String?): Boolean = raw != "0"

    fun autoBackupEnabled(raw: String?): Boolean = raw != "0"

    fun darkThemeEnabled(raw: String?): Boolean = raw != "0"

    fun lastAutoBackupAt(raw: String?): Instant? = raw?.toLongOrNull()?.let(Instant::ofEpochMilli)
}
