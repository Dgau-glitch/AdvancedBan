package me.leoko.advancedban.bukkit.service

import io.papermc.paper.ban.BanListType
import me.leoko.advancedban.bukkit.BukkitMain
import me.leoko.advancedban.bukkit.utils.FoliaSchedulers
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.Punishment
import org.bukkit.Bukkit
import org.bukkit.ban.IpBanList
import org.bukkit.ban.ProfileBanList
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID
import java.util.logging.Level

class BukkitBanListSynchronizer {
    private val plugin: BukkitMain get() = BukkitMain.get()

    fun banProfile(punishment: Punishment) {
        runGlobalBanMutation("profile ban for ${punishment.name}") {
            val profile = createProfile(punishment) ?: return@runGlobalBanMutation
            profileBanList().addBan(profile, punishment.getReason(), expiryFor(punishment), punishment.operator)
        }
    }

    fun pardonProfile(punishment: Punishment) {
        runGlobalBanMutation("profile pardon for ${punishment.name}") {
            val profile = createProfile(punishment) ?: return@runGlobalBanMutation
            profileBanList().pardon(profile)
        }
    }

    fun banIp(punishment: Punishment) {
        resolveIpThenMutate(punishment.name, "IP ban for ${punishment.name}") { address ->
            ipBanList().addBan(address, punishment.getReason(), expiryFor(punishment), punishment.operator)
        }
    }

    fun pardonIp(punishment: Punishment) {
        resolveIpThenMutate(punishment.name, "IP pardon for ${punishment.name}") { address ->
            ipBanList().pardon(address)
        }
    }

    private fun profileBanList(): ProfileBanList = Bukkit.getBanList(BanListType.PROFILE)

    private fun ipBanList(): IpBanList = Bukkit.getBanList(BanListType.IP)

    private fun expiryFor(punishment: Punishment): Instant? =
        if (punishment.type.isTemp()) Instant.ofEpochMilli(punishment.end) else null

    private fun createProfile(punishment: Punishment): com.destroystokyo.paper.profile.PlayerProfile? {
        val profileUuid = resolveProfileUuid(punishment)
        if (profileUuid == null) {
            plugin.logger.warning(
                "Skipping vanilla profile ban-list sync for ${punishment.name}: stored UUID '${punishment.uuid}' is not a valid UUID. " +
                    "AdvancedBan punishment data remains authoritative."
            )
            return null
        }
        return Bukkit.createProfile(profileUuid, punishment.name)
    }

    private fun resolveProfileUuid(punishment: Punishment): UUID? {
        val storedUuid = punishment.uuid?.let(UUIDManager.get()::fromString)
        if (storedUuid != null) return storedUuid
        return if (!Bukkit.getOnlineMode()) offlineModeUuid(punishment.name) else null
    }

    private fun offlineModeUuid(name: String): UUID =
        UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(StandardCharsets.UTF_8))

    private fun runGlobalBanMutation(operation: String, mutation: () -> Unit) {
        FoliaSchedulers.runGlobal(plugin) {
            try {
                mutation()
            } catch (ex: Exception) {
                plugin.logger.log(Level.WARNING, "Failed to sync $operation with the vanilla ban list", ex)
            }
        }
    }

    private fun resolveIpThenMutate(address: String, operation: String, mutation: (InetAddress) -> Unit) {
        FoliaSchedulers.runAsync(plugin) {
            val resolved = try {
                InetAddress.getByName(address)
            } catch (ex: Exception) {
                plugin.logger.log(Level.WARNING, "Failed to resolve address '$address' for $operation", ex)
                return@runAsync
            }
            runGlobalBanMutation(operation) { mutation(resolved) }
        }
    }
}
