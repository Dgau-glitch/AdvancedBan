package me.leoko.advancedban

import me.leoko.advancedban.manager.CommandManager
import me.leoko.advancedban.manager.DatabaseManager
import me.leoko.advancedban.manager.PunishmentManager
import me.leoko.advancedban.manager.TimeManager
import me.leoko.advancedban.utils.Command
import me.leoko.advancedban.utils.Punishment
import me.leoko.advancedban.utils.PunishmentType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class PunishmentTest {
    companion object {
        @TempDir
        @JvmField
        var dataFolder: File? = null

        @JvmStatic
        @BeforeAll
        fun setupUniversal() {
            CoreTestSupport.setupUniversal(requireNotNull(dataFolder))
        }

        @JvmStatic
        @AfterAll
        fun shutdownUniversal() {
            CoreTestSupport.shutdownUniversal()
        }
    }

    @Test
    fun shouldCreatePunishmentForGivenUserWithGivenReason() {
        assertFalse(PunishmentManager.get().isBanned("leoko"))
        CommandManager.get().onCommand("UnitTest", "ban", arrayOf("Leoko", "Doing", "some", "unit-testing"))
        assertTrue(PunishmentManager.get().isBanned("leoko"))
        assertEquals("Doing some unit-testing", PunishmentManager.get().getBan("leoko")!!.getReason())
    }

    @Test
    fun shouldKeepPunishmentAfterRestart() {
        val punishment = Punishment("leoko", "leoko", "Persistence test", "JUnit5", PunishmentType.MUTE, TimeManager.getTime(), -1, "", -1)
        punishment.create()
        val id = punishment.id
        DatabaseManager.get().shutdown()
        DatabaseManager.get().setup(false)
        val restored = PunishmentManager.get().getPunishment(id)
        assertNotNull(restored)
        assertEquals("Persistence test", restored!!.getReason())
    }

    @Test
    fun shouldWorkWithCachedAndNotCachedPunishments() {
        val punishment = Punishment("cache", "cache", "Cache test", "JUnit5", PunishmentType.BAN, TimeManager.getTime(), -1, "", -1)
        punishment.create()
        assertFalse(PunishmentManager.get().getLoadedPunishments(false).contains(punishment))
        assertTrue(PunishmentManager.get().isBanned("cache"))
        PunishmentManager.get().load("cache", "cache", "127.0.0.1")!!.accept()
        assertTrue(PunishmentManager.get().getLoadedPunishments(false).any { it.uuid == "cache" })
        assertTrue(PunishmentManager.get().isBanned("cache"))
    }


    @Test
    fun shouldHandleConcurrentPunishmentCreateLoadAndRevokeFlow() {
        val executor = Executors.newFixedThreadPool(4)
        try {
            val tasks = (1..4).map { index ->
                Callable {
                    val target = "concurrent$index"
                    val punishment = Punishment(target, target, "Concurrent test $index", "JUnit5", PunishmentType.BAN, TimeManager.getTime(), -1, "", -1)
                    punishment.create()
                    assertTrue(PunishmentManager.get().isBanned(target))

                    val loaded = PunishmentManager.get().load(target, target, "127.0.0.$index")
                    assertNotNull(loaded)
                    loaded!!.accept()
                    assertTrue(PunishmentManager.get().getLoadedPunishments(false).any { it.uuid == target })

                    punishment.delete("JUnit5", false, true)
                    assertFalse(PunishmentManager.get().getLoadedPunishments(false).any { it.uuid == target })
                }
            }
            executor.invokeAll(tasks).forEach { it.get() }
        } finally {
            executor.shutdownNow()
        }
    }


    @Test
    fun shouldSuggestKnownAndCurrentlyPunishedTargetsForTabs() {
        val ban = Punishment("TabBanTarget", "tab-ban-target", "Tab test", "JUnit5", PunishmentType.BAN, TimeManager.getTime(), -1, "", -1)
        ban.create(true)
        val mute = Punishment("TabMuteTarget", "tab-mute-target", "Tab test", "JUnit5", PunishmentType.MUTE, TimeManager.getTime(), -1, "", -1)
        mute.create(true)
        val warn = Punishment("TabWarnTarget", "tab-warn-target", "Tab test", "JUnit5", PunishmentType.WARNING, TimeManager.getTime(), -1, "", -1)
        warn.create(true)

        assertTrue(Command.BAN.tabCompleter!!.onTabComplete("JUnit5", arrayOf("tabb")).contains("TabBanTarget"))
        assertTrue(Command.UN_BAN.tabCompleter!!.onTabComplete("JUnit5", arrayOf("tabb")).contains("TabBanTarget"))
        assertTrue(Command.UN_MUTE.tabCompleter!!.onTabComplete("JUnit5", arrayOf("tabm")).contains("TabMuteTarget"))

        val warnSuggestions = Command.UN_WARN.tabCompleter!!.onTabComplete("JUnit5", arrayOf("tabw"))
        assertTrue(warnSuggestions.contains("TabWarnTarget"))
        assertTrue(Command.UN_WARN.tabCompleter!!.onTabComplete("JUnit5", arrayOf(warn.id.toString())).contains(warn.id.toString()))
    }

    @Test
    fun shouldUseConfiguredDisplayNameMessagePathsForPunishmentCommands() {
        val messages = requireNotNull(javaClass.classLoader.getResource("Messages.yml")).readText()
        fun assertSectionExists(section: String) {
            assertTrue(Regex("(?m)^${Regex.escape(section)}:").containsMatchIn(messages), "Missing Messages.yml section $section")
        }

        PunishmentType.entries.forEach { type ->
            assertSectionExists(type.getName())
            assertEquals("${type.getName()}.Layout", type.getConfSection("Layout"))
            assertEquals("${type.getName()}.Notification", type.getConfSection("Notification"))
        }

        listOf(PunishmentType.BAN, PunishmentType.MUTE, PunishmentType.WARNING, PunishmentType.NOTE).forEach { type ->
            assertSectionExists("Un${type.getName()}")
            assertEquals("Un${type.getName()}.Done", type.getUndoConfSection("Done"))
            assertEquals("Un${type.getName()}.NotPunished", type.getUndoConfSection("NotPunished"))
        }

        assertEquals("UnBan.Done", PunishmentType.IP_BAN.getUndoConfSection("Done"))
        assertEquals("UnBan.NotPunished", PunishmentType.TEMP_IP_BAN.getUndoConfSection("NotPunished"))
    }

    @Test
    fun shouldBlockBasicCommandsIncludingColons() {
        val universal = Universal.get()
        val muteCommands = listOf("msg", "reply", "tell")
        assertTrue(universal.isMuteCommand("msg", muteCommands))
        assertTrue(universal.isMuteCommand("plugin:reply", muteCommands))
        assertFalse(universal.isMuteCommand("fly", muteCommands))
        assertFalse(universal.isMuteCommand("replyall", muteCommands))
    }

    @Test
    fun shouldBlockCommandsStartingWithMuteCommandWords() {
        val universal = Universal.get()
        val muteCommand = "party msg"
        assertTrue(universal.muteCommandMatches("party msg user hello".split(" ").toTypedArray(), muteCommand))
        assertTrue(universal.muteCommandMatches("party msg".split(" ").toTypedArray(), muteCommand))
        assertFalse(universal.muteCommandMatches("party".split(" ").toTypedArray(), muteCommand))
        assertFalse(universal.muteCommandMatches("party invite user".split(" ").toTypedArray(), muteCommand))
        assertFalse(universal.muteCommandMatches("party invite".split(" ").toTypedArray(), muteCommand))
        assertFalse(universal.muteCommandMatches("broadcast party msg".split(" ").toTypedArray(), muteCommand))
    }
}
