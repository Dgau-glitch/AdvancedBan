package me.leoko.advancedban.bukkit.integration

import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class VaultPermissionHook private constructor(private val permission: Permission) : OfflinePermissionHook {
    override fun hasPermission(player: OfflinePlayer, permission: String): Boolean =
        this.permission.playerHas(null, player, permission)

    companion object {
        private const val PLUGIN_NAME = "Vault"

        fun create(): OfflinePermissionHook {
            if (Bukkit.getPluginManager().getPlugin(PLUGIN_NAME) == null) return OfflinePermissionHook.NONE
            val registration = Bukkit.getServicesManager().getRegistration(Permission::class.java)
                ?: return OfflinePermissionHook.NONE
            return VaultPermissionHook(registration.provider)
        }
    }
}
