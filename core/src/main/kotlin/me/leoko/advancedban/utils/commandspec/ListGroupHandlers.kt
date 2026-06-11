package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.PunishmentType
import me.leoko.advancedban.utils.commands.ListProcessor
import java.util.function.Consumer

/**
 * Stage 4: extracted handlers for WARNS/NOTES branches from legacy Command.java.
 */
object ListGroupHandlers {
    val warnsHandler: Consumer<Command.CommandInput> = Consumer { input ->
        if (input.hasNext() && !(input.getPrimary() ?: "").matches("[1-9][0-9]*".toRegex())) {
            if (!Universal.get().hasPerms(input.getSender(), "ab.warns.other")) {
                MessageManager.sendMessage(input.getSender(), "General.NoPerms", true)
                return@Consumer
            }

            ListProcessor(
                { target -> PunishmentManager.get().getPunishments(target ?: return@ListProcessor emptyList(), PunishmentType.WARNING, true) },
                "Warns", false, true
            ).accept(input)
        } else {
            if (!Universal.get().hasPerms(input.getSender(), "ab.warns.own")) {
                MessageManager.sendMessage(input.getSender(), "General.NoPerms", true)
                return@Consumer
            }

            val name = Universal.get().methods.getName(input.getSender())
            val identifier = me.leoko.advancedban.manager.UUIDManager.get().getUUID(name)
            ListProcessor(
                { PunishmentManager.get().getPunishments(identifier ?: return@ListProcessor emptyList(), PunishmentType.WARNING, true) },
                "WarnsOwn", false, false
            ).accept(input)
        }
    }

    val notesHandler: Consumer<Command.CommandInput> = Consumer { input ->
        if (input.hasNext() && !(input.getPrimary() ?: "").matches("[1-9][0-9]*".toRegex())) {
            if (!Universal.get().hasPerms(input.getSender(), "ab.notes.other")) {
                MessageManager.sendMessage(input.getSender(), "General.NoPerms", true)
                return@Consumer
            }

            ListProcessor(
                { target -> PunishmentManager.get().getPunishments(target ?: return@ListProcessor emptyList(), PunishmentType.NOTE, true) },
                "Notes", false, true
            ).accept(input)
        } else {
            if (!Universal.get().hasPerms(input.getSender(), "ab.notes.own")) {
                MessageManager.sendMessage(input.getSender(), "General.NoPerms", true)
                return@Consumer
            }

            val name = Universal.get().methods.getName(input.getSender())
            val identifier = me.leoko.advancedban.manager.UUIDManager.get().getUUID(name)
            ListProcessor(
                { PunishmentManager.get().getPunishments(identifier ?: return@ListProcessor emptyList(), PunishmentType.NOTE, true) },
                "NotesOwn", false, false
            ).accept(input)
        }
    }
}
