package gifts.givin.matching.manager

import com.uchuhimo.konf.Config
import gifts.givin.matching.common.MatcherSpec
import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.db.MatchesTable
import gifts.givin.matching.common.getConfig
import gifts.givin.matching.common.stages.CleanupInstances
import gifts.givin.matching.common.stages.CleanupPremium
import gifts.givin.matching.common.stages.DoChecks
import gifts.givin.matching.common.stages.NoMatchBehaviour
import gifts.givin.matching.common.stages.PremiumNoMatchBehaviour
import gifts.givin.matching.common.stages.PrepareMatching
import gifts.givin.matching.common.stages.PrepareWorldwideMatching
import gifts.givin.matching.common.stages.SendDiscordMessage
import gifts.givin.matching.common.stages.StartMatcher
import gifts.givin.matching.common.stages.WaitForMatchers
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger("Manager")

lateinit var config: Config

var matchPremium = true
var matchWorldwide = false
var matchGroups = true
fun main(args: Array<String>) {
    val time = measureTimeMillis {
        logger.info { "Starting Matching manager" }
        config = getConfig()
        DB.connect(config[MatcherSpec.db_url], config[MatcherSpec.db_user], config[MatcherSpec.db_pass])
        val allMatchingGroups = DB.getAllMatchingGroups()
        logger.info { "Initialization complete" }

        if (config[MatcherSpec.hook].isNotBlank() && args.isNotEmpty()) {
            SendDiscordMessage(logger) {
                url = config[MatcherSpec.hook]
                exchange = args.joinToString(" ")
            }
        }
        CleanupInstances(logger) {}
        matchingRound()
        PremiumNoMatchBehaviour(logger) {
            groups = allMatchingGroups
        }
        matchWorldwide = true
        matchingRound()
        CleanupPremium(logger) {}
        matchPremium = false
        matchWorldwide = false
        matchGroups = false
        matchingRound()
        NoMatchBehaviour(logger) {
            groups = allMatchingGroups
        }
        matchGroups = true
        matchingRound()
        NoMatchBehaviour(logger) {
            groups = allMatchingGroups
        }
        matchWorldwide = true
        matchingRound()
    }
    logger.info { "Matching complete with ${getUnmatchedUsers()} unmatched users. Time taken: $time ms" }
    DoChecks(logger) {}
}

private fun matchingRound() {
    if (!matchPremium && matchWorldwide) {
        PrepareWorldwideMatching(logger) {}
    } else {
        PrepareMatching(logger) {
            premium = matchPremium
            worldwide = matchWorldwide
            groups = matchGroups
        }
    }
    StartMatcher(logger) {
        maxInstances = config[MatcherSpec.max_instances]
        percentageInstances = config[MatcherSpec.instances_percentage]
        useScript = config[MatcherSpec.use_script]
        groups = DB.getMatchingGroups()
    }
    WaitForMatchers(logger) {}
    CleanupInstances(logger) {}
}

private fun getUnmatchedUsers() = transaction {
    MatchesTable
        .select { MatchesTable.sendTo.isNull() and (MatchesTable.isDropped eq false) }
        .count()
}
