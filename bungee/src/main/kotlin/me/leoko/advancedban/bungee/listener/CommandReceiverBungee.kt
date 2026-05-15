package me.leoko.advancedban.bungee.listener

import me.leoko.advancedban.bungee.BungeeMain
import me.leoko.advancedban.manager.CommandManager
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

class CommandReceiverBungee(name: String, permission: String?) : Command(name, permission) {
    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.isNotEmpty()) {
            args[0] = BungeeMain.get().proxy.getPlayer(args[0])?.name ?: args[0]
        }
        CommandManager.get().onCommand(sender, name, args)
    }
}
