package me.leoko.advancedban

import java.io.File

object CoreTestSupport {
    fun setupUniversal(dataFolder: File) {
        Universal.get().setup(TestMethods(dataFolder))
    }

    fun shutdownUniversal() {
        Universal.get().shutdown()
    }
}
