package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.LorittaLauncher
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.deviousfun.await
import net.perfectdreams.loritta.morenitta.LorittaBot

class ServerInvitesCommand(loritta: LorittaBot) : AbstractCommand(loritta, "serverinvites", category = net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Pega os invites de um servidor a partir do ID dele"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val serverId = context.args[0]

		var list = ""
		var idx = 0
		for (invite in loritta.lorittaShards.getGuildById(serverId)!!.retrieveInvites().await().sortedByDescending { it.uses }) {
			if (idx == 5)
				break
			list += "https://discord.gg/" + invite.code + " (" + invite.uses + "/" + invite.maxUses + ") (Criado por " + invite.inviter?.name + "#" + invite.inviter?.discriminator + ")\n"
			idx++
		}

		context.sendMessage(context.getAsMention(true) + "\n" + list)
	}
}