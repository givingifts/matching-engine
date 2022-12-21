import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId
import gifts.givin.matching.matcher.domain.MatchRepository

class TestMatchRepository(val matches: List<Match>, val doNotMatch: Map<UserId, List<UserId>>) : MatchRepository {
    var matchesMadeSenders = mutableMapOf<UserId, UserId>()
    var matchesMadeReceivers = mutableMapOf<UserId, UserId>()
    val droppedUsers = mutableListOf<UserId>()

    override fun getAllUsers(matchingGroupId: MatchingGroupId): List<Match> = matches

    override fun getAllDoNotMatch(matchingGroupId: MatchingGroupId): Map<UserId, List<UserId>> = doNotMatch

    override fun match(sender: UserId, receiver: UserId, matchingGroupId: MatchingGroupId) {
        matchesMadeSenders[sender] = receiver
        matchesMadeReceivers[receiver] = sender
    }

    override fun drop(user: UserId, matchingGroupId: MatchingGroupId) {
        droppedUsers.add(user)
        matchesMadeReceivers.remove(user)
        matchesMadeSenders.remove(user)
        matchesMadeSenders = matchesMadeSenders.filterValues { it != user }.toMutableMap()
        matchesMadeReceivers = matchesMadeReceivers.filterValues { it != user }.toMutableMap()
    }

    override fun markInstancesAsDone(matchingGroupId: MatchingGroupId) {
        // Do nothing
    }

    override fun getNumberOfUnmatchedUsers(matchingGroupId: MatchingGroupId): Long {
        // Do nothing
        return 0
    }

    override fun inTransaction(function: (() -> Unit) -> Unit) {
        function {}
    }

    fun cleanup() {
        matchesMadeSenders.clear()
        matchesMadeReceivers.clear()
        droppedUsers.clear()
    }
}