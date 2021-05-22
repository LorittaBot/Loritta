package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.commands.CommandCategory

class ServerInvitesCommand : AbstractCommand("serverinvites", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Pega os invites de um servidor a partir do ID dele"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val serverId = context.args[0]

		var list = ""
		var idx = 0
		for (invite in LorittaLauncher.loritta.lorittaShards.getGuildById(serverId)!!.retrieveInvites().await().sortedByDescending { it.uses }) {
			if (idx == 5)
				break
			list += "https://discord.gg/" + invite.code + " (" + invite.uses + "/" + invite.maxUses + ") (Criado por " + invite.inviter?.name + "#" + invite.inviter?.discriminator + ")\n"
			idx++
		}

		context.sendMessage(context.getAsMention(true) + "\n" + list)
	}
}