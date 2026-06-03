package me.leoko.advancedban.bukkit.utils

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object OnlinePlayerNameCache {
    private val names = ConcurrentHashMap.newKeySet<String>()

    fun replaceAll(currentNames: Iterable<String>) {
        names.clear()
        currentNames.forEach(::add)
    }

    fun add(name: String) {
        names.add(name)
    }

    fun remove(name: String) {
        names.removeIf { it.equals(name, ignoreCase = true) }
    }

    fun snapshot(): List<String> = Collections.unmodifiableList(names.sortedWith(String.CASE_INSENSITIVE_ORDER))

    fun resolveExact(input: String): String? = names.firstOrNull { it.equals(input, ignoreCase = true) }
}
