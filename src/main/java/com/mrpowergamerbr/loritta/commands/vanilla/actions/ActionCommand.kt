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
import net.dv8tion.jda.core.entities.User
import java.io.File

abstract class ActionCommand(name: String, aliases: List<String>) : AbstractCommand(name, aliases, CommandCategory.ACTION) {
	abstract fun getResponse(locale: BaseLocale, first: User, second: User): String
	abstract fun getFolderName(): String
	abstract fun getEmoji(): String

	override fun getUsage(): String {
		return "<usuário>"
	}

	override fun getExample(): List<String> {
		return listOf("297153970613387264", "@Loritta", "@MrPowerGamerBR")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
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

			val folder = File(Loritta.ASSETS, "actions/${getFolderName()}")
			val folderNames = context.lorittaUser.profile.gender.getValidActionFolderNames(other)

			val files = folderNames.flatMap {
				File(folder, it).listFiles().filter { it.extension == "gif" || it.extension == "png" }
			}

			val randomImage = files.getRandom()

			context.sendFile(
					randomImage,
					"action.gif",
					"${getEmoji()} **|** " + getResponse(locale, context.userHandle, user)
			)
		} else {
			context.explain()
		}
	}
}