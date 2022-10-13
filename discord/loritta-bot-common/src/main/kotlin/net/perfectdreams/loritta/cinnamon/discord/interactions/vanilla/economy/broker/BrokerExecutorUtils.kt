package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation

object BrokerExecutorUtils {
    fun MessageBuilder.brokerBaseEmbed(context: InteractionContext, block: dev.kord.rest.builder.message.EmbedBuilder.() -> kotlin.Unit) = embed {
        author("Loritta's Home Broker")
        // TODO: Move this to an object
        color = Color(23, 62, 163)
        thumbnailUrl = "${context.loritta.config.loritta.website}assets/img/loritta_stonks.png"
        footer(context.i18nContext.get(BrokerCommand.I18N_PREFIX.FooterDataInfo))
        block()
    }

    fun getEmojiStatusForTicker(brokerTickerInformation: BrokerTickerInformation) = if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(brokerTickerInformation.status))
        Emotes.DoNotDisturb
    else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(brokerTickerInformation.lastUpdatedAt))
        Emotes.Idle
    else Emotes.Online
}