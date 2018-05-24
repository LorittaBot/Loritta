package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaGuildUserData
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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
			val user = lorittaShards.retriveUserById(data.userId)

			if (user != null) {
				content += "${user.name.stripCodeMarks()}#${user.discriminator} - ${data.dreams} Sonhos\n"
			}
		}

		content += "```"

		context.sendMessage(content)
	}
}