package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import java.io.File

class HugCommand : AbstractCommand("hug", listOf("abraço", "abraçar", "abraco", "abracar"), CommandCategory.ACTION) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["HUG_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val user = context.getUserAt(0)

		if (user == null) {
			context.reply(
					LoriReply(
							locale["BAN_UserDoesntExist"],
							Constants.ERROR
					)
			)
			return
		}

		val lorittaProfile = loritta.getLorittaProfileForUser(user.id)
		val other = lorittaProfile.gender

		val folder = File(Loritta.ASSETS, "actions/hug")
		val folderNames = context.lorittaUser.profile.gender.getValidActionFolderNames(other)

		val files = folderNames.flatMap {
			File(folder, it).listFiles().filter { it.extension == "gif" || it.extension == "png" }
		}

		val randomImage = files.getRandom()

		context.sendFile(
				randomImage,
				"action.gif",
				"uwu"
		)
	}
}