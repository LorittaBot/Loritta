package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import org.apache.commons.codec.digest.DigestUtils


class Md5Command : CommandBase() {
	override fun getLabel(): String {
		return "md5"
	}

	override fun getUsage(): String {
		return "mensagem"
	}

	override fun getDescription(): String {
		return "Encripta uma mensagem usando MD5"
	}

	override fun getExample(): List<String> {
		return listOf("Loritta é muito fofa!");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val mensagem = context.args.joinToString(" ");

			val encrypted = DigestUtils.md5Hex(mensagem)

			context.sendMessage(context.getAsMention(true) + "`$mensagem` em MD5: `$encrypted`")
		} else {
			this.explain(context)
		}
	}
}