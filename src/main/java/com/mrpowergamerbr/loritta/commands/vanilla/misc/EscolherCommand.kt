package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat

class EscolherCommand : CommandBase() {
    override fun getLabel(): String {
        return "escolher"
    }

    override fun getAliases(): List<String> {
        return listOf("choose");
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale.ESCOLHER_DESCRIPTION.msgFormat()
    }

	override fun getExample(): List<String> {
		return listOf("Sonic, Tails, Knuckles", "Asriel Dreemurr, Chara Dreemurr", "Shantae, Risky Boots");
	}
	override fun getCategory(): CommandCategory {
		return CommandCategory.MISC;
	}

    override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var joined = context.args.joinToString(separator = " "); // Vamos juntar tudo em uma string
			var split = joined.split(","); // E vamos separar!

			// Hora de escolher algo aleatório!
			var chosen = split[Loritta.random.nextInt(split.size)];
			context.sendMessage(context.getAsMention(true) + "${context.locale.ESCOLHER_RESULT.msgFormat(chosen)}");
		} else {
			context.explain()
		}
    }
}