package gifts.givin.matching.common.db

import gifts.givin.matching.common.domain.NoMatchBehaviour
import gifts.givin.matching.common.domain.PremiumNoMatchBehaviour
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

typealias MatchesTable = Matches
typealias MatchingGroupTable = MatchingGroup
typealias MatchingInstancesTable = MatchingInstances
typealias DoNotMatchTable = DoNotMatch

object Matches : IntIdTable() {
    val userId = integer("UserId")
    val currentMatchingGroup = varchar("CurrentMatchingGroup", 40).nullable().default(null)
    val originalMatchingGroup = varchar("OriginalMatchingGroup", 40)
    val isPremium = bool("IsPremium").default(false)
    val isDropped = bool("IsDropped").default(false)
    val isUpgradedToWorldwide = bool("IsUpgradedToWorldwide").default(false)
    val noMatchBehaviour = customEnumeration(
        "NoMatchBehaviour",
        "ENUM('DROP', 'INTERNATIONAL_WORLDWIDE', 'WORLDWIDE', 'INTERNATIONAL_DROP')",
        { value -> NoMatchBehaviour.valueOf(value as String) },
        { it.toString() }
    )
    val premiumNoMatchBehaviour = customEnumeration(
        "PremiumNoMatchBehaviour",
        "ENUM('DROP', 'STANDARD', 'WORLDWIDE')",
        { value -> PremiumNoMatchBehaviour.valueOf(value as String) },
        { it.toString() }
    ).nullable().default(null)
    val sendTo = integer("SendTo").nullable()
    val receiveFrom = integer("ReceiveFrom").nullable()
}

object MatchingGroup : Table() {
    val id = varchar("id", 40)
    val parent = varchar("Parent", 40).nullable()
    override val primaryKey = PrimaryKey(id, name = "PK_MatchingGroup")
}

object MatchingInstances : IntIdTable() {
    val done = bool("Done").default(false)
    val matchingGroups = text("MatchingGroups").nullable()
}

object DoNotMatch : IntIdTable() {
    val firstUserId = integer("FirstUserId")
    val secondUserId = integer("SecondUserId")
}
