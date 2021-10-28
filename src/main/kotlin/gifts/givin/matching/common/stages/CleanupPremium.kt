package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.domain.PremiumNoMatchBehaviour
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CleanupPremium : Stage<Unit> {
    override fun run(func: Unit.() -> Unit) {
        transaction {
            MatchesTable.update({
                (MatchesTable.isPremium eq true) and
                    ((MatchesTable.premiumNoMatchBehaviour.isNull() or (MatchesTable.premiumNoMatchBehaviour neq PremiumNoMatchBehaviour.STANDARD))) and
                    (MatchesTable.sendTo.isNull() or MatchesTable.receiveFrom.isNull())
            }) {
                it[MatchesTable.currentMatchingGroup] = null
                it[MatchesTable.isDropped] = true
            }
        }
    }
}
