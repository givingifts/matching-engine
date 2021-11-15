package gifts.givin.matching

import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.NoMatchBehaviour
import gifts.givin.matching.common.stages.PrepareMatching
import gifts.givin.matching.matcher.matchMatchingGroup
import mu.KotlinLogging
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class MatcherTest {
    val logger = KotlinLogging.logger { }
    companion object {
        private val mysqlContainer: MySQLContainer<Nothing> = MySQLContainerWrapper.getContainer()
    }

    @BeforeEach
    fun cleanup() {
        DB.connect(mysqlContainer.jdbcUrl + "?useSSL=false", mysqlContainer.username, mysqlContainer.password)
        initSchema()
        clean()
    }

    @Test
    fun shouldMatch3Users() {
        insertMatchingGroup("USA", null)
        insertMatch(1, 1, "USA")
        insertMatch(2, 2, "USA")
        insertMatch(3, 3, "USA")

        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = false
        }
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)), logger)

        val match1 = getMatch(1)
        val match2 = getMatch(2)
        val match3 = getMatch(3)
        assertValidMatch(match1)
        assertValidMatch(match2)
        assertValidMatch(match3)
        assertDifferentMatch(match1, match2, match3)
    }

    @Test
    fun shouldMatch2Users() {
        insertMatchingGroup("USA", null)
        insertMatch(1, 1, "USA")
        insertMatch(2, 2, "USA")

        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = false
        }
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)), logger)

        val match1 = getMatch(1)
        val match2 = getMatch(2)
        assertTrue(match1.sendTo == match2.userId)
        assertTrue(match2.sendTo == match1.userId)
        assertTrue(match1.receiveFrom == match2.userId)
        assertTrue(match2.receiveFrom == match1.userId)
    }

    @Test
    fun shouldNotMatchUsersWithDoNotMatch2Users() {
        insertMatchingGroup("USA", null)
        insertMatch(1, 1, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertMatch(2, 2, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertDoNotMatch(1, 2)

        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = false
        }
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)), logger)

        val match1 = getMatch(1)
        val match2 = getMatch(2)
        assertFalse(match1.sendTo == match2.userId)
        assertFalse(match2.sendTo == match1.userId)
        assertFalse(match1.receiveFrom == match2.userId)
        assertFalse(match2.receiveFrom == match1.userId)
    }

    @Test
    fun shouldNotMatchUsersWithDoNotMatch4Users() {
        insertMatchingGroup("USA", null)
        insertMatch(1, 1, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertMatch(2, 2, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertMatch(3, 3, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertMatch(4, 4, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertDoNotMatch(1, 2)

        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = false
        }
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)), logger)

        val match1 = getMatch(1)
        val match2 = getMatch(2)
        val match3 = getMatch(3)
        val match4 = getMatch(4)
        assertFalse(match1.sendTo == match2.userId)
        assertFalse(match2.sendTo == match1.userId)
        assertFalse(match1.receiveFrom == match2.userId)
        assertFalse(match2.receiveFrom == match1.userId)
        assertTrue(match1.sendTo == match3.userId || match1.receiveFrom == match3.userId || match1.sendTo == match4.userId || match1.receiveFrom == match4.userId)
        assertTrue(match2.sendTo == match3.userId || match2.receiveFrom == match3.userId || match2.sendTo == match4.userId || match2.receiveFrom == match4.userId)
        assertFalse(match1.isDropped)
        assertFalse(match2.isDropped)
        assertFalse(match3.isDropped)
        assertValidMatch(match1)
        assertValidMatch(match2)
        assertValidMatch(match3)
        assertValidMatch(match4)
    }

    @Test
    fun shouldMatchUsersWithDoNotMatch3Users() {
        insertMatchingGroup("USA", null)
        insertMatch(1, 1, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertMatch(2, 2, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertMatch(3, 3, "USA", matchNoMatchBehaviour = NoMatchBehaviour.DROP)
        insertDoNotMatch(1, 2)

        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = false
        }
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)), logger)

        val match1 = getMatch(1)
        val match2 = getMatch(2)
        val match3 = getMatch(3)
        assertFalse(match1.sendTo == match2.userId && match1.receiveFrom == match2.userId)
        assertFalse(match2.sendTo == match1.userId && match2.receiveFrom == match1.userId)
        assertValidMatch(match1)
        assertValidMatch(match2)
        assertValidMatch(match3)
    }
}
