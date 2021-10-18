package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.PremiumNoMatchBehaviour
import gifts.givin.matching.common.domain.mapToMatch
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private val usersToDrop = mutableListOf<Int>()
private val usersToSetToWorldwide = mutableListOf<Int>()
private val usersToClearMatchingGroup = mutableListOf<Int>()
data class PremiumNoMatchBehaviourOptions(var groups: List<MatchingGroup> = emptyList())
object PremiumNoMatchBehaviour : Stage<PremiumNoMatchBehaviourOptions> {
    override fun run(func: PremiumNoMatchBehaviourOptions.() -> Unit) {
        val options = PremiumNoMatchBehaviourOptions()
        func(options)

        val unmatchedUsers = getUnmatchedUsers()
        for (user in unmatchedUsers) {
            if(user.currentMatchingGroup == "Worldwide") {
                dropUser(user)
            } else {
                when (user.premiumNoMatchBehaviour) {
                    PremiumNoMatchBehaviour.DROP -> dropUser(user)
                    PremiumNoMatchBehaviour.WORLDWIDE -> setMatchingGroupToWorldwide(user)
                    PremiumNoMatchBehaviour.STANDARD -> clearMatchingGroup(user)
                }
            }
        }

        transaction {
            MatchesTable.update({ MatchesTable.userId inList usersToDrop }) {
                it[MatchesTable.isDropped] = true
                it[MatchesTable.currentMatchingGroup] = null
            }
        }

        transaction {
            MatchesTable.update({ MatchesTable.userId inList usersToSetToWorldwide }) {
                it[MatchesTable.currentMatchingGroup] = "Worldwide"
            }
        }

        transaction {
            MatchesTable.update({ MatchesTable.userId inList usersToClearMatchingGroup }) {
                it[MatchesTable.currentMatchingGroup] = null
            }
        }
    }

    private fun clearMatchingGroup(user: Match) {
        usersToClearMatchingGroup.add(user.userId)
    }

    private fun dropUser(user: Match) {
        usersToDrop.add(user.userId)
    }

    private fun setMatchingGroupToWorldwide(user: Match) = transaction {
        usersToSetToWorldwide.add(user.userId)
    }

    private fun getUnmatchedUsers() = transaction {
        MatchesTable.select {
                    MatchesTable.currentMatchingGroup.isNotNull() and
                    (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
        }.mapToMatch()

    }
}