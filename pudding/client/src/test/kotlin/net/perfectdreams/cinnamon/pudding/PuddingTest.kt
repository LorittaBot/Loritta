package net.perfectdreams.cinnamon.pudding

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
class PuddingTest {
    @Container
    @JvmField
    val postgres = PostgreSQLContainer("postgres:14")

    @Test
    fun `check if all tables can be successfully created`() {
        val pudding = PuddingTestUtils.createPostgreSQLPudding(postgres)

        runBlocking {
            pudding.createMissingTablesAndColumns { true }
        }
        pudding.shutdown()
    }
}