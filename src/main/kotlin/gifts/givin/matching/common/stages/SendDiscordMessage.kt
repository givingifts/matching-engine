package gifts.givin.matching.common.stages

import gifts.givin.matching.common.db.MatchesTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class SendDiscordMessageOptions(
    var url: String = "",
    var exchange: String = ""
)

object SendDiscordMessage : Stage<SendDiscordMessageOptions> {
    override fun run(func: SendDiscordMessageOptions.() -> Unit) {
        val options = SendDiscordMessageOptions()
        func(options)

        val numberOfGroups = transaction {
            MatchesTable
                .slice(MatchesTable.originalMatchingGroup)
                .selectAll()
                .groupBy(MatchesTable.originalMatchingGroup)
                .count()
        }

        val numberOfUsers = transaction {
            MatchesTable.selectAll().count()
        }

        val json = """
            {
              "content": null,
              "embeds": [
                {
                  "title": "Matching has started!",
                  "color": 5814783,
                  "fields": [
                    {
                      "name": "Exchange",
                      "value": "${options.exchange}"
                    },
                    {
                      "name": "Registered users",
                      "value": "$numberOfUsers",
                      "inline": true
                    },
                    {
                      "name": "Total countries",
                      "value": "$numberOfGroups",
                      "inline": true
                    }
                  ]
                }
              ],
              "username": "GivinGifts",
              "avatar_url": "https://cdn.discordapp.com/avatars/856255559145881610/754b8b055a2ae1e518d6e9d6ca71ad89.png"
            }
        """.trimIndent()

        val connection = URL(options.url).openConnection() as HttpsURLConnection
        connection.addRequestProperty("Content-Type", "application/json")
        connection.addRequestProperty("User-Agent", "GivinGifts-Matching 1.0")
        connection.doOutput = true
        connection.requestMethod = "POST"
        val stream = connection.outputStream
        stream.write(json.toByteArray())
        stream.flush()
        stream.close()
        connection.inputStream.close()
        connection.disconnect()
    }
}
