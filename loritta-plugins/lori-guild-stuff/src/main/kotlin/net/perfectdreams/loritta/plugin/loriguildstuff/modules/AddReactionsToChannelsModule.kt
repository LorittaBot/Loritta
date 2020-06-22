package net.perfectdreams.loritta.plugin.loriguildstuff.modules

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

class AddReactionsToChannelsModule(val plugin: LoriGuildStuffPlugin) : MessageReceivedModule {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        return loritta.config.loritta.environment == EnvironmentType.CANARY
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
        val content = event.message.contentRaw

        if (content.startsWith(">"))
            return false

        if (event.channel.idLong == 359139508681310212L || event.channel.idLong == 664431430159302674L) {
            event.message.addReaction("\uD83D\uDC4D")
                    .queue()

            event.message.addReaction("lori_what:626942886361038868")
                    .queue()
        }

        if (event.channel.idLong == 583406099047252044L || event.channel.idLong == 510601125221761054L) {
            event.message.addReaction("❤")
                    .queue()

            event.message.addReaction("grand_cat:587347657866084352")
                    .queue()

            event.message.addReaction("catblush:585608228679712778")
                    .queue()

            event.message.addReaction("a:lori_pat:706263175892566097")
                    .queue()
        }

        return false
    }
}