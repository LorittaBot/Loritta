package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
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

	suspend fun runAction(context: CommandContext, user: User, userProfile: Profile?, receiver: User, receiverProfile: Profile?) {
		val locale = context.locale
		val userProfile = userProfile ?: loritta.getOrCreateLorittaProfile(user.id)
		val receiverProfile = receiverProfile ?: loritta.getOrCreateLorittaProfile(receiver.id)

		val other = receiverProfile.options.gender

		val folder = File(Loritta.ASSETS, "actions/${getFolderName()}")
		val folderNames = userProfile.options.gender.getValidActionFolderNames(other).toMutableList()
		if (folderNames.size != 1 && Loritta.RANDOM.nextBoolean()) // Remover "generic", para evitar muitas gifs repetidas
			folderNames.remove("generic")

		val files = folderNames.flatMap {
			File(folder, it).listFiles().filter { it.extension == "gif" || it.extension == "png" }
		}

		val randomImage = files.getRandom()

		val message = context.sendFile(
				randomImage,
				"action.gif",
				"${getEmoji()} **|** " + getResponse(locale, user, receiver)
		)

		message.addReaction("reverse:492845304438194176").queue()

		message.onReactionAdd(context) {
			if (it.reactionEmote.id == "492845304438194176" && it.user.id == receiver.id) {
				runAction(context, receiver, receiverProfile, user, null)
			}
		}
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
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

			runAction(context, context.userHandle, context.lorittaUser.profile, user, null)
		} else {
			context.explain()
		}
	}
}