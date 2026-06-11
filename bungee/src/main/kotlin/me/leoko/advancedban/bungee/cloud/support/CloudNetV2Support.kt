package me.leoko.advancedban.bungee.cloud.support

import de.dytanic.cloudnet.api.player.PlayerExecutorBridge
import de.dytanic.cloudnet.bridge.CloudServer
import me.leoko.advancedban.bungee.cloud.CloudSupport
import java.util.UUID

class CloudNetV2Support : CloudSupport {
    override fun kick(uniqueID: UUID, reason: String) {
        PlayerExecutorBridge.INSTANCE.kickPlayer(CloudServer.getInstance().cloudPlayers[uniqueID], reason)
    }
}
