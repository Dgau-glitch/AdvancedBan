package me.leoko.advancedban.bungee.listener

import com.imaginarycode.minecraft.redisbungee.RedisBungee
import me.leoko.advancedban.Universal
import me.leoko.advancedban.bungee.BungeeMain
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class ConnectionListenerBungee : Listener {
    @EventHandler(priority = EventPriority.LOW)
    fun onConnection(event: LoginEvent) {
        if (event.isCancelled) return

        UUIDManager.get().supplyInternUUID(event.connection.name, event.connection.uniqueId)
        event.registerIntent(Universal.get().methods.plugin as BungeeMain)
        Universal.get().methods.runAsync {
            val result = Universal.get().callConnection(event.connection.name, event.connection.address.address.hostAddress)
            if (result != null) {
                if (BungeeMain.getCloudSupport() != null) {
                    BungeeMain.getCloudSupport()!!.kick(event.connection.uniqueId, result)
                } else {
                    event.isCancelled = true
                    event.setCancelReason(result)
                }
            }

            if (Universal.isRedis()) {
                RedisBungee.getApi().sendChannelMessage("advancedban:connection", event.connection.name + "," + event.connection.address.address.hostAddress)
            }
            event.completeIntent(Universal.get().methods.plugin as BungeeMain)
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerDisconnectEvent) {
        Universal.get().methods.runAsync {
            PunishmentManager.get().discard(event.player.name)
        }
    }

    @EventHandler
    fun onLogin(event: PostLoginEvent) {
        Universal.get().methods.scheduleAsync({
            if (event.player.name.equals("Leoko", ignoreCase = true)) {
                if (Universal.get().broadcastLeoko()) {
                    ProxyServer.getInstance().broadcast("")
                    ProxyServer.getInstance().broadcast("§c§lAdvancedBan §8§l» §7My creator §c§oLeoko §7just joined the game ^^")
                    ProxyServer.getInstance().broadcast("")
                } else {
                    event.player.sendMessage("§c§lAdvancedBan v2 §8§l» §cHey Leoko we are using your Plugin (NO-BC)")
                }
            }
        }, 20)
    }
}
