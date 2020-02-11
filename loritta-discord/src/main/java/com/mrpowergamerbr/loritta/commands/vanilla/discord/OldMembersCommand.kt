package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.utils.Emotes
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.CommandCategory

class OldMembersCommand : AbstractCommand("oldmembers", listOf("membrosantigos", "oldusers", "usuáriosantigos", "usuariosantigos"), CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.discord.oldMembers.description"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		showOldMembers(null, context, 0)
	}

	suspend fun showOldMembers(message: Message?, context: CommandContext, page: Int) {
		val guild = context.guild

		val sortedMembers = guild.members.sortedBy { it.timeJoined }

		val sortedMembersInCurrentPage = sortedMembers.subList(page * 10, Math.min((page + 1) * 10, guild.members.size))

		val maxPage = guild.members.size / 10
		val userCurrentPage = sortedMembers.indexOf(context.handle) / 10

		val embed = EmbedBuilder().apply {
			setColor(Constants.DISCORD_BLURPLE)
			setTitle("\uD83C\uDF1F ${context.legacyLocale.toNewLocale()["commands.discord.oldMembers.theOldestPeople", guild.name]}")

			for ((index, member) in sortedMembersInCurrentPage.withIndex()) {
				val ownerEmote = when {
					member?.isOwner == true -> "\uD83D\uDC51"
					else -> ""
				}

				val userEmote = when {
					member == context.handle -> "\uD83D\uDC81"
					else -> ""
				}

				val typeEmote = when {
					member.user.isBot -> Emotes.BOT_TAG
					else -> Emotes.WUMPUS_BASIC
				}

				val statusEmote = when (member?.onlineStatus) {
					OnlineStatus.ONLINE -> Emotes.ONLINE
					OnlineStatus.IDLE -> Emotes.IDLE
					OnlineStatus.DO_NOT_DISTURB -> Emotes.DO_NOT_DISTURB
					else -> Emotes.OFFLINE
				}

				appendDescription("`${1 + index + (page * 10)}º` $ownerEmote$typeEmote$statusEmote$userEmote `${member.user.name.stripCodeMarks()}#${member.user.discriminator}`\n")
				setFooter("${context.legacyLocale.toNewLocale()["loritta.page"]} ${context.locale["loritta.xOfX", page + 1, maxPage + 1]} | ${context.locale["loritta.youAreCurrentlyOnPage", userCurrentPage + 1]}", null)
			}
		}

		val _message = message?.edit(context.getAsMention(true), embed.build()) ?: context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		_message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("⏪")) {
				showOldMembers(_message, context, 0)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("◀")) {
				showOldMembers(_message, context, page - 1)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("▶")) {
				showOldMembers(_message, context, page + 1)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("⏩")) {
				showOldMembers(_message, context, maxPage)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("\uD83D\uDC81")) {
				showOldMembers(_message, context, userCurrentPage)
				return@onReactionAddByAuthor
			}
		}

		_message.addReaction("\uD83D\uDC81").await()

		if (page != 0) {
			_message.addReaction("⏪").await()
			_message.addReaction("◀").await()
		}
		if (maxPage != page) {
			_message.addReaction("▶").await()
			_message.addReaction("⏩").await()
		}
	}
}