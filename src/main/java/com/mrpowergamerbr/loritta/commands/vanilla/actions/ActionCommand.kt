package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import net.dv8tion.jda.core.entities.User
import java.io.File

abstract class ActionCommand(name: String, aliases: List<String>) : AbstractCommand(name, aliases, CommandCategory.ACTION) {
	abstract fun getResponse(locale: BaseLocale, first: User, second: User): String
	abstract fun getFolderName(): String
	abstract fun getEmoji(): String

	override fun getUsage(): String {
		return "<usuário>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
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

			fun runAction(user: User, userProfile: LorittaProfile?, receiver: User, receiverProfile: LorittaProfile?) {
				val userProfile = userProfile ?: loritta.getLorittaProfileForUser(user.id)
				val receiverProfile = receiverProfile ?: loritta.getLorittaProfileForUser(receiver.id)

				val other = receiverProfile.gender

				val folder = File(Loritta.ASSETS, "actions/${getFolderName()}")
				val folderNames = userProfile.gender.getValidActionFolderNames(other).toMutableList()
				if (folderNames.size != 1 && Loritta.RANDOM.nextBoolean()) // Remover "generic", para evitar muitas gifs repetidas
					folderNames.remove("generic")

				val files = folderNames.flatMap {
					File(folder, it).listFiles().filter { it.extension == "gif" || it.extension == "png" }
				}

				val randomImage = files.getRandom()

				val message = context.sendFileComplete(
						randomImage,
						"action.gif",
						"${getEmoji()} **|** " + getResponse(locale, user, receiver)
				)

				message.addReaction("reverse:492845304438194176").queue()

				message.onReactionAdd(context) {
					if (it.reactionEmote.id == "492845304438194176" && it.user.id == receiver.id) {
						runAction(receiver, receiverProfile, user, null)
					}
				}
			}

			runAction(context.userHandle, context.lorittaUser.profile, user, null)
		} else {
			context.explain()
		}
	}
}