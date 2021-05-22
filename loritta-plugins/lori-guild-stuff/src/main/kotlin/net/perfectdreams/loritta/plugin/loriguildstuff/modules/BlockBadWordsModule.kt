package net.perfectdreams.loritta.plugin.loriguildstuff.modules

import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.loriguildstuff.LoriGuildStuffPlugin

class BlockBadWordsModule(val plugin: LoriGuildStuffPlugin) : MessageReceivedModule {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val GUILD_IDS = listOf(
                297732013006389252L,
                420626099257475072L,
                320248230917046282L,
                600478027113299972L,
                528204622666661888L,
                602640402830589954L
        )
    }

    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
        return loritta.config.loritta.environment == EnvironmentType.CANARY
                && (event.guild?.idLong in GUILD_IDS)
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
        val guild = event.guild ?: return false

        val content = event.message.contentRaw
                .replace("\u200B", "")
                .replace("\\", "")

        if (plugin.badWords.any { content.contains(it, true) }) {
            val moderationInfo = AdminUtils.retrieveModerationInfo(serverConfig)

            // Delete right now because fuck off
            event.message.delete().queue()

            BanCommand.ban(
                    moderationInfo.copy(
                            sendPunishmentViaDm = false
                    ),
                    guild,
                    event.author,
                    locale,
                    event.author,
                    "Automaticamente banido por Bad Words",
                    false,
                    7
            )
            return true
        }

        return false
    }
}