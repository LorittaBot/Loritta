package net.perfectdreams.loritta.socket

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.application.ApplicationCall
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import mu.KotlinLogging
import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.network.discord.entities.DiscordNetworkGuild
import net.perfectdreams.loritta.platform.network.discord.entities.DiscordNetworkUser
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.utils.extensions.obj
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.toNodeArray
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.utils.identification.SimpleUserIdentification
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class DiscordController(val main: LorittaController) {
    private companion object {
        val logger = KotlinLogging.logger {}
    }

    internal val discordShards = ConcurrentHashMap<Int, LorittaDiscordShard>()
    internal val queryUserOnShard = Caffeine.newBuilder()
        .expireAfterAccess(15L, TimeUnit.MINUTES)
        .maximumSize(1_000_000)
        .build<Long, Long>()
    internal var totalShards = 0

    fun registerShard(socketWrapper: SocketWrapper, lorittaShardId: Int, lorittaShardName: String, discordMaxShards: Int, discordShardMin: Int, discordShardMax: Int) {
        logger.info("Loritta Discord Shard $lorittaShardId ($lorittaShardName - $discordShardMin..$discordShardMax) was identified!")
        val shard = LorittaDiscordShard(socketWrapper, lorittaShardId.toLong(), lorittaShardName, discordMaxShards, discordShardMin, discordShardMax)

        val oldShard = discordShards[lorittaShardId]
        if (oldShard != null) {
            logger.info("Loritta Discord Shard $lorittaShardId is already registered, disconnecting old socket...")
            oldShard.socketWrapper.close()
        }

        shard.socketWrapper.setUpHeartbeat()

        discordShards[lorittaShardId] = shard
        totalShards = getTotalShardCount()
    }

    suspend fun retrieveUserById(id: String): User? = retrieveUserById(id.toLong())

    suspend fun retrieveUserById(id: Long): User? {
        // Para evitar spam de requests, vamos verificar se a gente conhece qual é a shard "preferencial" para o usuário
        val bestDiscordShardToQueryOn = queryUserOnShard.getIfPresent(id)
        val bestLoriShard = bestDiscordShardToQueryOn?.let {
            discordShards.values.firstOrNull { bestDiscordShardToQueryOn in it.discordShardMin..it.discordShardMax }
        }

        val result = if (bestDiscordShardToQueryOn != null && bestLoriShard != null) {
            bestLoriShard.socketWrapper.awaitResponse(
                SocketOpCode.Discord.GET_USER_BY_ID,
                JsonNodeFactory.instance.objectNode()
                    .put("userId", id.toString())
            )
        } else {
            // Caso a gente não saiba, pergunte em todas as shards
            val results = discordShards.values.map {
                GlobalScope.async {
                    it.socketWrapper.awaitResponse(
                        SocketOpCode.Discord.GET_USER_BY_ID,
                        JsonNodeFactory.instance.objectNode()
                            .put("userId", id.toString())
                    )
                }
            }

            val allResults = results.awaitAll()
            allResults.firstOrNull { it.has("user") && it.has("foundInShard") }
        }

        if (result == null) {
            queryUserOnShard.invalidate(id)
            return null
        }

        // Guardar qual shard o usuário foi encontrado para futuras pesquisas
        val foundInShard = result["foundInShard"].longValue()
        queryUserOnShard.put(id, foundInShard)
        return DiscordNetworkUser.from(result["user"].obj)
    }

    suspend fun retrieveUsersById(ids: List<Long>): List<User> {
        // Para evitar "bombardeio" de requests únicos, vamos agrupar tudo em um único request caso possível
        val needsToBeQueried = ids.toMutableList()
        val queriedUsers = mutableListOf<User>()

        val batchedRequests = discordShards.values.map { shard ->
            val batchQuery = mutableListOf<Long>()

            for (id in ids) {
                if (queryUserOnShard.getIfPresent(id) == shard.lorittaShardId) {
                    batchQuery.add(id)
                }
            }

            GlobalScope.async {
                shard.socketWrapper.awaitResponse(
                    SocketOpCode.Discord.GET_USERS_BY_ID,
                    JsonNodeFactory.instance.objectNode().apply {
                        this.putArray(
                            "userIds"
                        ).apply {
                            batchQuery.forEach {
                                this.add(it.toString())
                            }
                        }
                    }
                )
            }
        }

        val allResults = batchedRequests.awaitAll()
        allResults.forEach {
            it["users"].forEach { result ->
                val user = DiscordNetworkUser.from(result.obj)
                needsToBeQueried.remove(user.id)
                queriedUsers.add(user)
            }
        }

        // O resto pode ser pego usando a versão "normal" do request
        val individualRequests = needsToBeQueried.map {
            GlobalScope.async {
                retrieveUserById(it)
            }
        }

        individualRequests.awaitAll().filterNotNull().forEach {
            queriedUsers.add(it)
        }

        return queriedUsers
    }

    suspend fun retrieveGuildById(id: String): Guild? = retrieveGuildById(id.toLong())

    suspend fun retrieveGuildById(id: Long): Guild? {
        val queryShardId = getQueryShardId(id)

        val lorittaShard = discordShards.values.firstOrNull { queryShardId in it.discordShardMin..it.discordShardMax } ?: return null

        val result = lorittaShard.socketWrapper.awaitResponse(
            SocketOpCode.Discord.GET_GUILD_BY_ID,
            JsonNodeFactory.instance.objectNode()
                .put("guildId", id.toString())
        )

        if (!result.has("guild"))
            return null

        return DiscordNetworkGuild.from(
            result["guild"].obj
        )
    }

    suspend fun retrieveGuildsById(ids: List<Long>): List<Guild> {
        val batchedRequests = discordShards.values.map { shard ->
            val batchQuery = mutableListOf<Long>()

            for (id in ids) {
                if (getQueryShardId(id) in shard.discordShardMin..shard.discordShardMax) {
                    batchQuery.add(id)
                }
            }

            GlobalScope.async {
                shard.socketWrapper.awaitResponse(
                    SocketOpCode.Discord.GET_GUILDS_BY_ID,
                    JsonNodeFactory.instance.objectNode().apply {
                        this.putArray(
                            "guildIds"
                        ).apply {
                            batchQuery.forEach {
                                this.add(it.toString())
                            }
                        }
                    }
                )
            }
        }

        return batchedRequests.awaitAll().flatMap {
            it["guilds"].map {
                DiscordNetworkGuild.from(it.obj)
            }
        }
    }

    suspend fun getUserIdentification(call: ApplicationCall, session: SampleSession, discordAuth: TemmieDiscordAuth): SimpleUserIdentification {
        if (session.discordId != null) {
            // Quer dizer o ID do Discord é conhecido, então vamos utilizar!
            val user = retrieveUserById(session.discordId)

            logger.info("Retrieved ${session.discordId}'s information via socket: $user")

            if (user != null) {
                return SimpleUserIdentification(
                    user.idAsString,
                    user.name,
                    "https://cdn.discordapp.com/emojis/543235412609466368.png?v=1" // TODO
                )
            }
        }

        logger.info("Failed to retrieve user information via socket (unknown user or missing discordId on session), retrieving via API...")
        val discordIdentification = discordAuth.getUserIdentification()
        return SimpleUserIdentification(
            discordIdentification.id,
            discordIdentification.username,
            "https://cdn.discordapp.com/emojis/542034214854328335.png?v=1" // TODO
        )
    }

    fun getQueryShardId(id: Long): Long {
        val maxShard = totalShards.toLong()

        // (guild_id >> 22) % num_shards == shard_id
        // rem = mod, só que usado para longs (motivo: não sei, mas mod não funciona com long)
        logger.debug { "maxShard is $maxShard" }
        logger.debug { "ID is ${id}" }
        logger.debug { "ID right shifted by 22 is ${id shr 22}" }
        return (id shr 22).rem(maxShard)
    }

    suspend fun retrieveGuildConfigById(sectionName: String, id: String, userId: String? = null): ObjectNode? = retrieveGuildConfigById(sectionName, id.toLong(), userId)
    suspend fun retrieveGuildConfigById(sectionName: String, id: Long, userId: String? = null): ObjectNode? = retrieveGuildConfigById(listOf(sectionName), id, userId?.toLong())

    suspend fun retrieveGuildConfigById(sections: List<String>, id: Long, userId: Long? = null): ObjectNode? {
        val queryShardId = getQueryShardId(id)

        val lorittaShard = discordShards.values.firstOrNull { queryShardId in it.discordShardMin..it.discordShardMax } ?: return null

        val result = lorittaShard.socketWrapper.awaitResponse(
            SocketOpCode.Discord.GET_GUILD_CONFIG_BY_ID,
            objectNode(
                "guildId" to id.toString(),
                "userId" to userId?.toString(),
                "sections" to sections.toNodeArray()
            )
        )

        if (!result.has("config"))
            return null

        return result["config"].obj
    }

    suspend fun patchGuildConfigById(id: String, opCode: Int, data: JsonNode, userId: String? = null) = patchGuildConfigById(id.toLong(), opCode, data, userId?.toLong())

    suspend fun patchGuildConfigById(id: Long, opCode: Int, data: JsonNode, userId: Long? = null) {
        val queryShardId = getQueryShardId(id)

        val lorittaShard = discordShards.values.firstOrNull { queryShardId in it.discordShardMin..it.discordShardMax } ?: return

        lorittaShard.socketWrapper.awaitResponse(
            SocketOpCode.Discord.UPDATE_GUILD_CONFIG_BY_ID,
            objectNode(
                "patchCode" to opCode,
                "guildId" to id.toString(),
                "userId" to userId.toString(),
                "data" to data
            )
        )
    }

    fun getTotalShardCount(): Int {
        return discordShards.values.maxBy { it.discordMaxShards }?.discordMaxShards ?: 0
    }

    fun getDiscordShardFromSocketWrapper(socketWrapper: SocketWrapper): LorittaDiscordShard {
        return discordShards.values.first { it.socketWrapper == socketWrapper }
    }
}