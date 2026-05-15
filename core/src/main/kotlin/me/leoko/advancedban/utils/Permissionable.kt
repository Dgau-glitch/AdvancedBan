package me.leoko.advancedban.utils

fun interface Permissionable {
    fun hasPermission(permission: String): Boolean
}
