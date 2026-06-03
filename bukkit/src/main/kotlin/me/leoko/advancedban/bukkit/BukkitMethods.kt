package me.leoko.advancedban.bukkit

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.integration.OfflinePermissionHook
import me.leoko.advancedban.bukkit.integration.VaultPermissionHook
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.bukkit.listener.CommandReceiver
import me.leoko.advancedban.bukkit.utils.FoliaSchedulers
import me.leoko.advancedban.bukkit.utils.OnlinePlayerNameCache
import me.leoko.advancedban.bukkit.utils.TextComponents
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.NetworkUtils
import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.UUID

class BukkitMethods : MethodInterface {
    private data class CachedOnlinePlayerName(val name: String)

    private val messageFile = File(dataFolderRef, "Messages.yml")
    private val layoutFile = File(dataFolderRef, "Layouts.yml")
    private val mysqlFile = File(dataFolderRef, "MySQL.yml")
    private var configFile = File(dataFolderRef, "config.yml")

    private lateinit var config: YamlConfiguration
    private lateinit var messages: YamlConfiguration
    private lateinit var layouts: YamlConfiguration
    private lateinit var mysql: YamlConfiguration

    private val offlinePermissionHook: OfflinePermissionHook = VaultPermissionHook.create()

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
        val request = NetworkUtils.openHttpConnection(url)
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

    override fun isBungee(): Boolean = false
    override fun clearFormatting(text: String): String = TextComponents.stripLegacy(text)
    override fun getPlugin(): JavaPlugin = pluginRef
    override fun getDataFolder(): File = dataFolderRef

    override fun setCommandExecutor(cmd: String, permission: String?, tabCompleter: TabCompleter?) {
        val friendly = getBoolean(config, "Friendly Register Commands", false)
        val command: PluginCommand? = if (friendly) pluginRef.getCommand(cmd) else Bukkit.getPluginCommand(cmd)
        if (command != null) {
            command.setExecutor(CommandReceiver.get())
            command.tabCompleter = org.bukkit.command.TabCompleter { sender, _, _, args ->
                if (permission != null && !hasPerms(sender, permission)) return@TabCompleter Collections.emptyList()
                tabCompleter?.onTabComplete(sender, args) ?: Collections.emptyList()
            }
        } else {
            println("AdvancedBan >> Failed to register command $cmd")
        }
    }

    private fun deserializeMessage(msg: String) = TextComponents.legacy(msg)

    private fun sendMessageNow(sender: CommandSender, msg: String) {
        sender.sendMessage(deserializeMessage(msg))
    }

    private fun deliverMessages(sender: CommandSender, messages: Iterable<String>) {
        if (sender is Player) {
            FoliaSchedulers.runPlayer(sender, pluginRef) {
                messages.forEach { sendMessageNow(sender, it) }
            }
        } else {
            FoliaSchedulers.runGlobal(pluginRef) {
                messages.forEach { sendMessageNow(sender, it) }
            }
        }
    }

    private fun onlinePlayersSnapshot(): Array<Player> = Bukkit.getOnlinePlayers().toTypedArray()

    override fun sendMessage(player: Any, msg: String) {
        deliverMessages(player as CommandSender, listOf(msg))
    }
    override fun hasPerms(player: Any, perms: String): Boolean = (player as CommandSender).hasPermission(perms)

    override fun getOfflinePermissionPlayer(name: String): Permissionable {
        val player = Bukkit.getOfflinePlayer(name)
        if (!player.hasPlayedBefore()) return Permissionable { false }
        return Permissionable { permission -> offlinePermissionHook.hasPermission(player, permission) }
    }

    override fun isOnline(name: String): Boolean = Bukkit.getOfflinePlayer(name).isOnline
    override fun getPlayer(name: String): Player? = Bukkit.getPlayer(name)
    override fun kickPlayer(player: String, reason: String) {
        FoliaSchedulers.runGlobal(pluginRef) {
            getPlayer(player)?.let { target ->
                FoliaSchedulers.runPlayer(target, pluginRef) {
                    if (target.isOnline) target.kick(TextComponents.legacy(reason))
                }
            }
        }
    }
    override fun getOnlinePlayers(): Array<Any> = OnlinePlayerNameCache.snapshot().map(::CachedOnlinePlayerName).toTypedArray()
    override fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long) {
        FoliaSchedulers.runAsyncRepeating(pluginRef, l1, l2) { rn.run() }
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
    override fun getName(player: Any): String = when (player) {
        is CachedOnlinePlayerName -> player.name
        is CommandSender -> player.name
        else -> player.toString()
    }
    override fun getName(uuid: String): String? = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name
    override fun getIP(player: Any): String = (player as Player).address.hostName
    override fun getInternUUID(player: Any): String = if (player is OfflinePlayer) player.uniqueId.toString().replace("-", "") else "none"
    override fun getInternUUID(player: String): String = Bukkit.getOfflinePlayer(player).uniqueId.toString().replace("-", "")


    private fun sendPunishmentLayout(target: Any, punishment: Punishment) {
        deliverMessages(target as CommandSender, punishment.getLayout())
    }

    private fun getActiveMute(player: Any): Punishment? {
        val uuid = UUIDManager.get().getUUID(getName(player)) ?: return null
        return PunishmentManager.get().getMute(uuid)
    }

    override fun callChat(player: Any): Boolean {
        val punishment = getActiveMute(player) ?: return false
        sendPunishmentLayout(player, punishment)
        return true
    }

    override fun callCMD(player: Any, cmd: String): Boolean {
        if (!Universal.get().isMuteCommand(cmd.substring(1))) return false
        val punishment = getActiveMute(player) ?: return false
        sendPunishmentLayout(player, punishment)
        return true
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
    override fun getFileName(file: Any): String = when (file) {
        config -> "config.yml"
        messages -> "Messages.yml"
        layouts -> "Layouts.yml"
        mysql -> if (mysqlFile.exists()) "MySQL.yml" else "config.yml"
        is YamlConfiguration -> file.name.takeIf { it.isNotBlank() } ?: "unknown.yml"
        else -> "unknown.yml"
    }
    private fun callPluginEvent(event: org.bukkit.event.Event) {
        FoliaSchedulers.runGlobal(pluginRef) { Bukkit.getPluginManager().callEvent(event) }
    }

    override fun callPunishmentEvent(punishment: Punishment) {
        callPluginEvent(PunishmentEvent(punishment))
    }

    override fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean) {
        callPluginEvent(RevokePunishmentEvent(punishment, massClear))
    }
    override fun isOnlineMode(): Boolean = Bukkit.getOnlineMode()

    override fun notify(perm: String, notification: List<String>) {
        FoliaSchedulers.runGlobal(pluginRef) {
            onlinePlayersSnapshot().forEach { player ->
                FoliaSchedulers.runPlayer(player, pluginRef) {
                    if (hasPerms(player, perm)) {
                        notification.forEach { sendMessageNow(player, it) }
                    }
                }
            }
        }
    }

    override fun log(msg: String) {
        FoliaSchedulers.runGlobal(pluginRef) {
            Bukkit.getConsoleSender().sendMessage(TextComponents.legacy(msg.replace("&", "§")))
        }
    }
    override fun isUnitTesting(): Boolean = false

    private val pluginRef: JavaPlugin get() = BukkitMain.get()
    private val dataFolderRef: File get() = pluginRef.dataFolder
}
