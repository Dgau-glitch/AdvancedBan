package me.leoko.advancedban.bukkit.utils

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

object FoliaSchedulers {
    private fun asyncScheduler(): AsyncScheduler = Bukkit.getAsyncScheduler()
    private fun globalScheduler(): GlobalRegionScheduler = Bukkit.getGlobalRegionScheduler()
    private fun entityScheduler(player: Player): EntityScheduler = player.scheduler

    fun runAsync(plugin: JavaPlugin, task: () -> Unit) {
        asyncScheduler().runNow(plugin) { task() }
    }

    fun runAsyncLater(plugin: JavaPlugin, delayTicks: Long, task: () -> Unit) {
        asyncScheduler().runDelayed(plugin, { task() }, delayTicks * 50, TimeUnit.MILLISECONDS)
    }

    fun runGlobal(plugin: JavaPlugin, task: () -> Unit) {
        globalScheduler().run(plugin) { task() }
    }

    fun runPlayer(player: Player, plugin: JavaPlugin, task: () -> Unit) {
        entityScheduler(player).run(plugin, { task() }, null)
    }
}
