package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.MessageInteractionFunctions
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.GlobalPunishmentRelaySystemPlugin
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.base.toJDA
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.MessageProofs
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.tables.UserReports
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.utils.PunishmentCategory
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object LGPRSReportCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: GlobalPunishmentRelaySystemPlugin) = create(
			loritta,
			listOf("lgprs report")
	) {
		this.userRequiredPermissions = listOf(
				Permission.ADMINISTRATOR
		)

		executesDiscord {
			val context = this

			val realMemberCount = this.guild.members.filterNot { it.user.isBot }
					.size

			if (1000 > realMemberCount) {
				reply(
						LorittaReply(
								"Você precisa ter mais de 1000 membros no seu servidor para poder reportar usuários para a LGPRS!"
						)
				)
				return@executesDiscord
			}

			val user = user(0)?.toJDA() ?: run {
				this.explain()
				return@executesDiscord
			}

			val category = reply(
					LorittaReply(
							"Qual categoria? ${PunishmentCategory.values().joinToString(", ")}"
					)
			).toJDA()

			val functions = com.mrpowergamerbr.loritta.utils.loritta.messageInteractionCache.getOrPut(category.idLong) { MessageInteractionFunctions(guild.idLong, category.channel.idLong, context.user.idLong) }
			functions.onResponseByAuthor = {
				val categoryEnum = PunishmentCategory.valueOf(it.message.contentRaw)
				category.removeAllFunctions()

				val whatMsg = reply(
						LorittaReply(
								"Link da Mensagem?"
						)
				).toJDA()

				val functions = com.mrpowergamerbr.loritta.utils.loritta.messageInteractionCache.getOrPut(category.idLong) { MessageInteractionFunctions(guild.idLong, whatMsg.channel.idLong, context.user.idLong) }
				functions.onResponseByAuthor = {
					whatMsg.removeAllFunctions()

					val messageQuery = Regex("discordapp.com/channels/([0-9]+)/([0-9]+)/([0-9]+)")

					val matchers = messageQuery.findAll(it.message.contentRaw)

					val foundMessages = mutableListOf<Message>()

					for (matcher in matchers) {
						val guildId = matcher.groupValues[1]
						val channelId = matcher.groupValues[2]
						val messageId = matcher.groupValues[3]

						if (guild.id == guildId) {
							val channel = guild.getTextChannelById(channelId)

							if (channel != null) {
								val message = channel.retrieveMessageById(messageId).await()

								if (message.author.idLong == user.idLong)
									foundMessages.add(message)
							}
						}
					}

					transaction(Databases.loritta) {
						val report = UserReports.insertAndGetId {
							it[userId] = user.idLong
							it[reportedBy] = context.user.idLong
							it[UserReports.guildId] = context.guild.idLong
							it[reportedAt] = System.currentTimeMillis()
							it[revoked] = false
							it[approved] = false
							it[UserReports.category] = categoryEnum
							it[reason] = "test"
						}

						for (message in foundMessages) {
							MessageProofs.insert {
								it[MessageProofs.reportId] = report

								it[MessageProofs.guildId] = message.guild.idLong
								it[MessageProofs.channelId] = message.channel.idLong
								it[MessageProofs.messageId] = message.idLong

								it[MessageProofs.authorId] = message.author.idLong
								it[MessageProofs.authorName] = message.author.name
								it[MessageProofs.authorDiscriminator] = message.author.discriminator

								it[MessageProofs.content] = message.contentRaw
								it[MessageProofs.sentAt] = System.currentTimeMillis()
							}
						}
					}

					// TODO: Adicionar que suporte mensagens do event log da Lori
					reply(
							LorittaReply(
									"Adicionado!"
							)
					)
				}
			}
		}
	}
}