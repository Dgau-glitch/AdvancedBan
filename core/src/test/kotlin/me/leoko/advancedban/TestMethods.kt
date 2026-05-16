package me.leoko.advancedban

import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import org.junit.jupiter.api.Assertions.fail
import java.io.File
import java.io.InputStreamReader

class TestMethods(private val dataFolder: File) : MethodInterface {
    override fun loadFiles() {}
    override fun getFromUrlJson(url: String, key: String): String? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #1")
    override fun getVersion(): String = "TEST"
    override fun getKeys(file: Any, path: String): Array<String> = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #2")
    override fun getConfig(): Any = Any()
    override fun getMessages(): Any = Any()
    override fun getLayouts(): Any = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #5")
    override fun setupMetrics() {}
    override fun isBungee(): Boolean = false
    override fun clearFormatting(text: String): String = text
    override fun getPlugin(): Any = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #6")
    override fun getDataFolder(): File = dataFolder
    override fun setCommandExecutor(cmd: String, permission: String?, tabCompleter: TabCompleter?) {}
    override fun sendMessage(player: Any, msg: String) = println("Message: $player -> $msg")
    override fun getName(player: Any): String = player.toString()
    override fun getName(uuid: String): String = uuid
    override fun getIP(player: Any): String = "127.0.0.1"
    override fun getInternUUID(player: Any): String = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #7")
    override fun getInternUUID(player: String): String? = null
    override fun hasPerms(player: Any, perms: String): Boolean = true
    override fun getOfflinePermissionPlayer(name: String): Permissionable = Permissionable { false }
    override fun isOnline(name: String): Boolean = false
    override fun getPlayer(name: String): Any? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #10")
    override fun kickPlayer(player: String, reason: String) { fail<Unit>("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #11") }
    override fun getOnlinePlayers(): Array<Any> = emptyArray()
    override fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long) { fail<Unit>("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #13") }
    override fun scheduleAsync(rn: Runnable, l1: Long) { fail<Unit>("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #14") }
    override fun runAsync(rn: Runnable) = rn.run()
    override fun runSync(rn: Runnable) = rn.run()
    override fun executeCommand(cmd: String) { fail<Unit>("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #17") }
    override fun callChat(player: Any): Boolean = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #18")
    override fun callCMD(player: Any, cmd: String): Boolean = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #19")
    override fun getMySQLFile(): Any = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #22")
    override fun parseJSON(json: InputStreamReader, key: String): String? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #23")
    override fun parseJSON(json: String, key: String): String? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #24")
    override fun getBoolean(file: Any, path: String): Boolean? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #25")
    override fun getString(file: Any, path: String): String = path
    override fun getLong(file: Any, path: String): Long? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #27")
    override fun getInteger(file: Any, path: String): Int? = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #28")
    override fun getStringList(file: Any, path: String): List<String> = emptyList()
    override fun getBoolean(file: Any, path: String, def: Boolean): Boolean = when (path) {
        "DetailedEnableMessage", "UUID-Fetcher.Enabled", "DetailedDisableMessage" -> false
        "Debug" -> true
        else -> def
    }
    override fun getString(file: Any, path: String, def: String): String = def
    override fun getLong(file: Any, path: String, def: Long): Long = def
    override fun getInteger(file: Any, path: String, def: Int): Int = def
    override fun contains(file: Any, path: String): Boolean = true
    override fun getFileName(file: Any): String = fail("This method has not been setup for tests yet. Edit the me.leoko.advancedban.TestMethods Class! #35")
    override fun callPunishmentEvent(punishment: Punishment) = println("Called punishment event!")
    override fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean) = println("Called punishment-revoke event!")
    override fun isOnlineMode(): Boolean = false
    override fun notify(perm: String, notification: List<String>) = notification.forEach(::println)
    override fun log(msg: String) = println("Logging: $msg")
    override fun isUnitTesting(): Boolean = true
}
