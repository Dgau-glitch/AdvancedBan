package me.leoko.advancedban.utils.commands

import me.leoko.advancedban.Universal
import me.leoko.advancedban.manager.MessageManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.CommandUtils.processName
import me.leoko.advancedban.utils.Punishment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.function.Consumer
import java.util.function.Function

class ListProcessor(
    private val listSupplier: Function<String?, List<Punishment>>,
    private val config: String,
    private val history: Boolean,
    private val hasTarget: Boolean
) : Consumer<Command.CommandInput> {
    override fun accept(input: Command.CommandInput) {
        var target: String? = null
        var name = "invalid"
        if (hasTarget) {
            target = input.primary
            name = target
            if (!target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$".toRegex())) {
                target = processName(input)
                if (target == null) return
            } else {
                input.next()
            }
        }

        val mi = Universal.get().methods
        val punishments = listSupplier.apply(target).toMutableList()
        if (punishments.isEmpty()) {
            MessageManager.sendMessage(input.sender, "$config.NoEntries", true, "NAME", name)
            return
        }

        punishments.removeIf { punishment ->
            val expired = punishment.isExpired() && !history
            if (expired) punishment.delete()
            expired
        }

        val page = if (input.hasNext()) input.primary.toInt() else 1
        if (punishments.size / 5.0 + 1 <= page) {
            MessageManager.sendMessage(input.sender, "$config.OutOfIndex", true, "PAGE", page.toString())
            return
        }

        val prefix = MessageManager.getMessage("General.Prefix")
        val header = MessageManager.getLayout(mi.getMessages(), "$config.Header", "PREFIX", prefix, "NAME", name)
        header.forEach { line -> mi.sendMessage(input.sender, line) }

        val format = SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"))
        for (i in (page - 1) * 5 until page * 5) {
            if (punishments.size <= i) break
            val punishment = punishments[i]
            val nameOrIp = if (punishment.type.isIpOrientated()) "${punishment.name} / ${punishment.uuid}" else punishment.name
            val entryLayout = MessageManager.getLayout(
                mi.getMessages(), "$config.Entry",
                "PREFIX", prefix,
                "NAME", nameOrIp,
                "DURATION", punishment.getDuration(history),
                "OPERATOR", punishment.operator,
                "REASON", punishment.getReason(),
                "TYPE", punishment.type.getName(),
                "ID", punishment.id.toString(),
                "DATE", format.format(Date(punishment.start))
            )
            entryLayout.forEach { line -> mi.sendMessage(input.sender, line) }
        }

        MessageManager.sendMessage(
            input.sender, "$config.Footer", false,
            "CURRENT_PAGE", page.toString(),
            "TOTAL_PAGES", (punishments.size / 5 + if (punishments.size % 5 != 0) 1 else 0).toString(),
            "COUNT", punishments.size.toString()
        )
        if (punishments.size / 5.0 + 1 > page + 1) {
            MessageManager.sendMessage(
                input.sender, "$config.PageFooter", false,
                "NEXT_PAGE", (page + 1).toString(), "NAME", name
            )
        }
    }
}
