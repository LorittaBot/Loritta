package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.discordCommand

object NotifyCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("notify", "notificar"), CommandCategory.MISC) {
		this.hideInHelp = true
		this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
			lorittaMessageEvent.guild?.idLong == 297732013006389252L
		}

		executesDiscord {
			val roleId = if (loritta.config.loritta.environment == EnvironmentType.CANARY)
				526720753991811072L
			else
				334734175531696128L

			val role = guild.getRoleById(roleId)!!
			val member = this.member!!

			if (member.roles.contains(role)) {
				guild.removeRoleFromMember(member, role).await()

				reply(
						LorittaReply(
								"Sério mesmo que você não quer mais receber minhas incríveis novidades? E eu pensava que nós eramos amigos...",
								"<:lori_sadcraft:370344565967814659>"
						)
				)
			} else {
				guild.addRoleToMember(member, role).await()

				reply(
						LorittaReply(
								"Agora você irá ser notificado sobre as minhas novidades!",
								"<:lori_feliz:519546310978830355>"
						)
				)
			}
		}
	}
}