package gifts.givin.matching

import gifts.givin.matching.common.db.DB
import gifts.givin.matching.manager.main
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class IntegrationTest {
    companion object {
        private val mysqlContainer: MySQLContainer<Nothing> = MySQLContainerWrapper.getContainer()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun matchesEveryone() {
        System.setProperty("matcher.db_url", mysqlContainer.jdbcUrl + "?useSSL=false")
        System.setProperty("matcher.db_user", mysqlContainer.username)
        System.setProperty("matcher.db_pass", mysqlContainer.password)
        System.setProperty("matcher.instances", "1")
        System.setProperty("matcher.use_script", "false")
        DB.connect("${mysqlContainer.jdbcUrl}?useSSL=false", mysqlContainer.username, mysqlContainer.password)
        initSchema()
        main(emptyArray())
        val matches = getAllMatches()
        matches.forEach {
            assertValidMatch(it)
        }
        assertEquals(3, matches.count { it.isDropped })
    }
}
