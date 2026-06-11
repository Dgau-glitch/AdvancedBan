package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.utils.PunishmentType

/**
 * Stage 1 migration map from legacy Command.java to modular Kotlin specs.
 * This file intentionally contains only declarative metadata (regex, perms, names)
 * so it can be introduced without changing runtime behavior.
 */
object PunishmentCommandSpecs {
    data class Entry(
        val key: String,
        val permission: String,
        val regex: String,
        val usagePath: String,
        val names: List<String>
    )

    val entries: List<Entry> = listOf(
        Entry("BAN", PunishmentType.BAN.perms, ".+", PunishmentType.BAN.getConfSection("Usage"), listOf("ban")),
        Entry("TEMP_BAN", PunishmentType.TEMP_BAN.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentType.TEMP_BAN.getConfSection("Usage"), listOf("tempban")),
        Entry("IP_BAN", PunishmentType.IP_BAN.perms, ".+", PunishmentType.IP_BAN.getConfSection("Usage"), listOf("ipban", "banip", "ban-ip")),
        Entry("TEMP_IP_BAN", PunishmentType.TEMP_IP_BAN.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentType.TEMP_IP_BAN.getConfSection("Usage"), listOf("tempipban")),
        Entry("MUTE", PunishmentType.MUTE.perms, ".+", PunishmentType.MUTE.getConfSection("Usage"), listOf("mute")),
        Entry("TEMP_MUTE", PunishmentType.TEMP_MUTE.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentType.TEMP_MUTE.getConfSection("Usage"), listOf("tempmute")),
        Entry("WARN", PunishmentType.WARNING.perms, ".+", PunishmentType.WARNING.getConfSection("Usage"), listOf("warn")),
        Entry("TEMP_WARN", PunishmentType.TEMP_WARNING.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentType.TEMP_WARNING.getConfSection("Usage"), listOf("tempwarn")),
        Entry("NOTE", PunishmentType.NOTE.perms, ".+", PunishmentType.NOTE.getConfSection("Usage"), listOf("note")),
        Entry("KICK", PunishmentType.KICK.perms, ".+", PunishmentType.KICK.getConfSection("Usage"), listOf("kick"))
    )
}
