package net.perfectdreams.loritta.lorituber.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.loritta.lorituber.server.tables.LoriTuberChannels.uniqueIndex
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.time.measureTime

fun main() {
    // Findings: UUID is a bit slower than long when using the "uuid" type (which maps to SQLite binary), but not by much
    // Using text is slower (1s+)
    // So maybe it would be better to just use UUID anywhere
    // Long: 535.826100ms
    // UUID: 681.574200ms
    // Long: 460.813700ms
    // UUID: 654.196800ms
    // Long: 465.941200ms
    // UUID: 657.469ms
    // Long: 489.680800ms
    // UUID: 669.598400ms
    val cfg: HikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:testuuid.db"
        maximumPoolSize = 1
    }

    val dataSource = HikariDataSource(cfg)

    dataSource.connection.use { conn ->
        // val sqliteConn = (conn as HikariProxyConnection).unwrap(SQLiteConnection::class.java)
        // println("Loading database to memory...")
        // sqliteConn.database.restore("main", "lorituber.db", null)

        conn.createStatement().use { stmt ->
            stmt.executeUpdate("PRAGMA journal_mode = WAL;")
            stmt.executeUpdate("PRAGMA synchronous = normal;")
            stmt.executeUpdate("PRAGMA temp_store = memory;")
            // stmt.executeUpdate("PRAGMA mmap_size = 30000000000;")
            // stmt.executeUpdate("PRAGMA page_size = 32768;")
        }
    }

    val db = Database.connect(dataSource)

    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(UUIDTestTable)
        SchemaUtils.createMissingTablesAndColumns(LongTestTable)
    }

    var x = 0L
    val sRand = SplittableRandom()
    var i = 0L

    val rUniqueIds = (0 until 100_000).map { UUID.randomUUID() }
    val rUniqueIdsAsString = rUniqueIds.map { it.toString() }

    val timeLong = measureTime {
        transaction(db) {
            repeat(100_000) {
                LongTestTable.insert {
                    it[LongTestTable.id] = x
                    it[LongTestTable.data] = "hewwo"
                }
                x++
            }
        }
    }

    println("Insert Long: $timeLong")

    val timeUniqueId = measureTime {
        transaction(db) {
            for (id in rUniqueIds) {
                UUIDTestTable.insert {
                    it[UUIDTestTable.id] = id
                    it[UUIDTestTable.data] = "hewwo"
                }
                i++
            }
        }
    }

    println("Insert UUID: $timeUniqueId")

    repeat(100) {
        var y = 0L
        val timeLong = measureTime {
            transaction(db) {
                LongTestTable.batchUpsert(0 until 100_000L, LongTestTable.id) {
                    this[LongTestTable.id] = it
                    this[LongTestTable.data] = "uwu"
                }
            }
        }

        println("Long: $timeLong")

        val timeUniqueId = measureTime {
            transaction(db) {
                UUIDTestTable.batchUpsert(rUniqueIds, UUIDTestTable.id) {
                    this[UUIDTestTable.id] = it
                    this[UUIDTestTable.data] = "uwu"
                }
            }
        }

        println("UUID: $timeUniqueId")
    }
}

object UUIDTestTable : Table() {
    val id = uuid("id").uniqueIndex()
    val data = text("data")
}

object LongTestTable : Table() {
    val id = long("id").uniqueIndex()
    val data = text("data")
}