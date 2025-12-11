package net.perfectdreams.cinnamon.pudding

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import org.testcontainers.containers.PostgreSQLContainer

object PuddingTestUtils {
    fun createPostgreSQLPudding(postgres: PostgreSQLContainer<*>) = Pudding.createPostgreSQLPudding(122, "${postgres.containerIpAddress}:${postgres.getMappedPort(5432)}", postgres.databaseName, postgres.username, postgres.password)
}