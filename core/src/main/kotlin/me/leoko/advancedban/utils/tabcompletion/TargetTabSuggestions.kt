package me.leoko.advancedban.utils.tabcompletion

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.utils.PunishmentType

object TargetTabSuggestions {
    private const val MAX_SUGGESTIONS = 50

    fun knownPlayers(args: Array<String>): ArrayList<String> = suggestFirstArgument(args) {
        onlineNames() + PunishmentManager.get().getKnownTargetNames()
    }

    fun punishedPlayers(type: PunishmentType, args: Array<String>): ArrayList<String> = suggestFirstArgument(args) {
        onlineNames() + PunishmentManager.get().getCurrentPunishmentTargetNames(type)
    }

    fun warnedPlayers(args: Array<String>): ArrayList<String> = suggestFirstArgument(args) {
        onlineNames() + PunishmentManager.get().getCurrentPunishmentTargetNames(PunishmentType.WARNING)
    }

    fun filtered(args: Array<String>, candidates: Iterable<String>): ArrayList<String> = suggestFirstArgument(args) { candidates }

    private fun suggestFirstArgument(args: Array<String>, candidates: () -> Iterable<String>): ArrayList<String> {
        if (args.size != 1) return arrayListOf()
        val prefix = args.firstOrNull().orEmpty()
        val unique = LinkedHashSet<String>()
        candidates()
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .filter { prefix.isBlank() || it.startsWith(prefix, ignoreCase = true) }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
            .take(MAX_SUGGESTIONS)
            .forEach(unique::add)
        return ArrayList(unique)
    }

    private fun onlineNames(): List<String> {
        val methods = Universal.get().methods
        return methods.getOnlinePlayers().map(methods::getName)
    }
}
