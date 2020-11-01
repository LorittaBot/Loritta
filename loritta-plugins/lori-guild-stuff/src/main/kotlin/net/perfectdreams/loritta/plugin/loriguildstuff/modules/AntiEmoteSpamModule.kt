package net.perfectdreams.loritta.plugin.loriguildstuff.modules

import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.locale.getLegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.vdurmont.emoji.EmojiParser
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.loriguildstuff.LoriGuildStuffPlugin
import org.apache.commons.lang3.StringUtils

class AntiEmoteSpamModule(val plugin: LoriGuildStuffPlugin): MessageReceivedModule {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val maximumAllowedEmoteAmount = 100

        // Call this "fallback" because emoji-java can't extract some certain emojis (maybe the newer ones)
        private val FALLBACK_EMOJIS_CODEPOINTS = listOf(
                "\uD83E"
        )

        private val GUILD_IDS = listOf(
                297732013006389252L,
                420626099257475072L,
                320248230917046282L,
                501445050207830016L
        )
    }

    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        return loritta.config.loritta.environment == EnvironmentType.CANARY
                && (event.guild?.idLong in GUILD_IDS)
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        val guild = event.guild ?: return false
        val content = event.message.contentRaw
        logger.info { content }

        val emoteCollection = EmojiParser.extractEmojis(content)

        val fallbackEmoteAmount = FALLBACK_EMOJIS_CODEPOINTS.sumBy {
            StringUtils.countMatches(content, it)
        } - emoteCollection.distinct().toList().sumBy {
            StringUtils.countMatches(content, it)
        }
        // How many unicode emotes?
        val uniEmoteAmount = emoteCollection.size
        // How many discord emotes?
        val emoteAmount = event.message.emotesBag.size

        logger.info { "\"Fallback\" Emotes: $fallbackEmoteAmount" }
        logger.info { "Unicode Emotes: $uniEmoteAmount, Discord emotes: $emoteAmount" }

        if (fallbackEmoteAmount + uniEmoteAmount + emoteAmount > maximumAllowedEmoteAmount) {
            logger.info { "[Servidor: ${guild.id}] [Canal: ${event.channel.id}] Usuário ${event.author.id} está enviando emojis demais do permitido, irei banir..." }

            // Fuck off
            event.message.delete().queue()

            val moderationInfo = AdminUtils.retrieveModerationInfo(serverConfig)

            // The member SHOULD BE NOT NULL
            if (guild.selfMember.canInteract(event.message.member!!)) {
                BanCommand.ban(
                        moderationInfo.copy(
                                sendPunishmentViaDm = false
                        ),
                        guild,
                        guild.selfMember.user,
                        loritta.getLegacyLocaleById(serverConfig.localeId),
                        lorittaUser.profile.getLegacyBaseLocale(),
                        event.author,
                        "Automaticamente banido por enviar uma quantidade demasiadamente enrome de emojis (Spam/Flood)",
                        false,
                        7
                )
                return true
            } else {
                logger.info { "Eu não consigo banir o usuário infrator, provavelmente eu não tenho permissão!~" }
                return false
            }
        }

        return false
    }

}