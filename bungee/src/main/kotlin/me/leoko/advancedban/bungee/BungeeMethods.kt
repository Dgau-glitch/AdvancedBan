package me.leoko.advancedban.bungee

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.imaginarycode.minecraft.redisbungee.RedisBungee
import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import me.leoko.advancedban.bungee.event.PunishmentEvent
import me.leoko.advancedban.bungee.event.RevokePunishmentEvent
import me.leoko.advancedban.bungee.listener.CommandReceiverBungee
import me.leoko.advancedban.bungee.utils.CloudNetCloudPermsOfflineUser
import me.leoko.advancedban.bungee.utils.LuckPermsOfflineUser
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import org.bstats.bungeecord.Metrics
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.TimeUnit

class BungeeMethods : MethodInterface {
    private val configFile = File(dataFolderRef, "config.yml")
    private val messageFile = File(dataFolderRef, "Messages.yml")
    private val layoutFile = File(dataFolderRef, "Layouts.yml")
    private val mysqlFile = File(dataFolderRef, "MySQL.yml")

    private lateinit var config: Configuration
    private lateinit var messages: Configuration
    private lateinit var layouts: Configuration
    private lateinit var mysql: Configuration

    private val permissionableGenerator: ((String) -> Permissionable)? = when {
        ProxyServer.getInstance().pluginManager.getPlugin("LuckPerms") != null -> { name: String -> LuckPermsOfflineUser(name) }
        ProxyServer.getInstance().pluginManager.getPlugin("CloudNet-CloudPerms") != null -> { name: String -> CloudNetCloudPermsOfflineUser(name) }
        else -> null
    }

    init {
        when {
            ProxyServer.getInstance().pluginManager.getPlugin("LuckPerms") != null -> log("[AdvancedBan] Offline permission support through LuckPerms active")
            ProxyServer.getInstance().pluginManager.getPlugin("CloudNet-CloudPerms") != null -> log("[AdvancedBan] Offline permission support through CloudNet-CloudPerms active")
            else -> log("[AdvancedBan] No offline permission support through LuckPerms or CloudNet-CloudPerms")
        }
    }

    override fun loadFiles() {
        if (!dataFolderRef.exists()) dataFolderRef.mkdirs()
        if (!configFile.exists()) Files.copy(pluginRef.getResourceAsStream("config.yml"), configFile.toPath())
        if (!messageFile.exists()) Files.copy(pluginRef.getResourceAsStream("Messages.yml"), messageFile.toPath())
        if (!layoutFile.exists()) Files.copy(pluginRef.getResourceAsStream("Layouts.yml"), layoutFile.toPath())

        config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(configFile)
        messages = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(messageFile)
        layouts = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(layoutFile)
        mysql = if (mysqlFile.exists()) {
            ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(mysqlFile)
        } else {
            ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(configFile)
        }
    }

    override fun getFromUrlJson(url: String, key: String): String? = try {
        val request = URL(url).openConnection() as HttpURLConnection
        request.connect()
        var json = JsonParser().parse(InputStreamReader(request.inputStream)).asJsonObject
        val keys = key.split("\\|")
        for (i in 0 until keys.size - 1) {
            json = json.getAsJsonObject(keys[i])
        }
        json[keys.last()].asString
    } catch (_: Exception) {
        null
    }

    override fun getVersion(): String = pluginRef.description.version
    override fun getKeys(file: Any, path: String): Array<String> = (file as Configuration).getSection(path).keys.toTypedArray()
    override fun getConfig(): Configuration = config
    override fun getMessages(): Configuration = messages
    override fun getLayouts(): Configuration = layouts

    override fun setupMetrics() {
        val metrics = Metrics(pluginRef)
        metrics.addCustomChart(Metrics.SimplePie("MySQL") { if (DatabaseManager.get().isUseMySQL) "yes" else "no" })
    }

    override fun isBungee(): Boolean = true
    override fun clearFormatting(text: String): String? = ChatColor.stripColor(text)
    override fun getPlugin(): Plugin = pluginRef
    override fun getDataFolder(): File = dataFolderRef
    override fun setCommandExecutor(cmd: String, permission: String?, tabCompleter: TabCompleter?) {
        ProxyServer.getInstance().pluginManager.registerCommand(pluginRef, CommandReceiverBungee(cmd, permission))
    }

    override fun sendMessage(player: Any, msg: String) { (player as CommandSender).sendMessage(msg) }
    override fun hasPerms(player: Any, perms: String): Boolean = (player as CommandSender).hasPermission(perms)
    override fun getOfflinePermissionPlayer(name: String): Permissionable = permissionableGenerator?.invoke(name) ?: Permissionable { false }

    override fun isOnline(name: String): Boolean = try {
        if (Universal.isRedis()) {
            RedisBungee.getApi().humanPlayersOnline.forEach { str ->
                if (str.equals(name, true)) {
                    return RedisBungee.getApi().getPlayerIp(RedisBungee.getApi().getUuidFromName(str)) != null
                }
            }
        }
        getPlayer(name)?.address != null
    } catch (_: NullPointerException) {
        false
    }

