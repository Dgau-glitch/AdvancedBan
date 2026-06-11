package me.leoko.advancedban.utils.tabcompletion

import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.utils.PunishmentType

object UnwarnTabCompleter : CleanTabCompleter(
    MutableTabCompleter { _, args ->
        when {
            args.size == 1 -> {
                val suggestions = TargetTabSuggestions.warnedPlayers(args)
                suggestions.add("clear")
                suggestions.addAll(PunishmentManager.get().getCurrentPunishmentIds(PunishmentType.WARNING))
                suggestions
            }
            args.size == 2 && args[0].equals("clear", ignoreCase = true) -> TargetTabSuggestions.filtered(
                arrayOf(args[1]),
                PunishmentManager.get().getCurrentPunishmentTargetNames(PunishmentType.WARNING)
            )
            else -> MutableTabCompleter.list()
        }
    }
)
