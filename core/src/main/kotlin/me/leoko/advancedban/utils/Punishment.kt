package me.leoko.advancedban.utils

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.TimeManager
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date

class Punishment(
    val name: String,
    val uuid: String?,
    private var reasonRaw: String?,
    val operator: String,
    val type: PunishmentType,
    val start: Long,
    val end: Long,
    val calculation: String,
    var id: Int
) {
    fun getReason(): String = ((reasonRaw ?: (mi.getString(mi.getConfig(), "DefaultReason", "none") ?: "none"))).replace("'", "")

    fun getHexId(): String = Integer.toHexString(id).uppercase()

    fun getDate(date: Long): String {
        val format = SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"))
        return format.format(Date(date))
    }

    fun create() = create(false)

    fun create(silent: Boolean) {
        if (id != -1) {
            Universal.get().log("!! Failed! AB tried to overwrite the punishment:")
            Universal.get().log("!! Failed at: $this")
            return
        }

        if (uuid == null) {
            Universal.get().log("!! Failed! AB has not saved the ${type.name} because there is no fetched UUID")
            Universal.get().log("!! Failed at: $this")
            return
        }

        val cWarnings = if (type.getBasic() == PunishmentType.WARNING) PunishmentManager.get().getCurrentWarns(uuid) + 1 else 0

        DatabaseManager.get().executeStatement(SQLQuery.INSERT_PUNISHMENT_HISTORY, name, uuid, getReason(), operator, type.name, start, end, calculation)

        if (type != PunishmentType.KICK) {
            try {
                DatabaseManager.get().executeStatement(SQLQuery.INSERT_PUNISHMENT, name, uuid, getReason(), operator, type.name, start, end, calculation)
                DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_EXACT_PUNISHMENT, uuid, start, type.name).use { rs ->
                    if (rs == null) return@use
                    if (rs.next()) {
                        id = rs.getInt("id")
                    } else {
                        Universal.get().log("!! Not able to update ID of punishment! Please restart the server to resolve this issue!")
                        Universal.get().log("!! Failed at: $this")
                    }
                }
            } catch (ex: SQLException) {
                Universal.get().debugSqlException(ex)
            }
        }

        if (!silent) announce(cWarnings)

        if (mi.isOnline(name)) {
            val p = mi.getPlayer(name) ?: return
            if (type.getBasic() == PunishmentType.BAN || type == PunishmentType.KICK) {
                mi.runSync { mi.kickPlayer(name, getLayoutBSN()) }
            } else {
                if (type.getBasic() != PunishmentType.NOTE) {
                    for (str in getLayout()) mi.sendMessage(p, str)
                }
                PunishmentManager.get().getLoadedPunishments(false).add(this)
            }
        }

        PunishmentManager.get().loadedHistory.add(this)
        mi.callPunishmentEvent(this)

        if (type.getBasic() == PunishmentType.WARNING) {
            var cmd: String? = null
            for (i in 1..cWarnings) {
                if (mi.contains(mi.getConfig(), "WarnActions.$i")) cmd = mi.getString(mi.getConfig(), "WarnActions.$i")
            }
            if (cmd != null) {
                val finalCmd = cmd.replace("%PLAYER%", name).replace("%COUNT%", cWarnings.toString()).replace("%REASON%", getReason())
                mi.runSync {
                    mi.executeCommand(finalCmd)
                    Universal.get().log("Executing command: $finalCmd")
                }
            }
        }
    }

    fun updateReason(reason: String) {
        this.reasonRaw = reason
        if (id != -1) DatabaseManager.get().executeStatement(SQLQuery.UPDATE_PUNISHMENT_REASON, reason, id)
    }

    private fun announce(cWarnings: Int) {
        val notification = MessageManager.getLayout(
            mi.getMessages(),
            type.getConfSection("Notification"),
            "OPERATOR", operator,
            "PREFIX", if (mi.getBoolean(mi.getConfig(), "Disable Prefix", false)) "" else MessageManager.getMessage("General.Prefix"),
            "DURATION", getDuration(true),
            "REASON", getReason(),
            "NAME", name,
            "ID", id.toString(),
            "HEXID", getHexId(),
            "DATE", getDate(start),
            "COUNT", cWarnings.toString()
        )
        mi.notify(type.getNotifyPermission(), notification)
    }

    fun delete() = delete(null, false, true)

    fun delete(who: String?, massClear: Boolean, removeCache: Boolean) {
        if (type == PunishmentType.KICK) {
            Universal.get().log("!! Failed deleting! You are not able to delete Kicks!")
            return
        }
        if (id == -1) {
            Universal.get().log("!! Failed deleting! The Punishment is not created yet!")
            Universal.get().log("!! Failed at: $this")
            return
        }

        DatabaseManager.get().executeStatement(SQLQuery.DELETE_PUNISHMENT, id)
        if (removeCache) {
            PunishmentManager.get().getLoadedPunishments(false).removeIf { it.id == id || (it.uuid == uuid && it.type == type && it.start == start) }
        }

        if (who != null) {
            val message = MessageManager.getMessage(type.getUndoConfSection("Notification"), true, "OPERATOR", who, "NAME", name)
            mi.notify(type.getUndoNotifyPermission(), Collections.singletonList(message))
            Universal.get().debug("$who is deleting a punishment")
        }

        Universal.get().debug("Deleted punishment $id from $name punishment reason: ${getReason()}")
        mi.callRevokePunishmentEvent(this, massClear)
    }

    fun getLayout(): List<String> {
        val isLayout = getReason().startsWith("@") || getReason().startsWith("~")
        return MessageManager.getLayout(
            if (isLayout) mi.getLayouts() else mi.getMessages(),
            if (isLayout) "Message." + getReason().split(" ")[0].substring(1) else type.getConfSection("Layout"),
            "OPERATOR", operator,
            "PREFIX", if (mi.getBoolean(mi.getConfig(), "Disable Prefix", false)) "" else MessageManager.getMessage("General.Prefix"),
            "DURATION", getDuration(false),
            "REASON", if (isLayout) {
                val split = getReason().split(" ")
                if (split.size < 2) "" else getReason().substring(split[0].length + 1)
            } else getReason(),
            "HEXID", getHexId(),
            "ID", id.toString(),
            "DATE", getDate(start),
            "COUNT", if (type.getBasic() == PunishmentType.WARNING) (PunishmentManager.get().getCurrentWarns(uuid ?: "") + 1).toString() else "0"
        )
    }

    fun getDuration(fromStart: Boolean): String {
        var duration = "permanent"
        if (type.isTemp()) {
            val diff = ceilDiv(end - if (fromStart) start else TimeManager.getTime(), 1000L)
            duration = when {
                diff > 60 * 60 * 24 -> MessageManager.getMessage("General.TimeLayoutD", *getDurationParameter("D", (diff / 60 / 60 / 24).toString(), "H", (diff / 60 / 60 % 24).toString(), "M", (diff / 60 % 60).toString(), "S", (diff % 60).toString()))
                diff > 60 * 60 -> MessageManager.getMessage("General.TimeLayoutH", *getDurationParameter("H", (diff / 60 / 60).toString(), "M", (diff / 60 % 60).toString(), "S", (diff % 60).toString()))
                diff > 60 -> MessageManager.getMessage("General.TimeLayoutM", *getDurationParameter("M", (diff / 60).toString(), "S", (diff % 60).toString()))
                else -> MessageManager.getMessage("General.TimeLayoutS", *getDurationParameter("S", diff.toString()))
            }
        }
        return duration
    }

    fun ceilDiv(x: Long, y: Long): Long = -Math.floorDiv(-x, y)

    private fun getDurationParameter(vararg parameter: String): Array<String> {
        val length = parameter.size
        val newParameter = arrayOfNulls<String>(length * 2)
        var i = 0
        while (i < length) {
            val name = parameter[i]
            val count = parameter[i + 1]
            newParameter[i] = name
            newParameter[i + 1] = count
            newParameter[length + i] = name + name
            newParameter[length + i + 1] = (if (count.length <= 1) "0" else "") + count
            i += 2
        }
        @Suppress("UNCHECKED_CAST")
        return newParameter as Array<String>
    }

    fun getLayoutBSN(): String = getLayout().joinToString("\n")

    fun isExpired(): Boolean = type.isTemp() && end <= TimeManager.getTime()

    override fun toString(): String {
        return "Punishment(name=$name, uuid=$uuid, operator=$operator, calculation=$calculation, start=$start, end=$end, type=$type, reason=${getReason()}, id=$id)"
    }

    companion object {
        private val mi: MethodInterface = Universal.get().methods

        @JvmStatic
        fun create(name: String, target: String, reason: String?, operator: String, type: PunishmentType, end: Long, calculation: String, silent: Boolean) {
            Punishment(name, target, reason, operator, if (end == -1L) type.getPermanent() else type, TimeManager.getTime(), end, calculation, -1).create(silent)
        }
    }
}
