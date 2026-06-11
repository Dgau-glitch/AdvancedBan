package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.Universal
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class CommandListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (Universal.get().methods.callCMD(event.player, event.message)) {
            event.isCancelled = true
        }
    }
}
