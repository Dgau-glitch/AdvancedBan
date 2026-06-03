package me.leoko.advancedban

import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import java.io.File
import java.io.InputStreamReader

/**
 * Platform bridge used by the reusable core.
 *
 * Thread-context categories:
 * - blocking/async-safe: may be called by core code from an async/background context; Bukkit implementations must not mutate regionized state directly.
 * - global-only: must be executed on Folia's global scheduler by the implementation or caller.
 * - player/entity-only: may touch a player/entity and must run on that entity's scheduler before mutating player state.
 * - region/location-only: may touch world/chunk/block/location state and must run on the owning region scheduler.
 * - pure core/no scheduler required: data-only operation that does not touch live server state.
 *
 * Keep this interface source-compatible for existing core callers. Platform implementations may add helper methods internally to route work to the right scheduler.
 */
interface MethodInterface {
    /** Context: blocking/async-safe. Loads plugin files during startup/reload. */
    fun loadFiles()

    /** Context: blocking/async-safe. Performs network IO and JSON parsing. */
    fun getFromUrlJson(url: String, key: String): String?

    /** Context: pure core/no scheduler required. Reads immutable plugin metadata. */
    fun getVersion(): String

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data. */
    fun getKeys(file: Any, path: String): Array<String>

    /** Context: pure core/no scheduler required. Returns already-loaded configuration data. */
    fun getConfig(): Any

    /** Context: pure core/no scheduler required. Returns already-loaded message data. */
    fun getMessages(): Any

    /** Context: pure core/no scheduler required. Returns already-loaded layout data. */
    fun getLayouts(): Any

    /** Context: pure core/no scheduler required. Describes the active platform adapter. */
    fun isBungee(): Boolean

    /** Context: pure core/no scheduler required. Converts text without touching live server state. */
    fun clearFormatting(text: String): String?

    /** Context: pure core/no scheduler required. Returns the platform plugin object. */
    fun getPlugin(): Any

    /** Context: pure core/no scheduler required. Returns the plugin data directory handle. */
    fun getDataFolder(): File

    /** Context: global-only. Registers command executors and tab-completers with the server command map. */
    fun setCommandExecutor(cmd: String, permission: String?, tabCompleter: TabCompleter?)

    /** Context: blocking/async-safe. Implementations must route player recipients to player/entity scheduler and console/global recipients to global scheduler. */
    fun sendMessage(player: Any, msg: String)

    /** Context: pure core/no scheduler required. Reads a sender/player name without mutating state. */
    fun getName(player: Any): String

    /** Context: blocking/async-safe. Performs an offline profile/name lookup and does not mutate live state. */
    fun getName(uuid: String): String?

    /** Context: player/entity-only. Reads data from a live player object. */
    fun getIP(player: Any): String

    /** Context: pure core/no scheduler required. Reads UUID data from a supplied player/offline-player object. */
    fun getInternUUID(player: Any): String

    /** Context: blocking/async-safe. Performs an offline profile lookup and does not mutate live state. */
    fun getInternUUID(player: String): String?

    /** Context: player/entity-only. Reads permissions from a live sender/player object. */
    fun hasPerms(player: Any, perms: String): Boolean

    /** Context: blocking/async-safe. Builds an offline permission proxy for later permission checks. */
    fun getOfflinePermissionPlayer(name: String): Permissionable

    /** Context: global-only. Reads global online-player state by name. */
    fun isOnline(name: String): Boolean

    /** Context: global-only. Returns a live player reference for lookup only; callers must schedule mutations on the player scheduler. */
    fun getPlayer(name: String): Any?

    /** Context: blocking/async-safe. Implementations must route the kick to the target player's scheduler. */
    fun kickPlayer(player: String, reason: String)

    /** Context: global-only. Returns a snapshot for lookup/iteration only; callers must schedule each player mutation on the player scheduler. */
    fun getOnlinePlayers(): Array<out Any>

    /** Context: blocking/async-safe. Schedules repeating async work through the platform scheduler facade. */
    fun scheduleAsyncRep(rn: Runnable, l1: Long, l2: Long)

    /** Context: blocking/async-safe. Schedules delayed async work through the platform scheduler facade. */
    fun scheduleAsync(rn: Runnable, l1: Long)

    /** Context: blocking/async-safe. Schedules immediate async work through the platform scheduler facade. */
    fun runAsync(rn: Runnable)

    /** Context: global-only. Schedules global server work through the platform scheduler facade. */
    fun runSync(rn: Runnable)

    /** Context: global-only. Dispatches a console command on the global scheduler. */
    fun executeCommand(cmd: String)

    /** Context: blocking/async-safe. Checks mute state and routes mute-layout delivery to the target player's scheduler. */
    fun callChat(player: Any): Boolean

    /** Context: blocking/async-safe. Checks muted command state and routes mute-layout delivery to the target player's scheduler. */
    fun callCMD(player: Any, cmd: String): Boolean

    /** Context: pure core/no scheduler required. Returns already-loaded MySQL configuration data. */
    fun getMySQLFile(): Any

    /** Context: pure core/no scheduler required. Parses supplied JSON input. */
    fun parseJSON(json: InputStreamReader, key: String): String?

    /** Context: pure core/no scheduler required. Parses supplied JSON text. */
    fun parseJSON(json: String, key: String): String?

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data. */
    fun getBoolean(file: Any, path: String): Boolean?

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data. */
    fun getString(file: Any, path: String): String?

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data. */
    fun getLong(file: Any, path: String): Long?

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data. */
    fun getInteger(file: Any, path: String): Int?

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data. */
    fun getStringList(file: Any, path: String): List<String>

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data with a default. */
    fun getBoolean(file: Any, path: String, def: Boolean): Boolean

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data with a default. */
    fun getString(file: Any, path: String, def: String): String?

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data with a default. */
    fun getLong(file: Any, path: String, def: Long): Long

    /** Context: pure core/no scheduler required. Reads already-loaded configuration data with a default. */
    fun getInteger(file: Any, path: String, def: Int): Int

    /** Context: pure core/no scheduler required. Checks an already-loaded configuration path. */
    fun contains(file: Any, path: String): Boolean

    /** Context: pure core/no scheduler required. Reads a loaded configuration file name. */
    fun getFileName(file: Any): String

    /** Context: global-only. Fires a Bukkit/Bungee platform event through the platform event bus. */
    fun callPunishmentEvent(punishment: Punishment)

    /** Context: global-only. Fires a Bukkit/Bungee platform event through the platform event bus. */
    fun callRevokePunishmentEvent(punishment: Punishment, massClear: Boolean)

    /** Context: global-only. Reads server online-mode state. */
    fun isOnlineMode(): Boolean

    /** Context: blocking/async-safe. Implementations must enumerate recipients safely and route player notifications to each target player's scheduler. */
    fun notify(perm: String, notification: List<String>)

    /** Context: blocking/async-safe. Implementations must route console/global output to the global scheduler. */
    fun log(msg: String)

    /** Context: pure core/no scheduler required. Indicates whether this adapter is running tests. */
    fun isUnitTesting(): Boolean
}
