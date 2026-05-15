package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.Universal
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncPlayerChatEvent) {
        if (Universal.get().methods.callChat(event.player)) {
            event.isCancelled = true
        }
    }
}
