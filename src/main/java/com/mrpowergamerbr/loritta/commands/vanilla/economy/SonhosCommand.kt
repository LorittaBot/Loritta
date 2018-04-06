package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.threads.LoteriaThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class SonhosCommand : AbstractCommand("sonhos", category = CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SONHOS_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var retriveDreamsFromUser = LorittaUtils.getUserFromContext(context, 0) ?: context.userHandle

		val lorittaProfile = if (retriveDreamsFromUser == context.userHandle) {
			context.lorittaUser.profile
		} else {
			loritta.getLorittaProfileForUser(retriveDreamsFromUser.id)
		}

		if (context.userHandle == retriveDreamsFromUser) {
			context.reply(
					LoriReply(
							locale["SONHOS_YouHave", lorittaProfile.dreams],
							"\uD83D\uDCB5"
					)
			)
		} else {
			context.reply(
					LoriReply(
							locale["SONHOS_UserHas", retriveDreamsFromUser.asMention, lorittaProfile.dreams],
							"\uD83D\uDCB5"
					)
			)
		}
	}
}