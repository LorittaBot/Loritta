package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.transactions.transaction
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

	fun getGifsFor(userGender: Gender, receiverGender: Gender): List<File> {
		val folder = File(Loritta.ASSETS, "actions/${getFolderName()}")
		val folderNames = userGender.getValidActionFolderNames(receiverGender).toMutableList()
		if (folderNames.size != 1 && Loritta.RANDOM.nextBoolean()) // Remover "generic", para evitar muitas gifs repetidas
			folderNames.remove("generic")

		val files = folderNames.flatMap {
			File(folder, it).listFiles().filter { it.extension == "gif" || it.extension == "png" }
		}

		return files
	}

	suspend fun runAction(context: CommandContext, user: User, userProfile: Profile?, receiver: User, receiverProfile: Profile?) {
		val locale = context.locale
		val userProfile = userProfile ?: loritta.getOrCreateLorittaProfile(user.id)
		val receiverProfile = receiverProfile ?: loritta.getOrCreateLorittaProfile(receiver.id)

		// Anti-gente idiota
		if (this is KissCommand && receiver.id == Loritta.config.clientId) {
			context.reply(
					locale["KISS_NahPleaseDont"],
					"\uD83D\uDE45"
			)
			return
		}

		// R U a boy or girl?
		val userGender = transaction (Databases.loritta) { userProfile.settings.gender }
		val receiverGender = transaction(Databases.loritta) { receiverProfile.settings.gender }

		var files = getGifsFor(userGender, receiverGender)

		while (files.isEmpty()) {
			// Caso não tenha nenhuma GIF disponível, vamos abrir o nosso "leque" de GIFs, para evitar que dê erro
			files = getGifsFor(Gender.UNKNOWN, Gender.UNKNOWN)
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