package me.leoko.advancedban

import com.google.gson.Gson
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.LogManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.manager.UpdateManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.InterimData
import me.leoko.advancedban.utils.Punishment
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Scanner
import java.util.concurrent.ConcurrentHashMap

class Universal {
    val ips: MutableMap<String, String> = ConcurrentHashMap()
    lateinit var methods: MethodInterface
        private set
    private lateinit var logManager: LogManager
    val gson: Gson = Gson()

    fun setup(mi: MethodInterface) {
        methods = mi
        mi.loadFiles()
        logManager = LogManager()
        UpdateManager.get().setup()
        UUIDManager.get().setup()

        try {
            DatabaseManager.get().setup(mi.getBoolean(mi.getConfig(), "UseMySQL", false))
        } catch (ex: Exception) {
            log("Failed enabling database-manager...")
            debugException(ex)
        }

        mi.setupMetrics()
        PunishmentManager.get().setup()

        for (command in Command.entries) {
            for (commandName in command.names) {
                mi.setCommandExecutor(commandName, command.permission, command.tabCompleter)
            }
        }

        var upt = "You have the newest version"
        val response = getFromURL("https://api.spigotmc.org/legacy/update.php?resource=8695")
        if (response == null) {
            upt = "Failed to check for updates :("
        } else if (!mi.getVersion().startsWith(response)) {
            upt = "There is a new version available! [$response]"
        }

        if (mi.getBoolean(mi.getConfig(), "DetailedEnableMessage", true)) {
            mi.log("\n \n&8[]=====[&7Enabling AdvancedBan&8]=====[]&r"
                    + "\n&8| &cInformation:&r"
                    + "\n&8|   &cName: &7AdvancedBan&r"
                    + "\n&8|   &cDeveloper: &7Leoko&r"
                    + "\n&8|   &cVersion: &7" + mi.getVersion() + "&r"
                    + "\n&8|   &cStorage: &7" + (if (DatabaseManager.get().isUseMySQL) "MySQL (external)" else "HSQLDB (local)") + "&r"
                    + "\n&8| &cSupport:&r"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues &r"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS &r"
                    + "\n&8| &cTwitter: &7@LeokoGar&r"
                    + "\n&8| &cUpdate:&r"
                    + "\n&8|   &7" + upt + "&r"
                    + "\n&8[]================================[]&r\n ")
        } else {
            mi.log("&cEnabling AdvancedBan on Version &7&r" + mi.getVersion())
            mi.log("&cCoded by &7Leoko &8| &7Twitter: @LeokoGar&r")
        }
    }