    override fun getPlayer(name: String): ProxiedPlayer? = ProxyServer.getInstance().getPlayer(name)

    override fun kickPlayer(player: String, reason: String) {
        when {
            BungeeMain.getCloudSupport() != null -> {
                val target = getPlayer(player)
                if (target != null) {
                    BungeeMain.getCloudSupport()!!.kick(target.uniqueId, reason)
                }
            }
            Universal.isRedis() -> RedisBungee.getApi().sendChannelMessage("advancedban:main", "kick $player $reason")
            else -> getPlayer(player)?.disconnect(*TextComponent.fromLegacyText(reason))
        }
    }

    override fun getOnlinePlayers(): Array<ProxiedPlayer> = ProxyServer.getInstance().players.toTypedArray()
    override fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long) { ProxyServer.getInstance().scheduler.schedule(pluginRef, rn, l1 * 50, l2 * 50, TimeUnit.MILLISECONDS) }
    override fun scheduleAsync(rn: Runnable, l1: Long) { ProxyServer.getInstance().scheduler.schedule(pluginRef, rn, l1 * 50, TimeUnit.MILLISECONDS) }
    override fun runAsync(rn: Runnable) { ProxyServer.getInstance().scheduler.runAsync(pluginRef, rn) }
    override fun runSync(rn: Runnable) { rn.run() }
    override fun executeCommand(cmd: String) { ProxyServer.getInstance().pluginManager.dispatchCommand(ProxyServer.getInstance().console, cmd) }
    override fun getName(player: Any): String = (player as CommandSender).name
    override fun getName(uuid: String): String? = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid))?.name
    override fun getIP(player: Any): String = (player as ProxiedPlayer).address.hostName
    override fun getInternUUID(player: Any): String = if (player is ProxiedPlayer) player.uniqueId.toString().replace("-", "") else "none"
    override fun getInternUUID(player: String): String? = getPlayer(player)?.uniqueId?.toString()?.replace("-", "")

    private fun getActiveMute(player: Any): Punishment? {
        val uuid = UUIDManager.get().getUUID(getName(player)) ?: return null
        return PunishmentManager.get().getMute(uuid)
    }

    private fun sendPunishmentLayout(player: Any, punishment: Punishment) {
        punishment.getLayout().forEach { sendMessage(player, it) }
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

    override fun getMySQLFile(): Any = mysql

    override fun parseJSON(json: InputStreamReader, key: String): String? {
        val element = JsonParser().parse(json)
        if (element is JsonNull) return null
        val obj = (element as JsonObject).get(key)
        return obj?.asString
    }

    override fun parseJSON(json: String, key: String): String? {
        val element = JsonParser().parse(json)
        if (element is JsonNull) return null
        val obj = (element as JsonObject).get(key)
        return obj?.asString
    }

    override fun getBoolean(file: Any, path: String): Boolean = (file as Configuration).getBoolean(path)
    override fun getString(file: Any, path: String): String? = (file as Configuration).getString(path)
    override fun getLong(file: Any, path: String): Long = (file as Configuration).getLong(path)
    override fun getInteger(file: Any, path: String): Int = (file as Configuration).getInt(path)
    override fun getStringList(file: Any, path: String): List<String> = (file as Configuration).getStringList(path)
    override fun getBoolean(file: Any, path: String, def: Boolean): Boolean = (file as Configuration).getBoolean(path, def)
    override fun getString(file: Any, path: String, def: String): String? = (file as Configuration).getString(path, def)
    override fun getLong(file: Any, path: String, def: Long): Long = (file as Configuration).getLong(path, def)
    override fun getInteger(file: Any, path: String, def: Int): Int = (file as Configuration).getInt(path, def)
    override fun contains(file: Any, path: String): Boolean = (file as Configuration).get(path) != null
    override fun getFileName(file: Any): String = "[Only available on Bukkit-Version!]"
    override fun callPunishmentEvent(punishment: Punishment) { pluginRef.proxy.pluginManager.callEvent(PunishmentEvent(punishment)) }
    override fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean) { pluginRef.proxy.pluginManager.callEvent(RevokePunishmentEvent(punishment, massClear)) }
    override fun isOnlineMode(): Boolean = ProxyServer.getInstance().config.isOnlineMode

    override fun notify(perm: String, notification: List<String>) {
        if (Universal.isRedis()) {
            notification.forEach { RedisBungee.getApi().sendChannelMessage("advancedban:main", "notification $perm $it") }
            return
        }
        ProxyServer.getInstance().players.filter { Universal.get().hasPerms(it, perm) }.forEach { pp -> notification.forEach { sendMessage(pp, it) } }
    }

    override fun log(msg: String) { ProxyServer.getInstance().console.sendMessage(*TextComponent.fromLegacyText(msg.replace("&", "§"))) }
    override fun isUnitTesting(): Boolean = false

    private val pluginRef: Plugin get() = BungeeMain.get()
    private val dataFolderRef: File get() = pluginRef.dataFolder
}
