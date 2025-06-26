package net.perfectdreams.loritta.morenitta.reactionevents

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventDrops
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventPlayers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ReactionEventsConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.modules.MessageReceivedModule
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class DropPointsStuffModule(val m: LorittaBot) : MessageReceivedModule {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }
    private val dropInMessageAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<Long, Long>()
        .asMap()
    private val lastDropsAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<Long, Long>()
        .asMap()
    private val lastDropsByUserAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<Long, Long>()
        .asMap()

    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext): Boolean {
        val guild = event.guild ?: return false

        if (lorittaProfile == null)
            return false

        val now = Instant.now()

        // Get the current active event
        val activeEvent = ReactionEventsAttributes.getActiveEvent(now)

        val eventsEnabled = m.transaction {
            ReactionEventsConfigs
                .selectAll()
                .where {
                    ReactionEventsConfigs.id eq guild.idLong
                }
                .firstOrNull()
                ?.get(ReactionEventsConfigs.enabled) ?: true
        }

        return event.guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION) && activeEvent != null && eventsEnabled
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext): Boolean {
        if (lorittaProfile == null)
            return false

        val now = Instant.now()

        // Get the current active event
        val activeEvent = ReactionEventsAttributes.getActiveEvent(now) ?: return false

        if (event.guild == null || activeEvent.guildMemberThreshold > event.guild.memberCount)
            return false

        val date = System.currentTimeMillis()

        val id = event.channel.idLong

        val lastDrop = lastDropsAt.getOrDefault(id, 0L)
        val lastDropDiff = date - lastDrop

        if (1_000 >= lastDropDiff)
            return false

        val userDropTime = lastDropsByUserAt.getOrDefault(event.author.idLong, 0L)

        if (10_000 >= date - userDropTime)
            return false

        for (reactionSet in activeEvent.reactionSets) {
            val randomNumber = LorittaBot.RANDOM.nextFloat()

            val chance = reactionSet.chanceProvider.invoke(event.guild)
            if (chance >= randomNumber) {
                val shouldAddReaction = m.newSuspendedTransaction {
                    val spawnTheCandy = ReactionEventPlayers.selectAll()
                        .where {
                            ReactionEventPlayers.userId eq lorittaProfile.id.value and (ReactionEventPlayers.event eq activeEvent.internalId) and (ReactionEventPlayers.leftAt.isNull())
                        }.count() != 0L

                    if (spawnTheCandy) {
                        lastDropsAt[id] = date
                        lastDropsByUserAt[event.author.idLong] = date

                        // TODO: Fix this
                        // val type = LorittaEaster2023Event.easterEggColors.random()
                        // val emoji = LorittaEaster2023Event.easterEggColorToEmoji(type)
                        ReactionEventDrops.insert {
                            it[ReactionEventDrops.event] = activeEvent.internalId
                            it[ReactionEventDrops.reactionSetId] = reactionSet.reactionSetId
                            it[ReactionEventDrops.guildId] = event.guild.idLong
                            it[ReactionEventDrops.channelId] = event.channel.idLong
                            it[ReactionEventDrops.messageId] = event.message.idLong
                            it[ReactionEventDrops.createdAt] = Instant.now()
                        }
                        return@newSuspendedTransaction true
                    }

                    return@newSuspendedTransaction false
                }

                if (shouldAddReaction) {
                    event.message.addReaction(m.emojiManager.get(reactionSet.reaction).toJDA()).queue {
                        dropInMessageAt[event.message.idLong] = date
                    }
                }
            }
        }

        return false
    }
}