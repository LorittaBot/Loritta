package net.perfectdreams.loritta.lorituber.server.state

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer.Companion.GENERAL_INFO_KEY
import net.perfectdreams.loritta.lorituber.server.WorldTime
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberChannel
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberSuperViewer
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberVideo
import net.perfectdreams.loritta.lorituber.server.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.security.SecureRandom
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class GameState(
    val lorituberDatabase: Database,
    var worldInfo: WorldInfo,
    // We map them from ID to object because it is way faster than manually filtering the list
    val charactersById: HashMap<Long, LoriTuberCharacter>,
    val channelsById: HashMap<Long, LoriTuberChannel>,
    val videosById: HashMap<Long, LoriTuberVideo>,
    val superViewersById: HashMap<Long, LoriTuberSuperViewer>,
    val nelsonGroceryStore: GroceryStore
) {
    // Values removed from the collections ARE reflected on the map themselves
    val characters
        get() = charactersById.values
    val channels
        get() = channelsById.values
    val videos
        get() = videosById.values
    val superViewers
        get() = superViewersById.values

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // TODO: Maybe store the seed?
    // SHA1PRNG is WAY faster than the default SecureRandom
    val random = SecureRandom.getInstance("SHA1PRNG")
    val spRandom = SplittableRandom()
    private val gameStateSaveMutex = Mutex()
    var isShuttingDown = false

    fun nextCharacterId() = worldInfo.characterCounter++
    fun nextChannelId() = worldInfo.channelCounter++
    fun nextVideoId() = worldInfo.videoCounter++
    fun nextSuperViewerId() = worldInfo.superViewerCounter++

    /**
     * Gets the current world time
     */
    fun getWorldTime() = WorldTime(worldInfo.currentTick)

    private fun processAndSerializeCharacters(): PreDatabaseUpdateResults {
        val removed = characters.filter { it.isRemoved }
        for (entity in removed)
            charactersById.remove(entity.id)
        val dirty = characters.filter { it.isDirty }
        val serialized = dirty.map {
            SerializedEntityWrapper(
                it.id,
                ProtoBuf.encodeToByteArray(it.data)
            )
        }
        dirty.forEach { it.isDirty = false }
        return PreDatabaseUpdateResults(removed.map { it.id }, serialized)
    }

    private fun processAndSerializeChannels(): PreDatabaseUpdateResults {
        val removed = channels.filter { it.isRemoved }
        for (entity in removed)
            channelsById.remove(entity.id)
        val dirty = channels.filter { it.isDirty }
        val serialized = dirty.map {
            SerializedEntityWrapper(
                it.id,
                ProtoBuf.encodeToByteArray(it.data)
            )
        }
        dirty.forEach { it.isDirty = false }
        return PreDatabaseUpdateResults(removed.map { it.id }, serialized)
    }

    private fun processAndSerializeVideos(): PreDatabaseUpdateResults {
        val removed = videos.filter { it.isRemoved }
        for (entity in removed)
            videosById.remove(entity.id)
        // Dirty Epic: https://youtu.be/qxkrgDoRW0o
        val dirty = videos.filter { it.isDirty }
        val serialized = dirty.map {
            SerializedEntityWrapper(
                it.id,
                ProtoBuf.encodeToByteArray(it.data)
            )
        }
        dirty.forEach { it.isDirty = false }
        return PreDatabaseUpdateResults(removed.map { it.id }, serialized)
    }

    fun persist(): Job {
        logger.info { "Persisting state to disk... Hang tight!" }

        // TODO: Could we clone every entity and store it to the database?
        // We also could make the backed data "immutable" (like a snapshot) and only get the already present in memory data snapshot
        // ^ Another GOOD alternative after testing: Convert EVERYTHING to ProtoBuf and after converting, hand off to a separate thread to save the data
        // Converting these objects to ProtoBuf is VERY cheap, the slow part is the SQLite data saving
        logger.info { "Converting entities to serialized entities..." }
        val serializedCharacters = processAndSerializeCharacters()
        val serializedChannels = processAndSerializeChannels()
        val serializedVideos = processAndSerializeVideos()
        val serializedGroceriesStores = mutableListOf<SerializedEntityWrapperStringId>()
        if (nelsonGroceryStore.isDirty) {
            serializedGroceriesStores.add(
                SerializedEntityWrapperStringId(
                    nelsonGroceryStore.id,
                    ProtoBuf.encodeToByteArray(nelsonGroceryStore.items.map { it.data })
                )
            )
        }

        val serializedWorldInfo = ProtoBuf.encodeToByteArray(worldInfo)
        logger.info { "Done! Successfully serialized game state!" }

        return GlobalScope.launch {
            logger.info { "Saving game state on a separate thread..." }
            gameStateSaveMutex.withLock {
                val start = System.currentTimeMillis()

                logger.info { "Successfully acquired game state save mutex lock!" }
                transaction(lorituberDatabase) {
                    // TODO: Remove this:
                    // Deleting all and reinserting is WAY faster, as long as we don't index the tables
                    // (We don't even need the indexes anyway, considering that everything is in memory, and we never query using a specific index anyway)
                    //


                    // I thought that maybe it would be better to always dump everything to the disk (instead of upserting only the changed entities)
                    // But in my tests, the performance is just a lil bit worse for upsert, and ofc, you get big advantages if there are entities that aren't dirty
                    //
                    // However another BIG advantage is NOT using "LongIdTable" for the tables, I don't know what Exposed uses that causes a BIG performance impact
                    // on SQLite (autoincrement or primary key?)
                    //
                    // Using a "uniqueIndex" manually is actually WAY faster (like, from 13s to 3s to save 1 million videos!)

                    // ===[ CHARACTERS ]===
                    // Removed
                    LoriTuberCharactersAlt.deleteWhere {
                        LoriTuberCharactersAlt.id inList serializedCharacters.removed
                    }

                    // Dirty
                    LoriTuberCharactersAlt.batchUpsert(serializedCharacters.entities, LoriTuberCharactersAlt.id) {
                        this[LoriTuberCharactersAlt.id] = it.id
                        this[LoriTuberCharactersAlt.data] = ExposedBlob(it.data)
                    }

                    // ===[ CHANNELS ]===
                    // Removed
                    LoriTuberChannels.deleteWhere {
                        LoriTuberChannels.id inList serializedChannels.removed
                    }

                    // Dirty
                    LoriTuberChannels.batchUpsert(serializedChannels.entities, LoriTuberChannels.id) {
                        this[LoriTuberChannels.id] = it.id
                        this[LoriTuberChannels.data] = ExposedBlob(it.data)
                    }

                    // ===[ VIDEOS ]===
                    LoriTuberVideos.deleteWhere {
                        LoriTuberVideos.id inList serializedVideos.removed
                    }

                    // Dirty
                    // Whoops, this didn't have the correct ID
                    LoriTuberVideos.batchUpsert(serializedVideos.entities, LoriTuberVideos.id) {
                        this[LoriTuberVideos.id] = it.id
                        this[LoriTuberVideos.data] = ExposedBlob(it.data)
                    }

                    // ===[ GROCERY STORE ]===
                    LoriTuberGroceryStores.batchUpsert(serializedGroceriesStores, LoriTuberGroceryStores.shop) {
                        this[LoriTuberGroceryStores.shop] = it.id
                        this[LoriTuberGroceryStores.data] = ExposedBlob(it.data)
                    }

                    // ===[ WORLD INFO ]===
                    LoriTuberWorldTicks.upsert(LoriTuberWorldTicks.type) {
                        it[LoriTuberWorldTicks.type] = GENERAL_INFO_KEY
                        it[LoriTuberWorldTicks.data] = ExposedBlob(serializedWorldInfo)
                    }
                }
                logger.info { "Successfully saved game state to disk! Took ${(System.currentTimeMillis() - start).milliseconds}" }
            }
        }
    }

    data class PreDatabaseUpdateResults(
        val removed: List<Long>,
        val entities: List<SerializedEntityWrapper>
    )

    data class SerializedEntityWrapperStringId(
        val id: String,
        val data: ByteArray
    )

    data class SerializedEntityWrapper(
        val id: Long,
        val data: ByteArray
    )
}