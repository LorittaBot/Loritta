package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory

class DiscordBotListCommand : AbstractCommand("discordbotlist", listOf("dbl", "upvote"), category = CommandCategory.MISC) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.misc.dbl.description"]
    }

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val embed = EmbedBuilder().apply {
			setColor(Constants.LORITTA_AQUA)
			setThumbnail("${loritta.instanceConfig.loritta.website.url}assets/img/loritta_star.png")
			setTitle("✨ Discord Bot List")
			setDescription(locale["commands.misc.dbl.info", context.config.commandPrefix, "https://discordbots.org/bot/loritta"])
		}

	    context.sendMessage(context.getAsMention(true), embed.build())
    }
}