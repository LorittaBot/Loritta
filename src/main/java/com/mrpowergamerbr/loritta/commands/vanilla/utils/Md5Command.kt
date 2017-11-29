package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import org.apache.commons.codec.digest.DigestUtils


class Md5Command : CommandBase("md5") {
	override fun getUsage(): String {
		return "mensagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.MD5_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("Loritta é muito fofa!");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val mensagem = context.strippedArgs.joinToString(" ");

			val encrypted = DigestUtils.md5Hex(mensagem)

			context.sendMessage(context.getAsMention(true) + context.locale.MD5_RESULT.msgFormat(mensagem, encrypted))
		} else {
			this.explain(context)
		}
	}
}