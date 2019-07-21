package net.perfectdreams.loritta.commands.discord

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class GuildBannerCommand : LorittaCommand(arrayOf("guildbanner", "serverbanner"), CommandCategory.DISCORD) {

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.discord.guildbanner.description"]
    }

    override val canUseInPrivateChannel: Boolean = false

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.TEXT) {}
        }
    }

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        val discordGuild = context.discordGuild
        val guildBanner = discordGuild!!.bannerUrl

        // Verificar se a guild tem banner
        if (guildBanner == null) {
            context.reply(locale["commands.discord.guildbanner.noBanner"], Constants.ERROR)
        } else {
            val embed = EmbedBuilder()

            embed.setTitle("🖼 ${discordGuild.name}")
            embed.setColor(Constants.DISCORD_BLURPLE)
            embed.setDescription(locale["loritta.clickHere", guildBanner + "?size=2048"])
            embed.setImage(guildBanner + "?size=2048")

            context.sendMessage(context.getAsMention(true), embed.build())
        }
    }
}
