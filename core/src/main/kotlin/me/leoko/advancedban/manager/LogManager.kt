package me.leoko.advancedban.manager

import me.leoko.advancedban.Universal
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.GZIPOutputStream

class LogManager {
    private val logsFolder: File

    init {
        val universal = Universal.get()
        logsFolder = File(universal.methods.dataFolder, "logs")
        if (!logsFolder.exists()) logsFolder.mkdirs()
        checkLastLog(true)
        val files = logsFolder.listFiles() ?: emptyArray()
        for (file in files) {
            if (file.isFile && file.name.contains(".gz") &&
                (System.currentTimeMillis() - file.lastModified()) >= universal.methods.getInteger(universal.methods.config, "Log Purge Days") * 86400000L
            ) {
                file.delete()
            }
        }
    }

    fun checkLastLog(force: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val latestLog = File(logsFolder, "latest.log")
        if (!latestLog.exists()) return

        calendar.timeInMillis = latestLog.lastModified()
        if (day != calendar[Calendar.DAY_OF_MONTH] || force) {
            try {
                if (FileUtils.readLines(latestLog, Charsets.UTF_8).isEmpty()) return
                var fileN = 1
                while (File(logsFolder, "${sdf.format(latestLog.lastModified())}-$fileN.log.gz").exists()) fileN++
                gzipFile(Files.newInputStream(latestLog.toPath()), "$logsFolder/${sdf.format(latestLog.lastModified())}-$fileN.log.gz")
                latestLog.delete()
                latestLog.createNewFile()
            } catch (ex: IOException) {
                Logger.getLogger(LogManager::class.java.name).log(
                    Level.WARNING,
                    "An unexpected error has occurred while trying to compress the latest log file. {0}",
                    ex.message
                )
            }
        }
    }

    @Throws(IOException::class)
    private fun gzipFile(input: java.io.InputStream, to: String) {
        input.use { `in` ->
            GZIPOutputStream(FileOutputStream(to)).use { out ->
                val buffer = ByteArray(4096)
                while (true) {
                    val bytesRead = `in`.read(buffer)
                    if (bytesRead == -1) break
                    out.write(buffer, 0, bytesRead)
                }
            }
        }
    }
}
