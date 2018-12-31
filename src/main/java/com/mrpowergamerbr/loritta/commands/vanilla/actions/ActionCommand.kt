package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.locale.Gender
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

abstract class ActionCommand(name: String, aliases: List<String>) : AbstractCommand(name, aliases, CommandCategory.ACTION) {
	abstract fun getResponse(locale: LegacyBaseLocale, first: User, second: User): String
	abstract fun getFolderName(): String
	abstract fun getEmoji(): String

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return locale.commands.actions.examples.map { it.toString() }
	}

	private fun getGifsFor(userGender: Gender, receiverGender: Gender): List<File> {
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
		val response: String
		var files: List<File>
		val locale = context.legacyLocale
		val senderProfile = userProfile ?: loritta.getOrCreateLorittaProfile(user.id)
		val recProfile = receiverProfile ?: loritta.getOrCreateLorittaProfile(receiver.id)

		// Anti-gente idiota
		if (this is KissCommand && receiver.id == Loritta.config.clientId) {
			context.reply(
					LoriReply(
							locale.commands.actions.kiss.responseAntiIdiot,
							"\uD83D\uDE45"
					)
			)
			return
		}

		// R U a boy or girl?
		val userGender = transaction (Databases.loritta) { senderProfile.settings.gender }
		val receiverGender = transaction(Databases.loritta) { recProfile.settings.gender }

		response = getResponse(locale, user, receiver)

		// Quem tentar estapear a Loritta, vai ser estapeado
		files = if ((this is SlapCommand || this is AttackCommand || this is KissCommand) && receiver.id == Loritta.config.clientId) {
			getGifsFor(receiverGender, userGender)
		} else {
			getGifsFor(userGender, receiverGender)
		}

		while (files.isEmpty()) {
			// Caso não tenha nenhuma GIF disponível, vamos abrir o nosso "leque" de GIFs, para evitar que dê erro
			files = getGifsFor(Gender.UNKNOWN, Gender.UNKNOWN)
		}

		val randomImage = files.getRandom()

		val message = context.sendFile(
				randomImage,
				"action.gif",
				"${getEmoji()} **|** " + response
		)

		message.addReaction("reverse:492845304438194176").queue()

		message.onReactionAdd(context) {
			if (it.reactionEmote.id == "492845304438194176" && it.user.id == receiver.id) {
				runAction(context, receiver, recProfile, user, null)
			}
		}
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			val user = context.getUserAt(0)

			if (user == null) {
				context.reply(
						LoriReply(
								locale.commands.userDoesNotExists,
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