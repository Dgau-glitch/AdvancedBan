package me.leoko.advancedban.utils

enum class PunishmentType(
    val displayName: String,
    private val basic: PunishmentType?,
    private val temp: Boolean,
    val perms: String
) {
    BAN("Ban", null, false, "ab.ban.perma"),
    TEMP_BAN("Tempban", BAN, true, "ab.ban.temp"),
    IP_BAN("Ipban", BAN, false, "ab.ipban.perma"),
    TEMP_IP_BAN("Tempipban", BAN, true, "ab.ipban.temp"),
    MUTE("Mute", null, false, "ab.mute.perma"),
    TEMP_MUTE("Tempmute", MUTE, true, "ab.mute.temp"),
    WARNING("Warn", null, false, "ab.warn.perma"),
    TEMP_WARNING("Tempwarn", WARNING, true, "ab.warn.temp"),
    KICK("Kick", null, false, "ab.kick.use"),
    NOTE("Note", null, false, "ab.note.use");

    fun getName(): String = displayName

    fun isTemp(): Boolean = temp

    fun getConfSection(path: String): String = "$displayName.$path"

    fun getBasic(): PunishmentType = basic ?: this

    fun getPermanent(): PunishmentType = if (this == IP_BAN || this == TEMP_IP_BAN) IP_BAN else getBasic()

    fun isIpOrientated(): Boolean = this == IP_BAN || this == TEMP_IP_BAN

    companion object {
        @JvmStatic
        fun fromCommandName(cmd: String): PunishmentType? {
            return when (cmd) {
                "ban" -> BAN
                "tempban" -> TEMP_BAN
                "ban-ip", "banip", "ipban" -> IP_BAN
                "tempipban", "tipban" -> TEMP_IP_BAN
                "mute" -> MUTE
                "tempmute" -> TEMP_MUTE
                "warn" -> WARNING
                "note" -> NOTE
                "tempwarn" -> TEMP_WARNING
                "kick" -> KICK
                else -> null
            }
        }
    }
}
