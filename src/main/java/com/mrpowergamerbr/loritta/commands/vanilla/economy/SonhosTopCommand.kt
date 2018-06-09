package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class SonhosTopCommand : AbstractCommand("sonhostop", listOf("topsonhos"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["RANK_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val userData = loritta.usersColl
				.find(Filters.gt("dreams", 60000))
				.sort(Sorts.descending("dreams"))
				.limit(10)

		var content = "```"

		for (data in userData) {
			val user = lorittaShards.retrieveUserById(data.userId)

			if (user != null) {
				content += "${user.name.stripCodeMarks()}#${user.discriminator} - ${data.dreams} Sonhos\n"
			}
		}

		content += "```"

		context.sendMessage(content)
	}
}