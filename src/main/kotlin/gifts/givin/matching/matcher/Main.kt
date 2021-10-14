package gifts.givin.matching.matcher

import com.uchuhimo.konf.Config
import gifts.givin.matching.common.MatcherSpec
import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.db.MatchingGroupTable
import gifts.givin.matching.common.db.MatchingInstancesTable
import gifts.givin.matching.common.domain.MatchingGroup
import gifts.givin.matching.common.getConfig
import gifts.givin.matching.common.stages.MarkInstanceAsDone
import gifts.givin.matching.common.stages.MatchWithEachOther
import gifts.givin.matching.common.stages.MatchingRound
import mu.KLogger
import mu.KotlinLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.exitProcess

lateinit var config: Config

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("No instance id")
        exitProcess(1)
    }
    val instanceId = args.first().toInt()
    val logger = KotlinLogging.logger("Matcher $instanceId")
    config = getConfig()
    DB.connect(config[MatcherSpec.db_url], config[MatcherSpec.db_user], config[MatcherSpec.db_pass])
    val matchingGroups = getMatchingGroupsForInstance(instanceId).toMutableList()
    if (matchingGroups.size > 0) {
        logger.info { "Matching ${matchingGroups.joinToString(",") { it.id }}" }
        matchMatchingGroup(matchingGroups, logger)
    }

    MarkInstanceAsDone(logger) {
        this.instanceId = instanceId
    }
}

fun matchMatchingGroup(matchingGroups: List<MatchingGroup>, logger: KLogger) {
    MatchingRound(logger) {
        groups = matchingGroups.toMutableList()
    }
    MatchWithEachOther(logger) {
        groups = matchingGroups
    }
}

fun getMatchingGroupsForInstance(instanceId: Int): List<MatchingGroup> = transaction {
    val groups = MatchingInstancesTable
        .slice(MatchingInstancesTable.matchingGroups)
        .select { MatchingInstancesTable.id eq instanceId }
        .limit(1)
        .map { it[MatchingInstancesTable.matchingGroups]?.split(",").orEmpty() }
        .firstOrNull()
        .orEmpty()

    MatchingGroupTable
        .select { MatchingGroupTable.id inList groups }
        .map { MatchingGroup(it[MatchingGroupTable.id], it[MatchingGroupTable.parent]) }
}
