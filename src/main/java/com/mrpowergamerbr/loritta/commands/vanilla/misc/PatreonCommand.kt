package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores", "contribuidores", "contribuidor"), category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PATREON_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var patrons = ""

		val lorittaGuild = com.mrpowergamerbr.loritta.utils.lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val roleDonators = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
			val roleInative = lorittaGuild.getRoleById("435856512787677214") // Contribuidor Inativo

			val donators = lorittaGuild.getMembersWithRoles(roleDonators)
			val inative = lorittaGuild.getMembersWithRoles(roleInative)

			val lorittaProfiles = transaction(Databases.loritta) {
				Profile.find { Profiles.id inList donators.map { it.user.idLong } }.toMutableList()
			}

			donators.forEach {
				val lorittaProfile = lorittaProfiles.firstOrNull { profile -> it.user.idLong == profile.userId }
				val isBold = if (lorittaProfile != null) {
					lorittaProfile.donatorPaid >= 59.99
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
			setTitle("<:loritta:331179879582269451> " + context.legacyLocale["PATREON_THANKS"])
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField("\uD83C\uDF80 " + context.legacyLocale["PATREON_DO_YOU_WANNA_HELP"], context.legacyLocale["PATREON_HOW_TO_HELP", "https://www.patreon.com/mrpowergamerbr", "${Loritta.config.websiteUrl}donate", "https://apoia.se/mrpowergamerbr"], false)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}

	class GenericPledge(val name: String, val pledge: Int, val discordId: String?, val source: PledgeSource)

	enum class PledgeSource {
		PATREON, APOIA_SE
	}
}