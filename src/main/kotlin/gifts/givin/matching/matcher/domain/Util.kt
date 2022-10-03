package gifts.givin.matching.matcher.domain

import gifts.givin.matching.common.domain.UserId
import mu.KLogger

fun Map<UserId, List<UserId>>.trySwap(allUsers: IntArray, userId: UserId, logger: KLogger) {
    val indexOfUser = allUsers.indexOf(userId)
    allUsers.forEachIndexed { indexOfCandidate, candidate ->
        if (!isInDoNotMatch(userId, candidate)
            && !isInDoNotMatch(userId, allUsers.getPreviousUser(indexOfCandidate))
            && !isInDoNotMatch(userId, allUsers.getNextUser(indexOfCandidate))
            && !isInDoNotMatch(candidate, allUsers.getPreviousUser(indexOfUser))
            && !isInDoNotMatch(candidate, allUsers.getNextUser(indexOfUser))
        ) {
            logger.info("Do not match: Found suitable candidate, swapping $userId with $candidate")
            allUsers[indexOfUser] = candidate
            allUsers[indexOfCandidate] = userId
            return
        }
    }
    logger.info("Do not match: Found no suitable candidates, $userId is matched with someone from their do not match list!")
}

fun IntArray.getNextUser(index: Int): UserId {
    if (index == size - 1) {
        return first()
    }
    return get(index + 1)
}

fun IntArray.getPreviousUser(index: Int): UserId {
    if (index == 0) {
        return last()
    }
    return get(index - 1)
}

fun Map<UserId, List<UserId>>.isInDoNotMatch(firstUser: UserId, secondUser: UserId) =
    (get(firstUser)?.contains(secondUser) ?: false || get(secondUser)?.contains(firstUser) ?: false)
