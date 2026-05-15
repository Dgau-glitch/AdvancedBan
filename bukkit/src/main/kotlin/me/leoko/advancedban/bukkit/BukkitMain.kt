package me.leoko.advancedban.bukkit

import me.leoko.advancedban.Universal
import me.leoko.advancedban.bukkit.listener.ChatListener
import me.leoko.advancedban.bukkit.listener.CommandListener
import me.leoko.advancedban.bukkit.listener.ConnectionListener
import me.leoko.advancedban.bukkit.listener.InternalListener
import net.kyori.adventure.text.Component
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

        Bukkit.getOnlinePlayers().forEach { player ->
            Universal.get().callConnection(player.name, player.address?.address?.hostAddress ?: "")?.let { reason ->
                player.kick(Component.text(reason))
            }
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
