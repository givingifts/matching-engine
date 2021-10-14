package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchingInstancesTable
import gifts.givin.matching.common.domain.MatchingGroup
import mu.KotlinLogging
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.roundToInt

data class StartMatcherOptions(
    var maxInstances: Int = 10,
    var percentageInstances: Int = 50,
    var groups: Map<MatchingGroup, Long> = emptyMap(),
    var useScript: Boolean = false
)

object StartMatcher : Stage<StartMatcherOptions> {
    override fun run(func: StartMatcherOptions.() -> Unit) {
        val options = StartMatcherOptions()
        func(options)
        var instances =
            min(options.maxInstances, options.groups.size * (options.percentageInstances / 100.0).roundToInt())
        val lists = getMatchingGroupsPerInstance(instances, options.groups)
        instances = lists.size
        (1..instances).forEach {
            startInstance(it, lists.getOrNull(it - 1).orEmpty().map { it.id }, options.useScript)
        }
    }

    override fun extraLogs(func: StartMatcherOptions.() -> Unit): String {
        val options = StartMatcherOptions()
        func(options)
        return "with ${options.maxInstances} max instances, allocating ${options.percentageInstances}% of groups, ${if (options.useScript) "using new instances" else "running in the same host"} for ${options.groups}"
    }

    private fun startInstance(instanceId: Int, matchingGroups: List<String>, useScript: Boolean) {
        addInstance(instanceId, matchingGroups)
        Thread {
            if (useScript) {
                ProcessBuilder("./start_instance.sh", instanceId.toString())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor(60, TimeUnit.MINUTES)
            } else {
                gifts.givin.matching.matcher.main(arrayOf(instanceId.toString()))
            }
        }.start()
    }

    private fun addInstance(instanceId: Int, matchingGroups: List<String>) = transaction {
        MatchingInstancesTable.insert {
            it[MatchingInstancesTable.id] = instanceId
            it[MatchingInstancesTable.done] = false
            it[MatchingInstancesTable.matchingGroups] = matchingGroups.joinToString(",")
        }
    }

    private fun getMatchingGroupsPerInstance(
        instances: Int,
        matchingGroups: Map<MatchingGroup, Long>
    ): List<List<MatchingGroup>> {
        // Early return if we cant do better :D
        if (instances >= matchingGroups.size) {
            return matchingGroups.keys.chunked(1)
        }
        // Order from biggest to smallest and assign one to each group
        val orderedGroups = matchingGroups.entries.map { it.toPair() }.sortedByDescending { it.second }
        val lists = (1..instances).map { mutableListOf(orderedGroups[it - 1]) }
        // Order the groups from smallest to biggest
        var newList = orderedGroups.drop(instances - 1)

        // While we still have groups
        while (newList.isNotEmpty()) {
            // Get the one with the least users
            val element = newList.first()
            newList = newList.drop(1)

            // And add it to the group with the least users
            lists.minByOrNull { it.sumOf { it.second } }!!.add(element)
        }
        val logger = KotlinLogging.logger("MatcherGrouper")
        logger.info { "Original groups: ${orderedGroups.map { it.first.id to it.second }}" }
        logger.info { "Separated into $instances groups, ${lists.mapIndexed { index, value -> "Group $index with ${value.sumOf { it.second }} users" }}" }
        return lists.map { it.map { it.first } }
    }
}
