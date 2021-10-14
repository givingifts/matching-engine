package gifts.givin.matching.common.domain

import gifts.givin.matching.common.db.MatchesTable
import org.jetbrains.exposed.sql.Query

data class Match(
    val id: Int,
    val userId: Int,
    val currentMatchingGroup: String?,
    val originalMatchingGroup: String,
    val isPremium: Boolean,
    val isDropped: Boolean,
    val premiumBehaviour: PremiumBehaviour?,
    val sendTo: Int?,
    val receiveFrom: Int?
)

fun Query.mapToMatch() = map {
    Match(
        it[MatchesTable.id].value,
        it[MatchesTable.userId],
        it[MatchesTable.currentMatchingGroup],
        it[MatchesTable.originalMatchingGroup],
        it[MatchesTable.isPremium],
        it[MatchesTable.isDropped],
        it[MatchesTable.premiumBehaviour],
        it[MatchesTable.sendTo],
        it[MatchesTable.receiveFrom],
    )
}
