package gifts.givin.matching.matcher

import com.uchuhimo.konf.Config
import gifts.givin.matching.common.MatcherSpec
import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.getConfig
import gifts.givin.matching.matcher.domain.Matcher
import gifts.givin.matching.matcher.infrastructure.DBMatchRepository
import kotlin.system.exitProcess

var startupLock = Any()
var config: Config? = null

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("No matching group")
        exitProcess(1)
    }

    val matchingGroupId = args.joinToString(" ")

    // Read config and connect to the DB one instance at a time
    synchronized(startupLock) {
        if (config == null) {
            config = getConfig()
        }
        DB.connect(config!![MatcherSpec.db_url], config!![MatcherSpec.db_user], config!![MatcherSpec.db_pass])
    }

    val matchRepository = DBMatchRepository()

    val matcher = Matcher(matchRepository, matchingGroupId)
    matcher.doMatchingRound()
}

