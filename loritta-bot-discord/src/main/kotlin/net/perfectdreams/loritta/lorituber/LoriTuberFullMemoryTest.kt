package net.perfectdreams.loritta.lorituber

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

data class TestVideo(
    val title: String,
    var views: Int,
    var likes: Int,
    var dislikes: Int
)

fun main() {
    val videos = mutableListOf<TestVideo>()

    repeat(1_000_000) {
        if (it % 5_000 == 0) {
            println(it)
        }

        videos.add(TestVideo(UUID.randomUUID().toString(), 0, 0, 0))
    }

    repeat(10) {
        val d = measureTimedValue {
            videos.forEach {
                it.views++
                it.likes++
                it.dislikes++
            }

            videos.size
        }

        println(d)
    }

    File("test_fm.db").delete()

    // We want to always create a database, no matter if it exists or not
    val cfg: HikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:test_fm.db"
        maximumPoolSize = 1
    }

    val dataSource = HikariDataSource(cfg)

    val videosDatabase = Database.connect(dataSource)

    val d = measureTime {
        transaction(videosDatabase) {
            SchemaUtils.createMissingTablesAndColumns(
                LoriTuberVideosTest
            )

            LoriTuberVideosTest.batchInsert(
                videos
            ) {
                this[LoriTuberVideosTest.name] = it.title
                this[LoriTuberVideosTest.likes] = it.likes
                this[LoriTuberVideosTest.dislikes] = it.dislikes
                this[LoriTuberVideosTest.views] = it.views
            }
        }
    }

    println("${d}")
}