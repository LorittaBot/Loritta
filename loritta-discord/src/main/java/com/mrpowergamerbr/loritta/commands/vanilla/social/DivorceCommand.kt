package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.update

class DivorceCommand : AbstractCommand("divorce", listOf("divorciar"), CommandCategory.SOCIAL) {
	companion object {
	    const val LOCALE_PREFIX = "commands.social.divorce"
		const val DIVORCE_REACTION_EMOJI = "\uD83D\uDC94"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["$LOCALE_PREFIX.description"]
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val marriage = loritta.newSuspendedTransaction { context.lorittaUser.profile.marriage }
		val userId = if (context.userHandle.idLong == marriage?.user2) marriage.user1 else marriage?.user2
		val user = lorittaShards.getUserById(userId)
		val userDM = user?.openPrivateChannel()?.await()

		if (marriage != null) {
			val message = context.reply(
                    LorittaReply(
                            locale["$LOCALE_PREFIX.prepareToDivorce", Emotes.LORI_CRYING],
                            "\uD83D\uDDA4"
                    ),
                    LorittaReply(
                            locale["$LOCALE_PREFIX.pleaseConfirm", DIVORCE_REACTION_EMOJI],
                            mentionUser = false
                    )
			)

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.isEmote(DIVORCE_REACTION_EMOJI)) {
					// depois
					loritta.newSuspendedTransaction {
						Profiles.update({ Profiles.marriage eq marriage.id }) {
							it[Profiles.marriage] = null
						}
						marriage.delete()
					}

					message.delete().queue()

					context.reply(
                            LorittaReply(
                                    locale["$LOCALE_PREFIX.divorced", Emotes.LORI_HUG]
                            )
					)
					
					userDM?.sendMessage(locale["$LOCALE_PREFIX.divorcedDM", context.userHandle.name])?.queue()
				}
			}

			message.addReaction(DIVORCE_REACTION_EMOJI).queue()
		} else {
			context.reply(
                    LorittaReply(
                            locale["commands.social.youAreNotMarried", "`${context.config.commandPrefix}casar`", Emotes.LORI_HUG],
                            Constants.ERROR
                    )
			)
		}
	}
}
