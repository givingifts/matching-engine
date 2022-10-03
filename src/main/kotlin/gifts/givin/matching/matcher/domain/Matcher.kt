package gifts.givin.matching.matcher.domain

import gifts.givin.matching.common.domain.MatchingGroupId
import mu.KLogger
import mu.KotlinLogging
import java.time.Instant
import kotlin.random.Random

class Matcher(
    private val repository: MatchRepository,
    private val matchingGroupId: MatchingGroupId
) {
    private val logger: KLogger = KotlinLogging.logger("Matcher for $matchingGroupId")

    fun doMatchingRound() {
        logger.info("Matcher: Matching $matchingGroupId")
        val matchedUsers = getMatches()

        matchUsersWithEachOther(matchedUsers)

        verify()
        repository.markInstancesAsDone(matchingGroupId)

        val numberOfUnmatchedUsers = repository.getNumberOfUnmatchedUsers(matchingGroupId)
        logger.info("Matcher: Matched $matchingGroupId, Matched users: ${matchedUsers.size - numberOfUnmatchedUsers}, not matched: $numberOfUnmatchedUsers")
    }

    private fun matchUsersWithEachOther(matchedUsers: IntArray) {
        if (matchedUsers.size > 2) {
            matchedUsers.forEachIndexed { index, userId ->
                repository.match(userId, matchedUsers.getNextUser(index), matchingGroupId)
            }
        }
    }

    private fun getMatches(): IntArray {
        // Get all users
        val allUsers = repository.getAllUsers(matchingGroupId).map { it.userId }.toIntArray()

        // No users = nothing to do!
        if (allUsers.isEmpty()) {
            logger.info("Matching result: No users to match")
            return allUsers
        }

        // When we have a single user, drop them
        if (allUsers.size == 1) {
            repository.drop(allUsers.first(), matchingGroupId)
            logger.info("Matching result: Only 1 user (${allUsers.first()}) to match, dropping them")
            return allUsers
        }

        // When we have 2 users, match them together
        if (allUsers.size == 2) {
            val firstUser = allUsers.first()
            val secondUser = allUsers.last()
            repository.match(firstUser, secondUser, matchingGroupId)
            repository.match(secondUser, firstUser, matchingGroupId)
            logger.info("Matching result: Only 2 users (${firstUser},  ${secondUser}) to match, matching them together")
            return allUsers
        }

        shuffleUsers(allUsers)

        val doNotMatch = repository.getAllDoNotMatch(matchingGroupId)

        // Check against do not match list
        for (i in allUsers.indices) {
            val firstUser = allUsers[i]
            val nextUser = allUsers.getNextUser(i)
            val previousUser = allUsers.getPreviousUser(i)

            if (doNotMatch.isInDoNotMatch(firstUser, nextUser) || doNotMatch.isInDoNotMatch(firstUser, previousUser)) {
                doNotMatch.trySwap(allUsers, firstUser, logger)
            }
        }
        return allUsers
    }

    private fun shuffleUsers(allUsers: IntArray) {
        val seed = Instant.now().epochSecond
        logger.info("Matcher: Starting match with ${allUsers.size} users, seed used for random user distribution: $seed")
        val random = Random(seed)
        allUsers.shuffle(random)
    }

    private fun verify() {
        val allUsers =
            repository.getAllUsers(matchingGroupId).filter { it.sendTo != null && it.receiveFrom != null }
                .toTypedArray()
        if (allUsers.isEmpty()) {
            logger.error("Verifier: All users were dropped or none were matched")
            return
        }
        val firstUser = allUsers.first()
        var count = 0
        var currentUser = firstUser
        while (true) {
            val sendToUser = allUsers.find { it.userId == currentUser.sendTo }
            val receiveFromUser = allUsers.find { it.userId == currentUser.receiveFrom }
            if (sendToUser?.receiveFrom != currentUser.userId || receiveFromUser?.sendTo != currentUser.userId) {
                logger.error("Verifier: Matching couldn't be verified, user id ${currentUser.userId} has problems")
                return
            }
            currentUser = sendToUser
            count++
            if (count == allUsers.size) {
                logger.info("Verifier: Matching verified with ${allUsers.size} users!")
                break
            }
        }
        if (currentUser.userId != firstUser.userId) {
            logger.info("Verifier: Couldn't verify matching, loop is incomplete or not fully formed! ")
            return
        }
    }
}