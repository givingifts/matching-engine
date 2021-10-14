package gifts.givin.matching.manager

import com.uchuhimo.konf.Config
import gifts.givin.matching.common.MatcherSpec
import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.getConfig
import gifts.givin.matching.common.stages.CleanupInstances
import gifts.givin.matching.common.stages.CleanupPremium
import gifts.givin.matching.common.stages.DoChecks
import gifts.givin.matching.common.stages.PremiumBehaviour
import gifts.givin.matching.common.stages.PrepareMatching
import gifts.givin.matching.common.stages.PromoteMatchingGroups
import gifts.givin.matching.common.stages.StartMatcher
import gifts.givin.matching.common.stages.WaitForMatchers
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger("Manager")

lateinit var config: Config
var shouldMatchWorldwide = false
var shouldMatchGroups = false

fun main() {
    var unmatchedUsers = 0L
    val time = measureTimeMillis {
        logger.info { "Starting Matching manager" }
        config = getConfig()
        DB.connect(config[MatcherSpec.db_url], config[MatcherSpec.db_user], config[MatcherSpec.db_pass])
        val allMatchingGroups = DB.getAllMatchingGroups()
        logger.info { "Initialization complete" }

        CleanupInstances(logger) {}
        PrepareMatching(logger) {
            premium = true
            worldwide = false
            groups = true
        }
        StartMatcher(logger) {
            maxInstances = config[MatcherSpec.max_instances]
            percentageInstances = config[MatcherSpec.instances_percentage]
            useScript = config[MatcherSpec.use_script]
            groups = DB.getMatchingGroups()
        }
        WaitForMatchers(logger) {}
        CleanupInstances(logger) {}
        PremiumBehaviour(logger) {}
        PrepareMatching(logger) {
            premium = true
            worldwide = true
            groups = true
        }
        StartMatcher(logger) {
            maxInstances = config[MatcherSpec.max_instances]
            percentageInstances = config[MatcherSpec.instances_percentage]
            useScript = config[MatcherSpec.use_script]
            groups = DB.getMatchingGroups()
        }
        WaitForMatchers(logger) {}
        CleanupInstances(logger) {}
        CleanupPremium(logger) {}
        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = false
        }
        do {
            val matchingGroups = DB.getMatchingGroups()
            if (matchingGroups.isEmpty()) {
                prepareNextMatching()
            }
            StartMatcher(logger) {
                maxInstances = config[MatcherSpec.max_instances]
                percentageInstances = config[MatcherSpec.instances_percentage]
                useScript = config[MatcherSpec.use_script]
                groups = matchingGroups
            }
            WaitForMatchers(logger) {}
            CleanupInstances(logger) {}
            PromoteMatchingGroups(logger) {
                groups = allMatchingGroups
            }
            unmatchedUsers = getUnmatchedUsers()
        } while (unmatchedUsers > 0)
    }
    logger.info { "Matching complete with $unmatchedUsers unmatched users. Time taken: $time ms" }
    DoChecks(logger) {}
}

fun prepareNextMatching() {
    if (!shouldMatchGroups) {
        shouldMatchGroups = true
        PrepareMatching(logger) {
            premium = false
            worldwide = false
            groups = true
        }
    } else if (!shouldMatchWorldwide) {
        shouldMatchWorldwide = true
        PrepareMatching(logger) {
            premium = false
            worldwide = true
            groups = true
        }
    }
}

private fun getUnmatchedUsers() = transaction {
    MatchesTable
        .select { MatchesTable.sendTo.isNull() and (MatchesTable.isDropped eq false) }
        .count()
}
