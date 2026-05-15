package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.utils.PunishmentType
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.Date

class InternalListener : Listener {
    @Suppress("UNCHECKED_CAST")
    @EventHandler
    fun onPunish(event: PunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> {
                val banList = Bukkit.getBanList(BanList.Type.NAME) as BanList<Any>
                banList.addBan(punishment.name, punishment.reason, Date(punishment.end), punishment.operator)
            }

            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> {
                val banList = Bukkit.getBanList(BanList.Type.IP) as BanList<Any>
                banList.addBan(punishment.name, punishment.reason, Date(punishment.end), punishment.operator)
            }

            else -> Unit
        }
    }

    @Suppress("UNCHECKED_CAST")
    @EventHandler
    fun onRevokePunishment(event: RevokePunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> {
                val banList = Bukkit.getBanList(BanList.Type.NAME) as BanList<Any>
                banList.pardon(punishment.name)
            }

            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> {
                val banList = Bukkit.getBanList(BanList.Type.IP) as BanList<Any>
                banList.pardon(punishment.name)
            }

            else -> Unit
        }
    }
}
