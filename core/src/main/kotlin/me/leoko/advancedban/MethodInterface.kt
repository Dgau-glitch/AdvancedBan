package me.leoko.advancedban

import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import java.io.File
import java.io.InputStreamReader

interface MethodInterface {
    fun loadFiles()
    fun getFromUrlJson(url: String, key: String): String?
    fun getVersion(): String
    fun getKeys(file: Any, path: String): Array<String>
    fun getConfig(): Any
    fun getMessages(): Any
    fun getLayouts(): Any
    fun setupMetrics()
    fun isBungee(): Boolean
    fun clearFormatting(text: String): String?
    fun getPlugin(): Any
    fun getDataFolder(): File
    fun setCommandExecutor(cmd: String, permission: String?, tabCompleter: TabCompleter?)
    fun sendMessage(player: Any, msg: String)
    fun getName(player: Any): String
    fun getName(uuid: String): String?
    fun getIP(player: Any): String
    fun getInternUUID(player: Any): String
    fun getInternUUID(player: String): String?
    fun hasPerms(player: Any, perms: String): Boolean
    fun getOfflinePermissionPlayer(name: String): Permissionable
    fun isOnline(name: String): Boolean
    fun getPlayer(name: String): Any?
    fun kickPlayer(player: String, reason: String)
    fun getOnlinePlayers(): Array<out Any>
    fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long)
    fun scheduleAsync(rn: Runnable, l1: Long)
    fun runAsync(rn: Runnable)
    fun runSync(rn: Runnable)
    fun executeCommand(cmd: String)
    fun callChat(player: Any): Boolean
    fun callCMD(player: Any, cmd: String): Boolean
    fun getMySQLFile(): Any
    fun parseJSON(json: InputStreamReader, key: String): String?
    fun parseJSON(json: String, key: String): String?
    fun getBoolean(file: Any, path: String): Boolean?
    fun getString(file: Any, path: String): String?
    fun getLong(file: Any, path: String): Long?
    fun getInteger(file: Any, path: String): Int?
    fun getStringList(file: Any, path: String): List<String>
    fun getBoolean(file: Any, path: String, def: Boolean): Boolean
    fun getString(file: Any, path: String, def: String): String?
    fun getLong(file: Any, path: String, def: Long): Long
    fun getInteger(file: Any, path: String, def: Int): Int
    fun contains(file: Any, path: String): Boolean
    fun getFileName(file: Any): String
    fun callPunishmentEvent(punishment: Punishment)
    fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean)
    fun isOnlineMode(): Boolean
    fun notify(perm: String, notification: List<String>)
    fun log(msg: String)
    fun isUnitTesting(): Boolean

}
