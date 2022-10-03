package gifts.givin.matching.common.db

import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId
import gifts.givin.matching.common.domain.mapToMatch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.sql.Connection

object DB {
    var initialized = false

    fun connect(url: String, username: String, password: String) {
        if (!initialized) {
            Database.connect(url, "com.mysql.cj.jdbc.Driver", username, password)
            println("Current transaction isolation level: ${TransactionManager.manager.defaultIsolationLevel}")
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            initialized = true
        }
    }

    fun getMatchingGroupsInUse(): List<String> = transaction {
        MatchesTable
            .slice(MatchesTable.matchingGroup)
            .select { MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull() }
            .map { it[MatchesTable.matchingGroup] }
            .distinct()
    }

    fun matchUsers(sender: UserId, receiver: UserId, matchingGroup: MatchingGroupId) = transaction {
        MatchesTable.update({ MatchesTable.userId eq sender and MatchesTable.matchingGroup.eq(matchingGroup) }) {
            it[MatchesTable.sendTo] = receiver
        }
        MatchesTable.update({ MatchesTable.userId eq receiver and MatchesTable.matchingGroup.eq(matchingGroup) }) {
            it[MatchesTable.receiveFrom] = sender
        }
    }

    fun getNumberOfUnmatchedUsers(): Long = transaction {
        MatchesTable.select { MatchesTable.isMatched eq false }
            .count()
    }

    fun getNumberOfUnmatchedUsers(matchingGroup: MatchingGroupId): Long = transaction {
        MatchesTable.select {
            (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull()) and MatchesTable.matchingGroup.eq(
                matchingGroup
            )
        }
            .count()
    }

    fun getNotDoneInstances(): Long = transaction {
        MatchingInstancesTable.select { MatchingInstancesTable.done eq false }.count()
    }

    fun addInstance(id: MatchingGroupId) = transaction {
        MatchingInstancesTable.insert {
            it[MatchingInstancesTable.done] = false
            it[MatchingInstancesTable.matchingGroup] = id
        }
    }

    fun cleanupInstances() = transaction {
        MatchingInstancesTable.deleteAll()
    }

    fun cleanupMatching() = transaction {
        MatchesTable.update({ MatchesTable.sendTo.isNotNull() and MatchesTable.receiveFrom.isNotNull() }) {
            it[MatchesTable.isMatched] = true
        }
    }

    fun getAllUsers(matchingGroup: MatchingGroupId) = transaction {
        MatchesTable.select { MatchesTable.matchingGroup.eq(matchingGroup) }
            .orderBy(MatchesTable.userId)
            .mapToMatch()
    }

    fun getAllDoNotMatch(matchingGroup: MatchingGroupId) = transaction {
        DoNotMatchTable
            .select {
                DoNotMatchTable.firstUserId.inSubQuery(
                    MatchesTable.slice(MatchesTable.userId).select { MatchesTable.matchingGroup.eq(matchingGroup) }
                ) or DoNotMatchTable.secondUserId.inSubQuery(
                    MatchesTable.slice(MatchesTable.userId).select { MatchesTable.matchingGroup.eq(matchingGroup) }
                )
            }
            .map { it[DoNotMatchTable.firstUserId] to it[DoNotMatchTable.secondUserId] }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }
    }

    fun drop(id: UserId, matchingGroup: MatchingGroupId) {
        transaction {
            MatchesTable.update({ MatchesTable.userId.eq(id) and MatchesTable.matchingGroup.eq(matchingGroup) }) {
                it[MatchesTable.isMatched] = false
                it[MatchesTable.receiveFrom] = null
                it[MatchesTable.sendTo] = null
            }
        }
    }

    fun markInstancesAsDone(matchingGroupId: MatchingGroupId) = transaction {
        MatchingInstancesTable.update({ MatchingInstancesTable.matchingGroup eq matchingGroupId }) {
            it[MatchingInstancesTable.done] = true
        }
    }

}
