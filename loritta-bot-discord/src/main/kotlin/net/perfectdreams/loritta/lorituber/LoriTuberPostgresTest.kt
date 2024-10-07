package net.perfectdreams.loritta.lorituber

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import kotlin.time.measureTimedValue

fun main() {
    File("videos.db").delete()

    // We want to always create a database, no matter if it exists or not
    val cfg: HikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://127.0.0.1/cinnamon"
        username = "postgres"
        password = "postgres"
        maximumPoolSize = 1
    }

    val dataSource = HikariDataSource(cfg)

    val videosDatabase = Database.connect(dataSource)

    transaction(videosDatabase) {
        SchemaUtils.createMissingTablesAndColumns(
            LoriTuberVideosTest
        )
    }

    transaction(videosDatabase) {
        repeat(100_000) {
            if (it % 5_000 == 0) {
                println(it)
            }

            LoriTuberVideosTest.insert {
                it[LoriTuberVideosTest.name] = UUID.randomUUID().toString()
                it[LoriTuberVideosTest.views] = 0
                it[LoriTuberVideosTest.likes] = 0
                it[LoriTuberVideosTest.dislikes] = 0
            }
        }
    }

    repeat(1_000_000) {
        val d = measureTimedValue {
            transaction(videosDatabase) {
                LoriTuberVideosTest.update {
                    with(SqlExpressionBuilder) {
                        it[LoriTuberVideosTest.views] = LoriTuberVideosTest.views + 1
                        it[LoriTuberVideosTest.likes] = LoriTuberVideosTest.likes + 1
                        it[LoriTuberVideosTest.dislikes] = LoriTuberVideosTest.dislikes + 1
                    }
                }

                LoriTuberVideosTest.selectAll().count()
            }
        }

        println(d)
    }
}