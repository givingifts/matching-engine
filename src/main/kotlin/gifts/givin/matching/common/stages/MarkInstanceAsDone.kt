package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchingInstancesTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class MarkInstanceAsDoneOptions(var instanceId: Int = 0)

object MarkInstanceAsDone : Stage<MarkInstanceAsDoneOptions> {
    override fun run(func: MarkInstanceAsDoneOptions.() -> Unit) {
        val options = MarkInstanceAsDoneOptions()
        func(options)

        transaction {
            MatchingInstancesTable.update({ MatchingInstancesTable.id eq options.instanceId }) {
                it[MatchingInstancesTable.done] = true
            }
        }
    }

    override fun extraLogs(func: MarkInstanceAsDoneOptions.() -> Unit): String {
        val options = MarkInstanceAsDoneOptions()
        func(options)
        return "for instance ${options.instanceId}"
    }
}
