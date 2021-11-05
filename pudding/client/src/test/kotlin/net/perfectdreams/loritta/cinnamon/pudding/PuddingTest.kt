package net.perfectdreams.loritta.cinnamon.pudding

import kotlinx.coroutines.runBlocking

class PuddingTest {
    private fun createPudding() = Pudding.createMemoryPostgreSQLPudding()

    // TODO: Fix this test, for some reason it is *sometimes* causing errors where the database is closed while the queries are still running
    // "received fast shutdown request"
    // @Test
    fun `check if all tables can be successfully created`() {
        val pudding = createPudding()
        runBlocking {
            pudding.createMissingTablesAndColumns { true }
        }
        pudding.shutdown()
    }
}