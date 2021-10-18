package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PrepareWorldwideMatching : Stage<Unit> {
    override fun run(func: Unit.() -> Unit) {
        transaction {
            MatchesTable.update({
                (MatchesTable.isDropped eq false) and
                        ((MatchesTable.isUpgradedToWorldwide eq true) or (MatchesTable.originalMatchingGroup eq "Worldwide")) and
                        (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
            }) {
                it[MatchesTable.currentMatchingGroup] = "Worldwide"
            }
        }
    }
}
