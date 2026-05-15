package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.utils.PunishmentType
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.Date

@Suppress("DEPRECATION")
class InternalListener : Listener {
    @Suppress("UNCHECKED_CAST")
    private fun nameBanList(): BanList<Any> = Bukkit.getBanList(BanList.Type.NAME) as BanList<Any>

    @Suppress("UNCHECKED_CAST")
    private fun ipBanList(): BanList<Any> = Bukkit.getBanList(BanList.Type.IP) as BanList<Any>

    @EventHandler
    fun onPunish(event: PunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> {
                nameBanList().addBan(punishment.name, punishment.reason, Date(punishment.end), punishment.operator)
            }

            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> {
                ipBanList().addBan(punishment.name, punishment.reason, Date(punishment.end), punishment.operator)
            }

            else -> Unit
        }
    }

    @EventHandler
    fun onRevokePunishment(event: RevokePunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> nameBanList().pardon(punishment.name)
            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> ipBanList().pardon(punishment.name)
            else -> Unit
        }
    }
}
