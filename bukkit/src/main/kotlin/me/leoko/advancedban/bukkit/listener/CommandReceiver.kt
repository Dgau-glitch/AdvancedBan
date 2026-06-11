package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.bukkit.utils.OnlinePlayerNameCache
import me.leoko.advancedban.manager.CommandManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CommandReceiver private constructor() : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            // Cache-only normalization; command execution must schedule any later player mutation explicitly.
            args[0] = OnlinePlayerNameCache.resolveExact(args[0]) ?: args[0]
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
