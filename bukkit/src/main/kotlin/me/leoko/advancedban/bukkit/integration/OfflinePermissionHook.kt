package me.leoko.advancedban.bukkit.integration

import org.bukkit.OfflinePlayer

fun interface OfflinePermissionHook {
    fun hasPermission(player: OfflinePlayer, permission: String): Boolean

    companion object {
        val NONE: OfflinePermissionHook = OfflinePermissionHook { _, _ -> false }
    }
}
