package me.leoko.advancedban.utils.commands

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.TimeManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.getPunishment
import me.leoko.advancedban.utils.CommandUtils.processIP
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.CommandUtils.processReason
import me.leoko.advancedban.utils.Permissionable
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.PunishmentType
import java.util.function.Consumer
import java.util.function.Function

class PunishmentProcessor(private val type: PunishmentType) : Consumer<Command.CommandInput> {
    override fun accept(input: Command.CommandInput) {
        val silent = processTag(input, "-s")
        val name = input.primary

        val target = if (type.isIpOrientated()) processIP(input) else processName(input)
        if (target == null) return
        if (processExempt(name, target, input.sender, type)) return

        var end = -1L
        var timeTemplate = ""
        if (type.isTemp()) {
            val calculation = processTime(input, target, type) ?: return
            end = calculation.time
            if (calculation.template != null) timeTemplate = calculation.template!!
        }

        var reason: String? = processReason(input) ?: return
        if (reason?.isEmpty() == true) reason = null

        val mi: MethodInterface = Universal.get().methods
        val operator = mi.getName(input.sender)
        replaceExistingPunishment(target, type, operator)
        Punishment.create(name, target, reason, operator, type, end, timeTemplate, silent)
        MessageManager.sendMessage(input.sender, "${type.getBasic().getName()}.Done", true, "NAME", name)
    }

    private fun processTime(input: Command.CommandInput, uuid: String, type: PunishmentType): TimeCalculation? {
        val time = input.primary
        input.next()
        val mi: MethodInterface = Universal.get().methods
        if (time.matches("#.+".toRegex())) {
            val layout = time.substring(1)
            if (!mi.contains(mi.getLayouts(), "Time.$layout")) {
                MessageManager.sendMessage(input.sender, "General.LayoutNotFound", true, "NAME", layout)
                return null
            }
            val i = PunishmentManager.get().getCalculationLevel(uuid, layout)
            val timeLayout = mi.getStringList(mi.getLayouts(), "Time.$layout")
            val timeName = timeLayout[minOf(i, timeLayout.size - 1)]
            if (timeName.equals("perma", ignoreCase = true)) return TimeCalculation(layout, -1L)
            return TimeCalculation(layout, TimeManager.getTime() + TimeManager.toMilliSec(timeName))
        }

        val toAdd = TimeManager.toMilliSec(time)
        if (!Universal.get().hasPerms(input.sender, "ab.${type.getName()}.dur.max")) {
            var max = -1L
            for (i in 10 downTo 1) {
                if (Universal.get().hasPerms(input.sender, "ab.${type.getName()}.dur.$i")
                    && mi.contains(mi.getConfig(), "TempPerms.$i")
                ) {
                    max = mi.getLong(mi.getConfig(), "TempPerms.$i")!! * 1000
                    break
                }
            }
            if (max != -1L && toAdd > max) {
                MessageManager.sendMessage(input.sender, "${type.getName()}.MaxDuration", true, "MAX", (max / 1000).toString())
                return null
            }
        }
        return TimeCalculation(null, TimeManager.getTime() + toAdd)
    }

    private fun processExempt(name: String, target: String, sender: Any, type: PunishmentType): Boolean {
        val mi: MethodInterface = Universal.get().methods
        val dataName = name.lowercase()
        val exempt = if (mi.isOnline(dataName)) {
            val onlineTarget = mi.getPlayer(dataName)
            canNotPunish({ perms -> mi.hasPerms(sender, perms) }, { perms -> mi.hasPerms(onlineTarget!!, perms) }, type.getName())
        } else {
            val offlinePermissionPlayer: Permissionable = mi.getOfflinePermissionPlayer(name)
            Universal.get().isExemptPlayer(dataName) ||
                canNotPunish({ perms -> mi.hasPerms(sender, perms) }, offlinePermissionPlayer::hasPermission, type.getName())
        }

        if (exempt) {
            MessageManager.sendMessage(sender, "${type.getBasic().getName()}.Exempt", true, "NAME", name)
            return true
        }
        return false
    }

    private fun processTag(input: Command.CommandInput, tag: String): Boolean {
        val args = input.args
        for (i in args.indices) {
            if (i >= 4) break
            if (tag.equals(args[i], ignoreCase = true)) {
                input.removeArgument(i)
                return true
            }
        }
        return false
    }

    private fun alreadyPunished(target: String, type: PunishmentType): Boolean {
        return (type.getBasic() == PunishmentType.MUTE && PunishmentManager.get().isMuted(target)) ||
            (type.getBasic() == PunishmentType.BAN && PunishmentManager.get().isBanned(target))
    }

    private fun replaceExistingPunishment(target: String, type: PunishmentType, operator: String) {
        if (!alreadyPunished(target, type)) return
        val activePunishment = getPunishment(target, type)
        activePunishment?.delete(operator, false, true)
    }

    private data class TimeCalculation(val template: String?, val time: Long)

    companion object {
        @JvmStatic
        fun canNotPunish(
            operatorHasPerms: Function<String, Boolean>,
            targetHasPerms: Function<String, Boolean>,
            path: String
        ): Boolean {
            val perms = "ab.$path.exempt"
            if (targetHasPerms.apply(perms)) return true
            val targetLevel = permissionLevel(targetHasPerms, perms)
            return targetLevel != 0 && permissionLevel(operatorHasPerms, perms) <= targetLevel
        }

        private fun permissionLevel(hasPerms: Function<String, Boolean>, permission: String): Int {
            for (i in 10 downTo 1) if (hasPerms.apply("$permission.$i")) return i
            return 0
        }
    }
}
