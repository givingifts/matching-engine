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
private typealias NoMatchBehaviourEnum = gifts.givin.matching.common.domain.NoMatchBehaviour
object PremiumNoMatchBehaviour : Stage<PremiumNoMatchBehaviourOptions> {
    override fun run(func: PremiumNoMatchBehaviourOptions.() -> Unit) {
        val options = PremiumNoMatchBehaviourOptions()
        func(options)

        val unmatchedUsers = getUnmatchedUsers()
        for (user in unmatchedUsers) {
            if (user.currentMatchingGroup == "Worldwide") {
                if(user.premiumNoMatchBehaviour == PremiumNoMatchBehaviour.DROP) {
                    dropUser(user)
                } else {
                    clearMatchingGroup(user)
                }
            } else {
                when (user.noMatchBehaviour) {
                    NoMatchBehaviourEnum.DROP -> dropUser(user)
                    NoMatchBehaviourEnum.INTERNATIONAL_WORLDWIDE -> if (isInternationalMatch(user)) {
                        setMatchingGroupToWorldwide(user)
                    } else {
                        clearMatchingGroup(user)
                    }
                    NoMatchBehaviourEnum.WORLDWIDE -> setMatchingGroupToWorldwide(user)
                    NoMatchBehaviourEnum.INTERNATIONAL_DROP -> if (isInternationalMatch(user)) {
                        dropUser(user)
                    } else {
                        clearMatchingGroup(user)
                    }
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

    private fun isInternationalMatch(user: Match) =
        user.currentMatchingGroup!!.startsWith("Group ")
}
