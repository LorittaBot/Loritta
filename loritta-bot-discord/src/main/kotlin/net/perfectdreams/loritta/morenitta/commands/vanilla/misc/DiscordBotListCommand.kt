package net.perfectdreams.loritta.morenitta.commands.vanilla.misc

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.common.utils.Emotes

class DiscordBotListCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("dbl", "upvote"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.dbl"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val context = this
            val embed = EmbedBuilder().apply {
                setColor(Constants.LORITTA_AQUA)
                setThumbnail("${loritta.config.loritta.website.url}assets/img/loritta_star.png")
                setTitle("✨ Discord Bot List")
                setDescription(
                    locale[
                            "$LOCALE_PREFIX.info",
                            context.serverConfig.commandPrefix,
                            "https://top.gg/bot/${loritta.config.loritta.discord.applicationId.toString()}",
                            Emotes.DISCORD_BOT_LIST
                    ]
                )
            }

            context.sendMessage(context.getUserMention(true), embed.build())
        }
    }
}