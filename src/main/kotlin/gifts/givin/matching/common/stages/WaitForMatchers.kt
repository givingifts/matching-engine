package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchingInstancesTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

object WaitForMatchers : Stage<Unit> {
    override fun run(func: Unit.() -> Unit) {
        while (isNotDone()) {
            TimeUnit.MILLISECONDS.sleep(100)
        }
    }

    private fun isNotDone() = transaction {
        MatchingInstancesTable.selectAll().any { !it[MatchingInstancesTable.done] }
    }
}
