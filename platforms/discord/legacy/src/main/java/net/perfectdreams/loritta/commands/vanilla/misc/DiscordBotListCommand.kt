package net.perfectdreams.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes

class DiscordBotListCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("dbl", "upvote"), CommandCategory.MISC) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.dbl"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val context = this
            val embed = EmbedBuilder().apply {
                setColor(Constants.LORITTA_AQUA)
                setThumbnail("${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}assets/img/loritta_star.png")
                setTitle("✨ Discord Bot List")
                setDescription(
                    locale[
                            "$LOCALE_PREFIX.info",
                            context.serverConfig.commandPrefix,
                            "https://discordbots.org/bot/loritta",
                            Emotes.DISCORD_BOT_LIST
                    ]
                )
            }

            context.sendMessage(context.getUserMention(true), embed.build())
        }
    }
}