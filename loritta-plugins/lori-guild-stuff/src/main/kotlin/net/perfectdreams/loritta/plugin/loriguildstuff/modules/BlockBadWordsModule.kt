package net.perfectdreams.loritta.plugin.loriguildstuff.modules

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullInt
import com.github.salomonbrys.kotson.nullString
import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.loriguildstuff.LoriGuildStuffPlugin
import net.perfectdreams.loritta.website.utils.extensions.respondJson

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

    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        return loritta.config.loritta.environment == EnvironmentType.CANARY
                && (event.guild?.idLong in GUILD_IDS)
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
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
                    loritta.getLegacyLocaleById(serverConfig.localeId),
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