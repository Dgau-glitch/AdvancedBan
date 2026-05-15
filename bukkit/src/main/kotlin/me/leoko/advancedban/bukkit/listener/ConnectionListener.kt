package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
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
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, result)
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
                            Bukkit.broadcastMessage("")
                            Bukkit.broadcastMessage("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^")
                            Bukkit.broadcastMessage("")
                        }
                    } else {
                        Universal.get().methods.runSync { event.player.sendMessage("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)") }
                    }
                }, 20)
            }
        }, 20)
    }
}
