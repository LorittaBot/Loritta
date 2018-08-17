package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores", "contribuidores", "contribuidor"), category = CommandCategory.MISC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PATREON_DESCRIPTION"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var patrons = ""

		val lorittaGuild = com.mrpowergamerbr.loritta.utils.lorittaShards.getGuildById("297732013006389252")

		if (lorittaGuild != null) {
			val roleDonators = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
			val roleInative = lorittaGuild.getRoleById("435856512787677214") // Contribuidor Inativo

			val donators = lorittaGuild.getMembersWithRoles(roleDonators)
			val inative = lorittaGuild.getMembersWithRoles(roleInative)

			val lorittaProfiles = loritta.usersColl.find(
					Filters.`in`(
							"_id",
							donators.map { it.user.id }
					)
			)

			donators.forEach {
				val lorittaProfile = lorittaProfiles.firstOrNull { profile -> it.user.id == profile.userId }
				val isBold = if (lorittaProfile != null) {
					lorittaProfile.donatorPaid >= 19.99
				} else {
					false
				}

				var name = "`${it.user.name}#${it.user.discriminator}`"
				if (isBold) {
					name = "**$name**"
				}
				patrons += "<:lori_owo:417813932380520448> $name\n"
			}

			patrons += "\uD83D\uDCB8"
			inative.forEach {
				patrons += " `${it.user.name}#${it.user.discriminator}`"
			}
		}

		val embed = EmbedBuilder().apply {
			setThumbnail("https://i.imgur.com/Vl9ejFk.png")
			setTitle("<:loritta:331179879582269451> " + context.locale["PATREON_THANKS"])
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField("\uD83C\uDF80 " + context.locale["PATREON_DO_YOU_WANNA_HELP"], context.locale["PATREON_HOW_TO_HELP", "https://www.patreon.com/mrpowergamerbr", "${Loritta.config.websiteUrl}donate", "https://apoia.se/mrpowergamerbr"], false)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}

	class GenericPledge(val name: String, val pledge: Int, val discordId: String?, val source: PledgeSource)

	enum class PledgeSource {
		PATREON, APOIA_SE
	}
}