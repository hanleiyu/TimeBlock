package com.shijiankuai.app.domain

object Prefs {
    const val SHOW_ALL_TIME = "show_all_time"

    fun showAllTimeEnabled(raw: String?): Boolean = raw != "0"
}
