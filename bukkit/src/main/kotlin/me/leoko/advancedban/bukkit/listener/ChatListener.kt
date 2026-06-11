package me.leoko.advancedban.bukkit.listener

import io.papermc.paper.event.player.AsyncChatEvent
import me.leoko.advancedban.Universal
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncChatEvent) {
        if (Universal.get().methods.callChat(event.player)) {
            event.isCancelled = true
        }
    }
}
