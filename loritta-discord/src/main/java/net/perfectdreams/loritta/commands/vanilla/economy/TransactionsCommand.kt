package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.RankingGenerator.isValidRankingPage
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZoneId

class TransactionsCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("transactions", "transações", "transacoes", "transaçoes"), CommandCategory.ECONOMY) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command.transactions"
		private const val ENTRIES_PER_PAGE = 10
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		arguments {
			argument(ArgumentType.USER) {
				optional = true
			}
		}

		executesDiscord {

			var customPage = if (user(0) != null) {
				args.getOrNull(1)?.toLongOrNull()
			} else {
				args.getOrNull(0)?.toLongOrNull()
			}

			if (customPage != null) {
				customPage -= 1
			}

			if (customPage != null && !isValidRankingPage(customPage)) {
				reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.pageDoesNotExist"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			if (customPage == null) customPage = 0

			val allTransactions = loritta.newSuspendedTransaction {
				SonhosTransaction.select {
					SonhosTransaction.givenBy eq user.idLong or (SonhosTransaction.receivedBy eq user.idLong)
				}.count()
			}

			if (allTransactions == 0L) {
				reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.unknownTransactions"]
						)
				)
				return@executesDiscord
			}

			sendTransactionEmbed(
					this,
					locale,
					customPage,
					null,
			)
		}
	}

	suspend fun sendTransactionEmbed(context: DiscordCommandContext, locale: BaseLocale, item: Long?, currentMessage: Message?) {
		val user = context.user(0)?.handle ?: context.user

		var page = item

		if (page == null) page = 0

		val transactions = loritta.newSuspendedTransaction {
			SonhosTransaction.select {
				SonhosTransaction.givenBy eq user.idLong or (SonhosTransaction.receivedBy eq user.idLong)
			}.orderBy(SonhosTransaction.id, SortOrder.DESC)
					.limit(ENTRIES_PER_PAGE, page * ENTRIES_PER_PAGE)
					.toList()
		}

		val allTransactions = loritta.newSuspendedTransaction {
			SonhosTransaction.select {
				SonhosTransaction.givenBy eq user.idLong or (SonhosTransaction.receivedBy eq user.idLong)
			}.count()
		}

		if (transactions.isEmpty()) {
			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.pageDoesNotExist"],
							Constants.ERROR
					)
			)
			return
		}

		if (allTransactions == 0L) {
			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.unknownTransactions"]
					)
			)
			return
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
						this.append(locale["$LOCALE_PREFIX.receivedMoneySonhos", transaction[SonhosTransaction.quantity], "`$name`"])
					} else {
						this.append(locale["$LOCALE_PREFIX.sentMoneySonhos", transaction[SonhosTransaction.quantity], "`$name`"])
					}
				} else if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.PAYMENT_TAX) {
					this.append(locale["$LOCALE_PREFIX.sentMoneySonhosTax", transaction[SonhosTransaction.quantity]])
				} else if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.COIN_FLIP_BET) { 
					val receivedByUserId = if (receivedSonhos) {
						transaction[SonhosTransaction.givenBy]
					} else {
						transaction[SonhosTransaction.receivedBy]
					}

					val receivedByUser = lorittaShards.retrieveUserInfoById(receivedByUserId)

					val name = ("${receivedByUser?.name}#${receivedByUser?.discriminator} ($receivedByUserId)")

					if (receivedSonhos) {
						this.append(locale["$LOCALE_PREFIX.receivedMoneySonhosOnCoinFlipBet", transaction[SonhosTransaction.quantity], "`$name`"])
					} else {
						this.append(locale["$LOCALE_PREFIX.sentMoneySonhosOnCoinFlipBet", transaction[SonhosTransaction.quantity], "`$name`"])
					}		
				} else if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.DISCORD_BOTS) { 
					this.append(locale["$LOCALE_PREFIX.receivedMoneySonhosOnDiscordBotList", transaction[SonhosTransaction.quantity]])
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

					val genericTypeName = locale["$LOCALE_PREFIX.types.${type}"]

					if (receivedSonhos)
						this.append(locale["$LOCALE_PREFIX.genericReceived", transaction[SonhosTransaction.quantity], genericTypeName])
					else
						this.append(locale["$LOCALE_PREFIX.genericSent", transaction[SonhosTransaction.quantity], genericTypeName])
				}
				this.append("\n")
			}
		}

		val embed = EmbedBuilder().apply {
			setTitle(
					"${Emotes.LORI_RICH} " +
							if (user != context.user)
								"${locale["$LOCALE_PREFIX.otherUserTransactions", user.asTag]} — ${locale["$LOCALE_PREFIX.page"]} ${page + 1}"
							else
								"${locale["$LOCALE_PREFIX.title"]} — ${locale["$LOCALE_PREFIX.page"]} ${page + 1}"
			)
			setColor(Constants.LORITTA_AQUA)
			setDescription(description)
			setFooter("${locale["$LOCALE_PREFIX.totalTransactions"]}: $allTransactions")
		}

		val message = currentMessage?.edit(context.getUserMention(true), embed.build(), clearReactions = false) ?: context.sendMessage(context.getUserMention(true), embed.build())

		// We don't want the user to see more than 100 pages of transactions
		val allowForward = allTransactions >= (page + 1) * ENTRIES_PER_PAGE && 100 > page
		val allowBack = page != 0L

		message.onReactionByAuthor(context) {
			if (allowForward && it.reactionEmote.isEmote("⏩")) {
				sendTransactionEmbed(
							context,
							locale,
							page + 1,
							message,
				)
			}
			if (allowBack && it.reactionEmote.isEmote("⏪")) {
				sendTransactionEmbed(
							context,
							locale,
							page - 1,
							message,
				)
			}
		}

		val emotes = mutableListOf<String>()

		if (allowBack)
			emotes.add("⏪")
		if (allowForward)
			emotes.add("⏩")

		message.doReactions(*emotes.toTypedArray())
	}
}
