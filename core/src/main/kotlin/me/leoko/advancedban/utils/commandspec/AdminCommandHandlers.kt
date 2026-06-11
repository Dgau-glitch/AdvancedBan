package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.Command
import java.util.function.Consumer

/**
 * Stage 4: extracted ADVANCED_BAN handler from legacy Command.java.
 */
object AdminCommandHandlers {
    val advancedBanHandler: Consumer<Command.CommandInput> = Consumer { input ->
        val mi = Universal.get().methods
        val sender = input.getSender()
        if (input.hasNext()) {
            val primary = (input.getPrimary() ?: "").lowercase()
            if (primary == "reload") {
                if (Universal.get().hasPerms(sender, "ab.reload")) {
                    mi.loadFiles()
                    mi.sendMessage(sender, "§a§lAdvancedBan §8§l» §7Reloaded!")
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true)
                }
                return@Consumer
            } else if (primary == "help") {
                if (Universal.get().hasPerms(sender, "ab.help")) {
                    mi.sendMessage(sender, "§8")
                    mi.sendMessage(sender, "§c§lAdvancedBan §7Command-Help")
                    mi.sendMessage(sender, "§8")
                    mi.sendMessage(sender, "§c/ban [Name] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Ban a user permanently")
                    mi.sendMessage(sender, "§c/banip [Name/IP] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Ban a user by IP")
                    mi.sendMessage(sender, "§c/tempban [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Ban a user temporary")
                    mi.sendMessage(sender, "§c/mute [Name] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Mute a user permanently")
                    mi.sendMessage(sender, "§c/tempmute [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Mute a user temporary")
                    mi.sendMessage(sender, "§c/warn [Name] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Warn a user permanently")
                    mi.sendMessage(sender, "§c/note [Name] [Note]")
                    mi.sendMessage(sender, "§8» §7Adds a note to a user")
                    mi.sendMessage(sender, "§c/tempwarn [Name] [Xmo/Xd/Xh/Xm/Xs/#TimeLayout] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Warn a user temporary")
                    mi.sendMessage(sender, "§c/kick [Name] [Reason/@Layout]")
                    mi.sendMessage(sender, "§8» §7Kick a user")
                    mi.sendMessage(sender, "§c/unban [Name/IP]")
                    mi.sendMessage(sender, "§8» §7Unban a user")
                    mi.sendMessage(sender, "§c/unmute [Name]")
                    mi.sendMessage(sender, "§8» §7Unmute a user")
                    mi.sendMessage(sender, "§c/unwarn [ID] or /unwarn clear [Name]")
                    mi.sendMessage(sender, "§8» §7Deletes a warn")
                    mi.sendMessage(sender, "§c/unnote [ID] or /unnote clear [Name]")
                    mi.sendMessage(sender, "§8» §7Deletes a note")
                    mi.sendMessage(sender, "§c/change-reason [ID or ban/mute USER] [New reason]")
                    mi.sendMessage(sender, "§8» §7Changes the reason of a punishment")
                    mi.sendMessage(sender, "§c/unpunish [ID]")
                    mi.sendMessage(sender, "§8» §7Deletes a punishment by ID")
                    mi.sendMessage(sender, "§c/banlist <Page>")
                    mi.sendMessage(sender, "§8» §7See all punishments")
                    mi.sendMessage(sender, "§c/history [Name/IP] <Page>")
                    mi.sendMessage(sender, "§8» §7See a users history")
                    mi.sendMessage(sender, "§c/warns [Name] <Page>")
                    mi.sendMessage(sender, "§8» §7See your or a users warnings")
                    mi.sendMessage(sender, "§c/notes [Name] <Page>")
                    mi.sendMessage(sender, "§8» §7See your or a users notes")
                    mi.sendMessage(sender, "§c/check [Name]")
                    mi.sendMessage(sender, "§8» §7Get all information about a user")
                    mi.sendMessage(sender, "§c/AdvancedBan <reload/help>")
                    mi.sendMessage(sender, "§8» §7Reloads the plugin or shows help page")
                    mi.sendMessage(sender, "§8")
                } else {
                    MessageManager.sendMessage(sender, "General.NoPerms", true)
                }
                return@Consumer
            }
        }

        mi.sendMessage(sender, "§8§l§m-=====§r §c§lAdvancedBan v2 §8§l§m=====-§r ")
        mi.sendMessage(sender, "  §cDev §8• §7Leoko")
        mi.sendMessage(sender, "  §cStatus §8• §a§oStable")
        mi.sendMessage(sender, "  §cVersion §8• §7${mi.getVersion()}")
        mi.sendMessage(sender, "  §cLicense §8• §7Public")
        mi.sendMessage(sender, "  §cStorage §8• §7" + (if (DatabaseManager.get().isUseMySQL) "MySQL (external)" else "HSQLDB (local)"))
        mi.sendMessage(sender, "  §cServer §8• §7" + (if (Universal.get().isBungee) "Bungeecord" else "Spigot/Bukkit"))
        if (Universal.get().isBungee) {
            mi.sendMessage(sender, "  §cRedisBungee §8• §7" + if (Universal.isRedis()) "true" else "false")
        }
        mi.sendMessage(sender, "  §cUUID-Mode §8• §7${UUIDManager.get().getMode()}")
        mi.sendMessage(sender, "  §cPrefix §8• §7" + (if (mi.getBoolean(mi.getConfig(), "Disable Prefix", false)) "" else MessageManager.getMessage("General.Prefix")))
        mi.sendMessage(sender, "§8§l§m-=========================-§r ")
    }
}
