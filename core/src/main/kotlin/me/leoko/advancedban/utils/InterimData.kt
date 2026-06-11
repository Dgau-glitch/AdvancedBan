package me.leoko.advancedban.utils

import me.leoko.advancedban.manager.PunishmentManager

data class InterimData(
    val uuid: String,
    val name: String,
    val ip: String,
    val punishments: Set<Punishment>,
    val history: Set<Punishment>
) {
    fun getBan(): Punishment? = punishments.firstOrNull { it.type.getBasic() == PunishmentType.BAN && !it.isExpired() }

    fun accept() {
        PunishmentManager.get().getLoadedPunishments(false).addAll(punishments)
        PunishmentManager.get().loadedHistory.addAll(history)
        PunishmentManager.get().setCached(this)
    }
}
