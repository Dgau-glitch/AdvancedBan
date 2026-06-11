package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.utils.PunishmentType

/**
 * Stage 2: declarative metadata for revoke/undo command group from legacy Command.java.
 */
object RevokeCommandSpecs {
    data class Entry(
        val key: String,
        val permission: String,
        val regex: String,
        val usagePath: String,
        val names: List<String>
    )

    val entries: List<Entry> = listOf(
        Entry("UN_BAN", "ab.${PunishmentType.BAN.getName()}.undo", "\\S+", "Un" + PunishmentType.BAN.getConfSection("Usage"), listOf("unban")),
        Entry("UN_MUTE", "ab.${PunishmentType.MUTE.getName()}.undo", "\\S+", "Un" + PunishmentType.MUTE.getConfSection("Usage"), listOf("unmute")),
        Entry("UN_WARN", "ab.${PunishmentType.WARNING.getName()}.undo", "[0-9]+|\\S+|(?i:clear \\S+)", "Un" + PunishmentType.WARNING.getConfSection("Usage"), listOf("unwarn")),
        Entry("UN_NOTE", "ab.${PunishmentType.NOTE.getName()}.undo", "[0-9]+|\\S+|(?i:clear \\S+)", "Un" + PunishmentType.NOTE.getConfSection("Usage"), listOf("unnote")),
        Entry("UN_PUNISH", "ab.all.undo", "[0-9]+", "UnPunish.Usage", listOf("unpunish")),
        Entry("CHANGE_REASON", "ab.changeReason", "([0-9]+|(?i)(ban|mute) \\S+) .+", "ChangeReason.Usage", listOf("change-reason"))
    )
}
