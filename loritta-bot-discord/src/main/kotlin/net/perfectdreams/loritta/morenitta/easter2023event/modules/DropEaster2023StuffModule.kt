package net.perfectdreams.loritta.morenitta.easter2023event.modules

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Players
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.easter2023event.LorittaEaster2023Event
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.modules.MessageReceivedModule
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class DropEaster2023StuffModule(val m: LorittaBot) : MessageReceivedModule {
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
        if (lorittaProfile == null)
            return false

        return event.guild?.selfMember?.hasPermission(Permission.MESSAGE_ADD_REACTION) == true && LorittaEaster2023Event.isEventActive()
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext): Boolean {
        if (lorittaProfile == null)
            return false

        if (event.guild == null || LorittaEaster2023Event.GUILD_MEMBER_COUNT_THRESHOLD > event.guild.memberCount)
            return false

        val date = System.currentTimeMillis()

        val millis = event.member!!.timeJoined.toInstant().toEpochMilli()

        val chance = 350

        val id = event.channel.idLong

        val lastDrop = lastDropsAt.getOrDefault(id, 0L)
        val lastDropDiff = date - lastDrop

        val today = LocalDateTime.now(Constants.LORITTA_TIMEZONE)

        val randomNumber = LorittaBot.RANDOM.nextInt(0, 1_000)

        if (randomNumber in 0..chance && event.message.contentStripped.hashCode() != lorittaProfile.lastMessageSentHash && event.message.contentRaw.length >= 5) {
            if (1_000 >= lastDropDiff)
                return false

            val userDropTime = lastDropsByUserAt.getOrDefault(event.author.idLong, 0L)

            if (10_000 >= date - userDropTime)
                return false

            m.newSuspendedTransaction {
                val getTheCandy = Easter2023Players.selectAll().where {
                    Easter2023Players.id eq lorittaProfile.id
                }.count() != 0L

                if (getTheCandy) {
                    lastDropsAt[id] = date
                    lastDropsByUserAt[event.author.idLong] = date

                    val type = LorittaEaster2023Event.easterEggColors.random()
                    val emoji = LorittaEaster2023Event.easterEggColorToEmoji(type)

                    event.message.addReaction(emoji).queue {
                        dropInMessageAt[event.message.idLong] = date
                    }

                    Easter2023Drops.insert {
                        it[guildId] = event.guild.idLong
                        it[channelId] = event.channel.idLong
                        it[messageId] = event.message.idLong
                        it[createdAt] = Instant.now()
                        it[eggColor] = type
                    }
                }
            }
        }

        return false
    }
}