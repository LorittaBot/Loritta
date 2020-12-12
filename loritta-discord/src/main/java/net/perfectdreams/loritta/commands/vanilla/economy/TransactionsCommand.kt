package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.TransactionLogHandler
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZoneId

class TransactionsCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("transactions", "transações"), CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.economy.transactions.description")

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

					this.append("$emoji ${TransactionLogHandler.getLogByTransactionData(user.idLong, locale, transaction)}")
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