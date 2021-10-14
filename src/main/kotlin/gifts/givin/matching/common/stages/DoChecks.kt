package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.db.MatchingGroupTable
import gifts.givin.matching.common.domain.mapToMatch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.ResultSet

object DoChecks : Stage<Unit> {
    private val logger = KotlinLogging.logger("Checks")
    override fun run(func: Unit.() -> Unit) {
        val droppedUsers = transaction {
            MatchesTable.select { MatchesTable.isDropped eq true }.count()
        }
        val usersWithDifferentMatchingGroup = transaction {
            MatchesTable.select { MatchesTable.currentMatchingGroup neq MatchesTable.originalMatchingGroup }.count()
        }
        val usersSendingToEachOther = transaction {
            MatchesTable.select { MatchesTable.sendTo eq MatchesTable.receiveFrom }.count()
        }
        val usersWithoutSendTo = transaction {
            MatchesTable.select { MatchesTable.sendTo.isNull() }.count()
        }
        val usersWithoutReceiveFrom = transaction {
            MatchesTable.select { MatchesTable.receiveFrom.isNull() }.count()
        }
        val totalUsers = transaction {
            MatchesTable.selectAll().count()
        }
        val totalMatchingGroups = transaction {
            MatchingGroupTable.selectAll().count()
        }
        val usedMatchingGroups = transaction {
            MatchesTable.selectAll().groupBy(MatchesTable.originalMatchingGroup).count()
        }
        val droppedUsersWithMatch = transaction {
            MatchesTable.select { (MatchesTable.isDropped eq true) and (MatchesTable.sendTo.isNotNull() or MatchesTable.receiveFrom.isNotNull()) }
                .mapToMatch()
        }
        transaction {
            "SELECT m1.*, m2.IsPremium As matchIsPremium FROM Matches m1 JOIN Matches m2 ON m1.ReceiveFrom = m2.UserId WHERE m1.IsPremium = 1 AND m2.isPremium = false;".execAndMap { rs ->
                logger.info {
                    "Premium user ${rs.getString("UserId")} matched with non-premium user ${rs.getString("ReceiveFrom")} with original matching group being ${
                    rs.getString(
                        "OriginalMatchingGroup"
                    )
                    }"
                }
            }
        }
        logger.info { "Total matched users: $totalUsers" }
        logger.info { "Total matching groups: $totalMatchingGroups, with $usedMatchingGroups actually used for matching" }
        logger.info { "Users without a match to send: $usersWithoutSendTo" }
        logger.info { "Users without a match to receieve: $usersWithoutReceiveFrom" }
        logger.info { "Users sending to each other: $usersSendingToEachOther" }
        logger.info { "Users with different matching group from their selected one: $usersWithDifferentMatchingGroup" }
        logger.info { "Dropped users: $droppedUsers" }
        droppedUsersWithMatch.forEach {
            logger.info { "Warning: ${it.userId} has been dropped but they are supposed to send to ${it.sendTo} and/or receive from ${it.receiveFrom}" }
        }
    }
}

fun <T : Any> String.execAndMap(transform: (ResultSet) -> T): List<T> {
    val result = arrayListOf<T>()
    TransactionManager.current().exec(this) { rs ->
        while (rs.next()) {
            result += transform(rs)
        }
    }
    return result
}
