package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.PunishmentType
import me.leoko.advancedban.utils.commands.PunishmentProcessor
import java.util.function.Consumer

/**
 * Stage 6: extracted punishment-family handlers from legacy Command.java.
 */
object PunishmentCommandHandlers {
    fun processor(type: PunishmentType): Consumer<Command.CommandInput> = Consumer { input ->
        PunishmentProcessor(type).accept(input)
    }

    val kickHandler: Consumer<Command.CommandInput> = Consumer { input ->
        if (!Universal.get().methods.isOnline((input.primary ?: "").lowercase())) {
            MessageManager.sendMessage(input.sender, "Kick.NotOnline", true, "NAME", input.primary)
            return@Consumer
        }
        PunishmentProcessor(PunishmentType.KICK).accept(input)
    }
}
