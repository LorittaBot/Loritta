package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.GlobalPunishmentRelaySystemPlugin
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.base.toJDA
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.UserReports
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object LGPRSCheckUserCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: GlobalPunishmentRelaySystemPlugin) = create(
			loritta,
			listOf("lgprs check user")
	) {
		executesDiscord {
			val user = user(0)?.toJDA() ?: run {
				return@executesDiscord
			}

			val allReports = transaction(Databases.loritta) {
				UserReports.select {
					UserReports.userId eq user.idLong
				}.toList()
			}

			if (allReports.isEmpty()) {
				reply(
						LorittaReply(
								"Sem reports!"
						)
				)
				return@executesDiscord
			}

			val embed = EmbedBuilder()
					.setTitle("Reports de ${user.name}#${user.discriminator}")
					.setThumbnail(user.effectiveAvatarUrl)

			val guildScores = mutableMapOf<Long, Int>()
			val userScores = mutableMapOf<Long, Int>()

			for (report in allReports.sortedByDescending { it[UserReports.reportedAt] }) {
				val reportedUserInfo = lorittaShards.retrieveUserInfoById(report[UserReports.reportedBy])

				embed.appendDescription("`(#" + report[UserReports.id].value + ") " + report[UserReports.category].name + "`, reportado por ${reportedUserInfo?.name}#${reportedUserInfo?.discriminator}\n")
			}

			sendMessage(embed.build())
		}
	}
}