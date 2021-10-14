package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchingInstancesTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

object CleanupInstances : Stage<Unit> {
    override fun run(func: Unit.() -> Unit) {
        transaction {
            MatchingInstancesTable.deleteAll()
        }
    }
}
