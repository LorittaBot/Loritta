package net.perfectdreams.loritta.lorituber.server.state

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer.Companion.GENERAL_INFO_KEY
import net.perfectdreams.loritta.lorituber.server.WorldTime
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberTrendData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberChannel
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot
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
    val charactersById: HashMap<UUID, LoriTuberCharacter>,
    val channelsById: HashMap<UUID, LoriTuberChannel>,
    val videosById: HashMap<UUID, LoriTuberVideo>,
    val lotsById: HashMap<UUID, LoriTuberLot>,
    val trendsByCategory: EnumMap<LoriTuberVideoContentCategory, LoriTuberTrendData>,
    val trendTargetsByCategory: EnumMap<LoriTuberVideoContentCategory, LoriTuberTrendData>,
    val nelsonGroceryStore: GroceryStore,
    val viewerHandles: List<String>
) {
    // Values removed from the collections ARE reflected on the map themselves
    val characters
        get() = charactersById.values
    val channels
        get() = channelsById.values
    val videos
        get() = videosById.values
    val lots
        get() = lotsById.values

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // TODO: Maybe store the seed?
    // SHA1PRNG is WAY faster than the default SecureRandom
    val random = SecureRandom.getInstance("SHA1PRNG")
    val spRandom = SplittableRandom()
    private val gameStateSaveMutex = Mutex()
    var isShuttingDown = false

    val oddCalls = listOf(
        PhoneCall.OddCall0,
        PhoneCall.OddCall1,
        PhoneCall.OddCall2,
        PhoneCall.OddCall3,
        PhoneCall.OddCall4,
        PhoneCall.OddCall5,
        PhoneCall.OddCall6,
        PhoneCall.OddCall7,
        PhoneCall.OddCall8,
        PhoneCall.OddCall9,
        PhoneCall.OddCall10,
        PhoneCall.OddCall11,
        PhoneCall.OddCall12,
        PhoneCall.OddCall13,
        PhoneCall.OddCall14,
        PhoneCall.OddCall15,
        PhoneCall.OddCall16,
        PhoneCall.OddCall17,
        PhoneCall.OddCall18,
        PhoneCall.OddCall19,
        PhoneCall.OddCall20,
        PhoneCall.OddCall21,
        PhoneCall.OddCall22,
        PhoneCall.OddCall23,
        PhoneCall.OddCall24,
        PhoneCall.OddCall25,
        PhoneCall.OddCall26,
    )

    val sonhosRewardCalls = listOf(
        PhoneCall.SonhosReward.SonhosRewardCall0,
        PhoneCall.SonhosReward.SonhosRewardCall1,
        PhoneCall.SonhosReward.SonhosRewardCall2,
        PhoneCall.SonhosReward.SonhosRewardCall3,
        PhoneCall.SonhosReward.SonhosRewardCall4,
        PhoneCall.SonhosReward.SonhosRewardCall5,
        PhoneCall.SonhosReward.SonhosRewardCall6,
        PhoneCall.SonhosReward.SonhosRewardCall7,
        PhoneCall.SonhosReward.SonhosRewardCall8,
        PhoneCall.SonhosReward.SonhosRewardCall9,
        PhoneCall.SonhosReward.SonhosRewardCall10,
        PhoneCall.SonhosReward.SonhosRewardCall11,
        PhoneCall.SonhosReward.SonhosRewardCall12,
        PhoneCall.SonhosReward.SonhosRewardCall13,
        PhoneCall.SonhosReward.SonhosRewardCall14,
        PhoneCall.SonhosReward.SonhosRewardCall15,
        PhoneCall.SonhosReward.SonhosRewardCall16,
        PhoneCall.SonhosReward.SonhosRewardCall17,
        PhoneCall.SonhosReward.SonhosRewardCall18,
        PhoneCall.SonhosReward.SonhosRewardCall19,
        PhoneCall.SonhosReward.SonhosRewardCall20,
        PhoneCall.SonhosReward.SonhosRewardCall21,
        PhoneCall.SonhosReward.SonhosRewardCall22,
    )

    /**
     * Generates a unique character ID
     */
    fun generateCharacterId() = generateId(charactersById)

    /**
     * Generates a unique channel ID
     */
    fun generateChannelId() = generateId(channelsById)

    /**
     * Generates a unique video ID
     */
    fun generateVideoId() = generateId(videosById)

    /**
     * Generates a unique lot ID
     */
    fun generateLotId() = generateId(lotsById)

    private fun generateId(idMap: Map<UUID, *>): UUID {
        while (true) {
            val randomId = UUID.randomUUID()
            if (!idMap.containsKey(randomId)) {
                return randomId
            }
        }
    }

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

    private fun processAndSerializeLots(): PreDatabaseUpdateResults {
        val removed = lots.filter { it.isRemoved }
        for (entity in removed)
            lotsById.remove(entity.id)
        // Dirty Epic: https://youtu.be/qxkrgDoRW0o
        val dirty = lots.filter { it.isDirty }
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
        val serializedLots = processAndSerializeLots()
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
                    LoriTuberCharacters.deleteWhere {
                        LoriTuberCharacters.id inList serializedCharacters.removed
                    }

                    // Dirty
                    LoriTuberCharacters.batchUpsert(serializedCharacters.entities, LoriTuberCharacters.id) {
                        this[LoriTuberCharacters.id] = it.id
                        this[LoriTuberCharacters.data] = ExposedBlob(it.data)
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

                    // ===[ LOTS ]===
                    LoriTuberLots.deleteWhere {
                        LoriTuberLots.id inList serializedLots.removed
                    }

                    // Dirty
                    // Whoops, this didn't have the correct ID
                    LoriTuberLots.batchUpsert(serializedLots.entities, LoriTuberLots.id) {
                        this[LoriTuberLots.id] = it.id
                        this[LoriTuberLots.data] = ExposedBlob(it.data)
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
        val removed: List<UUID>,
        val entities: List<SerializedEntityWrapper>
    )

    data class SerializedEntityWrapperStringId(
        val id: String,
        val data: ByteArray
    )

    data class SerializedEntityWrapper(
        val id: UUID,
        val data: ByteArray
    )
}