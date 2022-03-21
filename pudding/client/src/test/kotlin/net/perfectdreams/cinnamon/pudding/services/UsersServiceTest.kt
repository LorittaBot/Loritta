package net.perfectdreams.cinnamon.pudding.services

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.perfectdreams.cinnamon.pudding.PuddingTestUtils
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
class UsersServiceTest {
    @Container
    @JvmField
    val postgres = PostgreSQLContainer("postgres:14")

    @Test
    fun `check if only one achievement is inserted`() {
        val pudding = PuddingTestUtils.createPostgreSQLPudding(postgres)

        runBlocking {
            pudding.createMissingTablesAndColumns {
                it == "UserAchievements" || it == "Profiles"
            }

            pudding.users.getOrCreateUserProfile(UserId(1L))

            val jobs = mutableListOf<Deferred<*>>()

            repeat(128) {
                jobs += async {
                    pudding.users.giveAchievement(
                        UserId(1L),
                        AchievementType.FISHY_SHIP,
                        Clock.System.now()
                    )
                }
            }

            // ".awaitAll()" fails when a coroutine throws an exception, but we don't want that!
            val results = jobs.map {
                try {
                    it.await()
                } catch (e: Exception) {
                    null
                }
            }

            require(results.count { it == true } == 1) { "There is more than one successful achievement!" }

            val count = pudding.transaction {
                UserAchievements.selectAll().count()
            }

            require(count == 1L) { "There is more than one achievement in the table!" }
        }
    }
}