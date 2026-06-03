package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.bukkit.service.BukkitBanListSynchronizer
import me.leoko.advancedban.utils.PunishmentType

class InternalListener : org.bukkit.event.Listener {
    private val banListSynchronizer = BukkitBanListSynchronizer()

    @org.bukkit.event.EventHandler
    fun onPunish(event: PunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> banListSynchronizer.banProfile(punishment)
            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> banListSynchronizer.banIp(punishment)
            else -> Unit
        }
    }

    @org.bukkit.event.EventHandler
    fun onRevokePunishment(event: RevokePunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> banListSynchronizer.pardonProfile(punishment)
            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> banListSynchronizer.pardonIp(punishment)
            else -> Unit
        }
    }
}
