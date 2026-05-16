package me.leoko.advancedban.bukkit.listener

import io.papermc.paper.ban.BanListType
import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.utils.PunishmentType
import org.bukkit.Bukkit
import org.bukkit.ban.IpBanList
import org.bukkit.ban.ProfileBanList
import java.net.InetAddress
import java.time.Instant

class InternalListener : org.bukkit.event.Listener {
    private fun profileBanList(): ProfileBanList = Bukkit.getBanList(BanListType.PROFILE)

    private fun ipBanList(): IpBanList = Bukkit.getBanList(BanListType.IP)

    @org.bukkit.event.EventHandler
    fun onPunish(event: PunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> {
                val profile = Bukkit.createProfile(punishment.name)
                profileBanList().addBan(profile, punishment.getReason(), Instant.ofEpochMilli(punishment.end), punishment.operator)
            }

            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> {
                InetAddress.getByName(punishment.name)?.let {
                    ipBanList().addBan(it, punishment.getReason(), Instant.ofEpochMilli(punishment.end), punishment.operator)
                }
            }

            else -> Unit
        }
    }

    @org.bukkit.event.EventHandler
    fun onRevokePunishment(event: RevokePunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> profileBanList().pardon(Bukkit.createProfile(punishment.name))
            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> InetAddress.getByName(punishment.name)?.let { ipBanList().pardon(it) }
            else -> Unit
        }
    }
}
