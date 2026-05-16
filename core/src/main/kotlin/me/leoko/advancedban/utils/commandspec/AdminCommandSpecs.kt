package me.leoko.advancedban.utils.commandspec

/**
 * Stage 2: declarative metadata for admin/system command group from legacy Command.java.
 */
object AdminCommandSpecs {
    data class Entry(
        val key: String,
        val permission: String?,
        val regex: String,
        val usagePath: String?,
        val names: List<String>
    )

    val entries: List<Entry> = listOf(
        Entry("SYSTEM_PREFERENCES", "ab.systemprefs", ".*", null, listOf("systemprefs")),
        Entry("ADVANCED_BAN", null, ".*", null, listOf("advancedban"))
    )
}
