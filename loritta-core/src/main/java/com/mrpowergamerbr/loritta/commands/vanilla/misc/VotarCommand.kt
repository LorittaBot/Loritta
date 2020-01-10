package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class VotarCommand : AbstractCommand("vote", listOf("votar"), category = CommandCategory.MISC) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["VOTE_Description"]
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
	    if (context.config.serverListConfig.isEnabled) {
		    val serverListConfig = context.config.serverListConfig

		    val canUseVanityUrl = serverListConfig.isPartner || (serverListConfig.isSponsored && (serverListConfig.sponsoredUntil == -1L || System.currentTimeMillis() > serverListConfig.sponsoredUntil))

		    val websiteLink = loritta.instanceConfig.loritta.website.url + "s/" + if (canUseVanityUrl && serverListConfig.vanityUrl != null) {
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