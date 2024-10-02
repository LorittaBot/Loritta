package net.perfectdreams.loritta.lorituber.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer.Companion.GENERAL_INFO_KEY
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.GroceryStore
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.WorldInfo
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberChannelData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberCharacterData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberChannel
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberVideo
import net.perfectdreams.loritta.lorituber.server.state.items.LoriTuberGroceryItem
import net.perfectdreams.loritta.lorituber.server.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

object LoriTuberServerLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // We use SQLite because it is WAY faster than PostgreSQL if you only have a single client
        // Because LoriTuber is a game server, then SQLite isn't a bad idea

        // We don't use in-memory databases
        val cfg: HikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:lorituber.db"
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

        val lorituberDatabase = Database.connect(dataSource)

        transaction(lorituberDatabase) {
            SchemaUtils.createMissingTablesAndColumns(
                LoriTuberWorldTicks,
                LoriTuberCharacters,
                LoriTuberVideos,
                LoriTuberCharactersAlt,
                LoriTuberChannels,
                LoriTuberGroceryStores
            )
        }

        val viewerHandles = LoriTuberServerLauncher::class.java.getResourceAsStream("/viewer-handles.txt")
            .readAllBytes()
            .toString(Charsets.UTF_8)
            .lines()

        logger.info { "Attempting to restore state..." }

        val start = System.currentTimeMillis()
        val gameState = transaction(lorituberDatabase) {
            val serverInfo = LoriTuberWorldTicks.selectAll()
                .where { LoriTuberWorldTicks.type eq GENERAL_INFO_KEY }
                .firstOrNull()
            val isNewServer = serverInfo == null

            logger.info { "Is this a new server? $isNewServer" }

            val characters = LoriTuberCharactersAlt.selectAll()
                .map {
                    LoriTuberCharacter(
                        it[LoriTuberCharactersAlt.id],
                        ProtoBuf.decodeFromByteArray<LoriTuberCharacterData>(it[LoriTuberCharactersAlt.data].bytes)
                    )
                }

            val channels = LoriTuberChannels.selectAll()
                .map {
                    LoriTuberChannel(
                        it[LoriTuberChannels.id],
                        ProtoBuf.decodeFromByteArray<LoriTuberChannelData>(it[LoriTuberChannels.data].bytes)
                    )
                }

            val videos = LoriTuberVideos.selectAll()
                .map {
                    LoriTuberVideo(
                        it[LoriTuberVideos.id],
                        ProtoBuf.decodeFromByteArray<LoriTuberVideoData>(it[LoriTuberVideos.data].bytes)
                    )
                }

            val nelsonGroceryStore = LoriTuberGroceryStores.selectAll()
                .where {
                    LoriTuberGroceryStores.shop eq "lorituber:nelson_grocery_store"
                }
                .firstOrNull()
                ?.let {
                    GroceryStore(
                        it[LoriTuberGroceryStores.shop],
                        ProtoBuf.decodeFromByteArray<List<LoriTuberGroceryItemData>>(it[LoriTuberGroceryStores.data].bytes).map {
                            LoriTuberGroceryItem(it)
                        }.toMutableList()
                    )
                } ?: GroceryStore(
                "lorituber:nelson_grocery_store",
                mutableListOf()
            )

            GameState(
                lorituberDatabase,
                serverInfo?.get(LoriTuberWorldTicks.data)?.let {
                    // For some reason we need to specify the type directly here to avoid a crash
                    ProtoBuf.decodeFromByteArray<WorldInfo>(it.bytes)
                } ?: WorldInfo(
                    -1,
                    System.currentTimeMillis(),
                    0,
                    0,
                    0,
                    0,
                ),
                HashMap<Long, LoriTuberCharacter>(characters.size).apply {
                    for (entity in characters) {
                        put(entity.id, entity)
                    }
                },
                HashMap<Long, LoriTuberChannel>(channels.size).apply {
                    for (entity in channels) {
                        put(entity.id, entity)
                    }
                },
                HashMap<Long, LoriTuberVideo>(videos.size).apply {
                    for (entity in videos) {
                        put(entity.id, entity)
                    }
                },
                EnumMap(LoriTuberVideoContentCategory::class.java),
                EnumMap(LoriTuberVideoContentCategory::class.java),
                nelsonGroceryStore,
                viewerHandles
            )
        }

        logger.info { "Successfully restored state! Took ${(System.currentTimeMillis() - start).milliseconds}" }

        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                runBlocking {
                    gameState.persist().join()
                }
            }
        )

        /* Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                println("Backing up the database...")
                dataSource.connection.use { conn ->
                    val sqliteConn = (conn as HikariProxyConnection).unwrap(SQLiteConnection::class.java)
                    sqliteConn.database.backup("main", "lorituber.db", null)
                }
                println("Done!")
            }
        ) */

        val m = LoriTuberServer(
            lorituberDatabase,
            gameState
        )
        m.start()
    }
}