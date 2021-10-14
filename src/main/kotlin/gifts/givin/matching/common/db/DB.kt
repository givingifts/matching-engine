package gifts.givin.matching.common.db

import gifts.givin.matching.common.domain.MatchingGroup
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DB {
    fun connect(url: String, username: String, password: String) {
        Database.connect(url, "com.mysql.cj.jdbc.Driver", username, password)
    }

    fun getAllMatchingGroups(): List<MatchingGroup> = transaction {
        MatchingGroupTable
            .selectAll()
            .map {
                MatchingGroup(
                    it[MatchingGroupTable.id],
                    it[MatchingGroupTable.parent]
                )
            }
    }

    fun getMatchingGroups(): Map<MatchingGroup, Long> = transaction {
        val allGroups = getAllMatchingGroups()
        val groupsInUse = getMatchingGroupsInUse()
        val matchingGroups = allGroups.filter { groupsInUse.contains(it.id) }
        val count = MatchesTable.id.count().alias("count")
        val countPerGroup = MatchesTable
            .slice(MatchesTable.currentMatchingGroup, count)
            .selectAll()
            .groupBy(MatchesTable.currentMatchingGroup)
            .filter { it[MatchesTable.currentMatchingGroup] != null }
            .associate { it[MatchesTable.currentMatchingGroup] to it[count] }

        matchingGroups.map {
            it to countPerGroup.getOrDefault(it.id, 0)
        }.toMap()
    }

    fun getMatchingGroupsInUse(): List<String> = transaction {
        MatchesTable
            .slice(MatchesTable.currentMatchingGroup)
            .select { MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull() }
            .distinct()
            .mapNotNull { it[MatchesTable.currentMatchingGroup] }
    }
}
