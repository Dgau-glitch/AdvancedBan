package me.leoko.advancedban.utils.commandspec

class CommandInput(val sender: Any, var args: Array<String>) {
    fun primary(): String? = args.firstOrNull()
    fun primaryData(): String = primary()?.lowercase() ?: ""
    fun removeArgument(index: Int) {
        args = args.filterIndexed { i, _ -> i != index }.toTypedArray()
    }
    fun next() {
        if (args.isNotEmpty()) args = args.copyOfRange(1, args.size)
    }
    fun hasNext(): Boolean = args.isNotEmpty()
}
