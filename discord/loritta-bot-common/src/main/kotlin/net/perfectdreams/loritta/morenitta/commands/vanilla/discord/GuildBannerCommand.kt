package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class GuildBannerCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("guildbanner", "serverbanner"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
    override fun command() = create {
        localizedDescription("commands.command.guildbanner.description")

        arguments {
            argument(ArgumentType.TEXT) {
                optional = true
            }
        }

        canUseInPrivateChannel = false

        executesDiscord {
            OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "server banner")

            val discordGuild = guild

            // Verificar se a guild tem banner
            val guildBanner = discordGuild.bannerUrl ?: fail(locale["commands.command.guildbanner.noBanner"])

            val embed = EmbedBuilder()

            embed.setTitle("🖼 ${discordGuild.name}")
            embed.setColor(Constants.DISCORD_BLURPLE)
            embed.setDescription(locale["loritta.clickHere", "$guildBanner?size=2048"])
            embed.setImage("$guildBanner?size=2048")

            sendMessage(embed.build())
        }
    }
}