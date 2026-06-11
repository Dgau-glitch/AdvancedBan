package me.leoko.advancedban.manager

import me.leoko.advancedban.MethodInterface
import me.leoko.advancedban.Universal
import java.util.Collections

object MessageManager {
    private fun mi(): MethodInterface = Universal.get().methods

    @JvmStatic
    fun getMessage(path: String, vararg parameters: String): String {
        val mi = mi()
        val raw = mi.getString(mi.getMessages(), path)
        return if (raw == null) {
            println(
                "!! Message-Error!\nIn order to solve the problem please:" +
                    "\n  - Check the Message.yml-File for any missing or double \" or '" +
                    "\n  - Visit yamllint.com to  validate your Message.yml" +
                    "\n  - Delete the message file and restart the server"
            )
            "Failed! See console for details!"
        } else {
            replace(raw, *parameters).replace('&', '§')
        }
    }

    @JvmStatic
    fun getMessage(path: String, prefix: Boolean, vararg parameters: String): String {
        val mi = mi()
        val prefixStr = if (prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false)) "${getMessage("General.Prefix")} " else ""
        return prefixStr + getMessage(path, *parameters)
    }

    @JvmStatic
    fun getLayout(file: Any, path: String, vararg parameters: String): List<String> {
        val mi = mi()
        if (mi.contains(file, path)) {
            return mi.getStringList(file, path).map { replace(it, *parameters).replace('&', '§') }
        }

        val fileName = mi.getFileName(file)
        println(
            "!! Message-Error in $fileName!\nIn order to solve the problem please:" +
                "\n  - Check the $fileName-File for any missing or double \" or '" +
                "\n  - Visit yamllint.com to  validate your $fileName" +
                "\n  - Delete the message file and restart the server"
        )
        return Collections.singletonList("Failed! See console for details!")
    }

    @JvmStatic
    fun sendMessage(receiver: Any, path: String, prefix: Boolean, vararg parameters: String) {
        val mi = mi()
        val message = getMessage(path, *parameters)
        if (message.isEmpty()) return
        val prefixString = if (prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false)) "${getMessage("General.Prefix")} " else ""
        mi.sendMessage(receiver, prefixString + message)
    }

    private fun replace(str: String, vararg parameters: String): String {
        var updated = str
        var i = 0
        while (i < parameters.size - 1) {
            updated = updated.replace("%${parameters[i]}%", parameters[i + 1])
            i += 2
        }
        return updated
    }
}
