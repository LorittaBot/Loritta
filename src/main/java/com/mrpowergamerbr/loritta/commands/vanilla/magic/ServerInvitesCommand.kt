package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class ServerInvitesCommand : AbstractCommand("serverinvites", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Pega os invites de um servidor a partir do ID dele"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val serverId = context.args[0]

		var list = ""
		for (invite in LorittaLauncher.loritta.lorittaShards.getGuildById(serverId)!!.invites.complete()) {
			list += "https://discord.gg/" + invite.code + " (" + invite.uses + "/" + invite.maxUses + ") (Criado por " + invite.inviter.name + "#" + invite.inviter.discriminator + ")\n"
		}
		context.sendMessage(context.getAsMention(true) + "\n" + list)
	}
}