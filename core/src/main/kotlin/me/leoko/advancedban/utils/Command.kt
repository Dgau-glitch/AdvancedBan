package me.leoko.advancedban.utils

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.CommandUtils.getPunishment
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.CommandUtils.processReason
import me.leoko.advancedban.utils.commands.ListProcessor
import me.leoko.advancedban.utils.commands.RevokeByIdProcessor
import me.leoko.advancedban.utils.commands.RevokeProcessor
import me.leoko.advancedban.utils.commandspec.*
import me.leoko.advancedban.utils.tabcompletion.BasicTabCompleter
import me.leoko.advancedban.utils.tabcompletion.CleanTabCompleter
import me.leoko.advancedban.utils.tabcompletion.PunishmentTabCompleter
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.function.Consumer
import java.util.function.Predicate

enum class Command(
    val permission: String?,
    private val syntaxValidator: Predicate<Array<String>>,
    val tabCompleter: TabCompleter?,
    private val commandHandler: Consumer<CommandInput>,
    val usagePath: String,
    vararg val names: String
) {
    BAN(PunishmentType.BAN.perms, ".+", PunishmentTabCompleter(false), PunishmentCommandHandlers.processor(PunishmentType.BAN), PunishmentType.BAN.getConfSection("Usage"), "ban"),
    TEMP_BAN(PunishmentType.TEMP_BAN.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentTabCompleter(true), PunishmentCommandHandlers.processor(PunishmentType.TEMP_BAN), PunishmentType.TEMP_BAN.getConfSection("Usage"), "tempban"),
    IP_BAN(PunishmentType.IP_BAN.perms, ".+", PunishmentTabCompleter(false), PunishmentCommandHandlers.processor(PunishmentType.IP_BAN), PunishmentType.IP_BAN.getConfSection("Usage"), "ipban", "banip", "ban-ip"),
    TEMP_IP_BAN(PunishmentType.TEMP_IP_BAN.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentTabCompleter(true), PunishmentCommandHandlers.processor(PunishmentType.TEMP_IP_BAN), PunishmentType.TEMP_IP_BAN.getConfSection("Usage"), "tempipban"),
    MUTE(PunishmentType.MUTE.perms, ".+", PunishmentTabCompleter(false), PunishmentCommandHandlers.processor(PunishmentType.MUTE), PunishmentType.MUTE.getConfSection("Usage"), "mute"),
    TEMP_MUTE(PunishmentType.TEMP_MUTE.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentTabCompleter(true), PunishmentCommandHandlers.processor(PunishmentType.TEMP_MUTE), PunishmentType.TEMP_MUTE.getConfSection("Usage"), "tempmute"),
    WARN(PunishmentType.WARNING.perms, ".+", PunishmentTabCompleter(false), PunishmentCommandHandlers.processor(PunishmentType.WARNING), PunishmentType.WARNING.getConfSection("Usage"), "warn"),
    TEMP_WARN(PunishmentType.TEMP_WARNING.perms, "(-s )?\\S+ ?([1-9][0-9]*([wdhms]|mo)|#.+)( .*)?", PunishmentTabCompleter(true), PunishmentCommandHandlers.processor(PunishmentType.TEMP_WARNING), PunishmentType.TEMP_WARNING.getConfSection("Usage"), "tempwarn"),
    NOTE(PunishmentType.NOTE.perms, ".+", PunishmentTabCompleter(false), PunishmentCommandHandlers.processor(PunishmentType.NOTE), PunishmentType.NOTE.getConfSection("Usage"), "note"),
    KICK(PunishmentType.KICK.perms, ".+", PunishmentTabCompleter(false), PunishmentCommandHandlers.kickHandler, PunishmentType.KICK.getConfSection("Usage"), "kick"),

    UN_BAN("ab.${PunishmentType.BAN.getName()}.undo", "\\S+", BasicTabCompleter("[Name/IP]"), RevokeProcessor(PunishmentType.BAN), "Un" + PunishmentType.BAN.getConfSection("Usage"), "unban"),
    UN_MUTE("ab.${PunishmentType.MUTE.getName()}.undo", "\\S+", BasicTabCompleter(CleanTabCompleter.PLAYER_PLACEHOLDER, "[Name]"), RevokeProcessor(PunishmentType.MUTE), "Un" + PunishmentType.MUTE.getConfSection("Usage"), "unmute"),
    UN_WARN("ab.${PunishmentType.WARNING.getName()}.undo", "[0-9]+|(?i:clear \\S+)", RevokeListTabCompleters.unWarnUnNote, RevokeCommandHandlers.unWarnClearOrId, "Un" + PunishmentType.WARNING.getConfSection("Usage"), "unwarn"),
    UN_NOTE("ab.${PunishmentType.NOTE.getName()}.undo", "[0-9]+|(?i:clear \\S+)", RevokeListTabCompleters.unWarnUnNote, RevokeCommandHandlers.unNoteClearOrId, "Un" + PunishmentType.NOTE.getConfSection("Usage"), "unnote"),
    UN_PUNISH("ab.all.undo", "[0-9]+", BasicTabCompleter("<ID>"), RevokeByIdProcessor("UnPunish", PunishmentManager.get()::getPunishment), "UnPunish.Usage", "unpunish"),
    CHANGE_REASON("ab.changeReason", "([0-9]+|(?i)(ban|mute) \\S+) .+", RevokeListTabCompleters.changeReason, RevokeListHandlers.changeReasonHandler, "ChangeReason.Usage", "change-reason"),

    BAN_LIST("ab.banlist", "([1-9][0-9]*)?", BasicTabCompleter("<Page>"), RevokeListHandlers.banListHandler, "Banlist.Usage", "banlist"),
    HISTORY("ab.history", "\\S+( [1-9][0-9]*)?", RevokeListTabCompleters.history, RevokeListHandlers.historyHandler, "History.Usage", "history"),
    WARNS(null, "\\S+( [1-9][0-9]*)?|\\S+|", RevokeListTabCompleters.warns, ListGroupHandlers.warnsHandler, "Warns.Usage", "warns"),
    NOTES(null, "\\S+( [1-9][0-9]*)?|\\S+|", RevokeListTabCompleters.notes, ListGroupHandlers.notesHandler, "Notes.Usage", "notes"),
    CHECK("ab.check", "\\S+", BasicTabCompleter(CleanTabCompleter.PLAYER_PLACEHOLDER, "[Name]"), ListAdminHandlers.checkHandler, "Check.Usage", "check"),

    SYSTEM_PREFERENCES("ab.systemprefs", ".*", null, ListAdminHandlers.systemPreferencesHandler, "", "systemprefs"),
    ADVANCED_BAN(null, ".*", BasicTabCompleter("help", "reload"), AdminCommandHandlers.advancedBanHandler, "", "advancedban");

    constructor(permission: String?, regex: String, tabCompleter: TabCompleter?, commandHandler: Consumer<CommandInput>, usagePath: String, vararg names: String) :
            this(permission, Predicate { args -> args.joinToString(" ").matches(regex.toRegex()) }, tabCompleter, commandHandler, usagePath, *names)

    fun validateArguments(args: Array<String>): Boolean = syntaxValidator.test(args)

    fun execute(player: Any, args: Array<String>) {
        commandHandler.accept(CommandInput(player, args))
    }

    companion object {
        @JvmStatic
        fun getByName(name: String): Command? {
            val lowerCase = name.lowercase()
            return entries.firstOrNull { command -> command.names.any { it == lowerCase } }
        }
    }

    class CommandInput(private val sender: Any, private var args: Array<String>) {
        fun getPrimary(): String = if (args.isEmpty()) "" else args[0]
        fun getPrimaryData(): String = getPrimary().lowercase()
        fun removeArgument(index: Int) { args = args.filterIndexed { i, _ -> i != index }.toTypedArray() }
        fun next() { if (args.isNotEmpty()) args = args.copyOfRange(1, args.size) }
        fun hasNext(): Boolean = args.isNotEmpty()
        fun getSender(): Any = sender
        fun getArgs(): Array<String> = args

    }
}
