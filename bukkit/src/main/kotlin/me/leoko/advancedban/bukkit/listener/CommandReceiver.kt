package me.leoko.advancedban.bukkit.listener

import me.leoko.advancedban.manager.CommandManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CommandReceiver private constructor() : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            args[0] = Bukkit.getPlayer(args[0])?.name ?: args[0]
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
