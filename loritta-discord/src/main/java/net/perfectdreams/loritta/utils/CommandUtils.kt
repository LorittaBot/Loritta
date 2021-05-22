package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser

object CommandUtils {
	suspend fun checkIfCommandIsDisabledInGuild(serverConfig: ServerConfig, locale: BaseLocale, channel: MessageChannel, member: Member, clazzName: String): Boolean {
		if (serverConfig.disabledCommands.contains(clazzName)) {
			val replies = mutableListOf(
					LorittaReply(
							locale["commands.commandDisabled"],
							Emotes.LORI_CRYING
					)
			)

			if (member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)) {
				replies.add(
						LorittaReply(
								locale["commands.howToReEnableCommands", "<${loritta.instanceConfig.loritta.website.url}guild/${member.guild.idLong}/configure/commands>"],
								Emotes.LORI_SMILE
						)
				)
			}

			channel.sendMessageAsync(
					replies.joinToString("\n") {
						it.build(JDAUser(member.user))
					}
			)
			return true
		}

		return false
	}
}