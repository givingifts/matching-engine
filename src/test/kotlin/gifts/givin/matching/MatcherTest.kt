package gifts.givin.matching

import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.stages.PrepareFirstMatching
import gifts.givin.matching.matcher.matchMatchingGroup
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class MatcherTest {
    companion object {
        @Container
        private val mysqlContainer = MySQLContainer<Nothing>("mysql:latest")
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

        PrepareFirstMatching {}
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)))

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

        PrepareFirstMatching {}
        matchMatchingGroup(mutableListOf(MatchingGroup("USA", null)))

        val match1 = getMatch(1)
        val match2 = getMatch(2)
        assertTrue(match1.sendTo == match2.userId)
        assertTrue(match2.sendTo == match1.userId)
        assertTrue(match1.receiveFrom == match2.userId)
        assertTrue(match2.receiveFrom == match1.userId)
    }
}
