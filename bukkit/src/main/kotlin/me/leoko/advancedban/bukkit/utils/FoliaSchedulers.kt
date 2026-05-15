package me.leoko.advancedban.bukkit.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

object FoliaSchedulers {
    fun runAsync(plugin: JavaPlugin, task: () -> Unit) {
        Bukkit.getAsyncScheduler().runNow(plugin) { task() }
    }

    fun runAsyncLater(plugin: JavaPlugin, delayTicks: Long, task: () -> Unit) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, { task() }, delayTicks * 50, TimeUnit.MILLISECONDS)
    }

    fun runGlobal(plugin: JavaPlugin, task: () -> Unit) {
        Bukkit.getGlobalRegionScheduler().run(plugin) { task() }
    }

    fun runPlayer(player: Player, plugin: JavaPlugin, task: () -> Unit) {
        player.scheduler.run(plugin, { task() }, null)
    }
}
