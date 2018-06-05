package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder

class VotarCommand : AbstractCommand("vote", listOf("votar"), category = CommandCategory.MISC) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["VOTE_Description"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
	    if (context.config.serverListConfig.isEnabled) {
		    val serverListConfig = context.config.serverListConfig

		    val canUseVanityUrl = serverListConfig.isPartner || (serverListConfig.isSponsored && (serverListConfig.sponsoredUntil == -1L || System.currentTimeMillis() > serverListConfig.sponsoredUntil))

		    val websiteLink = Loritta.config.websiteUrl + "s/" + if (canUseVanityUrl && serverListConfig.vanityUrl != null) {
			    serverListConfig.vanityUrl
		    } else {
			    context.guild.id
		    }

		    context.reply(
				    LoriReply(
						    locale["VOTE_YourLink", context.guild.name.stripCodeMarks(), websiteLink],
						    "<:loritta:331179879582269451>"
				    )
		    )
	    } else {
		    context.reply(
				    LoriReply(
						    locale["VOTE_NotInServerList"],
						    Constants.ERROR
				    )
		    )
	    }
    }
}