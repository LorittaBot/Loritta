package net.perfectdreams.loritta.morenitta.christmas2022event.modules

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Players
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.christmas2022event.LorittaChristmas2022Event
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.modules.MessageReceivedModule
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class DropChristmasStuffModule(val m: LorittaBot) : MessageReceivedModule {
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

        return event.guild?.selfMember?.hasPermission(Permission.MESSAGE_ADD_REACTION) == true && LorittaChristmas2022Event.isEventActive()
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext): Boolean {
        if (lorittaProfile == null)
            return false

        if (event.guild == null || LorittaChristmas2022Event.GUILD_MEMBER_COUNT_THRESHOLD > event.guild.memberCount)
            return false

        val date = System.currentTimeMillis()

        val millis = event.member!!.timeJoined.toInstant().toEpochMilli()

        val diff = date - millis

        val chance = (((diff.toDouble() * 100.0) / 1_296_000_000).coerceAtMost(100.0) - 1).toInt()

        if (0 >= chance)
            return false

        val id = event.channel.idLong

        val lastDrop = lastDropsAt.getOrDefault(id, 0L)
        val lastDropDiff = date - lastDrop

        val today = LocalDateTime.now(Constants.LORITTA_TIMEZONE)

        val randomNumber = when (today.dayOfMonth) {
            25 -> LorittaBot.RANDOM.nextInt(0, 750)
            24 -> LorittaBot.RANDOM.nextInt(0, 1000)
            23 -> LorittaBot.RANDOM.nextInt(0, 1250)
            else -> LorittaBot.RANDOM.nextInt(0, 1500)
        }

        if (randomNumber in 0..chance && event.message.contentStripped.hashCode() != lorittaProfile.lastMessageSentHash && event.message.contentRaw.length >= 5) {
            if (5_000 >= lastDropDiff)
                return false

            val userDropTime = lastDropsByUserAt.getOrDefault(event.author.idLong, 0L)

            if (30_000 >= date - userDropTime)
                return false

            m.newSuspendedTransaction {
                val getTheCandy = Christmas2022Players.selectAll().where {
                    Christmas2022Players.id eq lorittaProfile.id
                }.count() != 0L

                if (getTheCandy) {
                    lastDropsAt[id] = date
                    lastDropsByUserAt[event.author.idLong] = date
                    event.message.addReaction(LorittaChristmas2022Event.emoji).queue {
                        dropInMessageAt[event.message.idLong] = date
                    }

                    Christmas2022Drops.insert {
                        it[guildId] = event.guild.idLong
                        it[channelId] = event.channel.idLong
                        it[messageId] = event.message.idLong
                        it[createdAt] = Instant.now()
                    }
                }
            }
        }

        return false
    }
}