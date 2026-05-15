package me.leoko.advancedban.bungee.cloud

import me.leoko.advancedban.bungee.cloud.support.CloudNetV2Support
import me.leoko.advancedban.bungee.cloud.support.CloudNetV3Support
import net.md_5.bungee.api.ProxyServer

object CloudSupportHandler {
    @JvmStatic
    fun getCloudSystem(): CloudSupport? {
        if (ProxyServer.getInstance().pluginManager.getPlugin("CloudNet-Bridge") != null) {
            return CloudNetV3Support()
        }
        if (ProxyServer.getInstance().pluginManager.getPlugin("CloudNetAPI") != null) {
            return CloudNetV2Support()
        }
        return null
    }
}
