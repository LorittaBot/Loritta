package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.datetime.Clock
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.junit.Test
import java.lang.Exception

@OptIn(ExperimentalCoroutinesApi::class)
class UsersServiceTest {
    private fun createPudding() = Pudding.createMemoryPostgreSQLPudding()

    // This tests a race condition, this also checks if the constraints in the UserAchievements table are working
    @Test
    fun `check if only one achievement is inserted`() {
        val pudding = createPudding()

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