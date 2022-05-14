package gifts.givin.matching.common.db

import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId
import gifts.givin.matching.common.domain.mapToMatch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
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
        if(!initialized) {
            Database.connect(url, "com.mysql.cj.jdbc.Driver", username, password)
            println("Current transaction isolation level: ${TransactionManager.manager.defaultIsolationLevel}")
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            initialized = true
        }
    }

    fun getMatchingGroupsInUse(): List<String> = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable
            .slice(MatchesTable.matchingGroup)
            .select { MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull() }
            .map { it[MatchesTable.matchingGroup] }
            .distinct()
    }

    fun getDoNotMatch(userId: UserId, receiveFrom: UserId?, droppedList: List<UserId>): List<UserId> {
        val list = transaction {
            addLogger(StdOutSqlLogger)
            DoNotMatchTable.slice(DoNotMatchTable.secondUserId).select { DoNotMatchTable.firstUserId.eq(userId) }
                .mapNotNull { it[DoNotMatchTable.secondUserId] }
        }
        val secondList = transaction {
            addLogger(StdOutSqlLogger)
            DoNotMatchTable.slice(DoNotMatchTable.firstUserId).select { DoNotMatchTable.secondUserId.eq(userId) }
                .mapNotNull { it[DoNotMatchTable.firstUserId] }
        }

        return if (receiveFrom == null) {
            (list + secondList + userId + droppedList).distinct()
        } else {
            (list + secondList + userId + receiveFrom + droppedList).distinct()
        }
    }

    fun matchUsers(sender: UserId, receiver: UserId) = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable.update({ MatchesTable.userId eq sender }) {
            it[MatchesTable.sendTo] = receiver
        }
        MatchesTable.update({ MatchesTable.userId eq receiver }) {
            it[MatchesTable.receiveFrom] = sender
        }
    }

    fun getUserWithoutSendTo(matchingGroup: MatchingGroupId, droppedList: List<UserId>): Match? = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable.select { (MatchesTable.matchingGroup eq matchingGroup) and (MatchesTable.sendTo.isNull()) and MatchesTable.userId.notInList(droppedList) }
            .orderBy(Random())
            .limit(1)
            .mapToMatch()
            .firstOrNull()
    }

    fun getUserWithoutReceiveFrom(matchingGroup: MatchingGroupId, doNotSendList: List<UserId>): Match? =
        transaction {
            addLogger(StdOutSqlLogger)
            MatchesTable.select { (MatchesTable.matchingGroup eq matchingGroup) and (MatchesTable.receiveFrom.isNull()) and (MatchesTable.userId notInList doNotSendList) }
                .orderBy(Random())
                .limit(1)
                .mapToMatch()
                .firstOrNull()
        }

    fun getRandomMatch(matchingGroup: MatchingGroupId, doNotSendList: List<UserId>): Pair<Match, Match> {
        val match = transaction {
            addLogger(StdOutSqlLogger)
            MatchesTable.select { ((MatchesTable.matchingGroup eq matchingGroup) and (MatchesTable.receiveFrom.isNotNull()) and (MatchesTable.sendTo.isNotNull())) and MatchesTable.userId.notInList(doNotSendList) }
                .orderBy(Random())
                .limit(1)
                .mapToMatch()
                .first()
        }
        return match to getMatch(match.sendTo!!)
    }

    fun getMatch(userId: UserId): Match = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable.select { MatchesTable.userId eq userId }.limit(1).mapToMatch().first()
    }

    fun getNumberOfUnmatchedUsers(matchingGroup: MatchingGroupId, droppedList: List<UserId>): Long = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable.select { ((MatchesTable.matchingGroup eq matchingGroup) and (MatchesTable.receiveFrom.isNull() or MatchesTable.sendTo.isNull())) and MatchesTable.userId.notInList(droppedList) }
            .count()
    }

    fun getNumberOfUnmatchedUsers(): Long = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable.select { MatchesTable.isMatched eq false }
            .count()
    }

    fun getNotDoneInstances(): Long = transaction {
        addLogger(StdOutSqlLogger)
        MatchingInstancesTable.select { MatchingInstancesTable.done eq false }.count()
    }

    fun addInstance(id: MatchingGroupId) = transaction {
        MatchingInstancesTable.insert {
            it[MatchingInstancesTable.done] = false
            it[MatchingInstancesTable.matchingGroup] = id
        }
    }

    fun cleanupInstances() = transaction {
        addLogger(StdOutSqlLogger)
        MatchingInstancesTable.deleteAll()
    }

    fun cleanupMatching() = transaction {
        addLogger(StdOutSqlLogger)
        MatchesTable.update({ MatchesTable.sendTo.isNotNull() and MatchesTable.receiveFrom.isNotNull() }) {
            it[MatchesTable.isMatched] = true
        }
    }

    fun drop(id: UserId) {
        transaction {
            addLogger(StdOutSqlLogger)
            MatchesTable.update({ MatchesTable.sendTo.eq(id) }) {
                it[MatchesTable.isMatched] = false
                it[MatchesTable.sendTo] = null
            }
        }
        transaction {
            addLogger(StdOutSqlLogger)
            MatchesTable.update({ MatchesTable.receiveFrom.eq(id) }) {
                it[MatchesTable.isMatched] = false
                it[MatchesTable.receiveFrom] = null
            }
        }
        transaction {
            addLogger(StdOutSqlLogger)
            MatchesTable.update({ MatchesTable.userId.eq(id) }) {
                it[MatchesTable.isMatched] = false
                it[MatchesTable.receiveFrom] = null
                it[MatchesTable.sendTo] = null
            }
        }
    }
}
