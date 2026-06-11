package me.leoko.advancedban.utils.tabcompletion

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.utils.PunishmentType

object TargetTabSuggestions {
    private const val MAX_SUGGESTIONS = 80

    fun knownPlayers(args: Array<String>): ArrayList<String> = suggestFirstArgument(args) {
        sequenceOf(
            Universal.get().methods.getKnownPlayerNames(),
            PunishmentManager.get().getKnownTargetNames()
        )
    }

    fun punishedPlayers(type: PunishmentType, args: Array<String>): ArrayList<String> = suggestFirstArgument(args) {
        sequenceOf(PunishmentManager.get().getCurrentPunishmentTargetNames(type))
    }

    fun warnedPlayers(args: Array<String>): ArrayList<String> = suggestFirstArgument(args) {
        sequenceOf(PunishmentManager.get().getCurrentPunishmentTargetNames(PunishmentType.WARNING))
    }

    fun filtered(args: Array<String>, candidates: Iterable<String>): ArrayList<String> = suggestFirstArgument(args) { sequenceOf(candidates) }

    private fun suggestFirstArgument(args: Array<String>, candidates: () -> Sequence<Iterable<String>>): ArrayList<String> {
        if (args.size != 1) return arrayListOf()
        val needle = args.firstOrNull().orEmpty().trim()
        val unique = LinkedHashSet<String>()
        collectMatches(candidates(), unique) { candidate -> needle.isBlank() || candidate.startsWith(needle, ignoreCase = true) }
        if (needle.isNotBlank() && unique.size < MAX_SUGGESTIONS) {
            collectMatches(candidates(), unique) { candidate -> candidate.contains(needle, ignoreCase = true) }
        }
        return ArrayList(unique)
    }

    private fun collectMatches(
        sources: Sequence<Iterable<String>>,
        target: LinkedHashSet<String>,
        predicate: (String) -> Boolean
    ) {
        for (source in sources) {
            for (candidate in source) {
                if (target.size >= MAX_SUGGESTIONS) return
                val normalized = candidate.trim()
                if (normalized.isNotEmpty() && predicate(normalized)) target.add(normalized)
            }
        }
    }
}
