import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId
import gifts.givin.matching.matcher.domain.Matcher
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class MatchingTest {
    val logger = KotlinLogging.logger("Test Logger")

    @Test
    fun shouldDrop1User() {
        val repo = TestMatchRepository(listOf(emptyMatch(1)), emptyMap())
        Matcher(repo, "Test").doMatchingRound()
        assertEquals(1, repo.droppedUsers.size)
        assertEquals(0, repo.matchesMadeReceivers.size)
        assertEquals(0, repo.matchesMadeSenders.size)
    }

    @Test
    fun shouldMatch2Users() {
        val repo = TestMatchRepository(listOf(emptyMatch(1), emptyMatch(2)), emptyMap())
        Matcher(repo, "Test").doMatchingRound()
        assertEquals(0, repo.droppedUsers.size)
        assertEquals(2, repo.matchesMadeReceivers.size)
        assertEquals(2, repo.matchesMadeSenders.size)
        assertEquals(1, repo.matchesMadeSenders[2])
        assertEquals(2, repo.matchesMadeSenders[1])
        assertEquals(1, repo.matchesMadeReceivers[2])
        assertEquals(2, repo.matchesMadeReceivers[1])
    }

    @Test
    fun shouldNotMatch2UsersThatHaveDoNotMatch() {
        val repo = TestMatchRepository(listOf(emptyMatch(1), emptyMatch(2)), mapOf(1 to listOf(2)))
        Matcher(repo, "Test").doMatchingRound()
        assertEquals(2, repo.droppedUsers.size)
    }

    @Test
    fun shouldMatch3Users() {
        val repo = TestMatchRepository(listOf(emptyMatch(1), emptyMatch(2), emptyMatch(3)), emptyMap())
        Matcher(repo, "Test").doMatchingRound()
        assertEquals(0, repo.droppedUsers.size)
        assertEquals(3, repo.matchesMadeReceivers.size)
        assertEquals(3, repo.matchesMadeSenders.size)
    }

    @Test
    fun shouldMatch3UsersWithDoNotMatch() {
        val repo = TestMatchRepository(listOf(emptyMatch(1), emptyMatch(2), emptyMatch(3)), mapOf(1 to listOf(2)))
        Matcher(repo, "Test").doMatchingRound()
        assertEquals(1, repo.droppedUsers.size)
        assertEquals(2, repo.matchesMadeReceivers.size)
        assertEquals(2, repo.matchesMadeSenders.size)
    }


    @Test
    fun shouldMatch4UsersWithDoNotMatch() {
        val repo = TestMatchRepository(
            listOf(emptyMatch(1), emptyMatch(2), emptyMatch(3), emptyMatch(4)),
            mapOf(1 to listOf(2))
        )
        repeat(100) {
            repo.cleanup()
            Matcher(repo, "Test").doMatchingRound()
            assertEquals(0, repo.droppedUsers.size)
            assertEquals(4, repo.matchesMadeReceivers.size)
            assertEquals(4, repo.matchesMadeSenders.size)
            assertNotEquals(2, repo.matchesMadeSenders[1])
            assertNotEquals(2, repo.matchesMadeReceivers[1])
            assertNotEquals(1, repo.matchesMadeSenders[2])
            assertNotEquals(1, repo.matchesMadeReceivers[2])
        }
    }

    private fun emptyMatch(id: UserId, matchingGroupId: MatchingGroupId = "Test") =
        Match(1, id, matchingGroupId, null, null, false)
}