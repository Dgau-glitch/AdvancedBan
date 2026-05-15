package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionListener : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onConnect(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            UUIDManager.get().supplyInternUUID(event.name, event.uniqueId)
            val result = Universal.get().callConnection(event.name, event.address.hostAddress)
            if (result != null) {
                event.kickMessage(Component.text(result))
                event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
            }
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        PunishmentManager.get().discard(event.player.name)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        Universal.get().methods.scheduleAsync({
            if (event.player.name.equals("Leoko", ignoreCase = true)) {
                Universal.get().methods.scheduleAsync({
                    if (Universal.get().broadcastLeoko()) {
                        Universal.get().methods.runSync {
                            val message = Component.text("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^")
                            Bukkit.getOnlinePlayers().forEach { it.sendMessage(message) }
                        }
                    } else {
                        Universal.get().methods.runSync { event.player.sendMessage(Component.text("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)")) }
                    }
                }, 20)
            }
        }, 20)
    }
}
