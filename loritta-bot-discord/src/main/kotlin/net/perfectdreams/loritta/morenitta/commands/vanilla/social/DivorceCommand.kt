package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.common.utils.Emotes
import org.jetbrains.exposed.sql.update
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction

class DivorceCommand(loritta: LorittaBot) : AbstractCommand(loritta, "divorce", listOf("divorciar"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.divorce"
		const val DIVORCE_REACTION_EMOJI = "\uD83D\uDC94"
		const val DIVORCE_EMBED_URI = "https://cdn.discordapp.com/emojis/556524143281963008.png?size=2048"
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val userProfile = context.lorittaUser._profile ?: run {
			// If the user doesn't have any profile, then he won't have any marriage anyway
			context.reply(
					LorittaReply(
							locale["commands.category.social.youAreNotMarried", "`${context.config.commandPrefix}casar`", Emotes.LORI_HEART],
							Constants.ERROR
					)
			)
			return
		}

		val marriage = ProfileUtils.getMarriageInfo(loritta, userProfile) ?: run {
			// Now that's for when the marriage doesn't exist
			context.reply(
					LorittaReply(
							locale["commands.category.social.youAreNotMarried", "`${context.config.commandPrefix}casar`", Emotes.LORI_HEART],
							Constants.ERROR
					)
			)
			return
		}

		val marriagePartner = marriage.partner
		val userMarriage = marriage.marriage

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
			if (it.emoji.isEmote(DIVORCE_REACTION_EMOJI)) {
				// depois
				loritta.newSuspendedTransaction {
					Profiles.update({ Profiles.marriage eq userMarriage.id }) {
						it[Profiles.marriage] = null
					}
					userMarriage.delete()
				}

				message.delete().queue()

				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.divorced", Emotes.LORI_HEART]
						)
				)

				try {
					// We don't care if we can't find the user, just exit
					val userPrivateChannel = loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(marriagePartner.id) ?: return@onReactionAddByAuthor

					userPrivateChannel.sendMessageEmbeds(
							EmbedBuilder()
									.setTitle(locale["$LOCALE_PREFIX.divorcedTitle"])
									.setDescription(locale["$LOCALE_PREFIX.divorcedDescription", context.userHandle.name])
									.setThumbnail(DIVORCE_EMBED_URI)
									.setColor(Constants.LORITTA_AQUA)
									.build()
					).queue()
				} catch (e: Exception) {}
			}
		}

		message.addReaction(DIVORCE_REACTION_EMOJI).queue()
	}
}