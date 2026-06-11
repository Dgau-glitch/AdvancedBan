package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.Universal
import me.leoko.advancedban.utils.tabcompletion.CleanTabCompleter
import me.leoko.advancedban.utils.tabcompletion.MutableTabCompleter

/**
 * Stage 5: extracted tab-completer lambdas from legacy Command.java
 * for revoke/list groups, preserving permission checks before suggestions.
 */
object RevokeListTabCompleters {
    val unWarnUnNote = CleanTabCompleter { _, args ->
        if (args.size == 1) {
            MutableTabCompleter.list("[ID]", "clear")
        } else if (args.size == 2 && args[0].equals("clear", true)) {
            MutableTabCompleter.list(CleanTabCompleter.PLAYER_PLACEHOLDER, "[Name]")
        } else {
            MutableTabCompleter.list()
        }
    }

    val changeReason = CleanTabCompleter { _, args ->
        if (args.size <= 1) {
            MutableTabCompleter.list("<ID>", "ban", "mute")
        } else {
            val playerTarget = args[0].equals("ban", true) || args[0].equals("mute", true)
            if (args.size == 2 && playerTarget) {
                MutableTabCompleter.list(CleanTabCompleter.PLAYER_PLACEHOLDER, "[Name]")
            } else if ((playerTarget && args.size == 3) || args.size == 2) {
                MutableTabCompleter.list("new reason...")
            } else {
                MutableTabCompleter.list()
            }
        }
    }

    val history = CleanTabCompleter { _, args ->
        when (args.size) {
            1 -> MutableTabCompleter.list(CleanTabCompleter.PLAYER_PLACEHOLDER, "[Name]")
            2 -> MutableTabCompleter.list("<Page>")
            else -> MutableTabCompleter.list()
        }
    }

    val warns = CleanTabCompleter { user, args ->
        if (args.size == 1) {
            if (Universal.get().methods.hasPerms(user, "ab.warns.other")) {
                MutableTabCompleter.list(CleanTabCompleter.PLAYER_PLACEHOLDER, "<Name>", "<Page>")
            } else {
                MutableTabCompleter.list("<Page>")
            }
        } else if (args.size == 2 && !args[0].matches("\\d+".toRegex())) {
            MutableTabCompleter.list("<Page>")
        } else {
            MutableTabCompleter.list()
        }
    }

    val notes = CleanTabCompleter { user, args ->
        if (args.size == 1) {
            if (Universal.get().methods.hasPerms(user, "ab.notes.other")) {
                MutableTabCompleter.list(CleanTabCompleter.PLAYER_PLACEHOLDER, "<Name>", "<Page>")
            } else {
                MutableTabCompleter.list("<Page>")
            }
        } else if (args.size == 2 && !args[0].matches("\\d+".toRegex())) {
            MutableTabCompleter.list("<Page>")
        } else {
            MutableTabCompleter.list()
        }
    }
}
