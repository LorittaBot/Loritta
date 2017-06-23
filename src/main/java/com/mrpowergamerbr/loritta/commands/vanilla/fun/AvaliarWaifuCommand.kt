package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.util.*

class AvaliarWaifuCommand : CommandBase() {
	override fun getLabel(): String {
		return "avaliarwaifu"
	}

	override fun getAliases(): List<String> {
		return listOf("ratemywaifu", "avaliarminhawaifu", "notawaifu");
	}

	override fun getDescription(): String {
		return "Receba uma nota para a sua Waifu!"
	}

	override fun getExample(): List<String> {
		return listOf("Loritta");
	}
	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var joined = context.args.joinToString(separator = " "); // Vamos juntar tudo em uma string
			if (context.message.mentionedUsers.isNotEmpty()) {
				joined = context.message.mentionedUsers[0].name;
			}
			var random = SplittableRandom(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + joined.hashCode().toLong()) // Usar um random sempre com a mesma seed
			var nota = random.nextInt(0, 11).toString();

			if (joined == "Loritta") {
				nota = "∞";
			}
			context.sendMessage(context.getAsMention(true) + "Eu dou uma nota **$nota/10** para `$joined`!");
		} else {
			this.explain(context);
		}
	}
}