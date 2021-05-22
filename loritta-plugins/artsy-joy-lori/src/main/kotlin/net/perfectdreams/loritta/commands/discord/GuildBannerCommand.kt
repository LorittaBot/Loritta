package net.perfectdreams.loritta.commands.discord

import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class GuildBannerCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("guildbanner", "serverbanner"), CommandCategory.MODERATION) {
    override fun command() = create {
        localizedDescription("commands.command.guildbanner.description")

        arguments {
            argument(ArgumentType.TEXT) {
                optional = true
            }
        }

        canUseInPrivateChannel = false

        executesDiscord {
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