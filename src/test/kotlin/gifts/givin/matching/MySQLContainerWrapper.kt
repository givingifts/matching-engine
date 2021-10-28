package gifts.givin.matching

import org.testcontainers.containers.MySQLContainer

object MySQLContainerWrapper {
    private var container: MySQLContainer<Nothing>? = null

    fun getContainer(): MySQLContainer<Nothing> {
        if (container == null) {
            container = MySQLContainer<Nothing>("mysql:latest").withReuse(true)
            container!!.start()
        }
        return container!!
    }
}
