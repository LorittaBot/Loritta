package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands

import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.GlobalPunishmentRelaySystemPlugin
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.MessageProofs
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.UserReports
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object LGPRSCheckPunishmentCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: GlobalPunishmentRelaySystemPlugin) = create(
			loritta,
			listOf("lgprs check punishment")
	) {
		executesDiscord {
			loritta as Loritta

			val reportId = this.args.getOrNull(0) ?: return@executesDiscord

			val report = transaction(Databases.loritta) {
				UserReports.select {
					UserReports.id eq reportId.toLong()
				}.firstOrNull()
			}

			if (report != null) {
				val messageProofs = transaction(Databases.loritta) {
					MessageProofs.select {
						MessageProofs.reportId eq reportId.toLong()
					}.toList()
				}

				val user = lorittaShards.retrieveUserInfoById(report[UserReports.userId])
				val reporter = lorittaShards.retrieveUserById(report[UserReports.reportedBy])

				val embed = EmbedBuilder()
						.setColor(Constants.DISCORD_BLURPLE)
						.setAuthor(user?.name ?: "???", null, user?.effectiveAvatarUrl)
						.setTitle("Report #${report[UserReports.id].value}")
						.setThumbnail(user?.effectiveAvatarUrl)
						.setFooter("Categoria: ${report[UserReports.category]}")

				val guildInfo = lorittaShards.queryGuildById(report[UserReports.guildId])

				if (reporter != null) {
					embed.addField(
							"\uD83D\uDC6E Reportado por",
							"`${reporter.name}#${reporter.discriminator}` (`${reporter.idLong}`)",
							true
					)
				}

				if (guildInfo != null) {
					val name = guildInfo["name"].string
					val id = guildInfo["id"].string

					embed.addField(
							"\uD83D\uDC6E Servidor",
							"`$name` (`$id`)",
							true
					)
				}

				val legacyLocale = loritta.getLegacyLocaleById(locale.id)

				val reportedAt = report[UserReports.reportedAt].humanize(loritta.getLegacyLocaleById(locale.id))

				embed.addField(
						"⏰ Punido em",
						reportedAt,
						true
				)

				if (messageProofs.isNotEmpty()) {
					embed.addField(
							"\uD83E\uDDFE Provas (Mensagens)",
							messageProofs.joinToString("\n", transform = {
								buildString {
									this.append("[${it[MessageProofs.sentAt].humanize(legacyLocale)}](https://discordapp.com/channels/${it[MessageProofs.guildId]}/${it[MessageProofs.channelId]}/${it[MessageProofs.messageId]})")
									this.append("\n")
									this.append("```")
									this.append("\n")
									this.append(it[MessageProofs.content])
									this.append("\n")
									this.append("```")
								}
							}),
							false
					)
				}

				sendMessage(embed.build())
			}
		}
	}
}