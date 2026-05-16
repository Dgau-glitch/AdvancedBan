package me.leoko.advancedban.manager

import me.leoko.advancedban.Universal
import java.util.Date

object TimeManager {
    @JvmStatic
    fun getTime(): Long {
        return Date().time + Universal.get().methods.getInteger(Universal.get().methods.config, "TimeDiff", 0) * 60L * 60L * 1000L
    }

    @JvmStatic
    fun toMilliSec(s: String): Long {
        val sl = s.lowercase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)".toRegex())
        val i = sl[0].toLong()
        return when (sl[1]) {
            "s" -> i * 1000L
            "m" -> i * 1000L * 60L
            "h" -> i * 1000L * 60L * 60L
            "d" -> i * 1000L * 60L * 60L * 24L
            "w" -> i * 1000L * 60L * 60L * 24L * 7L
            "mo" -> i * 1000L * 60L * 60L * 24L * 30L
            else -> -1L
        }
    }
}
