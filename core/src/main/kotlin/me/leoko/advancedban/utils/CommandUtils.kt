package me.leoko.advancedban.utils

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager

object CommandUtils {
    @JvmStatic
    fun getPunishment(target: String, type: PunishmentType): Punishment? {
        return if (type == PunishmentType.MUTE) PunishmentManager.get().getMute(target) else PunishmentManager.get().getBan(target)
    }

    @JvmStatic
    fun processName(input: Command.CommandInput): String? {
        val name = input.primary
        input.next()
        val uuid = UUIDManager.get().getUUID(name.lowercase())
        if (uuid == null) {
            MessageManager.sendMessage(input.sender, "General.FailedFetch", true, "NAME", name)
        }
        return uuid
    }

    @JvmStatic
    fun processIP(input: Command.CommandInput): String? {
        val name = input.primaryData
        input.next()
        if (name.matches(Regex("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"))) {
            return name
        }

        val ip = Universal.get().ips[name]
        if (ip == null) {
            MessageManager.sendMessage(input.sender, "Ipban.IpNotCashed", true, "NAME", name)
        }
        return ip
    }

    @JvmStatic
    fun processReason(input: Command.CommandInput): String? {
        val mi = Universal.get().methods
        val reason = input.args.joinToString(" ")
        if (reason.matches(Regex("[~@].+")) && !mi.contains(mi.layouts, "Message." + input.primary.substring(1))) {
            MessageManager.sendMessage(input.sender, "General.LayoutNotFound", true, "NAME", input.primary.substring(1))
            return null
        }
        return reason
    }
}
