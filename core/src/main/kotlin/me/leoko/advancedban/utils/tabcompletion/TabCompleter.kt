package me.leoko.advancedban.utils.tabcompletion

fun interface TabCompleter {
    fun onTabComplete(user: Any, args: Array<String>): List<String>
}
