package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZoneId

object TransactionsCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("transactions", "transações"),  CommandCategory.ECONOMY) {
		description { it["commands.economy.transactions.description"] }

		arguments {
			argument(ArgumentType.USER) {
				optional = true
			}
		}

		executesDiscord {
			val user = user(0)?.handle ?: user

			val transactions = loritta.newSuspendedTransaction {
				SonhosTransaction.select {
					SonhosTransaction.givenBy eq user.idLong or (SonhosTransaction.receivedBy eq user.idLong)
				}.orderBy(SonhosTransaction.givenAt, SortOrder.DESC)
						.limit(20)
						.toMutableList()
			}

			val description = buildString {
				for (transaction in transactions) {
					val receivedSonhos = transaction[SonhosTransaction.receivedBy] == user.idLong

					val givenAtTime = Instant.ofEpochMilli(transaction[SonhosTransaction.givenAt])
							.atZone(ZoneId.systemDefault())

					val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
					val month = givenAtTime.monthValue.toString().padStart(2, '0')
					val year = givenAtTime.year

					val hour = givenAtTime.hour.toString().padStart(2, '0')
					val minute = givenAtTime.minute.toString().padStart(2, '0')

					this.append("`[$day/$month/$year $hour:$minute]` ")

					val emoji = if (receivedSonhos)
						"\uD83D\uDCB5"
					else
						"\uD83D\uDCB8"

					this.append(emoji)

					this.append(" ")

					if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.PAYMENT) {
						val receivedByUserId = if (receivedSonhos) {
							transaction[SonhosTransaction.givenBy]
						} else {
							transaction[SonhosTransaction.receivedBy]
						}

						val receivedByUser = lorittaShards.retrieveUserInfoById(receivedByUserId)

						val name = ("${receivedByUser?.name}#${receivedByUser?.discriminator} ($receivedByUserId)")

						if (receivedSonhos) {
							this.append(locale["commands.economy.transactions.receivedMoneySonhos", transaction[SonhosTransaction.quantity], "`$name`"])
						} else {
							this.append(locale["commands.economy.transactions.sentMoneySonhos", transaction[SonhosTransaction.quantity], "`$name`"])
						}
					} else if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.PAYMENT_TAX) {
						this.append(locale["commands.economy.transactions.sentMoneySonhosTax", transaction[SonhosTransaction.quantity]])
					} else {
						val type = transaction[SonhosTransaction.reason].name
								.toLowerCase()
								.replace("_", " ")
								.split(" ")
								.map {
									it.capitalize()
								}
								.joinToString("")
								.toCharArray().apply {
									this[0] = this[0].toLowerCase()
								}
								.joinToString("")

						val genericTypeName = locale["commands.economy.transactions.types.${type}"]

						if (receivedSonhos)
							this.append(locale["commands.economy.transactions.genericReceived", transaction[SonhosTransaction.quantity], genericTypeName])
						else
							this.append(locale["commands.economy.transactions.genericSent", transaction[SonhosTransaction.quantity], genericTypeName])
					}
					this.append("\n")
				}
			}

			val embed = EmbedBuilder()
					.setTitle(
							"${Emotes.LORI_RICH} " +
									if (user != this.user)
										locale["commands.economy.transactions.otherUserTransactions", user.asTag]
									else
										locale["commands.economy.transactions.title"]
					)
					.setColor(Constants.LORITTA_AQUA)
					.setDescription(description)

			sendMessage(getUserMention(true), embed.build())
		}
	}
}