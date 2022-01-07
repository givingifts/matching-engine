package gifts.givin.matching.common.domain

import gifts.givin.matching.common.db.MatchesTable
import org.jetbrains.exposed.sql.Query

typealias MatchId = Int
typealias UserId = Int
typealias MatchingGroupId = String

data class Match(
    val id: MatchId,
    val userId: UserId,
    val matchingGroup: MatchingGroupId,
    val sendTo: UserId?,
    val receiveFrom: UserId?,
    val isMatched: Boolean
)

fun Query.mapToMatch() = map {
    Match(
        it[MatchesTable.id].value,
        it[MatchesTable.userId],
        it[MatchesTable.matchingGroup],
        it[MatchesTable.sendTo],
        it[MatchesTable.receiveFrom],
        it[MatchesTable.isMatched]
    )
}
