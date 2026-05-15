package me.leoko.advancedban.bungee.utils

import de.dytanic.cloudnet.driver.CloudNetDriver
import de.dytanic.cloudnet.driver.permission.IPermissionUser
import me.leoko.advancedban.utils.Permissionable

class CloudNetCloudPermsOfflineUser(name: String) : Permissionable {
    private val permissionUser: IPermissionUser?

    init {
        val users = CloudNetDriver.getInstance().permissionManagement.getUsers(name)
        permissionUser = users.firstOrNull()
    }

    override fun hasPermission(permission: String): Boolean {
        return permissionUser?.hasPermission(permission)?.asBoolean() == true
    }
}
