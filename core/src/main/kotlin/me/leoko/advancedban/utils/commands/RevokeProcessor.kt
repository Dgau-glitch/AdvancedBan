package me.leoko.advancedban.utils.commands

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.getPunishment
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.PunishmentType
import java.util.function.Consumer

class RevokeProcessor(private val type: PunishmentType) : Consumer<Command.CommandInput> {
    override fun accept(input: Command.CommandInput) {
        val name = input.primary
        var target: String? = name
        if (!target!!.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$".toRegex())) {
            target = processName(input)
            if (target == null) return
        }

        val punishment = getPunishment(target, type)
        if (punishment == null) {
            MessageManager.sendMessage(input.sender, "Un${type.name}.NotPunished", true, "NAME", name)
            return
        }

        val operator = Universal.get().methods.getName(input.sender)
        punishment.delete(operator, false, true)
        MessageManager.sendMessage(input.sender, "Un${type.name}.Done", true, "NAME", name)
    }
}
