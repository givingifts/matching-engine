package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.Matches
import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.mapToMatch
import gifts.givin.matching.common.randomAndRemove
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class MatchWithEachOtherOptions(var groups: List<MatchingGroup> = emptyList())

object MatchWithEachOther : Stage<MatchWithEachOtherOptions> {
    override fun run(func: MatchWithEachOtherOptions.() -> Unit) {
        val options = MatchWithEachOtherOptions()
        func(options)
        options.groups.forEach {
            val usersWithoutSendTo = getUsersWithoutSendTo(it.id)
            val usersWithoutReceiveFrom = getUsersWithoutReceiveFrom(it.id)

            if (usersWithoutReceiveFrom.size == 1 && usersWithoutReceiveFrom.size == 1 && usersWithoutReceiveFrom.first().userId != usersWithoutSendTo.first().userId) {
                matchUsers(usersWithoutSendTo.first().userId, usersWithoutReceiveFrom.first().userId)
            }
        }

        options.groups.filterNot { it.parent == null }.forEach {
            val users = getUnmatchedUsers(it.id)
            if (users.size >= 2) {
                transaction {
                    val missingSendTo = users.filter { it.sendTo == null }
                    val missingReceiveFrom = users.filter { it.receiveFrom == null }.toMutableList()
                    missingSendTo.forEach {
                        val to = missingReceiveFrom.randomAndRemove()
                        if (to != null) {
                            matchUsers(it.userId, to.userId)
                        }
                    }
                }
            }
        }
        options.groups.forEach {
            val users = getUnmatchedUsers(it.id)
            /*
                    User 3 is missing
                    User 1 is randomMatchedUser.userId
                    User 2 is randomMatchedUser.sendto

                    Original situation
                    User 1 sends to User 2
                    User 2 sends to User 1
                    User 3

                    New situation
                    User 1 sends to User 3
                    User 3 sends to User 2
                    User 2 sends to User 1
                 */
            if (users.size == 1) {
                do3WayMatch(users, it)
            }
        }
    }

    override fun extraLogs(func: MatchWithEachOtherOptions.() -> Unit): String {
        val options = MatchWithEachOtherOptions()
        func(options)
        return "with groups ${options.groups.joinToString(",") { it.id }}"
    }

    private fun do3WayMatch(
        users: List<Match>,
        it: MatchingGroup
    ) {
        val missing = users.first()
        var randomMatchedUser: Match? = null
        if (missing.isPremium) {
            randomMatchedUser = getRandomMatchedPremiumUser(it.id).firstOrNull()
        }
        if (randomMatchedUser == null || !missing.isPremium) {
            randomMatchedUser = getRandomMatchedUser(it.id).firstOrNull()
        }
        if (randomMatchedUser != null) {
            matchUsers(randomMatchedUser.userId, missing.userId)
            matchUsers(missing.userId, randomMatchedUser.sendTo!!)
        }
    }

    private fun matchUsers(first: Int, second: Int) {
        transaction {
            MatchesTable.update({ MatchesTable.userId eq first }) {
                it[Matches.sendTo] = second
            }
            MatchesTable.update({ MatchesTable.userId eq second }) {
                it[Matches.receiveFrom] = first
            }
        }
    }

    private fun getUnmatchedUsers(matchingGroup: String) = transaction {
        MatchesTable
            .select { MatchesTable.sendTo.isNull() and (MatchesTable.currentMatchingGroup eq matchingGroup) }
            .mapToMatch()
    }

    private fun getUsersWithoutSendTo(matchingGroup: String) = transaction {
        MatchesTable
            .select { MatchesTable.sendTo.isNull() and (MatchesTable.currentMatchingGroup eq matchingGroup) }
            .mapToMatch()
    }

    private fun getUsersWithoutReceiveFrom(matchingGroup: String) = transaction {
        MatchesTable
            .select { MatchesTable.receiveFrom.isNull() and (MatchesTable.currentMatchingGroup eq matchingGroup) }
            .mapToMatch()
    }

    private fun getRandomMatchedUser(matchingGroup: String) = transaction {
        MatchesTable
            .select { MatchesTable.sendTo.isNotNull() and (MatchesTable.isPremium eq false) and (MatchesTable.currentMatchingGroup eq matchingGroup) }
            .mapToMatch()
    }

    private fun getRandomMatchedPremiumUser(matchingGroup: String) = transaction {
        MatchesTable
            .select { MatchesTable.sendTo.isNotNull() and (MatchesTable.isPremium eq true) and (MatchesTable.currentMatchingGroup eq matchingGroup) }
            .mapToMatch()
    }
}
