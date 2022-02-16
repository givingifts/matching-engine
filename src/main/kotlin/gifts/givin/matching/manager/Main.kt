package gifts.givin.matching.manager

import com.uchuhimo.konf.Config
import gifts.givin.matching.common.MatcherSpec
import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.getConfig
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


private val logger = KotlinLogging.logger("Manager")

lateinit var config: Config

fun main(args: Array<String>) {
    val time = measureTimeMillis {
        logger.info { "Starting Matching manager" }
        config = getConfig()
        DB.connect(config[MatcherSpec.db_url], config[MatcherSpec.db_user], config[MatcherSpec.db_pass])
        DB.cleanupInstances()
        logger.info { "Initialization complete" }
        matchingRound()
    }
    logger.info { "Matching complete with ${DB.getNumberOfUnmatchedUsers()} unmatched users. Time taken: $time ms" }
}

fun matchingRound() {
    val matchingGroups = DB.getMatchingGroupsInUse().toMutableList()
    val es = Executors.newCachedThreadPool()
    while (matchingGroups.isNotEmpty()) {
        if (DB.getNotDoneInstances() < config[MatcherSpec.max_instances]) {
            val matchingGroup = matchingGroups.removeFirst()
            DB.addInstance(matchingGroup)
            es.execute {
                if (config[MatcherSpec.use_script]) {
                    ProcessBuilder("./start_instance.sh", matchingGroup)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor(60, TimeUnit.MINUTES)
                } else {
                    gifts.givin.matching.matcher.main(arrayOf(matchingGroup))
                }
            }
        } else {
            TimeUnit.MILLISECONDS.sleep(100)
        }
    }
    es.shutdown()
    es.awaitTermination(5, TimeUnit.MINUTES)
    DB.cleanupInstances()
    DB.cleanupMatching()
}
