package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.DiscordAddedGuildMember
import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordRemovedGuildMember
import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.DiscordUpdatedGuildMember
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.gateway.ChannelCreate
import dev.kord.gateway.ChannelDelete
import dev.kord.gateway.ChannelUpdate
import dev.kord.gateway.Event
import dev.kord.gateway.GuildCreate
import dev.kord.gateway.GuildDelete
import dev.kord.gateway.GuildMemberAdd
import dev.kord.gateway.GuildMemberRemove
import dev.kord.gateway.GuildMemberUpdate
import dev.kord.gateway.GuildRoleCreate
import dev.kord.gateway.GuildRoleDelete
import dev.kord.gateway.GuildRoleUpdate
import dev.kord.gateway.GuildUpdate
import dev.kord.gateway.MessageCreate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordChannelsMap
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordEmojisMap
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordRolesMap
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import pw.forst.exposed.insertOrUpdate
import java.sql.ResultSet
import java.util.concurrent.TimeUnit

class DiscordCacheModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Used to avoid updating guild information at the same time, causing "Could not serialize access due to concurrent update"
     */
    private val guildMutexes = Caffeine.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<Snowflake, Mutex>()
        .asMap()
    /**
     * Used to avoid updating user information at the same time, causing "Could not serialize access due to concurrent update"
     */
    private val userMutexes = Caffeine.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<Snowflake, Mutex>()
        .asMap()

    private suspend inline fun withGuildIdLock(guildId: Snowflake, action: () -> Unit) = guildMutexes.getOrPut(guildId) { Mutex() }.withLock(action = action)
    private suspend inline fun withUserIdLock(guildId: Snowflake, action: () -> Unit) = userMutexes.getOrPut(guildId) { Mutex() }.withLock(action = action)

    override suspend fun processEvent(event: Event): ModuleResult {
        when (event) {
            is GuildCreate -> {
                // logger.info { "Howdy ${event.guild.id} (${event.guild.name})! Is unavailable? ${event.guild.unavailable}" }

                if (!event.guild.unavailable.discordBoolean) {
                    val start = System.currentTimeMillis()

                    // This looks weird, but it is actually used to indicate to the JVM that "hey, we aren't using the "event" object anymore!", which helps with GC (or, well, I hope it does)
                    val guildId = event.guild.id
                    val guildName = event.guild.name
                    val guildIcon = event.guild.icon
                    val guildOwnerId = event.guild.ownerId
                    val guildRoles = event.guild.roles
                    val guildChannels = event.guild.channels.value!! // Shouldn't be null in a GUILD_CREATE event
                    val guildEmojis = event.guild.emojis
                    // If your bot does not have the GUILD_PRESENCES Gateway Intent, or if the guild has over 75k members, members and presences returned in this event will only contain your bot and users in voice channels.
                    val guildMembers = event.guild.members.value ?: emptyList()

                    withGuildIdLock(guildId) {
                        m.services.transaction {
                            createOrUpdateGuild(
                                guildId,
                                guildName,
                                guildIcon,
                                guildOwnerId,
                                guildRoles,
                                guildChannels,
                                guildEmojis
                            )

                            for (member in guildMembers) {
                                createOrUpdateGuildMember(
                                    guildId,
                                    member.user.value!!.id,
                                    member
                                )
                            }
                        }
                    }

                    logger.info { "GuildCreate for $guildId took ${System.currentTimeMillis() - start}ms" }
                }
            }
            is GuildUpdate -> {
                if (!event.guild.unavailable.discordBoolean) {
                    val start = System.currentTimeMillis()

                    // This looks weird, but it is actually used to indicate to the JVM that "hey, we aren't using the "event" object anymore!", which helps with GC (or, well, I hope it does)
                    val guildId = event.guild.id
                    val guildName = event.guild.name
                    val guildIcon = event.guild.icon
                    val guildOwnerId = event.guild.ownerId
                    val guildRoles = event.guild.roles
                    val guildChannels = event.guild.channels.value
                    val guildEmojis = event.guild.emojis

                    withGuildIdLock(guildId) {
                        m.services.transaction {
                            createOrUpdateGuild(
                                guildId,
                                guildName,
                                guildIcon,
                                guildOwnerId,
                                guildRoles,
                                guildChannels,
                                guildEmojis
                            )
                        }
                    }

                    logger.info { "GuildUpdate for $guildId took ${System.currentTimeMillis() - start}ms" }
                }
            }
            is MessageCreate -> {
                val guildId = event.message.guildId.value
                val member = event.message.member.value

                if (guildId != null && member != null) {
                    withUserIdLock(event.message.author.id) {
                        m.services.transaction {
                            createOrUpdateGuildMember(guildId, event.message.author.id, member)
                        }
                    }
                }
            }
            is GuildMemberAdd -> {
                withUserIdLock(event.member.user.value!!.id) {
                    m.services.transaction {
                        createOrUpdateGuildMember(event.member)
                    }
                }
            }
            is GuildMemberUpdate -> {
                withUserIdLock(event.member.user.id) {
                    m.services.transaction {
                        createOrUpdateGuildMember(event.member)
                    }
                }
            }
            is GuildMemberRemove -> {
                withUserIdLock(event.member.user.id) {
                    m.services.transaction {
                        deleteGuildMember(event.member)
                    }
                }
            }
            is ChannelCreate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withGuildIdLock(guildId) {
                        m.services.transaction {
                            createOrUpdateGuildChannel(guildId, event.channel)
                        }
                    }
            }
            is ChannelUpdate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withGuildIdLock(guildId) {
                        m.services.transaction {
                            createOrUpdateGuildChannel(guildId, event.channel)
                        }
                    }
            }
            is ChannelDelete -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withGuildIdLock(guildId) {
                        m.services.transaction {
                            deleteGuildChannel(guildId, event.channel)
                        }
                    }
            }
            is GuildRoleCreate -> {
                withGuildIdLock(event.role.guildId) {
                    m.services.transaction {
                        createOrUpdateRole(event.role.guildId, event.role.role)
                    }
                }
            }
            is GuildRoleUpdate -> {
                withGuildIdLock(event.role.guildId) {
                    m.services.transaction {
                        createOrUpdateRole(event.role.guildId, event.role.role)
                    }
                }
            }
            is GuildRoleDelete -> {
                withGuildIdLock(event.role.guildId) {
                    m.services.transaction {
                        deleteRole(event.role.guildId, event.role.id)
                    }
                }
            }
            is GuildDelete -> {
                // If the unavailable field is not set, the user/bot was removed from the guild.
                if (event.guild.unavailable.value == null) {
                    logger.info { "Someone removed me @ ${event.guild.id}! :(" }
                    withGuildIdLock(event.guild.id) {
                        m.services.transaction {
                            removeGuildData(event.guild.id)
                        }
                    }
                }
            }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private fun createOrUpdateGuild(
        guildId: Snowflake,
        guildName: String,
        guildIcon: String?,
        guildOwnerId: Snowflake,
        guildRoles: List<DiscordRole>,
        guildChannels: List<DiscordChannel>?,
        guildEmojis: List<DiscordEmoji>
    ) {
        // Verify if we really need to update the roles/channels/etc JSON field
        val currentStoredValues = DiscordGuilds.slice(
            DiscordGuilds.id,
            DiscordGuilds.roles,
            DiscordGuilds.channels,
            DiscordGuilds.emojis
        ).selectFirstOrNull {
            DiscordGuilds.id eq guildId.toLong()
        }

        val isPresent = currentStoredValues?.get(DiscordGuilds.id) != null

        val storedRolesAsJson = currentStoredValues?.get(DiscordGuilds.roles)
        val storedChannelsAsJson = currentStoredValues?.get(DiscordGuilds.channels)
        val storedEmojisAsJson = currentStoredValues?.get(DiscordGuilds.emojis)

        if (currentStoredValues != null && isPresent) {
            // It exists, let's update it
            DiscordGuilds.update({ DiscordGuilds.id eq guildId.toLong() }) {
                it[DiscordGuilds.id] = guildId.toLong()
                it[DiscordGuilds.name] = guildName
                it[DiscordGuilds.icon] = guildIcon
                it[DiscordGuilds.ownerId] = guildOwnerId.toLong()

                if (storedRolesAsJson != null) {
                    val storedRoles = Json.decodeFromString<PuddingDiscordRolesMap>(storedRolesAsJson).values

                    if (!(storedRoles.containsAll(guildRoles) && guildRoles.containsAll(storedRoles))) {
                        it[DiscordGuilds.roles] = Json.encodeToString(guildRoles.associateBy { it.id.toString() })
                    }
                }

                if (guildChannels != null && storedChannelsAsJson != null) {
                    val storedChannels = Json.decodeFromString<PuddingDiscordChannelsMap>(storedChannelsAsJson).values

                    if (!(storedChannels.containsAll(guildChannels) && guildChannels.containsAll(storedChannels))) {
                        it[DiscordGuilds.channels] = Json.encodeToString(guildChannels.associateBy { it.id.toString() })
                    }
                }

                if (storedEmojisAsJson != null) {
                    val storedEmojis = Json.decodeFromString<PuddingDiscordEmojisMap>(storedEmojisAsJson).values

                    if (!(storedEmojis.containsAll(guildEmojis) && guildEmojis.containsAll(storedEmojis))) {
                        it[DiscordGuilds.channels] = Json.encodeToString(guildEmojis.associateBy { it.id.toString() })
                    }
                }
            }
        } else {
            // Does not exist, let's insert it
            DiscordGuilds.insert {
                it[DiscordGuilds.id] = guildId.toLong()
                it[DiscordGuilds.name] = guildName
                it[DiscordGuilds.icon] = guildIcon
                it[DiscordGuilds.ownerId] = guildOwnerId.toLong()

                it[DiscordGuilds.roles] = Json.encodeToString(guildRoles.associateBy { it.id.toString() })
                it[DiscordGuilds.channels] = Json.encodeToString((guildChannels ?: listOf()).associateBy { it.id.toString() })
            }
        }
    }

    private fun createOrUpdateGuildMember(guildMember: DiscordAddedGuildMember) {
        createOrUpdateGuildMember(
            guildMember.guildId,
            guildMember.user.value!!.id,
            guildMember.roles
        )
    }

    private fun createOrUpdateGuildMember(guildMember: DiscordUpdatedGuildMember) {
        createOrUpdateGuildMember(
            guildMember.guildId,
            guildMember.user.id,
            guildMember.roles
        )
    }

    private fun createOrUpdateGuildMember(guildId: Snowflake, userId: Snowflake, guildMember: DiscordGuildMember) {
        createOrUpdateGuildMember(
            guildId,
            userId,
            guildMember.roles
        )
    }

    private fun createOrUpdateGuildMember(
        guildId: Snowflake,
        userId: Snowflake,
        roles: List<Snowflake>
    ) {
        DiscordGuildMembers.insertOrUpdate(DiscordGuildMembers.guildId, DiscordGuildMembers.userId) {
            it[DiscordGuildMembers.guildId] = guildId.toLong()
            it[DiscordGuildMembers.userId] = userId.toLong()
            it[DiscordGuildMembers.roles] = Json.encodeToString(roles.map { it.toString() })
        }
    }

    private fun deleteGuildMember(guildMember: DiscordRemovedGuildMember) {
        DiscordGuildMembers.deleteWhere {
            DiscordGuildMembers.guildId eq guildMember.guildId.toLong() and (DiscordGuildMembers.userId eq guildMember.user.id.toLong())
        }
    }

    private fun createOrUpdateGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        val channels = DiscordGuilds.slice(DiscordGuilds.channels).selectFirstOrNull {
            DiscordGuilds.id eq guildId.toLong()
        }?.get(DiscordGuilds.channels)

        if (channels != null) {
            val newChannelsMap = Json.decodeFromString<PuddingDiscordChannelsMap>(channels)
                .toMutableMap()
                .apply {
                    this[channel.id.toString()] = channel
                }

            DiscordGuilds.update({ DiscordGuilds.id eq guildId.toLong() }) {
                it[DiscordGuilds.channels] = Json.encodeToString(newChannelsMap)
            }
        } else {
            logger.info { "Channel $channel was created or updated in $guildId, but there isn't any database entries associated with it!" }
        }
    }

    private fun createOrUpdateRole(guildId: Snowflake, role: DiscordRole) {
        val roles = DiscordGuilds.slice(DiscordGuilds.roles).selectFirstOrNull {
            DiscordGuilds.id eq guildId.toLong()
        }?.get(DiscordGuilds.roles)

        if (roles != null) {
            val newChannelsMap = Json.decodeFromString<PuddingDiscordRolesMap>(roles)
                .toMutableMap()
                .apply {
                    this[role.id.toString()] = role
                }

            DiscordGuilds.update({ DiscordGuilds.id eq guildId.toLong() }) {
                it[DiscordGuilds.roles] = Json.encodeToString(newChannelsMap)
            }
        } else {
            logger.info { "Role ${role} was created or updated in $guildId, but there isn't any database entries associated with it!" }
        }
    }

    private fun deleteRole(guildId: Snowflake, roleId: Snowflake) {
        val roles = DiscordGuilds.slice(DiscordGuilds.roles).selectFirstOrNull {
            DiscordGuilds.id eq guildId.toLong()
        }?.get(DiscordGuilds.roles)

        if (roles != null) {
            val newChannelsMap = Json.decodeFromString<PuddingDiscordRolesMap>(roles)
                .toMutableMap()
                .apply {
                    this.remove(roleId.toString())
                }

            DiscordGuilds.update({ DiscordGuilds.id eq guildId.toLong() }) {
                it[DiscordGuilds.roles] = Json.encodeToString(newChannelsMap)
            }
        } else {
            logger.info { "Role $roleId was deleted in $guildId, but there isn't any database entries associated with it!" }
        }
    }

    private fun deleteGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        val channels = DiscordGuilds.slice(DiscordGuilds.channels).selectFirstOrNull {
            DiscordGuilds.id eq guildId.toLong()
        }?.get(DiscordGuilds.channels)

        if (channels != null) {
            val newChannelsMap = Json.decodeFromString<PuddingDiscordChannelsMap>(channels)
                .toMutableMap()
                .apply {
                    this.remove(channel.id.toString())
                }

            DiscordGuilds.update({ DiscordGuilds.id eq guildId.toLong() }) {
                it[DiscordGuilds.channels] = Json.encodeToString(newChannelsMap)
            }
        } else {
            logger.info { "Channel $channel was deleted in $guildId, but there isn't any database entries associated with it!" }
        }
    }

    private fun removeGuildData(guildId: Snowflake) {
        logger.info { "Removing $guildId's cached data..." }
        val guildIdAsLong = guildId.toLong()

        DiscordGuildMembers.deleteWhere {
            DiscordGuildMembers.guildId eq guildIdAsLong
        }

        DiscordGuilds.deleteWhere {
            DiscordGuilds.id eq guildIdAsLong
        }
    }

    private fun <T:Any> String.execAndMap(transform : (ResultSet) -> T) : List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(this) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }
}