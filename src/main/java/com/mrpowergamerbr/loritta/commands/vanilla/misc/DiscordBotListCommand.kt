package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder

class DiscordBotListCommand : AbstractCommand("discordbotlist", listOf("dbl", "upvote"), category = CommandCategory.MISC) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["DBL_Description"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
		val embed = EmbedBuilder().apply {
			setColor(Constants.LORITTA_AQUA)
			setThumbnail("${Loritta.config.websiteUrl}assets/img/loritta_star.png")
			setTitle("✨ Discord Bot List")
			setDescription(locale["DBL_Info", context.config.commandPrefix, PerfilCommand.userVotes?.size ?: 0, "https://discordbots.org/bot/loritta"])
		}

	    context.sendMessage(context.getAsMention(true), embed.build())
    }
}