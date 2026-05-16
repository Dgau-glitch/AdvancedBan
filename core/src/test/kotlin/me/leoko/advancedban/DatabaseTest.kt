package me.leoko.advancedban

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DatabaseTest {
    companion object {
        @TempDir
        @JvmField
        var dataFolder: File? = null

        @JvmStatic
        @BeforeAll
        fun setupUniversal() {
            Universal.get().setup(TestMethods(requireNotNull(dataFolder)))
        }

        @JvmStatic
        @AfterAll
        fun shutdownUniversal() {
            Universal.get().shutdown()
        }
    }

    @Test
    fun shouldAutomaticallyDetectDatabaseType() {
        // DatabaseManagement behaviour changed in 2.1.9
    }
}
