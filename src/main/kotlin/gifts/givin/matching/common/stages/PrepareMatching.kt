package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class PrepareMatchingOptions(
    var premium: Boolean = false,
    var worldwide: Boolean = false,
    var groups: Boolean = false
)

object PrepareMatching : Stage<PrepareMatchingOptions> {
    override fun run(func: PrepareMatchingOptions.() -> Unit) {
        val options = PrepareMatchingOptions()
        func(options)

        transaction {
            MatchesTable.update({
                (MatchesTable.isDropped eq false) and
                    (MatchesTable.isUpgradedToWorldwide eq false) and
                    MatchesTable.currentMatchingGroup.isNull() and
                    ifCondition(options.premium) { MatchesTable.isPremium eq true } and
                    ifCondition(!options.worldwide) { MatchesTable.originalMatchingGroup neq "Worldwide" } and
                    ifCondition(!options.groups) { MatchesTable.originalMatchingGroup notLike "Group %" }
            }) {
                it[MatchesTable.currentMatchingGroup] = MatchesTable.originalMatchingGroup
            }
        }
    }

    override fun extraLogs(func: PrepareMatchingOptions.() -> Unit): String {
        val options = PrepareMatchingOptions()
        func(options)
        return buildString {
            append("with ")
            append(if (options.premium) "premium" else "normal")
            append(" matching, ")
            append(if (options.worldwide) "including worldwide" else "not including worldwide")
            append(" matching, ")
            append(if (options.groups) "including international" else "not including international")
            append(" matching")
        }
    }

    private fun ifCondition(condition: Boolean, function: () -> Op<Boolean>) = if (condition) {
        function()
    } else {
        (MatchesTable.isDropped eq false)
    }
}
