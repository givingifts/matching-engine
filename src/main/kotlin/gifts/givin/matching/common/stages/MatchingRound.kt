package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.DoNotMatchTable
import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.mapToMatch
import gifts.givin.matching.common.randomAndRemove
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.notExists
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
                        if (secondUser != null) {
                            matchUsers(user.userId, secondUser.userId)
                        } else if (countOfUsers(matchingGroup.id) == 1L || (countOfUsers(matchingGroup.id) == 2L && usersCantBeMatched(matchingGroup.id))) {
                            continueRunning = false
                        }
                    }
                }
            }
        }
    }

    private fun usersCantBeMatched(matchingGroup: String): Boolean {
        val users = MatchesTable.select {
            (MatchesTable.sendTo.isNull()) and (MatchesTable.currentMatchingGroup eq matchingGroup)
        }.mapToMatch()
        return getRandomUnmatchedUser(matchingGroup, users.first().userId) == null && getRandomUnmatchedUser(matchingGroup, users.last().userId) == null
    }

    private fun countOfUsers(matchingGroup: String): Long = MatchesTable.select {
        MatchesTable.receiveFrom.isNull() and (MatchesTable.currentMatchingGroup eq matchingGroup)
    }.count()

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
            (MatchesTable.sendTo.isNull() or (MatchesTable.sendTo neq firstUser)) and
            notExists(
                DoNotMatchTable.slice(DoNotMatchTable.firstUserId).select {
                    DoNotMatchTable.firstUserId.eq(MatchesTable.userId) and DoNotMatchTable.secondUserId.eq(firstUser)
                }
            ) and
            notExists(
                DoNotMatchTable.slice(DoNotMatchTable.secondUserId).select {
                    DoNotMatchTable.secondUserId.eq(MatchesTable.userId) and DoNotMatchTable.firstUserId.eq(firstUser)
                }
            )
    }
        .limit(1)
        .orderBy(Random())
        .forUpdate()
        .mapToMatch()
        .firstOrNull()

    private fun getUser(matchingGroup: String) = MatchesTable
        .select { MatchesTable.sendTo.isNull() and (MatchesTable.currentMatchingGroup eq matchingGroup) }
        .limit(1)
        .orderBy(Random())
        .forUpdate()
        .mapToMatch()
        .firstOrNull()
}
