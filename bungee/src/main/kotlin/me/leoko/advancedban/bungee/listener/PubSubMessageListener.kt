package me.leoko.advancedban.bungee.listener

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent
import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class PubSubMessageListener : Listener {
    @EventHandler
    fun onMessageReceive(event: PubSubMessageEvent) {
        when (event.channel) {
            "advancedban:main" -> handleMainChannel(event.message)
            "advancedban:connection" -> handleConnectionChannel(event.message)
        }
    }

    private fun handleMainChannel(message: String) {
        when {
            message.startsWith("kick ") -> {
                val payload = splitPayload(message)
                ProxyServer.getInstance().getPlayer(payload.target)?.disconnect(payload.body)
            }

            message.startsWith("notification ") -> {
                val payload = splitPayload(message)
                ProxyServer.getInstance().players.forEach { player: ProxiedPlayer ->
                    if (methods.hasPerms(player, payload.target)) {
                        methods.sendMessage(player, payload.body)
                    }
                }
            }

            message.startsWith("message ") -> {
                val payload = splitPayload(message)
                ProxyServer.getInstance().getPlayer(payload.target)?.sendMessage(payload.body)
                if (payload.target.equals("CONSOLE", ignoreCase = true)) {
                    ProxyServer.getInstance().console.sendMessage(payload.body)
                }
            }
        }
    }

    private fun handleConnectionChannel(message: String) {
        val parts = message.split(",", limit = 2)
        if (parts.size < 2) return
        Universal.get().ips.remove(parts[0].lowercase())
        Universal.get().ips[parts[0].lowercase()] = parts[1]
    }

    private fun splitPayload(message: String): Payload {
        val parts = message.split(" ", limit = 3)
        val target = parts.getOrElse(1) { "" }
        val body = parts.getOrElse(2) { "" }
        return Payload(target, body)
    }

    private data class Payload(val target: String, val body: String)

    companion object {
        private val methods: MethodInterface = Universal.get().methods
    }
}
