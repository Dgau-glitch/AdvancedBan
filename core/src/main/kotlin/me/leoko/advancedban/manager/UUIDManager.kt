package me.leoko.advancedban.manager

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import java.util.Scanner
import java.util.UUID

class UUIDManager {
    private var mode: FetcherMode? = null
    private val activeUUIDs: MutableMap<String, String> = HashMap()

    private fun mi(): MethodInterface = Universal.get().methods

    fun setup() {
        val mi = mi()
        mode = if (mi.getBoolean(mi.getConfig(), "UUID-Fetcher.Dynamic", true)) {
            if (!mi.isOnlineMode()) {
                FetcherMode.DISABLED
            } else if (Universal.get().isBungee) {
                FetcherMode.MIXED
            } else {
                FetcherMode.INTERN
            }
        } else {
            if (!mi.getBoolean(mi.getConfig(), "UUID-Fetcher.Enabled", true)) {
                FetcherMode.DISABLED
            } else if (mi.getBoolean(mi.getConfig(), "UUID-Fetcher.Intern", false)) {
                FetcherMode.INTERN
            } else {
                FetcherMode.RESTFUL
            }
        }
    }

    fun getInitialUUID(nameInput: String): String? {
        val mi = mi()
        val name = nameInput.lowercase()
        if (mode == FetcherMode.DISABLED) return name

        if (mode == FetcherMode.INTERN || mode == FetcherMode.MIXED) {
            val internUUID = mi.getInternUUID(name)
            if (mode == FetcherMode.INTERN || internUUID != null) return internUUID
        }

        var uuid: String? = null
        try {
            uuid = askAPI(
                mi.getString(mi.getConfig(), "UUID-Fetcher.REST-API.URL") ?: "",
                name,
                mi.getString(mi.getConfig(), "UUID-Fetcher.REST-API.Key") ?: ""
            )
        } catch (e: IOException) {
            println("Error -> ${e.message}")
            println("!! Failed fetching UUID of $name")
            println("!! Could not connect to REST-API under " + mi.getString(mi.getConfig(), "UUID-Fetcher.REST-API.URL") ?: "")
        }

        if (uuid == null) {
            println("Trying to fetch UUID form BackUp-API...")
            try {
                uuid = askAPI(
                    mi.getString(mi.getConfig(), "UUID-Fetcher.BackUp-API.URL") ?: "",
                    name,
                    mi.getString(mi.getConfig(), "UUID-Fetcher.BackUp-API.Key") ?: ""
                )
            } catch (e: IOException) {
                println("!! Failed fetching UUID of $name")
                println("!! Could not connect to REST-API under " + mi.getString(mi.getConfig(), "UUID-Fetcher.BackUp-API.URL") ?: "")
            }
        }

        if (uuid == null) {
            println("!! !! Warning we have not been able to fetch the UUID of the Player $name")
            println("!! Make sure that the name is spelled correctly and if it is change your UUID-Fetcher settings!")
        }

        return uuid
    }

    fun supplyInternUUID(name: String, uuid: UUID) {
        if (mode == FetcherMode.INTERN || mode == FetcherMode.MIXED) {
            activeUUIDs[name.lowercase()] = uuid.toString().replace("-", "")
        }
    }

    fun fromString(uuidInput: String): UUID? {
        var uuid = uuidInput
        if (!uuid.contains("-") && uuid.length == 32) {
            uuid = uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)".toRegex(), "$1-$2-$3-$4-$5")
        }
        return if (uuid.length == 36 && uuid.contains("-")) UUID.fromString(uuid) else null
    }

    fun getUUID(name: String): String? = getInMemoryUUID(name) ?: getInitialUUID(name)

    fun getInMemoryUUID(name: String): String? = activeUUIDs[name.lowercase()]

    fun getInMemoryName(uuid: String): String? {
        for ((key, value) in activeUUIDs.entries) {
            if (value.equals(uuid, ignoreCase = true)) return key
        }
        return null
    }

    fun getNameFromUUID(uuid: String, forceInitial: Boolean): String? {
        val mi = mi()
        if (mode == FetcherMode.DISABLED) return uuid

        if (mode == FetcherMode.INTERN || mode == FetcherMode.MIXED) {
            val internName = mi.getName(uuid)
            if (mode == FetcherMode.INTERN || internName != null) return internName
        }

        if (!forceInitial) {
            val inMemoryName = getInMemoryName(uuid)
            if (inMemoryName != null) return inMemoryName
        }

        return try {
            Scanner(URL("https://api.mojang.com/user/profiles/$uuid/names").openStream(), "UTF-8").use { scanner ->
                var s = scanner.useDelimiter("\\A").next()
                s = s.substring(s.lastIndexOf('{'), s.lastIndexOf('}') + 1)
                mi.parseJSON(s, "name")
            }
        } catch (_: Exception) {
            null
        }
    }

    @Throws(IOException::class)
    private fun askAPI(url: String, nameInput: String, key: String): String? {
        val mi = mi()
        val name = nameInput.lowercase()
        val request = URL(url.replace("%NAME%", name).replace("%TIMESTAMP%", Date().time.toString())).openConnection() as HttpURLConnection
        request.connect()

        val uuid = mi.parseJSON(InputStreamReader(request.inputStream), key)

        if (uuid == null) {
            println("!! Failed fetching UUID of $name")
            println("!! Could not find key '$key' in the servers response")
            println("!! Response: " + request.responseMessage)
        } else {
            activeUUIDs[name] = uuid
        }
        return uuid
    }

    fun getMode(): FetcherMode? = mode

    enum class FetcherMode {
        DISABLED,
        INTERN,
        MIXED,
        RESTFUL
    }

    companion object {
        @Volatile
        private var instance: UUIDManager? = null

        @Synchronized
        @JvmStatic
        fun get(): UUIDManager {
            if (instance == null) instance = UUIDManager()
            return instance!!
        }
    }
}
