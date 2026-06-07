package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.PunishmentType
import me.leoko.advancedban.utils.commands.RevokeByIdProcessor
import java.util.function.Consumer

/**
 * Stage 3: extracted revoke-related handler lambdas from legacy Command.java
 * without switching runtime wiring yet.
 */
object RevokeCommandHandlers {
    val unWarnClearOrId: Consumer<Command.CommandInput> = Consumer { input ->
        val confSection = PunishmentType.WARNING.getName()
        val primary = input.getPrimary()
        if (primary.lowercase() == "clear" || !primary.matches("[0-9]+".toRegex())) {
            if (primary.equals("clear", ignoreCase = true)) input.next()
            val name = input.getPrimary()
            val uuid = processName(input) ?: return@Consumer

            val punishments = PunishmentManager.get().getWarns(uuid)
            if (punishments.isEmpty()) {
                MessageManager.sendMessage(input.getSender(), "Un$confSection.Clear.Empty", true, "NAME", name)
                return@Consumer
            }

            val operator = Universal.get().methods.getName(input.getSender())
            for (punishment in punishments) {
                punishment.delete(operator, true, true)
            }
            MessageManager.sendMessage(input.getSender(), "Un$confSection.Clear.Done", true, "COUNT", punishments.size.toString())
        } else {
            RevokeByIdProcessor("Un$confSection", PunishmentManager.get()::getWarn).accept(input)
        }
    }

    val unNoteClearOrId: Consumer<Command.CommandInput> = Consumer { input ->
        val confSection = PunishmentType.NOTE.getName()
        if ((input.getPrimary() ?: "").lowercase() == "clear") {
            input.next()
            val name = input.getPrimary()
            val uuid = processName(input) ?: return@Consumer

            val punishments = PunishmentManager.get().getNotes(uuid)
            if (punishments.isEmpty()) {
                MessageManager.sendMessage(input.getSender(), "Un$confSection.Clear.Empty", true, "NAME", name)
                return@Consumer
            }

            val operator = Universal.get().methods.getName(input.getSender())
            for (punishment in punishments) {
                punishment.delete(operator, true, true)
            }
            MessageManager.sendMessage(input.getSender(), "Un$confSection.Clear.Done", true, "COUNT", punishments.size.toString())
        } else {
            RevokeByIdProcessor("Un$confSection", PunishmentManager.get()::getNote).accept(input)
        }
    }
}
