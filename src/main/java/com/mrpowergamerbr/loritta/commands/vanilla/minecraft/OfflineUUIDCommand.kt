package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import org.apache.commons.codec.Charsets
import java.util.*

class OfflineUUIDCommand : CommandBase() {
	override fun getLabel(): String {
		return "offlineuuid"
	}

	override fun getDescription(): String {
		return "Pega a UUID offline (ou seja, de servidores sem autenticação da Mojang) de um player"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MINECRAFT
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("Monerk")
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + context.args[0]).toByteArray(Charsets.UTF_8))

			context.sendMessage(context.getAsMention(true) + "**UUID offline (sem autenticação da Mojang) de `" + context.args[0] + "`:** `" + uuid.toString() + "`")
		} else {
			context.explain()
		}
	}
}