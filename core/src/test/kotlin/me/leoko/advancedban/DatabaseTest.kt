package me.leoko.advancedban

import me.leoko.advancedban.manager.TimeManager
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.PunishmentType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.DriverManager

class DatabaseTest {
    companion object {
        @TempDir
        @JvmField
        var dataFolder: File? = null

        @JvmStatic
        @BeforeAll
        fun setupUniversal() {
            val folder = requireNotNull(dataFolder)
            createLegacyDatabase(folder)
            CoreTestSupport.setupUniversal(folder)
        }

        @JvmStatic
        @AfterAll
        fun shutdownUniversal() {
            CoreTestSupport.shutdownUniversal()
        }

        private fun createLegacyDatabase(folder: File) {
            File(folder, "data").mkdirs()
            Class.forName("org.hsqldb.jdbc.JDBCDriver")
            DriverManager.getConnection("jdbc:hsqldb:file:${folder.path}/data/storage;hsqldb.lock_file=false", "SA", "").use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute("CREATE TABLE Punishments (id INTEGER IDENTITY PRIMARY KEY,name VARCHAR(16),uuid VARCHAR(35),reason VARCHAR(100),operator VARCHAR(16),punishmentType VARCHAR(16),start BIGINT,end BIGINT,calculation VARCHAR(50))")
                    statement.execute("CREATE TABLE PunishmentHistory (id INTEGER IDENTITY PRIMARY KEY,name VARCHAR(16),uuid VARCHAR(35),reason VARCHAR(100),operator VARCHAR(16),punishmentType VARCHAR(16),start BIGINT,end BIGINT,calculation VARCHAR(50))")
                    statement.execute("SHUTDOWN")
                }
            }
        }
    }

    @Test
    fun shouldAutomaticallyDetectDatabaseType() {
        // DatabaseManagement behaviour changed in 2.1.9
    }

    @Test
    fun shouldMigrateLegacyReasonColumnsForLongReasons() {
        val longReason = "Жизни закончились | через 8 часов у вас снова будет 1 жизнь | купи жизни на bloktopiya.easydonate.ru" +
            " | дополнительная проверка длинной причины"
        val punishment = Punishment("LongReason", "long-reason", longReason, "JUnit5", PunishmentType.TEMP_BAN, TimeManager.getTime(), TimeManager.getTime() + 1_000, "", -1)
        punishment.create(true)
        assertNotEquals(-1, punishment.id)
    }
}
