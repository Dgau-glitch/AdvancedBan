package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.getPunishment
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.CommandUtils.processReason
import me.leoko.advancedban.utils.PunishmentType
import me.leoko.advancedban.utils.commands.ListProcessor
import java.util.function.Consumer

/**
 * Stage 5: extracted additional handlers from legacy Command.java.
 */
object RevokeListHandlers {
    val changeReasonHandler: Consumer<Command.CommandInput> = Consumer { input ->
        val punishment = if (((input.primary ?: "").lowercase()).matches("[0-9]*".toRegex())) {
            val id = ((input.primary ?: "").lowercase()).toInt()
            input.next()
            PunishmentManager.get().getPunishment(id)
        } else {
            val type = PunishmentType.valueOf((input.primary ?: return@Consumer).uppercase())
            input.next()

            var target = input.primary ?: return@Consumer
            if (!target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$".toRegex())) {
                target = processName(input) ?: return@Consumer
            } else {
                input.next()
            }
            getPunishment(target, type)
        }

        val reason = processReason(input) ?: return@Consumer
        if (punishment != null) {
            punishment.updateReason(reason)
            MessageManager.sendMessage(input.sender, "ChangeReason.Done", true, "ID", punishment.id.toString())
        } else {
            MessageManager.sendMessage(input.sender, "ChangeReason.NotFound", true)
        }
    }

    val banListHandler: Consumer<Command.CommandInput> = Consumer { input ->
        ListProcessor(
            { PunishmentManager.get().getPunishments(me.leoko.advancedban.utils.SQLQuery.SELECT_ALL_PUNISHMENTS_LIMIT, 150) },
            "Banlist", false, false
        ).accept(input)
    }

    val historyHandler: Consumer<Command.CommandInput> = Consumer { input ->
        ListProcessor(
            { target -> PunishmentManager.get().getPunishments(target ?: return@ListProcessor emptyList(), null, false) },
            "History", true, true
        ).accept(input)
    }
}
