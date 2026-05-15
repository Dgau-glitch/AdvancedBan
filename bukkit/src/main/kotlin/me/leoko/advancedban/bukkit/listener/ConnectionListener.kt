package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.bukkit.BukkitMain
import me.leoko.advancedban.bukkit.utils.FoliaSchedulers
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
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text(result))
            }
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        PunishmentManager.get().discard(event.player.name)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (!event.player.name.equals("Leoko", ignoreCase = true)) return
        FoliaSchedulers.runAsyncLater(BukkitMain.get(), 20) {
            if (Universal.get().broadcastLeoko()) {
                val message = Component.text("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^")
                Bukkit.getOnlinePlayers().forEach { online ->
                    FoliaSchedulers.runPlayer(online, BukkitMain.get()) { online.sendMessage(message) }
                }
            } else {
                FoliaSchedulers.runPlayer(event.player, BukkitMain.get()) {
                    event.player.sendMessage(Component.text("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)"))
                }
            }
        }
    }
}