    fun shutdown() {
        DatabaseManager.get().shutdown()

        if (methods.getBoolean(methods.getConfig(), "DetailedDisableMessage", true)) {
            methods.log("\n \n&8[]=====[&7Disabling AdvancedBan&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBan"
                    + "\n&8|   &cDeveloper: &7Leoko"
                    + "\n&8|   &cVersion: &7" + methods.getVersion()
                    + "\n&8|   &cStorage: &7" + (if (DatabaseManager.get().isUseMySQL) "MySQL (external)" else "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cGithub: &7https://github.com/DevLeoko/AdvancedBan/issues"
                    + "\n&8|   &cDiscord: &7https://discord.gg/ycDG6rS"
                    + "\n&8| &cTwitter: &7@LeokoGar"
                    + "\n&8[]================================[]&r\n ")
        } else {
            methods.log("&cDisabling AdvancedBan on Version &7" + methods.getVersion())
            methods.log("&cCoded by Leoko &8| &7Twitter: @LeokoGar")
        }
    }

    val isBungee: Boolean
        get() = methods.isBungee()

    fun getFromURL(surl: String): String? {
        var response: String? = null
        try {
            val url = URL(surl)
            Scanner(url.openStream()).use { s ->
                if (s.hasNext()) {
                    response = s.next()
                }
            }
        } catch (_: IOException) {
            debug("!! Failed to connect to URL: $surl")
        }
        return response
    }

    fun isMuteCommand(cmd: String): Boolean {
        return isMuteCommand(cmd, methods.getStringList(methods.getConfig(), "MuteCommands"))
    }

    fun isMuteCommand(cmd: String, muteCommands: List<String>): Boolean {
        val words = cmd.split(" ").toMutableList()
        if (words[0].contains(':')) words[0] = words[0].split(":", limit = 2)[1]
        for (muteCommand in muteCommands) {
            if (muteCommandMatches(words.toTypedArray(), muteCommand)) return true
        }
        return false
    }

    fun muteCommandMatches(commandWords: Array<String>, muteCommand: String): Boolean {
        if (commandWords[0].equals(muteCommand, true)) return true
        if (muteCommand.contains(' ')) {
            val muteCommandWords = muteCommand.split(" ")
            if (muteCommandWords.size > commandWords.size) return false
            for (n in muteCommandWords.indices) {
                if (!muteCommandWords[n].equals(commandWords[n], true)) return false
            }
            return true
        }
        return false
    }

    fun isExemptPlayer(name: String): Boolean {
        val exempt = methods.getStringList(methods.getConfig(), "ExemptPlayers")
        if (exempt != null) {
            for (str in exempt) if (name.equals(str, true)) return true
        }
        return false
    }

    fun broadcastLeoko(): Boolean {
        val readme = File(methods.getDataFolder(), "readme.txt")
        if (!readme.exists()) return true
        try {
            if (Files.readAllLines(Paths.get(readme.path), Charset.defaultCharset())[0].equals("I don't want that there will be any message when the dev of this plugin joins the server! I want this even though the plugin is 100% free and the join-message is the only reward for the Dev :(", true)) {
                return false
            }
        } catch (_: IOException) {
        }
        return true
    }

    fun callConnection(nameInput: String, ip: String?): String? {
        val name = nameInput.lowercase()
        val uuid = UUIDManager.get().getUUID(name) ?: return "[AdvancedBan] Failed to fetch your UUID"

        if (ip != null) {
            ips.remove(name)
            ips[name] = ip
        }

        val interimData: InterimData = PunishmentManager.get().load(name, uuid, ip)
            ?: return if (methods.getBoolean(methods.getConfig(), "LockdownOnError", true)) "[AdvancedBan] Failed to load player data!" else null

        val pt: Punishment? = interimData.getBan()
        if (pt == null) {
            interimData.accept()
            return null
        }

        return pt.getLayoutBSN()
    }

    fun hasPerms(player: Any, permsInput: String): Boolean {
        var perms = permsInput
        if (methods.hasPerms(player, perms)) return true

        if (methods.getBoolean(methods.getConfig(), "EnableAllPermissionNodes", false)) {
            while (perms.contains('.')) {
                perms = perms.substring(0, perms.lastIndexOf('.'))
                if (methods.hasPerms(player, "$perms.all")) return true
            }
        }
        return false
    }

    fun log(msg: String) {
        methods.log("§8[§cAdvancedBan§8] §7$msg")
        debugToFile(msg)
    }

    fun debug(msg: Any) {
        if (methods.getBoolean(methods.getConfig(), "Debug", false)) {
            methods.log("§8[§cAdvancedBan§8] §cDebug: §7$msg")
        }
        debugToFile(msg)
    }

    fun debugException(exc: Exception) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exc.printStackTrace(pw)
        debug(sw.toString())
    }

    fun debugSqlException(ex: SQLException) {
        if (methods.getBoolean(methods.getConfig(), "Debug", false)) {
            debug("§7An error has occurred with the database, the error code is: '${ex.errorCode}'")
            debug("§7The state of the sql is: ${ex.sqlState}")
            debug("§7Error message: ${ex.message}")
        }
        debugException(ex)
    }

    private fun debugToFile(msg: Any) {
        val debugFile = File(methods.getDataFolder(), "logs/latest.log")
        if (!debugFile.exists()) {
            try {
                debugFile.createNewFile()
            } catch (ex: IOException) {
                print("An error has occurred creating the 'latest.log' file again, check your server.")
                print("Error message" + ex.message)
            }
        } else {
            logManager.checkLastLog(false)
        }
        try {
            FileUtils.writeStringToFile(
                debugFile,
                "[" + SimpleDateFormat("HH:mm:ss").format(Date(System.currentTimeMillis())) + "] " + methods.clearFormatting(msg.toString()) + "\n",
                "UTF8",
                true
            )
        } catch (ex: IOException) {
            print("An error has occurred writing to 'latest.log' file.")
            print(ex.message)
        }
    }

    companion object {
        @Volatile
        private var instance: Universal? = null
        @Volatile
        private var redis = false

        @JvmStatic
        fun setRedis(redis: Boolean) {
            Companion.redis = redis
        }

        @JvmStatic
        fun isRedis(): Boolean = redis

        @JvmStatic
        fun get(): Universal {
            if (instance == null) instance = Universal()
            return instance!!
        }
    }
}
