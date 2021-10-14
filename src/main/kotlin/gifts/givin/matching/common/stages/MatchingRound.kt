package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.mapToMatch
import gifts.givin.matching.common.randomAndRemove
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class MatchingRoundOptions(var groups: MutableList<MatchingGroup> = mutableListOf())

object MatchingRound : Stage<MatchingRoundOptions> {
    override fun run(func: MatchingRoundOptions.() -> Unit) {
        val options = MatchingRoundOptions()
        func(options)
        while (options.groups.isNotEmpty()) {
            val matchingGroup = options.groups.randomAndRemove()!!
            matchMatchingGroup(matchingGroup)
        }
    }

    private fun matchMatchingGroup(matchingGroup: MatchingGroup) {
        var continueRunning = true
        while (continueRunning) {
            transaction {
                val user = getUser(matchingGroup.id)
                when (user) {
                    null -> continueRunning = false
                    else -> {
                        val secondUser = getRandomUnmatchedUser(matchingGroup.id, user.userId)
                        when (secondUser) {
                            null -> continueRunning = false
                            else -> matchUsers(user.userId, secondUser.userId)
                        }
                    }
                }
            }
        }
    }

    override fun extraLogs(func: MatchingRoundOptions.() -> Unit): String {
        val options = MatchingRoundOptions()
        func(options)
        return "with groups ${options.groups.joinToString(",") { it.id }}"
    }

    private fun matchUsers(first: Int, second: Int) {
        transaction {
            MatchesTable.update({ MatchesTable.userId eq first }) {
                it[MatchesTable.sendTo] = second
            }
            MatchesTable.update({ MatchesTable.userId eq second }) {
                it[MatchesTable.receiveFrom] = first
            }
        }
    }

    private fun getRandomUnmatchedUser(matchingGroup: String, firstUser: Int) = MatchesTable.select {
        MatchesTable.receiveFrom.isNull() and
            (MatchesTable.currentMatchingGroup eq matchingGroup) and
            (MatchesTable.userId neq firstUser) and
            (MatchesTable.sendTo.isNull() or (MatchesTable.sendTo neq firstUser))
    }
        .limit(1)
        .forUpdate()
        .mapToMatch()
        .firstOrNull()

    private fun getUser(matchingGroup: String) = MatchesTable
        .select { MatchesTable.sendTo.isNull() and (MatchesTable.currentMatchingGroup eq matchingGroup) }
        .limit(1)
        .forUpdate()
        .mapToMatch()
        .firstOrNull()
}
