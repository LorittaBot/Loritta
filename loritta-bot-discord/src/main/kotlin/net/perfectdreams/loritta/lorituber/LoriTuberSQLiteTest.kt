package net.perfectdreams.loritta.lorituber

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariProxyConnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteConnection
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

fun main() {
    // File("videos_from_memory.db").delete()

    // We want to always create a database, no matter if it exists or not
    val cfg: HikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:file:stuff.db"
        maximumPoolSize = 16
    }

    val dataSource = HikariDataSource(cfg)

    dataSource.connection.use { conn ->
        val sqliteConn = (conn as HikariProxyConnection).unwrap(SQLiteConnection::class.java)
        println("Loading database to memory...")
        sqliteConn.database.restore("main", "videos_from_memory.db", null)

        conn.createStatement().use { stmt ->
            stmt.executeUpdate("PRAGMA journal_mode = WAL;")
            stmt.executeUpdate("PRAGMA synchronous = off;")
            stmt.executeUpdate("PRAGMA temp_store = memory;")
            stmt.executeUpdate("PRAGMA mmap_size = 30000000000;")
            stmt.executeUpdate("PRAGMA page_size = 32768;")
        }
    }

    val videosDatabase = Database.connect(dataSource)

    /* Runtime.getRuntime().addShutdownHook(thread(start = false) {
        println("Backing up the database...")
        dataSource.connection.use { conn ->
            val sqliteConn = (conn as HikariProxyConnection).unwrap(SQLiteConnection::class.java)
            sqliteConn.database.backup("main", "videos_from_memory.db", null)
        }
        println("Done!")
    }) */

    transaction(videosDatabase) {
        SchemaUtils.createMissingTablesAndColumns(
            LoriTuberVideosTest
        )
    }

    /* transaction(videosDatabase) {
        repeat(2_000_000) {
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
    } */

    repeat(1_000_000) {
        val d = measureTimedValue {
            runBlocking {
                var end = 8
                (0 until end).map { ch ->
                    GlobalScope.async {
                        transaction {
                            val x = LoriTuberVideosTest.selectAll()
                                .limit(100)
                                .toList()

                            x.size
                        }
                    }
                }.awaitAll().sum()
            }
        }

        println(d)
    }
}

object LoriTuberVideosTest : LongIdTable() {
    val name = text("name").index()
    val views = integer("views")
    val likes = integer("likes")
    val dislikes = integer("dislikes")
}