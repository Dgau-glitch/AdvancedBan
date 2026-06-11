package me.leoko.advancedban.bukkit.event

import me.leoko.advancedban.utils.Punishment
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PunishmentEvent(val punishment: Punishment) : Event(false) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}
