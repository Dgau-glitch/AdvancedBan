package me.leoko.advancedban.bukkit.listener

import io.papermc.paper.ban.BanListType
import me.leoko.advancedban.bukkit.BukkitMain
import me.leoko.advancedban.bukkit.event.PunishmentEvent
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent
import me.leoko.advancedban.bukkit.utils.FoliaSchedulers
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.PunishmentType
import org.bukkit.Bukkit
import org.bukkit.ban.IpBanList
import org.bukkit.ban.ProfileBanList
import java.net.InetAddress
import java.time.Instant

class InternalListener : org.bukkit.event.Listener {
    private fun profileBanList(): ProfileBanList = Bukkit.getBanList(BanListType.PROFILE)

    private fun ipBanList(): IpBanList = Bukkit.getBanList(BanListType.IP)

    private fun expiryFor(punishment: Punishment): Instant? =
        if (punishment.type.isTemp()) Instant.ofEpochMilli(punishment.end) else null

    private fun runGlobalBanMutation(task: () -> Unit) {
        FoliaSchedulers.runGlobal(BukkitMain.get()) { task() }
    }

    private fun resolveIpThenMutate(address: String, mutation: (InetAddress) -> Unit) {
        FoliaSchedulers.runAsync(BukkitMain.get()) {
            val resolved = InetAddress.getByName(address)
            runGlobalBanMutation { mutation(resolved) }
        }
    }

    @org.bukkit.event.EventHandler
    fun onPunish(event: PunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> runGlobalBanMutation {
                val profile = Bukkit.createProfile(punishment.name)
                profileBanList().addBan(profile, punishment.getReason(), expiryFor(punishment), punishment.operator)
            }

            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> resolveIpThenMutate(punishment.name) { address ->
                ipBanList().addBan(address, punishment.getReason(), expiryFor(punishment), punishment.operator)
            }

            else -> Unit
        }
    }

    @org.bukkit.event.EventHandler
    fun onRevokePunishment(event: RevokePunishmentEvent) {
        val punishment = event.punishment
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMP_BAN -> runGlobalBanMutation {
                profileBanList().pardon(Bukkit.createProfile(punishment.name))
            }

            PunishmentType.IP_BAN, PunishmentType.TEMP_IP_BAN -> resolveIpThenMutate(punishment.name) { address ->
                ipBanList().pardon(address)
            }

            else -> Unit
        }
    }
}
