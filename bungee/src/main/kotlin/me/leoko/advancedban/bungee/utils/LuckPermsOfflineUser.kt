package me.leoko.advancedban.bungee.utils

import me.leoko.advancedban.utils.Permissionable
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User

class LuckPermsOfflineUser(name: String) : Permissionable {
    private val permissionUser: User?

    init {
        val userManager = LuckPermsProvider.get().userManager
        val uuid = userManager.lookupUniqueId(name).join()
        permissionUser = if (uuid != null) userManager.loadUser(uuid).join() else null
    }

    override fun hasPermission(permission: String): Boolean {
        return permissionUser?.cachedData?.permissionData?.checkPermission(permission)?.asBoolean() == true
    }
}
