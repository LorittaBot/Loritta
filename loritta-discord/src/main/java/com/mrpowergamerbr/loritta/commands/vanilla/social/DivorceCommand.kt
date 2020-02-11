package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.utils.Emotes
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DivorceCommand : AbstractCommand("divorce", listOf("divorciar"), CommandCategory.SOCIAL) {
	companion object {
	    const val LOCALE_PREFIX = "commands.social.divorce"
		const val DIVORCE_REACTION_EMOJI = "\uD83D\uDC94"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DIVORCE_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val marriage = transaction(Databases.loritta) { context.lorittaUser.profile.marriage }

		if (marriage != null) {
			val message = context.reply(
					LoriReply(
							locale.toNewLocale()["$LOCALE_PREFIX.prepareToDivorce", Emotes.LORI_CRYING],
							"\uD83D\uDDA4"
					),
					LoriReply(
							locale.toNewLocale()["$LOCALE_PREFIX.pleaseConfirm", DIVORCE_REACTION_EMOJI],
							mentionUser = false
					)
			)

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.isEmote(DIVORCE_REACTION_EMOJI)) {
					// depois
					transaction(Databases.loritta) {
						Profiles.update({ Profiles.marriage eq marriage.id }) {
							it[Profiles.marriage] = null
						}
						marriage.delete()
					}

					message.delete().queue()

					context.reply(
							LoriReply(
									locale.toNewLocale()["$LOCALE_PREFIX.divorced", Emotes.LORI_HUG]
							)
					)
				}
			}

			message.addReaction(DIVORCE_REACTION_EMOJI).queue()
		} else {
			context.reply(
					LoriReply(
							locale.toNewLocale()["commands.social.youAreNotMarried", "`${context.config.commandPrefix}casar`", Emotes.LORI_HUG],
							Constants.ERROR
					)
			)
		}
	}
}