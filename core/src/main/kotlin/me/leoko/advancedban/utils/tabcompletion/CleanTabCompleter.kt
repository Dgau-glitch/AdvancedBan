package me.leoko.advancedban.utils.tabcompletion

import me.leoko.advancedban.Universal
import java.util.ArrayList

open class CleanTabCompleter(private val rawTabCompleter: MutableTabCompleter) : MutableTabCompleter {
    override fun onTabComplete(user: Any, args: Array<String>): ArrayList<String> {
        val suggestions = rawTabCompleter.onTabComplete(user, args)

        if (suggestions.isNotEmpty() && suggestions[0] == PLAYER_PLACEHOLDER) {
            suggestions.removeAt(0)
            for (player in Universal.get().methods.getOnlinePlayers()) {
                suggestions.add(Universal.get().methods.getName(player))
            }
        }

        if (args.isNotEmpty()) {
            suggestions.removeIf { s -> !s.startsWith(args[args.size - 1]) }
        }
        return suggestions
    }

    companion object {
        const val PLAYER_PLACEHOLDER: String = "PLAYERS"
    }
}
