package gifts.givin.matching

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.db.MatchingGroupTable
import gifts.givin.matching.common.domain.NoMatchBehaviour
import gifts.givin.matching.common.domain.mapToMatch
import gifts.givin.matching.common.domain.PremiumNoMatchBehaviour
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun insertMatchingGroup(groupId: String, parentId: String?) = transaction {
    MatchingGroupTable.insert {
        it[id] = groupId
        it[parent] = parentId
    }
}

fun insertMatch(
    matchId: Int,
    matchUserId: Int,
    matchMatchingGroup: String,
    matchSendTo: Int? = null,
    matchReceiveFrom: Int? = null,
    matchIsPremium: Boolean = false,
    matchNoMatchBehaviour: NoMatchBehaviour = NoMatchBehaviour.INTERNATIONAL_WORLDWIDE,
    matchPremiumNoMatchBehaviour: PremiumNoMatchBehaviour? = null,
    matchCurrentMatchingGroups: String? = null
) = transaction {
    MatchesTable.insert {
        it[id] = matchId
        it[userId] = matchUserId
        it[originalMatchingGroup] = matchMatchingGroup
        it[currentMatchingGroup] = matchCurrentMatchingGroups
        it[sendTo] = matchSendTo
        it[receiveFrom] = matchReceiveFrom
        it[isPremium] = matchIsPremium
        it[noMatchBehaviour] = matchNoMatchBehaviour
        it[premiumNoMatchBehaviour] = matchPremiumNoMatchBehaviour
    }
}

fun getMatch(matchId: Int) = transaction {
    MatchesTable.select { MatchesTable.id eq matchId }.limit(1).mapToMatch().first()
}

fun getAllMatches() = transaction {
    MatchesTable.selectAll().mapToMatch()
}

object Dummy

fun initSchema() {
    Thread.currentThread().contextClassLoader.getResource("schema.sql")
        .readText()
        .split(";")
        .map { it.trim() }
        .filterNot { it.isBlank() }
        .filterNot { it == ";" }
        .forEach { statement -> transaction { exec(statement) } }
}

fun clean() = transaction {
    MatchesTable.deleteAll()
    MatchingGroupTable.deleteAll()
}
