package gifts.givin.matching.stages

import gifts.givin.matching.common.db.DB
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class StageTest {
    protected val logger = KotlinLogging.logger { }

    companion object {
        @Container
        private val mysqlContainer = MySQLContainer<Nothing>("mysql:latest")
    }

    @BeforeEach
    fun setup() {
        DB.connect("${mysqlContainer.jdbcUrl}?useSSL=false", mysqlContainer.username, mysqlContainer.password)
        Thread.currentThread().contextClassLoader.getResource("schema.sql")
            .readText()
            .split(";")
            .map { it.trim() }
            .filterNot { it.isBlank() }
            .filterNot { it == ";" }
            .forEach { statement -> transaction { exec(statement) } }
    }
}