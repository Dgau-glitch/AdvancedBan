package me.leoko.advancedban.utils.tabcompletion

class BasicTabCompleter(vararg firstLayerArguments: String) : CleanTabCompleter(
    MutableTabCompleter { _, args ->
        if (args.size == 1) MutableTabCompleter.list(*firstLayerArguments) else MutableTabCompleter.list()
    }
)
