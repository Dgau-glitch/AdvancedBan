package me.leoko.advancedban.bungee.cloud.support

import de.dytanic.cloudnet.driver.CloudNetDriver
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager
import me.leoko.advancedban.bungee.cloud.CloudSupport
import java.util.UUID

class CloudNetV3Support : CloudSupport {
    override fun kick(uniqueID: UUID, reason: String) {
        CloudNetDriver.getInstance().servicesRegistry.getFirstService(IPlayerManager::class.java)
            .getPlayerExecutor(uniqueID)
            .kick(reason)
    }
}
