package gifts.givin.matching.stages

import gifts.givin.matching.common.db.MatchingInstancesTable
import gifts.givin.matching.common.stages.CleanupInstances
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CleanupInstancesTest : StageTest() {
    @Test
    fun shouldCleanupAllInstances() {
        transaction {
            MatchingInstancesTable.insert {
                it[done] = false
                it[matchingGroups] = "Group 1, Group 2"
            }
            MatchingInstancesTable.insert {
                it[done] = true
                it[matchingGroups] = "Group 1"
            }
        }
        val count = transaction {
            MatchingInstancesTable.selectAll().count()
        }
        assertEquals(2, count)
        CleanupInstances(logger) {}
        val count2 = transaction {
            MatchingInstancesTable.selectAll().count()
        }
        assertEquals(0, count2)
    }
}