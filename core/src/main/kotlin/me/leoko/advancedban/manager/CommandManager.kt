package me.leoko.advancedban.manager

import me.leoko.advancedban.Universal
import me.leoko.advancedban.utils.Command

class CommandManager {
    fun onCommand(sender: Any, cmd: String, args: Array<String>) {
        val methods = Universal.get().methods
        val command = Command.getByName(cmd) ?: return

        val permission = command.permission
        if (permission != null && !Universal.get().hasPerms(sender, permission)) {
            MessageManager.sendMessage(sender, "General.NoPerms", true)
            return
        }

        if (!command.validateArguments(args)) {
            MessageManager.sendMessage(sender, command.usagePath, true)
            return
        }

        methods.runAsync {
            command.execute(sender, args)
        }
    }

    companion object {
        @Volatile
        private var instance: CommandManager? = null

        @JvmStatic
        @Synchronized
        fun get(): CommandManager {
            if (instance == null) {
                instance = CommandManager()
            }
            return instance!!
        }
    }
}
