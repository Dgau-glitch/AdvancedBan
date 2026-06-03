package me.leoko.advancedban.bukkit.utils

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

object FoliaSchedulers {
    private const val TICK_MILLIS = 50L

    private fun asyncScheduler(): AsyncScheduler = Bukkit.getAsyncScheduler()
    private fun globalScheduler(): GlobalRegionScheduler = Bukkit.getGlobalRegionScheduler()
    private fun regionScheduler(): RegionScheduler = Bukkit.getRegionScheduler()
    private fun entityScheduler(player: Player): EntityScheduler = player.scheduler
    private fun ticksToMillis(ticks: Long): Long = ticks * TICK_MILLIS

    fun runAsync(plugin: JavaPlugin, task: () -> Unit): ScheduledTask =
        asyncScheduler().runNow(plugin) { task() }

    fun runAsyncDelayed(plugin: JavaPlugin, delayTicks: Long, task: () -> Unit): ScheduledTask =
        asyncScheduler().runDelayed(plugin, { task() }, ticksToMillis(delayTicks), TimeUnit.MILLISECONDS)

    fun runAsyncLater(plugin: JavaPlugin, delayTicks: Long, task: () -> Unit): ScheduledTask =
        runAsyncDelayed(plugin, delayTicks, task)

    fun runAsyncRepeating(plugin: JavaPlugin, initialDelayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask =
        asyncScheduler().runAtFixedRate(plugin, { task() }, ticksToMillis(initialDelayTicks), ticksToMillis(periodTicks), TimeUnit.MILLISECONDS)

    fun runGlobal(plugin: JavaPlugin, task: () -> Unit): ScheduledTask =
        globalScheduler().run(plugin) { task() }

    fun runGlobalDelayed(plugin: JavaPlugin, delayTicks: Long, task: () -> Unit): ScheduledTask =
        globalScheduler().runDelayed(plugin, { task() }, delayTicks)

    fun runGlobalRepeating(plugin: JavaPlugin, initialDelayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask =
        globalScheduler().runAtFixedRate(plugin, { task() }, initialDelayTicks, periodTicks)

    fun runPlayer(player: Player, plugin: JavaPlugin, task: () -> Unit): ScheduledTask? =
        entityScheduler(player).run(plugin, { task() }, null)

    fun runPlayerDelayed(player: Player, plugin: JavaPlugin, delayTicks: Long, task: () -> Unit): ScheduledTask? =
        entityScheduler(player).runDelayed(plugin, { task() }, null, delayTicks)

    fun runPlayerRepeating(player: Player, plugin: JavaPlugin, initialDelayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask? =
        entityScheduler(player).runAtFixedRate(plugin, { task() }, null, initialDelayTicks, periodTicks)

    fun runRegion(plugin: JavaPlugin, location: Location, task: () -> Unit): ScheduledTask =
        regionScheduler().run(plugin, location) { task() }

    fun runRegion(plugin: JavaPlugin, world: World, chunkX: Int, chunkZ: Int, task: () -> Unit): ScheduledTask =
        regionScheduler().run(plugin, world, chunkX, chunkZ) { task() }

    fun runRegionDelayed(plugin: JavaPlugin, location: Location, delayTicks: Long, task: () -> Unit): ScheduledTask =
        regionScheduler().runDelayed(plugin, location, { task() }, delayTicks)

    fun runRegionDelayed(plugin: JavaPlugin, world: World, chunkX: Int, chunkZ: Int, delayTicks: Long, task: () -> Unit): ScheduledTask =
        regionScheduler().runDelayed(plugin, world, chunkX, chunkZ, { task() }, delayTicks)

    fun runRegionRepeating(plugin: JavaPlugin, location: Location, initialDelayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask =
        regionScheduler().runAtFixedRate(plugin, location, { task() }, initialDelayTicks, periodTicks)

    fun runRegionRepeating(plugin: JavaPlugin, world: World, chunkX: Int, chunkZ: Int, initialDelayTicks: Long, periodTicks: Long, task: () -> Unit): ScheduledTask =
        regionScheduler().runAtFixedRate(plugin, world, chunkX, chunkZ, { task() }, initialDelayTicks, periodTicks)
}
