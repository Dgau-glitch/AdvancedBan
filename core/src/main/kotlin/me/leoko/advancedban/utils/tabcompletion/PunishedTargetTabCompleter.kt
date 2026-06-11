package me.leoko.advancedban.utils.tabcompletion

import me.leoko.advancedban.utils.PunishmentType

class PunishedTargetTabCompleter(
    private val type: PunishmentType,
    private vararg val fallbackArguments: String
) : CleanTabCompleter(
    MutableTabCompleter { _, args ->
        if (args.size == 1) {
            val suggestions = TargetTabSuggestions.punishedPlayers(type, args)
            suggestions.addAll(fallbackArguments)
            suggestions
        } else {
            MutableTabCompleter.list()
        }
    }
)
