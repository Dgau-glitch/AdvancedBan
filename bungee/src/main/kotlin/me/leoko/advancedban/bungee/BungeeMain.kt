package me.leoko.advancedban.bungee

import com.imaginarycode.minecraft.redisbungee.RedisBungee
import me.leoko.advancedban.Universal
import me.leoko.advancedban.bungee.cloud.CloudSupport
import me.leoko.advancedban.bungee.cloud.CloudSupportHandler
import me.leoko.advancedban.bungee.listener.ChatListenerBungee
import me.leoko.advancedban.bungee.listener.ConnectionListenerBungee
import me.leoko.advancedban.bungee.listener.InternalListener
import me.leoko.advancedban.bungee.listener.PubSubMessageListener
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class BungeeMain : Plugin() {
    override fun onEnable() {
        instance = this
        Universal.get().setup(BungeeMethods())
        ProxyServer.getInstance().pluginManager.registerListener(this, ConnectionListenerBungee())
        ProxyServer.getInstance().pluginManager.registerListener(this, ChatListenerBungee())
        ProxyServer.getInstance().pluginManager.registerListener(this, InternalListener())
        ProxyServer.getInstance().registerChannel("advancedban:main")

        cloudSupport = CloudSupportHandler.getCloudSystem()

        if (ProxyServer.getInstance().pluginManager.getPlugin("RedisBungee") != null) {
            Universal.setRedis(true)
            ProxyServer.getInstance().pluginManager.registerListener(this, PubSubMessageListener())
            RedisBungee.getApi().registerPubSubChannels("advancedban:main", "advancedban:connection")
            Universal.get().log("RedisBungee detected, hooking into it!")
        }
    }

    override fun onDisable() {
        Universal.get().shutdown()
    }

    companion object {
        private lateinit var instance: BungeeMain
        private var cloudSupport: CloudSupport? = null

        @JvmStatic
        fun get(): BungeeMain = instance

        @JvmStatic
        fun getCloudSupport(): CloudSupport? = cloudSupport
    }
}
