package gifts.givin.matching.matcher.infrastructure

import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId
import gifts.givin.matching.matcher.domain.MatchRepository
import org.jetbrains.exposed.sql.transactions.transaction

class DBMatchRepository : MatchRepository {
    override fun getAllUsers(matchingGroupId: MatchingGroupId): List<Match> = DB.getAllUsers(matchingGroupId)

    override fun getAllDoNotMatch(matchingGroupId: MatchingGroupId): Map<UserId, List<UserId>> =
        DB.getAllDoNotMatch(matchingGroupId)

    override fun match(sender: UserId, receiver: UserId, matchingGroupId: MatchingGroupId) {
        DB.matchUsers(sender, receiver, matchingGroupId)
    }

    override fun drop(user: UserId, matchingGroupId: MatchingGroupId) {
        DB.drop(user, matchingGroupId)
    }

    override fun markInstancesAsDone(matchingGroupId: MatchingGroupId) {
        DB.markInstancesAsDone(matchingGroupId)
    }

    override fun getNumberOfUnmatchedUsers(matchingGroupId: MatchingGroupId) =
        DB.getNumberOfUnmatchedUsers(matchingGroupId)

    override fun inTransaction(function: (() -> Unit) -> Unit) {
        transaction { function(this::commit) }
    }
}
