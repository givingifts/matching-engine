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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.system.exitProcess

var startupLock = Any()
var config: Config? = null

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("No matching group")
        exitProcess(1)
    }

    val droppedList = mutableListOf<UserId>()

    val matchingGroupId = args.joinToString(" ")
    val logger = KotlinLogging.logger("Matcher for $matchingGroupId")
    synchronized(startupLock) {
        if(config == null) {
            config = getConfig()
        }
        DB.connect(config!![MatcherSpec.db_url], config!![MatcherSpec.db_user], config!![MatcherSpec.db_pass])
    }
    logger.info("Matching $matchingGroupId")
    match(matchingGroupId, droppedList)
    markInstanceAsDone(matchingGroupId)
    logger.info("Matched $matchingGroupId")
}

fun markInstanceAsDone(matchingGroupId: MatchingGroupId) = transaction {
    MatchingInstancesTable.update({ MatchingInstancesTable.matchingGroup eq matchingGroupId }) {
        it[MatchingInstancesTable.done] = true
    }
}

fun match(matchingGroup: MatchingGroupId, droppedList: MutableList<UserId>) {
    var unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup, droppedList)
    if (unmatchedUsers == 1L) {
        return
    }
    while (unmatchedUsers >= 2L) {
        if (unmatchedUsers == 2L) {
            val userWithoutSendTo = DB.getUserWithoutSendTo(matchingGroup, droppedList)
            if (userWithoutSendTo != null) {
                val doNoMatchList =
                    DB.getDoNotMatch(userWithoutSendTo.userId, userWithoutSendTo.receiveFrom, droppedList)
                val userWithoutReceiveFrom = DB.getUserWithoutReceiveFrom(matchingGroup, doNoMatchList)
                val userWithoutReceiveFromIgnoringDoNotMatch =
                    DB.getUserWithoutReceiveFrom(matchingGroup, listOf(userWithoutSendTo.userId))
                if (userWithoutReceiveFrom == null) {
                    if (userWithoutReceiveFromIgnoringDoNotMatch?.userId == userWithoutSendTo.receiveFrom && userWithoutSendTo.receiveFrom != null) {
                        // Match users with each other
                        DB.matchUsers(userWithoutSendTo.userId, userWithoutReceiveFromIgnoringDoNotMatch!!.userId)
                    } else {
                        // Drop user
                        droppedList.add(userWithoutSendTo.userId)
                        DB.drop(userWithoutSendTo.userId)
                    }
                } else if (userWithoutReceiveFrom.userId != userWithoutSendTo.userId) {
                    DB.matchUsers(userWithoutSendTo.userId, userWithoutReceiveFrom.userId)
                }
                unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup, droppedList)
            }
        } else {
            val sender = DB.getUserWithoutSendTo(matchingGroup, droppedList) ?: continue
            val senderUserId = sender.userId
            val doNoMatchList = DB.getDoNotMatch(senderUserId, sender.receiveFrom, droppedList)
            val receiver = DB.getUserWithoutReceiveFrom(matchingGroup, doNoMatchList)?.userId
            if(receiver == null) {
                droppedList.add(senderUserId)
                DB.drop(senderUserId)
                unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup, droppedList)
                continue
            }
            DB.matchUsers(senderUserId, receiver)
            unmatchedUsers = DB.getNumberOfUnmatchedUsers(matchingGroup, droppedList)
        }
    }
    if (unmatchedUsers == 1L) {
        val withoutSender = DB.getUserWithoutSendTo(matchingGroup, droppedList)
        if (withoutSender == null) {
            val withoutSender = DB.getUserWithoutReceiveFrom(matchingGroup, droppedList) ?: return
            threeWayMatchWithoutSender(withoutSender, matchingGroup, droppedList)
        } else {
            if (withoutSender.receiveFrom == null) {
                threeWayMatchWithoutAny(withoutSender.userId, matchingGroup, droppedList)
            } else {
                error("This should never happen, user without sendTo ${withoutSender.userId}. Rerun matching and tell @urielsalis")
            }
        }
    }
}

fun threeWayMatchWithoutSender(user: Match, matchingGroup: MatchingGroupId, droppedList: MutableList<UserId>) {
    val randomMatch = DB.getRandomMatch(matchingGroup, droppedList)
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

fun threeWayMatchWithoutAny(userId: UserId, matchingGroup: MatchingGroupId, droppedList: MutableList<UserId>) {
    val randomMatch = DB.getRandomMatch(matchingGroup, droppedList)
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
