package gifts.givin.matching.matcher

import com.uchuhimo.konf.Config
import gifts.givin.matching.common.MatcherSpec
import gifts.givin.matching.common.db.DB
import gifts.givin.matching.common.db.MatchingInstancesTable
import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId
import gifts.givin.matching.common.getConfig
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.system.exitProcess

lateinit var config: Config

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("No matching group")
        exitProcess(1)
    }
    val matchingGroupId = args.joinToString(" ")
    val logger = KotlinLogging.logger("Matcher for $matchingGroupId")
    config = getConfig()
    DB.connect(config[MatcherSpec.db_url], config[MatcherSpec.db_user], config[MatcherSpec.db_pass])
    logger.info("Matching $matchingGroupId")
    match(matchingGroupId)
    markInstanceAsDone(matchingGroupId)
    logger.info("Matched $matchingGroupId")
}

fun markInstanceAsDone(matchingGroupId: MatchingGroupId) = transaction {
    MatchingInstancesTable.update({ MatchingInstancesTable.matchingGroup eq matchingGroupId }) {
        it[MatchingInstancesTable.done] = true
    }
}

fun match(matchingGroup: MatchingGroupId) {
    var unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup)
    while (unmatchedUsers >= 2L) {
        if (unmatchedUsers == 2L) {
            val userWithoutSendTo = DB.getUserWithoutSendTo(matchingGroup)
            val userWithoutReceiveFrom = DB.getUserWithoutReceiveFrom(matchingGroup, emptyList())
            if (userWithoutSendTo != null && userWithoutReceiveFrom != null && userWithoutReceiveFrom.userId != userWithoutSendTo.userId) {
                DB.matchUsers(userWithoutSendTo.userId, userWithoutReceiveFrom.userId)
                unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup)
            }
        } else {
            val sender = DB.getUserWithoutSendTo(matchingGroup)?.userId ?: continue
            val doNoMatchList = DB.getDoNotMatch(sender)
            val receiver = DB.getUserWithoutReceiveFrom(matchingGroup, doNoMatchList)?.userId ?: continue
            DB.matchUsers(sender, receiver)
            unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup)
        }
    }
    if (unmatchedUsers == 1L) {
        val withoutReceiver = DB.getUserWithoutSendTo(matchingGroup)
        if (withoutReceiver == null) {
            val withoutSender = DB.getUserWithoutReceiveFrom(matchingGroup, emptyList()) ?: return
            threeWayMatchWithoutSender(withoutSender, matchingGroup)
        } else {
            if (withoutReceiver.receiveFrom == null) {
                threeWayMatchWithoutAny(withoutReceiver.userId, matchingGroup)
            } else {
                error("This should never happen, user without sendTo ${withoutReceiver.userId}. Rerun matching and tell @urielsalis")
            }
        }
    }
}

fun threeWayMatchWithoutSender(user: Match, matchingGroup: MatchingGroupId) {
    val randomMatch = DB.getRandomMatch(matchingGroup)
    val originalSender = randomMatch.first
    val originalReceiver = randomMatch.second
    val originalUserSendTo = user.sendTo!!
    /*
     * Someone -> OriginalSender -> originalReceiver -> SomeoneElse
     * null -> User -> SomeoneElse2
     *
     * Someone -> OriginalSender -> User -> SomeoneElse2 -> originalReceiver -> SomeoneElse
     */
    DB.matchUsers(originalSender.userId, user.userId)
    DB.matchUsers(originalUserSendTo, originalReceiver.userId)
}

fun threeWayMatchWithoutAny(userId: UserId, matchingGroup: MatchingGroupId) {
    val randomMatch = DB.getRandomMatch(matchingGroup)
    val originalSender = randomMatch.first.userId
    val originalReceiver = randomMatch.second.userId
    /*
     * Someone -> OriginalSender -> originalReceiver -> Someone
     * null -> UserId -> null
     *
     * OriginalSender -> UserId -> originalReceiver
     */
    /* 1 -> 2 -> 1
     * null -> 3 -> null
     *
     *
     */
    DB.matchUsers(originalSender, userId)
    DB.matchUsers(userId, originalReceiver)
}