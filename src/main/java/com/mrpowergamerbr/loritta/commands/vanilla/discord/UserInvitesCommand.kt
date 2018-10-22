package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission

class UserInvitesCommand : AbstractCommand("userinvites", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["USERINVITES_Description"]
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val invites = context.guild.invites.await().filter { it.inviter == context.userHandle }

		if (invites.isEmpty()) {
			context.reply(
					LoriReply(
							"Você não tem nenhum convite no servidor!",
							Constants.ERROR
					)
			)
			return
		}

		var content = ""
		val invitedCount = invites.sumBy { it.uses }

		for (invite in invites) {
			content += "**Convite:** [${invite.code}](${invite.url})\n"
			content += "**Usos:** ${invite.uses}"
			if (invite.maxUses != 0) {
				content += " (máximo de usos: ${invite.maxUses})"
			} else {
				content += " (máximo de usos: ∞)"
			}
			content += "\n\n"
		}

		val hasMore = 10 - invites.size
		if (0 > hasMore) {
			content += "*...e mais ${Math.abs(hasMore)} convites*\n\n"
		}

		content += "**Total de usuários convidados:** $invitedCount"

		val embed = EmbedBuilder()
				.setAuthor("${context.userHandle.name}#${context.userHandle.discriminator}", null, context.userHandle.effectiveAvatarUrl)
				.setDescription(content)
				.setFooter("Lembre-se: Spambot nem é gente!", null)
				.setColor(Constants.DISCORD_BLURPLE)
				.build()

		context.sendMessage(embed)
	}
}