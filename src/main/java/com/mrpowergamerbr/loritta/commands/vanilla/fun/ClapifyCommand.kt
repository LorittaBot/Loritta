package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext

class ClapifyCommand : CommandBase() {
    override fun getLabel(): String {
        return "clapify"
    }

    override fun getAliases(): List<String> {
        return listOf("baterpalmasinator");
    }

    override fun getDescription(): String {
        return "Quando👏você👏precisa👏chamar👏a👏atenção👏de👏alguém👏da👏maneira👏mais👏irritante👏possível!"
    }

	override fun getExample(): List<String> {
		return listOf("Não finja que você nunca viu alguém falar assim antes");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

    override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var joined = context.args.joinToString(separator = "👏"); // Vamos juntar tudo em uma string
			context.sendMessage(context.getAsMention(true) + joined);
		} else {
			this.explain(context);
		}
    }
}