package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class PatreonCommand : AbstractCommand("donator", listOf("donators", "patreons", "patreon", "doadores", "doador", "apoiador", "apoiadores", "contribuidores", "contribuidor", "doar", "donate"), category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PATREON_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var patrons = ""

		val moneySumId = Payments.money.sum()
		val mostPayingUsers = transaction(Databases.loritta) {
			Payments.slice(Payments.userId, moneySumId)
					.select {
						Payments.paidAt.isNotNull() and
								(Payments.reason eq PaymentReason.DONATION) or (Payments.reason eq PaymentReason.SPONSORED) and
								(Payments.expiresAt greaterEq System.currentTimeMillis())
					}
					.groupBy(Payments.userId)
					.orderBy(moneySumId, SortOrder.DESC)
					.toMutableList()
		}

		patrons = mostPayingUsers.map {
			val money = it[moneySumId]?.toDouble() ?: 0.0
			val isBold = money >= 59.99

			logger.info { "ID is ${it[Payments.userId]}" }

			val user = lorittaShards.retrieveUserById(it[Payments.userId])

			if (user != null) {
				var name = "`${user.name}#${user.discriminator}`"
				if (isBold) {
					name = "**$name**"
				}
				name
			} else { "???" }
		}.joinToString(", ")

		val embed = EmbedBuilder().apply {
			setThumbnail("https://loritta.website/assets/img/fanarts/Loritta_-_Heathecliff.png")
			setTitle("${Emotes.LORI_RICH} ${context.legacyLocale["PATREON_THANKS"]}")
			setColor(Color(0, 193, 223))
			setDescription(patrons)
			addField("\uD83C\uDF80 " + context.legacyLocale["PATREON_DO_YOU_WANNA_HELP"], context.locale["commands.misc.donate.howToHelp", "${loritta.instanceConfig.loritta.website.url}donate", Emotes.LORI_HEART, Emotes.LORI_CRYING, Emotes.LORI_RICH], false)
		}

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}