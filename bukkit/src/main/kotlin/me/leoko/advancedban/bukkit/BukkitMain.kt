package me.leoko.advancedban.bukkit

import me.leoko.advancedban.Universal
import me.leoko.advancedban.bukkit.listener.ChatListener
import me.leoko.advancedban.bukkit.listener.CommandListener
import me.leoko.advancedban.bukkit.listener.ConnectionListener
import me.leoko.advancedban.bukkit.listener.InternalListener
import me.leoko.advancedban.bukkit.utils.FoliaSchedulers
import me.leoko.advancedban.bukkit.utils.OnlinePlayerNameCache
import me.leoko.advancedban.bukkit.utils.TextComponents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class BukkitMain : JavaPlugin() {

    override fun onEnable() {
        instance = this
        Universal.get().setup(BukkitMethods())

        val connectionListener = ConnectionListener()
        server.pluginManager.registerEvents(connectionListener, this)
        server.pluginManager.registerEvents(ChatListener(), this)
        server.pluginManager.registerEvents(CommandListener(), this)
        server.pluginManager.registerEvents(InternalListener(), this)

        val onlinePlayers = Bukkit.getOnlinePlayers().toTypedArray()
        OnlinePlayerNameCache.replaceOnline(onlinePlayers.map { it.name })
        refreshKnownPlayerNameCache()

        // Snapshot only; every player mutation below is routed back to the player/entity scheduler.
        onlinePlayers.forEach { player ->
            Universal.get().callConnection(player.name, player.address?.address?.hostAddress ?: "")?.let { reason ->
                FoliaSchedulers.runPlayer(player, this) { player.kick(TextComponents.legacy(reason)) }
            }
        }
    }


    private fun refreshKnownPlayerNameCache() {
        FoliaSchedulers.runGlobal(this) {
            OnlinePlayerNameCache.replaceKnown(Bukkit.getOfflinePlayers().mapNotNull { it.name })
        }
    }

    override fun onDisable() {
        Universal.get().shutdown()
    }

    companion object {
        private lateinit var instance: BukkitMain

        @JvmStatic
        fun get(): BukkitMain = instance
    }
}
