package me.leoko.advancedban.utils.commands

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.Punishment
import java.util.function.Consumer
import java.util.function.Function

class RevokeByIdProcessor(
    private val path: String,
    private val resolver: Function<Int, Punishment?>
) : Consumer<Command.CommandInput> {
    override fun accept(input: Command.CommandInput) {
        val id = input.getPrimary().toInt()
        val punishment = resolver.apply(id)
        if (punishment == null) {
            MessageManager.sendMessage(input.getSender(), "$path.NotFound", true, "ID", id.toString())
            return
        }

        val operator = Universal.get().methods.getName(input.getSender())
        punishment.delete(operator, false, true)
        MessageManager.sendMessage(input.getSender(), "$path.Done", true, "ID", id.toString())
    }
}
