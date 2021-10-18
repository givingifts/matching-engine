package gifts.givin.matching.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.PremiumNoMatchBehaviour
import gifts.givin.matching.common.stages.CleanupPremium
import gifts.givin.matching.insertMatch
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CleanupPremiumTest : StageTest() {
    @Test
    fun shouldNotCleanupNonPremium() {
        insertMatch(100, 100, "Test", matchIsPremium = false)
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
        CleanupPremium(logger) {}
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
    }

    @Test
    fun shouldNotCleanupUsersThatAreMatchingWithStandardUsers() {
        insertMatch(
            100,
            100,
            "Test",
            matchIsPremium = true,
            matchPremiumNoMatchBehaviour = PremiumNoMatchBehaviour.STANDARD
        )
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
        CleanupPremium(logger) {}
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
    }

    @Test
    fun shouldNotCleanupPremiumUsersWithAMatch() {
        insertMatch(100, 100, "Test", matchIsPremium = true, matchSendTo = 1, matchReceiveFrom = 1)
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
        CleanupPremium(logger) {}
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
    }

    @Test
    fun shouldCleanupPremiumUserWithMissingReceiveFrom() {
        insertMatch(100, 100, "Test", matchIsPremium = true, matchSendTo = 1, matchReceiveFrom = null)
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
        CleanupPremium(logger) {}
        assertEquals(1, getCurrentCount())
        assertEquals(1, getDroppedCount())
    }

    @Test
    fun shouldCleanupPremiumUserWithMissingSendToFrom() {

        insertMatch(100, 100, "Test", matchIsPremium = true, matchSendTo = null, matchReceiveFrom = 1)
        assertEquals(1, getCurrentCount())
        assertEquals(0, getDroppedCount())
        CleanupPremium(logger) {}
        assertEquals(1, getCurrentCount())
        assertEquals(1, getDroppedCount())
    }

    private fun getDroppedCount() = transaction {
        MatchesTable.select { MatchesTable.isDropped eq true }.count()
    }

    private fun getCurrentCount() = transaction {
        MatchesTable.selectAll().count()
    }
}