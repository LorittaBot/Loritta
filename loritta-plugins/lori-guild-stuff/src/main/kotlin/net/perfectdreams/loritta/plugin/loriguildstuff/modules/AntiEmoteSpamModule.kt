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
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.loriguildstuff.LoriGuildStuffPlugin

class AntiEmoteSpamModule(val plugin: LoriGuildStuffPlugin): MessageReceivedModule {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val maximumAllowedEmoteAmount = 50

        val UNICODE_EMOJI_MATCHER = "(\\\\u00a9|\\\\u00ae|\\[\\\\u2000-\\\\u3300\\]|\\\\ud83c\\[\\\\ud000-\\\\udfff\\]|\\\\ud83d\\[\\\\ud000-\\\\udfff\\]|\\\\ud83e\\[\\\\ud000-\\\\udfff\\])".toRegex()
        val EMOJI_MATCHER = "<:.+?:\\d+>".toRegex()

        private val GUILD_IDS = listOf(
                //297732013006389252L,
                //420626099257475072L,
                //320248230917046282L,
                501445050207830016L // TODO: remove this guild
        )
    }

    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        return loritta.config.loritta.environment == EnvironmentType.CANARY
                && (event.guild?.idLong in GUILD_IDS)
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        logger.info { "Module Anti Emote fired" }
        val guild = event.guild ?: return false
        val content = event.message.contentRaw
        logger.info { content }

        Constants.EMOJI_PATTERN

        // How many unicode emotes?
        val uniEmoteAmount = UNICODE_EMOJI_MATCHER.findAll(content).count()
        // How many discord emotes?
        val emoteAmount = EMOJI_MATCHER.findAll(content).count()

        logger.info { "Unicode Emote: $uniEmoteAmount, Emotes: $emoteAmount" }

        if (uniEmoteAmount > maximumAllowedEmoteAmount || emoteAmount > maximumAllowedEmoteAmount) {
            logger.info { "Usuário ${event.author.id} está enviando emojis demais, banindo..." }

            // fucc off, kiddy spammer
            event.message.delete().queue()

            val moderationInfo = AdminUtils.retrieveModerationInfo(serverConfig)

            BanCommand.ban(
                    moderationInfo.copy(
                            sendPunishmentViaDm = false
                    ),
                    guild,
                    event.author,
                    loritta.getLegacyLocaleById(serverConfig.localeId),
                    event.author,
                    "Automaticamente banido por enviar uma quantidade demasiadamente enrome de emojis (Spam/Flood)",
                    false,
                    7
            )
            return true
        }

        return false
    }

}