package me.leoko.advancedban.utils.commandspec

/**
 * Stage 2: declarative metadata for list/check command group from legacy Command.java.
 */
object ListCommandSpecs {
    data class Entry(
        val key: String,
        val permission: String?,
        val regex: String,
        val usagePath: String,
        val names: List<String>
    )

    val entries: List<Entry> = listOf(
        Entry("BAN_LIST", "ab.banlist", "([1-9][0-9]*)?", "Banlist.Usage", listOf("banlist")),
        Entry("HISTORY", "ab.history", "\\S+( [1-9][0-9]*)?", "History.Usage", listOf("history")),
        Entry("WARNS", null, "\\S+( [1-9][0-9]*)?|\\S+|", "Warns.Usage", listOf("warns")),
        Entry("NOTES", null, "\\S+( [1-9][0-9]*)?|\\S+|", "Notes.Usage", listOf("notes")),
        Entry("CHECK", "ab.check", "\\S+", "Check.Usage", listOf("check"))
    )
}
