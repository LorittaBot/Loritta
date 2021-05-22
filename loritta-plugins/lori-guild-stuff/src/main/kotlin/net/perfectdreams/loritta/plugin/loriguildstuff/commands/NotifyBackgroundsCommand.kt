package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.discordCommand

object NotifyBackgroundsCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("notify backgrounds", "notificar backgrounds"), CommandCategory.MISC) {
		this.hideInHelp = true
		this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
			lorittaMessageEvent.guild?.idLong == 297732013006389252L
		}

		executesDiscord {
			val roleId = 700368699701592185L

			val role = guild.getRoleById(roleId)!!
			val member = this.member!!

			if (member.roles.contains(role)) {
				guild.removeRoleFromMember(member, role).await()

				reply(
						LorittaReply(
								"Sério mesmo que você não quer mais receber meus incríveis backgrounds? E eu pensava que nós eramos amigos...",
								"<:lori_sadcraft:370344565967814659>"
						)
				)
			} else {
				guild.addRoleToMember(member, role).await()

				reply(
						LorittaReply(
								"Agora você irá ser notificado sobre meus novos backgrounds!",
								"<:lori_feliz:519546310978830355>"
						)
				)
			}
		}
	}
}