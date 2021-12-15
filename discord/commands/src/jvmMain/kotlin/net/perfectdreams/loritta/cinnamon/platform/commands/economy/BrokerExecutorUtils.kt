package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation

object BrokerExecutorUtils {
    fun MessageBuilder.brokerBaseEmbed(context: ApplicationCommandContext, block: dev.kord.rest.builder.message.EmbedBuilder.() -> kotlin.Unit) = embed {
        author("Loritta's Home Broker")
        // TODO: Move this to an object
        color = Color(23, 62, 163)
        thumbnailUrl = "${context.loritta.config.website}assets/img/loritta_stonks.png"
        footer(context.i18nContext.get(BrokerCommand.I18N_PREFIX.FooterDataInfo))
        block()
    }

    fun getEmojiStatusForTicker(brokerTickerInformation: BrokerTickerInformation) = if (brokerTickerInformation.status != LorittaBovespaBrokerUtils.MARKET)
        Emotes.DoNotDisturb
    else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(brokerTickerInformation.lastUpdatedAt))
        Emotes.Idle
    else Emotes.Online
}