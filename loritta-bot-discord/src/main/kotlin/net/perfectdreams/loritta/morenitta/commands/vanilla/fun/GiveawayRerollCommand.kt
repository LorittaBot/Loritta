package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.Giveaways
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import org.jetbrains.exposed.sql.and

class GiveawayRerollCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("giveaway reroll", "sorteio reroll"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

		canUseInPrivateChannel = false

		localizedDescription("$LOCALE_PREFIX.giveawayreroll.description")

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

			if (!giveaway.finished) {
				context.reply(
						LorittaReply(
								locale[
										"$LOCALE_PREFIX.giveawayreroll.giveawayStillRunning",
										"`${locale["$LOCALE_PREFIX.giveawayreroll.giveawayHowToEnd", context.serverConfig.commandPrefix, link.stripCodeMarks()]}`"
								],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			val textChannel = context.guild.getGuildMessageChannelById(giveaway.textChannelId)

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

			val rerollCount = context.args.getOrNull(1)
				?.toIntOrNull()
				?.coerceIn(1..100)

			loritta.giveawayManager.rollWinners(message, giveaway, true, numberOfWinnersOverride = rerollCount)

			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.giveawayreroll.rerolledGiveaway"],
							Emotes.LORI_HAPPY
					)
			)
		}
	}
}