package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.common.entity.DiscordAddedGuildMember
import dev.kord.common.entity.DiscordChannel
import dev.kord.common.entity.DiscordGuild
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
import kotlinx.datetime.toJavaInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildChannelPermissionOverrides
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMemberRoles
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildRoles
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import pw.forst.exposed.insertOrUpdate

class DiscordCacheModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processEvent(event: Event) {
        when (event) {
            is GuildCreate -> {
                // logger.info { "Howdy ${event.guild.id} (${event.guild.name})! Is unavailable? ${event.guild.unavailable}" }

                if (!event.guild.unavailable.discordBoolean) {
                    m.services.transaction {
                        createOrUpdateGuild(event.guild)
                    }
                }
            }
            is GuildUpdate -> {
                if (!event.guild.unavailable.discordBoolean) {
                    m.services.transaction {
                        createOrUpdateGuild(event.guild)
                    }
                }
            }
            is MessageCreate -> {
                val guildId = event.message.guildId.value
                val member = event.message.member.value

                if (guildId != null && member != null) {
                    m.services.transaction {
                        createOrUpdateGuildMember(guildId, event.message.author.id, member)
                    }
                }
            }
            is GuildMemberAdd -> {
                m.services.transaction {
                    createOrUpdateGuildMember(event.member)
                }
            }
            is GuildMemberUpdate -> {
                m.services.transaction {
                    createOrUpdateGuildMember(event.member)
                }
            }
            is GuildMemberRemove -> {
                m.services.transaction {
                    deleteGuildMember(event.member)
                }
            }
            is ChannelCreate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    m.services.transaction {
                        createOrUpdateGuildChannel(guildId, event.channel)
                    }
            }
            is ChannelUpdate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    m.services.transaction {
                        createOrUpdateGuildChannel(guildId, event.channel)
                    }
            }
            is ChannelDelete -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    m.services.transaction {
                        deleteGuildChannel(guildId, event.channel)
                    }
            }
            is GuildRoleCreate -> {
                m.services.transaction {
                    createOrUpdateRole(event.role.guildId, event.role.role)
                }
            }
            is GuildRoleUpdate -> {
                m.services.transaction {
                    createOrUpdateRole(event.role.guildId, event.role.role)
                }
            }
            is GuildRoleDelete -> {
                m.services.transaction {
                    deleteRole(event.role.guildId, event.role.id)
                }
            }
            is GuildDelete -> {
                // If the unavailable field is not set, the user/bot was removed from the guild.
                if (event.guild.unavailable.value == null) {
                    // logger.info { "Someone removed me @ ${event.guild.id}! :(" }
                    m.services.transaction {
                        removeGuildData(event.guild.id)
                    }
                }
            }
            else -> {}
        }
    }

    private fun createOrUpdateGuild(guild: DiscordGuild) {
        DiscordGuilds.insertOrUpdate(DiscordGuilds.id) {
            it[DiscordGuilds.id] = guild.id.value.toLong()
            it[DiscordGuilds.name] = guild.name
            it[DiscordGuilds.icon] = guild.icon
            it[DiscordGuilds.ownerId] = guild.ownerId.value.toLong()
            it[DiscordGuilds.joinedAt] = guild.joinedAt.value!!.toJavaInstant()
        }

        // Shouldn't be null in a GUILD_CREATE event
        val channels = guild.channels.value
        if (channels != null) {
            createOrUpdateAndDeleteGuildChannelsBulk(guild.id, channels)
        }

        createOrUpdateAndDeleteRolesBulk(guild.id, guild.roles)
    }

    private fun createOrUpdateGuildMember(guildMember: DiscordAddedGuildMember) {
        DiscordGuildMembers.insertOrUpdate(DiscordGuildMembers.guildId, DiscordGuildMembers.userId) {
            it[DiscordGuildMembers.guildId] = guildMember.guildId.toLong()
            it[DiscordGuildMembers.userId] = guildMember.user.value!!.id.toLong()
        }

        for (roleId in guildMember.roles) {
            DiscordGuildMemberRoles.insertOrUpdate(DiscordGuildMemberRoles.guildId, DiscordGuildMemberRoles.userId, DiscordGuildMemberRoles.roleId) {
                it[DiscordGuildMemberRoles.guildId] = guildMember.guildId.toLong()
                it[DiscordGuildMemberRoles.userId] = guildMember.user.value!!.id.toLong()
                it[DiscordGuildMemberRoles.roleId] = roleId.toLong()
            }
        }

        DiscordGuildMemberRoles.deleteWhere {
            (DiscordGuildMemberRoles.guildId eq guildMember.guildId.toLong()) and
                    (DiscordGuildMemberRoles.userId eq guildMember.user.value!!.id.toLong()) and
                    (DiscordGuildMemberRoles.roleId notInList guildMember.roles.map { it.toLong() })
        }
    }

    private fun createOrUpdateGuildMember(guildMember: DiscordUpdatedGuildMember) {
        DiscordGuildMembers.insertOrUpdate(DiscordGuildMembers.guildId, DiscordGuildMembers.userId) {
            it[DiscordGuildMembers.guildId] = guildMember.guildId.toLong()
            it[DiscordGuildMembers.userId] = guildMember.user.id.toLong()
        }

        for (roleId in guildMember.roles) {
            DiscordGuildMemberRoles.insertOrUpdate(DiscordGuildMemberRoles.guildId, DiscordGuildMemberRoles.userId, DiscordGuildMemberRoles.roleId) {
                it[DiscordGuildMemberRoles.guildId] = guildMember.guildId.toLong()
                it[DiscordGuildMemberRoles.userId] = guildMember.user.id.toLong()
                it[DiscordGuildMemberRoles.roleId] = roleId.toLong()
            }
        }

        DiscordGuildMemberRoles.deleteWhere {
            (DiscordGuildMemberRoles.guildId eq guildMember.guildId.toLong()) and
                    (DiscordGuildMemberRoles.userId eq guildMember.user.id.toLong()) and
                    (DiscordGuildMemberRoles.roleId notInList guildMember.roles.map { it.toLong() })
        }
    }

    private fun createOrUpdateGuildMember(guildId: Snowflake, userId: Snowflake, guildMember: DiscordGuildMember) {
        DiscordGuildMembers.insertOrUpdate(DiscordGuildMembers.guildId, DiscordGuildMembers.userId) {
            it[DiscordGuildMembers.guildId] = guildId.toLong()
            it[DiscordGuildMembers.userId] = userId.toLong()
        }

        for (roleId in guildMember.roles) {
            DiscordGuildMemberRoles.insertOrUpdate(DiscordGuildMemberRoles.guildId, DiscordGuildMemberRoles.userId, DiscordGuildMemberRoles.roleId) {
                it[DiscordGuildMemberRoles.guildId] = guildId.toLong()
                it[DiscordGuildMemberRoles.userId] = userId.toLong()
                it[DiscordGuildMemberRoles.roleId] = roleId.toLong()
            }
        }

        DiscordGuildMemberRoles.deleteWhere {
            (DiscordGuildMemberRoles.guildId eq guildId.toLong()) and
                    (DiscordGuildMemberRoles.userId eq userId.toLong()) and
                    (DiscordGuildMemberRoles.roleId notInList guildMember.roles.map { it.toLong() })
        }
    }

    private fun deleteGuildMember(guildMember: DiscordRemovedGuildMember) {
        DiscordGuildMembers.deleteWhere {
            DiscordGuildMembers.guildId eq guildMember.guildId.toLong() and (DiscordGuildMembers.userId eq guildMember.user.id.toLong())
        }

        DiscordGuildMemberRoles.deleteWhere {
            (DiscordGuildMemberRoles.guildId eq guildMember.guildId.toLong()) and
                    (DiscordGuildMemberRoles.userId eq guildMember.user.id.toLong())
        }
    }

    private fun createOrUpdateAndDeleteGuildChannelsBulk(guildId: Snowflake, channels: List<DiscordChannel>) {
        // Create or update all
        for (role in channels) {
            createOrUpdateGuildChannel(guildId, role)
        }

        // Then delete roles that weren't present in the GuildCreate event
        DiscordGuildChannels.deleteWhere {
            DiscordGuildChannels.guildId eq guildId.toLong() and (DiscordGuildChannels.channelId notInList channels.map { it.id.toLong() })
        }
    }

    private fun createOrUpdateAndDeleteRolesBulk(guildId: Snowflake, roles: List<DiscordRole>) {
        // Create or update all
        for (role in roles) {
            createOrUpdateRole(guildId, role)
        }

        // Then delete roles that weren't present in the GuildCreate event
        DiscordGuildRoles.deleteWhere {
            DiscordGuildRoles.guildId eq guildId.toLong() and (DiscordGuildRoles.roleId notInList roles.map { it.id.toLong() })
        }
    }

    private fun createOrUpdateGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        DiscordGuildChannels.insertOrUpdate(DiscordGuildChannels.guildId, DiscordGuildChannels.channelId) {
            it[DiscordGuildChannels.guildId] = guildId.toLong()
            it[DiscordGuildChannels.channelId] = channel.id.toLong()
            it[DiscordGuildChannels.name] = channel.name.value
            it[DiscordGuildChannels.type] = channel.type.value

            val permissions = channel.permissions.value
            if (permissions != null) {
                it[DiscordGuildChannels.permissions] = permissions.code.value.toLong()
            }
        }

        val permissionsOverwrites = channel.permissionOverwrites.value
        if (permissionsOverwrites != null) {
            for (overwrite in permissionsOverwrites) {
                DiscordGuildChannelPermissionOverrides.insertOrUpdate(DiscordGuildChannelPermissionOverrides.guildId, DiscordGuildChannelPermissionOverrides.channelId, DiscordGuildChannelPermissionOverrides.entityId) {
                    it[DiscordGuildChannelPermissionOverrides.guildId] = guildId.toLong()
                    it[DiscordGuildChannelPermissionOverrides.channelId] = channel.id.toLong()
                    it[DiscordGuildChannelPermissionOverrides.entityId] = overwrite.id.toLong()
                    it[DiscordGuildChannelPermissionOverrides.type] = overwrite.type.value
                    it[DiscordGuildChannelPermissionOverrides.allow] = overwrite.allow.code.value.toLong()
                    it[DiscordGuildChannelPermissionOverrides.deny] = overwrite.deny.code.value.toLong()
                }
            }

            DiscordGuildChannelPermissionOverrides.deleteWhere {
                (DiscordGuildChannelPermissionOverrides.guildId eq guildId.toLong()) and
                        (DiscordGuildChannelPermissionOverrides.channelId eq channel.id.toLong()) and
                        (DiscordGuildChannelPermissionOverrides.entityId notInList permissionsOverwrites.map { it.id.toLong() })
            }
        }
    }

    private fun createOrUpdateRole(guildId: Snowflake, role: DiscordRole) = DiscordGuildRoles.insertOrUpdate(DiscordGuildRoles.guildId, DiscordGuildRoles.roleId) {
        it[DiscordGuildRoles.guildId] = guildId.toLong()
        it[DiscordGuildRoles.roleId] = role.id.toLong()
        it[DiscordGuildRoles.name] = role.name
        it[DiscordGuildRoles.color] = role.color
        it[DiscordGuildRoles.hoist] = role.hoist
        it[DiscordGuildRoles.icon] = role.icon.value
        it[DiscordGuildRoles.unicodeEmoji] = role.icon.value
        it[DiscordGuildRoles.position] = role.position
        it[DiscordGuildRoles.permissions] = role.permissions.code.value.toLong()
        it[DiscordGuildRoles.managed] = role.managed
        it[DiscordGuildRoles.mentionable] = role.mentionable
    }

    private fun deleteRole(guildId: Snowflake, roleId: Snowflake) {
        DiscordGuildRoles.deleteWhere {
            (DiscordGuildRoles.guildId eq guildId.toLong()) and (DiscordGuildRoles.roleId eq roleId.toLong())
        }
    }

    private fun deleteGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        DiscordGuildChannelPermissionOverrides.deleteWhere {
            (DiscordGuildChannelPermissionOverrides.guildId eq guildId.toLong()) and (DiscordGuildChannelPermissionOverrides.channelId eq channel.id.value.toLong())
        }

        DiscordGuildChannels.deleteWhere {
            (DiscordGuildChannels.guildId eq guildId.toLong()) and (DiscordGuildChannels.channelId eq channel.id.value.toLong())
        }
    }

    private fun removeGuildData(guildId: Snowflake) {
        logger.info { "Removing $guildId's cached data..." }
        val guildIdAsLong = guildId.toLong()

        DiscordGuildChannels.deleteWhere {
            DiscordGuildChannels.guildId eq guildIdAsLong
        }

        DiscordGuildChannelPermissionOverrides.deleteWhere {
            DiscordGuildChannelPermissionOverrides.guildId eq guildIdAsLong
        }

        DiscordGuildRoles.deleteWhere {
            DiscordGuildRoles.guildId eq guildIdAsLong
        }

        DiscordGuildMembers.deleteWhere {
            DiscordGuildMembers.guildId eq guildIdAsLong
        }

        DiscordGuildMemberRoles.deleteWhere {
            DiscordGuildMemberRoles.guildId eq guildIdAsLong
        }

        DiscordGuilds.deleteWhere {
            DiscordGuilds.id eq guildIdAsLong
        }
    }
}