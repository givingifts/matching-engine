package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.NoMatchBehaviour
import gifts.givin.matching.common.domain.mapToMatch
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private val usersToPromote = mutableMapOf<String, MutableList<Int>>()
private val usersToDrop = mutableListOf<Int>()
private val usersToUpgradeToWorldwide = mutableListOf<Int>()
data class NoMatchBehaviourOptions(var groups: List<MatchingGroup> = emptyList())
object NoMatchBehaviour : Stage<NoMatchBehaviourOptions> {
    override fun run(func: NoMatchBehaviourOptions.() -> Unit) {
        val options = NoMatchBehaviourOptions()
        func(options)

        val unmatchedUsers = getUnmatchedUsers()
        for (user in unmatchedUsers) {
            when (user.noMatchBehaviour) {
                NoMatchBehaviour.DROP -> dropUser(user)
                NoMatchBehaviour.INTERNATIONAL_WORLDWIDE -> if (isInternationalMatch(user)) {
                    upgradeToWorldwide(user)
                } else {
                    promoteUser(user)
                }
                NoMatchBehaviour.WORLDWIDE -> upgradeToWorldwide(user)
                NoMatchBehaviour.INTERNATIONAL_DROP -> if (isInternationalMatch(user)) {
                    dropUser(user)
                } else {
                    promoteUser(user)
                }
            }
        }

        transaction {
            MatchesTable.update({ MatchesTable.userId inList usersToUpgradeToWorldwide }) {
                it[MatchesTable.isUpgradedToWorldwide] = true
            }
        }

        transaction {
            MatchesTable.update({ MatchesTable.userId inList usersToDrop }) {
                it[MatchesTable.isDropped] = true
                it[MatchesTable.currentMatchingGroup] = null
            }
        }

        usersToPromote.forEach { (matchingGroup, users) ->
            val parent = options.groups.firstOrNull { it.id == matchingGroup }?.parent
            if (parent != null) {
                transaction {
                    MatchesTable.update({
                        (MatchesTable.isDropped eq false) and
                            (MatchesTable.userId inList users) and
                            (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
                    }) {
                        it[MatchesTable.currentMatchingGroup] = parent
                    }
                }
            }
        }
    }

    private fun promoteUser(user: Match) {
        val matchingGroup = user.currentMatchingGroup!!
        usersToPromote.putIfAbsent(matchingGroup, mutableListOf())
        usersToPromote[matchingGroup]!!.add(user.userId)
    }

    private fun dropUser(user: Match) {
        usersToDrop.add(user.userId)
    }

    private fun upgradeToWorldwide(user: Match) {
        usersToUpgradeToWorldwide.add(user.userId)
    }

    private fun isInternationalMatch(user: Match) =
        user.currentMatchingGroup!!.startsWith("Group ")

    private fun getUnmatchedUsers() = transaction {
        MatchesTable.select {
            MatchesTable.currentMatchingGroup.isNotNull() and
                (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
        }.mapToMatch()
    }
}
