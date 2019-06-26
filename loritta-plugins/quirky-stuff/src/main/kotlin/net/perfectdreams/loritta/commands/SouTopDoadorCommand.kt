package net.perfectdreams.loritta.commands

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.ColorUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction

class SouTopDoadorCommand(val config: QuirkyConfig) : LorittaDiscordCommand(arrayOf("soutopdoador", "soutopdonator"), CommandCategory.MAGIC) {
	@Subcommand
	suspend fun root(context: DiscordCommandContext, args: Array<String>) {
		if (context.discordGuild?.id != Constants.PORTUGUESE_SUPPORT_GUILD_ID)
			return

		val moneySumId = Payments.money.sum()
		val mostPayingUsers = transaction(Databases.loritta) {
			Payments.slice(Payments.userId, moneySumId)
					.select {
						Payments.paidAt.isNotNull() and
								(Payments.reason eq PaymentReason.DONATION)
					}

					.groupBy(Payments.userId)
					.orderBy(moneySumId, SortOrder.DESC)
					.limit(3)
					.toMutableList()
		}

		val index = mostPayingUsers.indexOfFirst { it[Payments.userId] == context.userHandle.idLong }

		if (index !in 0..2) {
			context.reply(
					LoriReply(
							"Para você personalizar o seu lindo cargo, você precisa ser um dos top doadores no <#592352881072668693>! Será que você irá conseguir chegar no topo? ${Emotes.LORI_TEMMIE}",
							Constants.ERROR
					)
			)
			return
		}

		val editRole = context.discordGuild!!.getRoleById(
				when (index) {
					0 -> config.topDonatorsRank.topRole1
					1 -> config.topDonatorsRank.topRole2
					2 -> config.topDonatorsRank.topRole3
					else -> throw RuntimeException("Unknown role for index $index")
				}
		)!!

		if (args.isEmpty()) {
			context.reply(
					LoriReply(
							"`+soutopdoador nome Nome do Cargo`",
							Constants.ERROR
					),
					LoriReply(
							"`+soutopdoador cor CorEmHexadecimalOuEmRGB`",
							Constants.ERROR
					)
			)
			return
		} else {
			if (args[0] == "nome") {
				val name = args.toMutableList().apply { this.removeAt(0) }.joinToString(" ")

				editRole.manager.setName("\uD83C\uDF1F $name | Top Doador ${index + 1}").await()

				context.reply(
						LoriReply(
								"Nome do cargo alterado com sucesso!",
								Emotes.LORI_HAPPY
						)
				)
			}
			if (args[0] == "cor") {
				val color = ColorUtils.getColorFromString(args.toMutableList().apply { this.removeAt(0) }.joinToString(" "))

				if (color == null) {
					context.reply(
							LoriReply(
									"Cor inválida! Você deve colocar uma cor em formato RGB (`255, 255, 255`) ou em formato hexadecimal `#fffff`!",
									Constants.ERROR
							)
					)
				}

				editRole.manager.setColor(color).await()

				context.reply(
						LoriReply(
								"Cor alterada com sucesso!",
								Emotes.LORI_HAPPY
						)
				)
			}
		}
	}
}