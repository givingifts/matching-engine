package gifts.givin.matching.common.db

import gifts.givin.matching.common.domain.PremiumBehaviour
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

typealias MatchesTable = Matches
typealias MatchingGroupTable = MatchingGroup
typealias MatchingInstancesTable = MatchingInstances

object Matches : IntIdTable() {
    val userId = integer("UserId")
    val currentMatchingGroup = varchar("CurrentMatchingGroup", 10).nullable().default(null)
    val originalMatchingGroup = varchar("OriginalMatchingGroup", 10)
    val isPremium = bool("IsPremium").default(false)
    val isDropped = bool("IsDropped").default(false)
    val premiumBehaviour = customEnumeration(
        "PremiumBehaviour",
        "ENUM('WORLDWIDE', 'NONPREMIUM', 'DROP')",
        { value -> PremiumBehaviour.valueOf(value as String) },
        { it.toString() }
    ).nullable().default(null)
    val sendTo = integer("SendTo").nullable()
    val receiveFrom = integer("ReceiveFrom").nullable()
}

object MatchingGroup : Table() {
    val id = varchar("id", 10)
    val parent = varchar("Parent", 10).nullable()
    override val primaryKey = PrimaryKey(id, name = "PK_MatchingGroup")
}

object MatchingInstances : IntIdTable() {
    val done = bool("Done").default(false)
    val matchingGroups = text("MatchingGroups").nullable()
}
