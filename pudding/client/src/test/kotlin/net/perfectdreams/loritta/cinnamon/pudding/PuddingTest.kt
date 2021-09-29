package net.perfectdreams.loritta.cinnamon.pudding

import kotlinx.coroutines.runBlocking
import org.junit.Test

class PuddingTest {
    private fun createPudding() = Pudding.createMemoryPostgreSQLPudding()

    @Test
    fun `check if all tables can be successfully created`() {
        val pudding = createPudding()
        runBlocking {
            pudding.createMissingTablesAndColumns { true }
        }
        pudding.shutdown()
    }
}