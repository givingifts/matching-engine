package gifts.givin.matching.matcher.domain

import gifts.givin.matching.common.domain.Match
import gifts.givin.matching.common.domain.MatchingGroupId
import gifts.givin.matching.common.domain.UserId

interface MatchRepository {
    fun getAllUsers(matchingGroupId: MatchingGroupId): List<Match>
    fun getAllDoNotMatch(matchingGroupId: MatchingGroupId): Map<UserId, List<UserId>>
    fun match(sender: UserId, receiver: UserId, matchingGroupId: MatchingGroupId)
    fun drop(user: UserId, matchingGroupId: MatchingGroupId)
    fun markInstancesAsDone(matchingGroupId: MatchingGroupId)
    fun getNumberOfUnmatchedUsers(matchingGroupId: MatchingGroupId): Long
}