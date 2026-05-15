package me.leoko.advancedban.bungee.listener

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import me.leoko.advancedban.Universal
import me.leoko.advancedban.bungee.event.PunishmentEvent
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent
import me.leoko.advancedban.manager.TimeManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.PunishmentType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class InternalListener : Listener {
    private val universal = Universal.get()

    @EventHandler
    fun onPunish(event: PunishmentEvent) {
        sendToBukkit("Punish", listOf(event.punishment.toString()))
    }

    @EventHandler
    fun onUnPunish(event: RevokePunishmentEvent) {
        sendToBukkit("Unpunish", listOf(event.punishment.toString()))
    }

    @EventHandler
    fun onPluginMessageEvent(event: PluginMessageEvent) {
        if (event.tag != "advancedban:main" || event.sender is ProxiedPlayer) return

        val input = ByteStreams.newDataInput(event.data)
        when (val channel = input.readUTF()) {
            "Punish" -> handlePunishMessage(input.readUTF())
            else -> universal.debug("Unknown channel for tag \"AdvancedBan\": $channel")
        }
    }

    fun sendToBukkit(channel: String, messages: List<String>) {
        val output = ByteStreams.newDataOutput().apply {
            writeUTF(channel)
            messages.forEach(::writeUTF)
        }

        ProxyServer.getInstance().servers.values.forEach {
            it.sendData("advancedban:main", output.toByteArray(), true)
        }
    }

    private fun handlePunishMessage(message: String) {
        try {
            val punishment = universal.gson.fromJson(message, JsonObject::class.java)
            Punishment(
                punishment["name"].asString,
                UUIDManager.get().getUUID(punishment["uuid"].asString),
                punishment["reason"].asString,
                punishment["operator"]?.asString ?: "CONSOLE",
                PunishmentType.valueOf(punishment["punishmenttype"].asString.uppercase()),
                punishment["start"]?.asLong ?: TimeManager.getTime(),
                TimeManager.getTime() + punishment["end"].asLong,
                punishment["calculation"]?.asString,
                -1
            ).create(punishment["silent"]?.asBoolean == true)

            universal.log("A punishment was created using PluginMessaging listener.")
            universal.debug(punishment.toString())
        } catch (ex: JsonSyntaxException) {
            logPluginMessageReadFailure(message, ex)
        } catch (ex: NullPointerException) {
            logPluginMessageReadFailure(message, ex)
        }
    }

    private fun logPluginMessageReadFailure(message: String, ex: Exception) {
        universal.log("An exception as occurred while reading a punishment from plugin messaging channel.")
        universal.debug("Message: $message")
        universal.log("StackTrace:")
        ex.printStackTrace()
    }
}
