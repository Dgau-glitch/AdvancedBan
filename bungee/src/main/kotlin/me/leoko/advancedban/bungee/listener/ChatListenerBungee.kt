package me.leoko.advancedban.bungee.listener

import me.leoko.advancedban.Universal
import me.leoko.advancedban.utils.Command
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.TabCompleteEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ChatListenerBungee : Listener {
    @EventHandler
    fun onChat(event: ChatEvent) {
        if (event.sender !is ProxiedPlayer) return

        if (!event.isCommand) {
            if (Universal.get().methods.callChat(event.sender)) event.isCancelled = true
        } else {
            if (Universal.get().methods.callCMD(event.sender, event.message)) event.isCancelled = true
        }
    }

    @EventHandler
    fun onTabComplete(event: TabCompleteEvent) {
        val commandName = event.cursor.split(" ")[0]
        if (commandName.length > 1 && event.cursor.length > commandName.length) {
            val command = Command.getByName(commandName.substring(1))
            if (command != null && event.sender is ProxiedPlayer) {
                if (command.permission == null || Universal.get().methods.hasPerms(event.sender, command.permission)) {
                    val args = event.cursor.substring(commandName.length + 1).split(" ", ignoreCase = false, limit = -1).toTypedArray()
                    event.suggestions.addAll(command.tabCompleter.onTabComplete(event.sender, args))
                }
            }
        }
    }
}
