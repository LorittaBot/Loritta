package net.perfectdreams.loritta.lorituber

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariProxyConnection
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteConnection
import java.io.File
import java.sql.PreparedStatement
import java.util.*
import kotlin.concurrent.thread
import kotlin.time.measureTimedValue

fun main() {
    File("videos_from_memory.db").delete()

    // We want to always create a database, no matter if it exists or not
    val cfg: HikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite::memory:"
        maximumPoolSize = 1
    }

    val dataSource = HikariDataSource(cfg)

    dataSource.connection.use { conn ->
        val sqliteConn = (conn as HikariProxyConnection).unwrap(SQLiteConnection::class.java)
        println("Loading database to memory...")
        sqliteConn.database.restore("main", "videos_from_memory.db", null)

        /* conn.createStatement().use { stmt ->
            stmt.executeUpdate("PRAGMA journal_mode = WAL;")
            stmt.executeUpdate("PRAGMA synchronous = normal;")
            stmt.executeUpdate("PRAGMA temp_store = memory;")
            stmt.executeUpdate("PRAGMA mmap_size = 30000000000;")
            stmt.executeUpdate("PRAGMA page_size = 32768;")
        } */
    }

    val videosDatabase = Database.connect(dataSource)

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        println("Backing up the database...")
        dataSource.connection.use { conn ->
            val sqliteConn = (conn as HikariProxyConnection).unwrap(SQLiteConnection::class.java)
            sqliteConn.database.backup("main", "videos_from_memory.db", null)
        }
        println("Done!")
    })

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
            dataSource.connection.use {
                val stmt = it.prepareStatement("UPDATE LoriTuberVideosTest SET views = views + 1, likes = likes + 1, dislikes = dislikes + 1;")
                stmt.execute()
            }
        }

        println(d)
    }
}