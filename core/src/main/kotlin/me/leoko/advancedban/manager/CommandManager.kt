package me.leoko.advancedban.manager

import me.leoko.advancedban.Universal
import me.leoko.advancedban.utils.Command

class CommandManager {
    fun onCommand(sender: Any, cmd: String, args: Array<String>) {
        Universal.get().methods.runAsync {
            val command = Command.getByName(cmd) ?: return@runAsync

            val permission = command.permission
            if (permission != null && !Universal.get().hasPerms(sender, permission)) {
                MessageManager.sendMessage(sender, "General.NoPerms", true)
                return@runAsync
            }

            if (!command.validateArguments(args)) {
                MessageManager.sendMessage(sender, command.usagePath, true)
                return@runAsync
            }

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
