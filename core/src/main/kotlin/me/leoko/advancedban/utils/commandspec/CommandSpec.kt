package me.leoko.advancedban.utils.commandspec

import me.leoko.advancedban.utils.tabcompletion.TabCompleter
import java.util.function.Consumer
import java.util.function.Predicate

data class CommandSpec(
    val key: String,
    val permission: String?,
    val syntaxValidator: Predicate<Array<String>>,
    val tabCompleter: TabCompleter?,
    val handler: Consumer<CommandInput>,
    val usagePath: String?,
    val names: List<String>
)
