package me.leoko.advancedban.bungee.cloud

import java.util.UUID

interface CloudSupport {
    fun kick(uniqueID: UUID, reason: String)
}
