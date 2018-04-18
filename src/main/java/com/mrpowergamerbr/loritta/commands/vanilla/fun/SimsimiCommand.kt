package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLEncoder

class SimsimiCommand : AbstractCommand("simsimi", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String = locale["SIMSIMI_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun hasCommandFeedback(): Boolean = false

	var currentProxy: Pair<String, Int>? = null

	override fun run(context: CommandContext, locale: BaseLocale) {
		val premiumKey = loritta.getPremiumKey(context.config.premiumKey)

		if (premiumKey == null || 90 > premiumKey.paid) {
			context.reply(
					locale["PREMIUM_CantUseFeature"],
					"\uD83D\uDCB8"
			)
			return
		}

		// TODO: Cadê o resto do comando? :whatdog:
	}
}