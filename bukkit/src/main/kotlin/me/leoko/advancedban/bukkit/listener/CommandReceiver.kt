package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.manager.CommandManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CommandReceiver private constructor() : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            // Lookup-only normalization; command execution must schedule any later player mutation explicitly.
            val onlineTarget = Bukkit.getPlayer(args[0])
            args[0] = onlineTarget?.name ?: args[0]
        }
        CommandManager.get().onCommand(sender, command.name, args)
        return true
    }

    companion object {
        private val instance: CommandReceiver = CommandReceiver()

        @JvmStatic
        fun get(): CommandReceiver = instance
    }
}
