package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmiePatreonAuth
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class PatreonCommand : CommandBase() {
    override fun getLabel(): String {
        return "patreon"
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale["PATREON_DESCRIPTION"]
    }

    override fun run(context: CommandContext) {
		val temmiePatreonAuth = TemmiePatreonAuth(Loritta.config.patreonClientId, Loritta.config.patreonClientSecret, Loritta.config.patreonAccessToken, Loritta.config.patreonRefreshToken, 2678400)

		val pledges = temmiePatreonAuth.getProjectPledges("1159176").filter { !it.isDeclined }

		var patrons = ""

		for (patron in pledges.sortedByDescending { it.pledge }) {
			if (patron.pledge >= 500) {
				patrons += "**"
			}
			patrons += "${patron.fullName}"
			if (patron.discordId != null) {
				val user = lorittaShards.getUserById(patron.discordId)

				if (user != null) {
					patrons += " `${user.name}#${user.discriminator}`"
				}
			}
			if (patron.pledge >= 500) {
				patrons += "**"
			}
			patrons += "\n"
		}

		val embed = EmbedBuilder().apply {
			setThumbnail("https://i.imgur.com/Vl9ejFk.png")
			setTitle(context.locale["PATREON_THANKS"] + " <:loritta:331179879582269451>")
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField(context.locale["PATREON_DO_YOU_WANNA_HELP"] + " \uD83C\uDF80", context.locale["PATREON_HOW_TO_HELP", "https://www.patreon.com/mrpowergamerbr", "https://loritta.website/donate"], false)
		}
        context.sendMessage(context.getAsMention(true), embed.build())
    }
}