package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.PremiumBehaviour
import gifts.givin.matching.common.domain.mapToMatch
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PremiumBehaviour : Stage<Unit> {
    override fun run(func: Unit.() -> Unit) {
        val unmatchedPremiumUsers = getUnmatchedPremiumUsers().filter { it.premiumBehaviour != null }
        for (user in unmatchedPremiumUsers) {
            when (user.premiumBehaviour) {
                PremiumBehaviour.DROP -> dropUser(user)
                PremiumBehaviour.NONPREMIUM -> clearMatchingGroup(user)
                else -> setMatchingGroupToWorldwide(user)
            }
        }
    }

    private fun setMatchingGroupToWorldwide(user: Match) = transaction {
        MatchesTable.update({ MatchesTable.id eq user.id }) {
            it[MatchesTable.currentMatchingGroup] = "Worldwide"
        }
    }

    private fun clearMatchingGroup(user: Match) = transaction {
        MatchesTable.update({ MatchesTable.id eq user.id }) {
            it[MatchesTable.currentMatchingGroup] = null
        }
    }

    private fun dropUser(user: Match) {
        transaction {
            MatchesTable.update({ MatchesTable.id eq user.id }) {
                it[MatchesTable.isDropped] = true
                it[MatchesTable.currentMatchingGroup] = null
            }
        }
    }

    private fun getUnmatchedPremiumUsers() = transaction {
        MatchesTable.select {
            (MatchesTable.isPremium eq true) and
                (MatchesTable.originalMatchingGroup neq "Worldwide") and
                (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
        }.mapToMatch()
    }
}
