package me.leoko.advancedban.bukkit.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object TextComponents {
    private val legacySerializer: LegacyComponentSerializer = LegacyComponentSerializer.legacySection()
    private val plainSerializer: PlainTextComponentSerializer = PlainTextComponentSerializer.plainText()

    fun legacy(text: String): Component = legacySerializer.deserialize(text)

    fun stripLegacy(text: String): String = plainSerializer.serialize(legacy(text))
}
