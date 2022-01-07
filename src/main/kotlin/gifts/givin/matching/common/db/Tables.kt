package gifts.givin.matching.common.db

import org.jetbrains.exposed.dao.id.IntIdTable

typealias MatchesTable = Matches
typealias MatchingInstancesTable = MatchingInstances
typealias DoNotMatchTable = DoNotMatch

object Matches : IntIdTable() {
    val userId = integer("UserId")
    val matchingGroup = varchar("MatchingGroup", 40)
    val sendTo = integer("SendTo").nullable()
    val receiveFrom = integer("ReceiveFrom").nullable()
    val isMatched = bool("IsMatched").default(false)
}

object MatchingInstances : IntIdTable() {
    val done = bool("Done").default(false)
    val matchingGroup = varchar("MatchingGroup", 10).nullable()
}

object DoNotMatch : IntIdTable() {
    val firstUserId = integer("FirstUserId")
    val secondUserId = integer("SecondUserId")
}
