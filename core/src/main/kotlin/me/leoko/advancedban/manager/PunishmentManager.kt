package me.leoko.advancedban.manager

import me.leoko.advancedban.Universal
import me.leoko.advancedban.utils.InterimData
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.PunishmentType
import me.leoko.advancedban.utils.SQLQuery
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class PunishmentManager {
    private val punishments: MutableSet<Punishment> = ConcurrentHashMap.newKeySet()
    private val history: MutableSet<Punishment> = ConcurrentHashMap.newKeySet()
    private val cached: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private fun universal(): Universal = Universal.get()

    fun setup() {
        DatabaseManager.get().executeStatement(SQLQuery.DELETE_OLD_PUNISHMENTS, TimeManager.getTime())
    }

    fun load(name: String, uuid: String, ip: String?): InterimData? {
        val punishments = HashSet<Punishment>()
        val history = HashSet<Punishment>()
        try {
            DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_WITH_IP, uuid, ip).use { resultsPunishments ->
                DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP, uuid, ip).use { resultsHistory ->
                    if (resultsHistory == null || resultsPunishments == null) return null
                    while (resultsPunishments.next()) punishments.add(getPunishmentFromResultSet(resultsPunishments))
                    while (resultsHistory.next()) history.add(getPunishmentFromResultSet(resultsHistory))
                }
            }
        } catch (ex: SQLException) {
            val universal = universal()
            universal.log("An error has occurred loading the punishments from the database.")
            universal.debugSqlException(ex)
            return null
        }
        return InterimData(uuid, name, ip ?: "", punishments, history)
    }

    fun discard(nameInput: String) {
        val name = nameInput.lowercase()
        val ip = Universal.get().ips[name]
        val uuid = UUIDManager.get().getUUID(name)
        cached.remove(name)
        cached.remove(uuid)
        cached.remove(ip)

        synchronized(punishments) {
            val iterator = punishments.iterator()
            while (iterator.hasNext()) {
                val punishment = iterator.next()
                if (punishment.uuid == uuid || punishment.uuid == ip) iterator.remove()
            }
        }

        synchronized(history) {
            val historyIterator = history.iterator()
            while (historyIterator.hasNext()) {
                val punishment = historyIterator.next()
                if (punishment.uuid == uuid || punishment.uuid == ip) historyIterator.remove()
            }
        }
    }

    fun getPunishments(target: String, put: PunishmentType?, current: Boolean): List<Punishment> {
        val ptList = ArrayList<Punishment>()
        if (isCached(target)) {
            val iterator = (if (current) punishments else history).iterator()
            while (iterator.hasNext()) {
                val pt = iterator.next()
                if ((put == null || put == pt.type.getBasic()) && pt.uuid == target) {
                    if (!current || !pt.isExpired()) {
                        ptList.add(pt)
                    } else {
                        pt.delete(null, false, false)
                        iterator.remove()
                    }
                }
            }
        } else {
            try {
                DatabaseManager.get().executeResultStatement(if (current) SQLQuery.SELECT_USER_PUNISHMENTS else SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY, target).use { rs ->
                    if (rs == null) return ptList
                    while (rs.next()) {
                        val punishment = getPunishmentFromResultSet(rs)
                        if ((put == null || put == punishment.type.getBasic()) && (!current || !punishment.isExpired())) {
                            ptList.add(punishment)
                        }
                    }
                }
            } catch (ex: SQLException) {
                val universal = universal()
                universal.log("An error has occurred getting the punishments for $target")
                universal.debugSqlException(ex)
            }
        }
        return ptList
    }

    fun getPunishments(sqlQuery: SQLQuery, vararg parameters: Any): List<Punishment> {
        val ptList = ArrayList<Punishment>()
        val rs = DatabaseManager.get().executeResultStatement(sqlQuery, *parameters) ?: return ptList
        try {
            while (rs.next()) ptList.add(getPunishmentFromResultSet(rs))
            rs.close()
        } catch (ex: SQLException) {
            val universal = universal()
            universal.log("An error has occurred executing a query in the database.")
            universal.debug("Query: \n$sqlQuery")
            universal.debugSqlException(ex)
        }
        return ptList
    }

    fun getPunishment(id: Int): Punishment? {
        val cachedPunishment: Optional<Punishment> = getLoadedPunishments(false).stream().filter { it.id == id }.findAny()
        if (cachedPunishment.isPresent) return cachedPunishment.get()

        try {
            DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_PUNISHMENT_BY_ID, id).use { rs ->
                if (rs == null) return null
                if (rs.next()) {
                    val punishment = getPunishmentFromResultSet(rs)
                    if (!punishment.isExpired()) return punishment
                }
            }
        } catch (ex: SQLException) {
            val universal = universal()
            universal.log("An error has occurred getting a punishment by his id.")
            universal.debug("Punishment id: '$id'")
            universal.debugSqlException(ex)
        }
        return null
    }

    fun getWarn(id: Int): Punishment? {
        val punishment = getPunishment(id) ?: return null
        return if (punishment.type.getBasic() == PunishmentType.WARNING) punishment else null
    }

    fun getWarns(uuid: String): List<Punishment> = getPunishments(uuid, PunishmentType.WARNING, true)

    fun getNote(id: Int): Punishment? {
        val punishment = getPunishment(id) ?: return null
        return if (punishment.type.getBasic() == PunishmentType.NOTE) punishment else null
    }

    fun getNotes(uuid: String): List<Punishment> = getPunishments(uuid, PunishmentType.NOTE, true)

    fun getBan(uuid: String): Punishment? = getPunishments(uuid, PunishmentType.BAN, true).firstOrNull()

    fun getMute(uuid: String): Punishment? = getPunishments(uuid, PunishmentType.MUTE, true).firstOrNull()

    fun isBanned(uuid: String): Boolean = getBan(uuid) != null

    fun isMuted(uuid: String): Boolean = getMute(uuid) != null

    fun isCached(target: String?): Boolean = cached.contains(target)

    fun setCached(data: InterimData) {
        cached.add(data.name)
        cached.add(data.ip)
        cached.add(data.uuid)
    }

    fun getCalculationLevel(uuid: String, layout: String): Int {
        if (isCached(uuid)) {
            return history.stream().filter { it.uuid == uuid && layout.equals(it.calculation, true) }.count().toInt()
        }

        var i = 0
        try {
            DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION, uuid, layout).use { resultSet ->
                if (resultSet == null) return i
                while (resultSet.next()) i++
            }
        } catch (ex: SQLException) {
            val universal = universal()
            universal.log("An error has occurred getting the level for the layout '$layout' for '$uuid'")
            universal.debugSqlException(ex)
        }
        return i
    }

    fun getCurrentWarns(uuid: String): Int = getWarns(uuid).size

    fun getCurrentNotes(uuid: String): Int = getNotes(uuid).size

    fun getLoadedPunishments(checkExpired: Boolean): MutableSet<Punishment> {
        if (checkExpired) {
            val toDelete = ArrayList<Punishment>()
            for (pu in punishments) if (pu.isExpired()) toDelete.add(pu)
            for (pu in toDelete) pu.delete()
        }
        return punishments
    }

    @Throws(SQLException::class)
    fun getPunishmentFromResultSet(rs: ResultSet): Punishment = Punishment(
        rs.getString("name"),
        rs.getString("uuid"),
        rs.getString("reason"),
        rs.getString("operator"),
        PunishmentType.valueOf(rs.getString("punishmentType")),
        rs.getLong("start"),
        rs.getLong("end"),
        rs.getString("calculation"),
        rs.getInt("id")
    )

    val loadedHistory: MutableSet<Punishment>
        get() = history

    companion object {
        @Volatile private var instance: PunishmentManager? = null

        @Synchronized
        @JvmStatic
        fun get(): PunishmentManager {
            if (instance == null) instance = PunishmentManager()
            return instance!!
        }
    }
}
