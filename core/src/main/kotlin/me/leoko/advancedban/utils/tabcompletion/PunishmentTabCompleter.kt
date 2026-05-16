package me.leoko.advancedban.utils.tabcompletion

import me.leoko.advancedban.Universal
import java.util.ArrayList

class PunishmentTabCompleter(private val temporary: Boolean) : TabCompleter {
    override fun onTabComplete(user: Any, args: Array<String>): List<String> {
        val methodInterface = Universal.get().methods
        val suggestions = ArrayList<String>()

        var hiddenTag = false
        var actualArgs = args
        if (actualArgs.size > 1 && actualArgs[0].equals("-s", ignoreCase = true)) {
            actualArgs = actualArgs.copyOfRange(1, actualArgs.size)
            hiddenTag = true
        }

        if (actualArgs.size == 1) {
            if (!hiddenTag) suggestions.add("-s")
            for (player in methodInterface.onlinePlayers) {
                suggestions.add(methodInterface.getName(player))
            }
            suggestions.add("[Name]")
        } else if (temporary && actualArgs.size == 2) {
            val current = actualArgs[actualArgs.size - 1]
            var amount = current.lowercase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)".toRegex())[0]
            if (current.isEmpty()) amount = "X"

            if (amount.matches("\\d+|X".toRegex())) {
                for (unit in arrayOf("s", "m", "h", "d", "w", "mo")) {
                    suggestions.add(amount + unit)
                }
            }
            for (layout in methodInterface.getKeys(methodInterface.layouts, "Time")) {
                suggestions.add("#$layout")
            }
        } else if ((temporary && actualArgs.size == 3) || actualArgs.size == 2) {
            suggestions.add("Reason...")
            for (layout in methodInterface.getKeys(methodInterface.layouts, "Message")) {
                suggestions.add("@$layout")
            }
        }

        if (actualArgs.isNotEmpty()) {
            suggestions.removeIf { s -> !s.startsWith(actualArgs[actualArgs.size - 1]) }
        }
        return suggestions
    }
}
