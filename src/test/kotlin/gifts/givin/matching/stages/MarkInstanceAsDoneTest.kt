package gifts.givin.matching.stages

import gifts.givin.matching.common.db.MatchingInstancesTable
import gifts.givin.matching.common.stages.MarkInstanceAsDone
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MarkInstanceAsDoneTest : StageTest() {
    @Test
    fun shouldMarkInstanceAsDone() {
        transaction {
            MatchingInstancesTable.insert {
                it[done] = false
                it[matchingGroups] = "Group 1, Group 2"
            }
            MatchingInstancesTable.insert {
                it[done] = false
                it[matchingGroups] = "Group 1, Group 2"
            }
            val count = transaction {
                MatchingInstancesTable.select { MatchingInstancesTable.done eq false }.count()
            }
            assertEquals(2, count)
            MarkInstanceAsDone(logger) {
                instanceId = 1
            }
            val count2 = transaction {
                MatchingInstancesTable.select { MatchingInstancesTable.done eq false }.count()
            }
            assertEquals(1, count2)
            val isDone = transaction {
                MatchingInstancesTable.select { MatchingInstancesTable.id eq 1 }.map { it[MatchingInstancesTable.done] }
            }.first()
            assertTrue(isDone)
        }
    }
}
