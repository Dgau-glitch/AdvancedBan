package me.leoko.advancedban.bukkit.utils

import java.util.Collections
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap

object OnlinePlayerNameCache {
    private val onlineNames = ConcurrentHashMap.newKeySet<String>()
    private val knownNames = ConcurrentHashMap.newKeySet<String>()

    fun replaceAll(currentNames: Iterable<String>) = replaceOnline(currentNames)

    fun replaceOnline(currentNames: Iterable<String>) {
        onlineNames.clear()
        currentNames.forEach(::add)
    }

    fun replaceKnown(knownPlayerNames: Iterable<String>) {
        knownNames.clear()
        knownPlayerNames.forEach(::addKnown)
        onlineNames.forEach(::addKnown)
    }

    fun add(name: String) {
        if (name.isBlank()) return
        onlineNames.add(name)
        addKnown(name)
    }

    fun addKnown(name: String?) {
        val normalized = name?.trim().orEmpty()
        if (normalized.isNotEmpty()) knownNames.add(normalized)
    }

    fun remove(name: String) {
        onlineNames.removeIf { it.equals(name, ignoreCase = true) }
    }

    fun snapshot(): List<String> = onlineSnapshot()

    fun onlineSnapshot(): List<String> = sortedSnapshot(onlineNames)

    fun knownSnapshot(): List<String> = sortedSnapshot(knownNames + onlineNames)

    fun resolveExact(input: String): String? = knownNames.firstOrNull { it.equals(input, ignoreCase = true) }
        ?: onlineNames.firstOrNull { it.equals(input, ignoreCase = true) }

    private fun sortedSnapshot(names: Iterable<String>): List<String> {
        val sorted = TreeSet<String>(String.CASE_INSENSITIVE_ORDER)
        names.asSequence().map(String::trim).filter(String::isNotEmpty).forEach(sorted::add)
        return Collections.unmodifiableList(sorted.toList())
    }
}
