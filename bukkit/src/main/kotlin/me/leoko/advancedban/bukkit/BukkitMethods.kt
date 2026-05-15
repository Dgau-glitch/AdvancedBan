package me.leoko.advancedban.bukkit

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.bukkit.listener.CommandReceiver
import me.leoko.advancedban.bukkit.utils.FoliaSchedulers
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.UUID
import java.util.function.BiFunction

class BukkitMethods : MethodInterface {
    private val legacySerializer: LegacyComponentSerializer = LegacyComponentSerializer.legacySection()
    private val plainSerializer: PlainTextComponentSerializer = PlainTextComponentSerializer.plainText()
    private val messageFile = File(dataFolder, "Messages.yml")
    private val layoutFile = File(dataFolder, "Layouts.yml")
    private val mysqlFile = File(dataFolder, "MySQL.yml")
    private var configFile = File(dataFolder, "config.yml")

    private lateinit var config: YamlConfiguration
    private lateinit var messages: YamlConfiguration
    private lateinit var layouts: YamlConfiguration
    private lateinit var mysql: YamlConfiguration

    private var permissionVault: BiFunction<OfflinePlayer, String, Boolean>? = null

    init {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            val rsp = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission::class.java)
            if (rsp != null) permissionVault = BiFunction { player, perms -> rsp.provider.playerHas(null, player, perms) }
        }
    }

    override fun loadFiles() {
        if (!configFile.exists()) pluginRef.saveResource("config.yml", true)
        if (!messageFile.exists()) pluginRef.saveResource("Messages.yml", true)
        if (!layoutFile.exists()) pluginRef.saveResource("Layouts.yml", true)

        config = YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(configFile), StandardCharsets.UTF_8))
        messages = YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(messageFile), StandardCharsets.UTF_8))
        layouts = YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(layoutFile), StandardCharsets.UTF_8))
        mysql = if (mysqlFile.exists()) {
            YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(mysqlFile), StandardCharsets.UTF_8))
        } else {
            YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(configFile), StandardCharsets.UTF_8))
        }
    }

    override fun getFromUrlJson(url: String, key: String): String? = try {
        val request = URI.create(url).toURL().openConnection() as HttpURLConnection
        request.connect()
        val jp = JSONParser()
        var json = jp.parse(InputStreamReader(request.inputStream)) as JSONObject
        val keys = key.split("\\|")
        for (i in 0 until keys.size - 1) {
            json = json[keys[i]] as JSONObject
        }
        json[keys[keys.size - 1]].toString()
    } catch (_: Exception) {
        null
    }

    override fun getVersion(): String = pluginRef.pluginMeta.version
    override fun getKeys(file: Any, path: String): Array<String> = (file as YamlConfiguration).getConfigurationSection(path)!!.getKeys(false).toTypedArray()
    override fun getConfig(): YamlConfiguration = config
    override fun getMessages(): YamlConfiguration = messages
    override fun getLayouts(): YamlConfiguration = layouts

    override fun setupMetrics() {
        val metrics = Metrics(pluginRef, 4732)
        metrics.addCustomChart(SimplePie("MySQL") { if (DatabaseManager.get().isUseMySQL) "yes" else "no" })
    }

    override fun isBungee(): Boolean = false
    override fun clearFormatting(text: String): String = plainSerializer.serialize(legacySerializer.deserialize(text))
    override fun getPlugin(): JavaPlugin = pluginRef
    override fun getDataFolder(): File = dataFolderRef

    override fun setCommandExecutor(cmd: String, permission: String?, tabCompleter: TabCompleter?) {
        val friendly = getBoolean(config, "Friendly Register Commands", false)
        val command: PluginCommand? = if (friendly) pluginRef.getCommand(cmd) else Bukkit.getPluginCommand(cmd)
        if (command != null) {
            command.setExecutor(CommandReceiver.get())
            if (tabCompleter != null) {
                command.tabCompleter = org.bukkit.command.TabCompleter { sender, _, _, args ->
                    if (permission != null && !hasPerms(sender, permission)) return@TabCompleter Collections.emptyList()
                    tabCompleter.onTabComplete(sender, args)
                }
            }
        } else {
            println("AdvancedBan >> Failed to register command $cmd")
        }
    }

    override fun sendMessage(player: Any, msg: String) { (player as CommandSender).sendMessage(msg) }
    override fun hasPerms(player: Any, perms: String): Boolean = (player as CommandSender).hasPermission(perms)

    override fun getOfflinePermissionPlayer(name: String): Permissionable {
        val player = Bukkit.getOfflinePlayer(name)
        val vault = permissionVault
        if (vault == null || !player.hasPlayedBefore()) return Permissionable { false }
        return Permissionable { permission -> vault.apply(player, permission) }
    }

    override fun isOnline(name: String): Boolean = Bukkit.getOfflinePlayer(name).isOnline
    override fun getPlayer(name: String): Player? = Bukkit.getPlayer(name)
    override fun kickPlayer(player: String, reason: String) { getPlayer(player)?.takeIf { it.isOnline }?.kick(Component.text(reason)) }
    override fun getOnlinePlayers(): Array<Player> = Bukkit.getOnlinePlayers().toTypedArray()
    override fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long) {
        Bukkit.getAsyncScheduler().runAtFixedRate(pluginRef, { rn.run() }, l1 * 50, l2 * 50, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
    override fun scheduleAsync(rn: Runnable, l1: Long) {
        FoliaSchedulers.runAsyncLater(pluginRef, l1) { rn.run() }
    }
    override fun runAsync(rn: Runnable) {
        FoliaSchedulers.runAsync(pluginRef) { rn.run() }
    }
    override fun runSync(rn: Runnable) {
        FoliaSchedulers.runGlobal(pluginRef) { rn.run() }
    }
    override fun executeCommand(cmd: String) {
        FoliaSchedulers.runGlobal(pluginRef) { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd) }
    }
    override fun getName(player: Any): String = (player as CommandSender).name
    override fun getName(uuid: String): String? = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name
    override fun getIP(player: Any): String = (player as Player).address.hostName
    override fun getInternUUID(player: Any): String = if (player is OfflinePlayer) player.uniqueId.toString().replace("-", "") else "none"
    override fun getInternUUID(player: String): String = Bukkit.getOfflinePlayer(player).uniqueId.toString().replace("-", "")


    private fun sendPunishmentLayout(target: Any, punishment: Punishment) {
        if (target is Player) {
            FoliaSchedulers.runPlayer(target, pluginRef) {
                punishment.layout.forEach { sendMessage(target, it) }
            }
            return
        }
        punishment.layout.forEach { sendMessage(target, it) }
    }

    override fun callChat(player: Any): Boolean {
        val pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))
        if (pnt != null) {
            sendPunishmentLayout(player, pnt)
            return true
        }
        return false
    }

    override fun callCMD(player: Any, cmd: String): Boolean {
        val pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))
        if (Universal.get().isMuteCommand(cmd.substring(1)) && pnt != null) {
            sendPunishmentLayout(player, pnt)
            return true
        }
        return false
    }

    override fun getMySQLFile(): YamlConfiguration = mysql
    override fun parseJSON(json: InputStreamReader, key: String): String? = try { ((JSONParser().parse(json) as JSONObject)[key]).toString() } catch (_: Exception) { null }
    override fun parseJSON(json: String, key: String): String? = try { ((JSONParser().parse(json) as JSONObject)[key]).toString() } catch (_: Exception) { null }
    override fun getBoolean(file: Any, path: String): Boolean = (file as YamlConfiguration).getBoolean(path)
    override fun getString(file: Any, path: String): String? = (file as YamlConfiguration).getString(path)
    override fun getLong(file: Any, path: String): Long = (file as YamlConfiguration).getLong(path)
    override fun getInteger(file: Any, path: String): Int = (file as YamlConfiguration).getInt(path)
    override fun getStringList(file: Any, path: String): List<String> = (file as YamlConfiguration).getStringList(path)
    override fun getBoolean(file: Any, path: String, def: Boolean): Boolean = (file as YamlConfiguration).getBoolean(path, def)
    override fun getString(file: Any, path: String, def: String): String? = (file as YamlConfiguration).getString(path, def)
    override fun getLong(file: Any, path: String, def: Long): Long = (file as YamlConfiguration).getLong(path, def)
    override fun getInteger(file: Any, path: String, def: Int): Int = (file as YamlConfiguration).getInt(path, def)
    override fun contains(file: Any, path: String): Boolean = (file as YamlConfiguration).contains(path)
    override fun getFileName(file: Any): String = (file as YamlConfiguration).name
    override fun callPunishmentEvent(punishment: Punishment) { runSync { Bukkit.getPluginManager().callEvent(PunishmentEvent(punishment)) } }
    override fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean) { runSync { Bukkit.getPluginManager().callEvent(RevokePunishmentEvent(punishment, massClear)) } }
    override fun isOnlineMode(): Boolean = Bukkit.getOnlineMode()

    override fun notify(perm: String, notification: List<String>) {
        Bukkit.getOnlinePlayers().filter { hasPerms(it, perm) }.forEach { player ->
            FoliaSchedulers.runPlayer(player, pluginRef) {
                notification.forEach { sendMessage(player, it) }
            }
        }
    }

    override fun log(msg: String) { Bukkit.getConsoleSender().sendMessage(msg.replace("&", "§")) }
    override fun isUnitTesting(): Boolean = false

    private val pluginRef: JavaPlugin get() = BukkitMain.get()
    private val dataFolderRef: File get() = pluginRef.dataFolder
}
