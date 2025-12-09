package net.perfectdreams.loritta.morenitta.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.update.GenericGuildUpdateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordGuilds
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

/**
 * A listener that tracks guilds to Loritta's database
 */
class TrackGuildsListener(val loritta: LorittaBot) : ListenerAdapter() {
    // Used for synchronization to avoid out of order updates
    val mutex = Mutex()

    override fun onReady(event: ReadyEvent) {
        GlobalScope.launch {
            mutex.withLock {
                val shardGuilds = event.jda.guilds
                val shardGuildsIds = shardGuilds.map { it.idLong }

                loritta.transaction {
                    // Delete any guild that we don't know about
                    DiscordGuilds.deleteWhere {
                        DiscordGuilds.id notInList shardGuildsIds and (DiscordGuilds.shardId eq event.jda.shardInfo.shardId)
                    }

                    // Upsert all guilds
                    DiscordGuilds.batchUpsert(shardGuilds, DiscordGuilds.id) {
                        populateGuildData(it)
                    }
                }
            }
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    upsertGuild(event.guild)
                }
            }
        }
    }

    override fun onGuildAvailable(event: GuildAvailableEvent) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    upsertGuild(event.guild)
                }
            }
        }
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    // Delete guild
                    DiscordGuilds.deleteWhere { DiscordGuilds.id eq event.guild.idLong }
                }
            }
        }
    }

    override fun onUnavailableGuildLeave(event: UnavailableGuildLeaveEvent) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    // Delete guild
                    DiscordGuilds.deleteWhere { DiscordGuilds.id eq event.guildIdLong }
                }
            }
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    upsertGuild(event.guild)
                }
            }
        }
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    upsertGuild(event.guild)
                }
            }
        }
    }

    private fun upsertGuild(guild: Guild) {
        DiscordGuilds.upsert(DiscordGuilds.id) {
            it.populateGuildData(guild)
        }
    }

    override fun onGenericGuildUpdate(event: GenericGuildUpdateEvent<*>) {
        GlobalScope.launch {
            mutex.withLock {
                loritta.transaction {
                    upsertGuild(event.guild)
                }
            }
        }
    }

    private fun InsertStatement<*>.populateGuildData(guild: Guild) {
        this[DiscordGuilds.id] = guild.idLong
        this[DiscordGuilds.name] = guild.name
        this[DiscordGuilds.iconId] = guild.iconId
        this[DiscordGuilds.bannerId] = guild.bannerId
        this[DiscordGuilds.splashId] = guild.splashId
        this[DiscordGuilds.ownerId] = guild.ownerIdLong
        this[DiscordGuilds.memberCount] = guild.memberCount
        this[DiscordGuilds.joinedAt] = guild.selfMember.timeJoined
        this[DiscordGuilds.channelCount] = guild.channelCache.size().toInt()
        this[DiscordGuilds.roleCount] = guild.roleCache.size().toInt()
        this[DiscordGuilds.emojiCount] = guild.emojiCache.size().toInt()
        this[DiscordGuilds.stickerCount] = guild.stickerCache.size().toInt()
        this[DiscordGuilds.boostCount] = guild.boostCount
        this[DiscordGuilds.vanityCode] = guild.vanityCode
        this[DiscordGuilds.verificationLevel] = guild.verificationLevel.key
        this[DiscordGuilds.nsfwLevel] = guild.nsfwLevel.key
        this[DiscordGuilds.explicitContentLevel] = guild.explicitContentLevel.key
        this[DiscordGuilds.features] = guild.features.toList()
        this[DiscordGuilds.locale] = guild.locale.locale
        this[DiscordGuilds.clusterId] = loritta.lorittaCluster.id
        this[DiscordGuilds.shardId] = guild.jda.shardInfo.shardId
        this[DiscordGuilds.lastUpdatedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
    }
}