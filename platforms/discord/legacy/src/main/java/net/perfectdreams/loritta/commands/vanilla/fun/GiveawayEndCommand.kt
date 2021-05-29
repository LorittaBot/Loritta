package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.tables.servers.Giveaways
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import org.jetbrains.exposed.sql.and

class GiveawayEndCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("giveaway end", "sorteio end"), CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

		canUseInPrivateChannel = false

		localizedDescription("$LOCALE_PREFIX.giveawayend.description")

		usage {
			arguments {
				argument(ArgumentType.TEXT) {}
			}
		}

		executesDiscord {
			val context = this

			val link = context.args.getOrNull(0)

			if (link == null) {
				context.explain()
				return@executesDiscord
			}

			val split = link.split("/")

			var messageId: Long? = null
			var channelId: Long? = null

			if (split.size == 1 && split[0].isValidSnowflake()) {
				messageId = split[0].toLong()
			} else {
				messageId = split.getOrNull(split.size - 1)?.toLongOrNull()
				channelId = split.getOrNull(split.size - 2)?.toLongOrNull()
			}

			if (messageId == null) {
				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.giveawayend.giveawayInvalidArguments", "`https://canary.discordapp.com/channels/297732013006389252/297732013006389252/594270558238146603`"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			val giveaway = loritta.newSuspendedTransaction {
				if (channelId != null) {
					Giveaway.find {
						(Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId) and (Giveaways.textChannelId eq channelId)
					}.firstOrNull()
				} else {
					Giveaway.find {
						(Giveaways.guildId eq context.guild.idLong) and (Giveaways.messageId eq messageId)
					}.firstOrNull()
				}
			}

			if (giveaway == null) {
				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.giveawayend.giveawayDoesNotExist"],
								Emotes.LORI_HM
						)
				)
				return@executesDiscord
			}

			if (giveaway.finished) {
				context.reply(
						LorittaReply(
								locale[
										"$LOCALE_PREFIX.giveawayend.giveawayAlreadyEnded",
										"`${locale["$LOCALE_PREFIX.giveawayend.giveawayHowToReroll", context.serverConfig.commandPrefix, link.stripCodeMarks()]}`"
								],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			val textChannel = context.guild.getTextChannelById(giveaway.textChannelId)

			if (textChannel == null) {
				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.giveawayend.channelDoesNotExist"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			val message = textChannel.retrieveMessageById(messageId).await()

			if (message == null) {
				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.giveawayend.messageDoesNotExist"],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			GiveawayManager.finishGiveaway(message, giveaway)

			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.giveawayend.finishedGiveaway"],
							Emotes.LORI_HAPPY
					)
			)
		}
	}
}
