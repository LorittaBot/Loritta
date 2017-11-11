package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import org.apache.commons.codec.Charsets
import java.util.*

class OfflineUUIDCommand : CommandBase("offlineuuid") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.OFFLINEUUID_DESCRIPTION.msgFormat()
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

	override fun getAliases(): List<String> {
		return listOf("mcofflineuuid")
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + context.args[0]).toByteArray(Charsets.UTF_8))

			context.sendMessage(context.getAsMention(true) + context.locale.OFFLINEUUID_RESULT.msgFormat(context.args[0], uuid.toString()))
		} else {
			context.explain()
		}
	}
}