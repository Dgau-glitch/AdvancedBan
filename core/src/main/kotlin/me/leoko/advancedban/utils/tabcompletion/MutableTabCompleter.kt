package me.leoko.advancedban.utils.tabcompletion

fun interface MutableTabCompleter : TabCompleter {
    override fun onTabComplete(user: Any, args: Array<String>): ArrayList<String>

    companion object {
        @JvmStatic
        fun <T> list(vararg elements: T): ArrayList<T> = arrayListOf(*elements)
    }
}
