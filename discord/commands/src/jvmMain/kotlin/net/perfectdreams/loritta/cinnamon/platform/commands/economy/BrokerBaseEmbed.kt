package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.Color
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand

fun MessageBuilder.brokerBaseEmbed(context: ApplicationCommandContext, block: dev.kord.rest.builder.message.EmbedBuilder.() -> kotlin.Unit) = embed {
    author("Loritta's Home Broker")
    // TODO: Move this to an object
    color = Color(23, 62, 163)
    // TODO: Image
    // .setThumbnail("${(loritta as LorittaDiscord).instanceConfig.loritta.website.url}assets/img/loritta_stonks.png")
    footer(context.i18nContext.get(BrokerCommand.I18N_PREFIX.FooterDataInfo))
    block()
}