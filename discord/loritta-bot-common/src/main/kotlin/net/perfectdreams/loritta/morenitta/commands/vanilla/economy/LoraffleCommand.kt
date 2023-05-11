package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.RaffleCommand

class LoraffleCommand(loritta: LorittaBot) : AbstractCommand(loritta, "loraffle", listOf("rifa", "raffle", "lorifa"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.raffle.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.raffle.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "comprar" || arg0 == "buy") {
			val quantity = Math.max(context.args.getOrNull(1)?.toLongOrNull() ?: 1, 1)

			RaffleCommand.executeBuyCompat(
				CommandContextCompat.LegacyMessageCommandContextCompat(context),
				RaffleType.ORIGINAL,
				quantity
			)
			return
		}

		RaffleCommand.executeStatusCompat(
			CommandContextCompat.LegacyMessageCommandContextCompat(context),
			RaffleType.ORIGINAL
		)
	}

	enum class BuyRaffleTicketStatus {
		SUCCESS,
		THRESHOLD_EXCEEDED,
		TOO_MANY_TICKETS,
		NOT_ENOUGH_MONEY,
		STALE_RAFFLE_DATA
	}
}