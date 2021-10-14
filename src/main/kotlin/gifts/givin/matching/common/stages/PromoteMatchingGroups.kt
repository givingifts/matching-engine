package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.domain.mapToMatch
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class PromoteMatchingGroupsOptions(var groups: List<MatchingGroup> = emptyList())
object PromoteMatchingGroups : Stage<PromoteMatchingGroupsOptions> {
    override fun run(func: PromoteMatchingGroupsOptions.() -> Unit) {
        val options = PromoteMatchingGroupsOptions()
        func(options)
        val users = transaction {
            MatchesTable.select {
                (MatchesTable.isDropped eq false) and
                    (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
            }.mapToMatch()
        }
        users
            .groupBy { it.currentMatchingGroup }
            .keys
            .filterNotNull()
            .distinct()
            .forEach { group ->
                kotlin.run {
                    val parentGroup = options.groups.firstOrNull { it.id == group }?.parent
                    if (parentGroup != null) {
                        transaction {
                            MatchesTable.update({
                                (MatchesTable.isDropped eq false) and
                                    (MatchesTable.currentMatchingGroup eq group) and
                                    (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
                            }) {
                                it[MatchesTable.currentMatchingGroup] = parentGroup
                            }
                        }
                    }
                }
            }
    }
}
