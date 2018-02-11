package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmiePatreonAuth
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores"), category = CommandCategory.MISC) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["PATREON_DESCRIPTION"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
		val temmiePatreonAuth = TemmiePatreonAuth(Loritta.config.patreonClientId, Loritta.config.patreonClientSecret, Loritta.config.patreonAccessToken, Loritta.config.patreonRefreshToken, 2678400)

		val pledges = temmiePatreonAuth.getProjectPledges("1159176").filter { !it.isDeclined }

		var patrons = ""

		for (patron in pledges.sortedByDescending { it.pledge }) {
			if (patron.pledge >= 500) {
				patrons += "**"
			}
			patrons += "<:patreon:412243234123808768> ${patron.fullName}"
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

	    val lorittaGuild = com.mrpowergamerbr.loritta.utils.lorittaShards.getGuildById("297732013006389252")

	    if (lorittaGuild != null) {
		    val rolePatreons = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
		    val roleDonators = lorittaGuild.getRoleById("334711262262853642") // Doadores

		    val patreons = lorittaGuild.getMembersWithRoles(rolePatreons)
		    val donators = lorittaGuild.getMembersWithRoles(roleDonators)

		    patrons += "\uD83D\uDCB5"
		    donators.forEach {
			    patrons += " `${it.user.name}#${it.user.discriminator}`"
		    }
	    }

		val embed = EmbedBuilder().apply {
			setThumbnail("https://i.imgur.com/Vl9ejFk.png")
			setTitle("<:loritta:331179879582269451> " + context.locale["PATREON_THANKS"])
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField(context.locale["PATREON_DO_YOU_WANNA_HELP"] + " \uD83C\uDF80", context.locale["PATREON_HOW_TO_HELP", "https://www.patreon.com/mrpowergamerbr", "https://loritta.website/donate", "https://apoia.se/mrpowergamerbr"], false)
		}
        context.sendMessage(context.getAsMention(true), embed.build())
    }
}