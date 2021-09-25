package net.perfectdreams.loritta.cinnamon.pudding

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.jetbrains.exposed.sql.Database

class EmbeddedPSQLPudding(val embeddedPostgres: EmbeddedPostgres, database: Database) : Pudding(database) {
    override fun shutdown() {
        puddingTasks.shutdown()
        embeddedPostgres.close()
    }
}