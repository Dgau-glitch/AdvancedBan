@file:Suppress("DEPRECATION")

package me.leoko.advancedban.bukkit.listener

import io.papermc.paper.event.player.AsyncChatEvent
import me.leoko.advancedban.Universal
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listener {
    /**
     * Paper invokes the legacy event before [AsyncChatEvent] when any legacy listener exists.
     * Cancelling at LOWEST prevents compatibility listeners from forwarding a muted message
     * before the modern event is reached.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onLegacyChat(event: AsyncPlayerChatEvent) {
        if (Universal.get().methods.callChat(event.player)) {
            event.isCancelled = true
        }
    }

    /**
     * Defensive modern-event guard. A mute handled by [onLegacyChat] arrives here already
     * cancelled, so ignoreCancelled also prevents sending the mute layout twice.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onModernChat(event: AsyncChatEvent) {
        if (Universal.get().methods.callChat(event.player)) {
            event.isCancelled = true
        }
    }
}
