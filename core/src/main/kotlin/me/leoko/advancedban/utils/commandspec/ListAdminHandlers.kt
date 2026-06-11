package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.UUIDManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.processName
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.function.Consumer

/**
 * Stage 3: extracted selected list/admin handlers from legacy Command.java.
 */
object ListAdminHandlers {
    val checkHandler: Consumer<Command.CommandInput> = Consumer { input ->
        val name = input.getPrimary()
        val uuid = processName(input) ?: return@Consumer

        val ip = Universal.get().ips.getOrDefault(name.lowercase(), "none cashed")
        val loc = Universal.get().methods.getFromUrlJson("http://ip-api.com/json/$ip", "country")
        val mute = PunishmentManager.get().getMute(uuid)
        val ban = PunishmentManager.get().getBan(uuid)

        val cached = MessageManager.getMessage("Check.Cached", false)
        val notCached = MessageManager.getMessage("Check.NotCached", false)

        val nameCached = PunishmentManager.get().isCached(name.lowercase())
        val ipCached = PunishmentManager.get().isCached(ip)
        val uuidCached = PunishmentManager.get().isCached(uuid)

        val sender = input.getSender()
        MessageManager.sendMessage(sender, "Check.Header", true, "NAME", name, "CACHED", if (nameCached) cached else notCached)
        MessageManager.sendMessage(sender, "Check.UUID", false, "UUID", uuid, "CACHED", if (uuidCached) cached else notCached)
        if (Universal.get().hasPerms(sender, "ab.check.ip")) {
            MessageManager.sendMessage(sender, "Check.IP", false, "IP", ip, "CACHED", if (ipCached) cached else notCached)
        }
        MessageManager.sendMessage(sender, "Check.Geo", false, "LOCATION", loc ?: "failed!")
        MessageManager.sendMessage(sender, "Check.Mute", false, "DURATION", if (mute == null) "§anone" else if (mute.type.isTemp()) "§e" + mute.getDuration(false) else "§cperma")
        if (mute != null) MessageManager.sendMessage(sender, "Check.MuteReason", false, "REASON", mute.getReason())
        MessageManager.sendMessage(sender, "Check.Ban", false, "DURATION", if (ban == null) "§anone" else if (ban.type.isTemp()) "§e" + ban.getDuration(false) else "§cperma")
        if (ban != null) MessageManager.sendMessage(sender, "Check.BanReason", false, "REASON", ban.getReason())
        MessageManager.sendMessage(sender, "Check.Warn", false, "COUNT", PunishmentManager.get().getCurrentWarns(uuid).toString())
        MessageManager.sendMessage(sender, "Check.Note", false, "COUNT", PunishmentManager.get().getCurrentNotes(uuid).toString())
    }

    val systemPreferencesHandler: Consumer<Command.CommandInput> = Consumer { input ->
        val mi = Universal.get().methods
        val calendar: Calendar = GregorianCalendar()
        val sender = input.getSender()
        mi.sendMessage(sender, "§c§lAdvancedBan v2 §cSystemPrefs")
        mi.sendMessage(sender, "§cServer-Time §8» §7${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")
        mi.sendMessage(sender, "§cYour UUID (Intern) §8» §7${mi.getInternUUID(sender)}")
        if (input.hasNext()) {
            val target = (input.getPrimary() ?: "").lowercase()
            mi.sendMessage(sender, "§c$target's UUID (Intern) §8» §7${mi.getInternUUID(target)}")
            mi.sendMessage(sender, "§c$target's UUID (Fetched) §8» §7${UUIDManager.get().getUUID(target)}")
        }
    }
}
