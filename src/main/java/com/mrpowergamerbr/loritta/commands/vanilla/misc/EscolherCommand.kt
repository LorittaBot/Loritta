package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext

class EscolherCommand : CommandBase() {
    override fun getLabel(): String {
        return "escolher"
    }

    override fun getAliases(): List<String> {
        return listOf("choose");
    }

    override fun getDescription(): String {
        return "Precisando de ajuda para escolher alguma coisa? Então deixe-me escolher para você!"
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
			context.sendMessage(context.getAsMention(true) + "Eu escolhi `" + chosen + "`!");
		} else {
			context.explain()
		}
    }
}