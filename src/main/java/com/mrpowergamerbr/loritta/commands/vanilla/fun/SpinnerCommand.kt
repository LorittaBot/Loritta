package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import kotlin.concurrent.thread

class SpinnerCommand : CommandBase() {
    override fun getLabel(): String {
        return "spinner"
    }

    override fun getAliases(): List<String> {
        return listOf("fidget");
    }

    override fun getDescription(): String {
        return "Gira um fidget spinner! Quanto tempo será que ele irá ficar rodando?"
    }

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

    override fun run(context: CommandContext) {
		var time = Loritta.random.nextInt(5, 61); // Tempo que o Fidget Spinner irá ficar rodando

		var random = listOf("<:spinner1:327243530244325376>", "<:spinner2:327245670052397066>", "<:spinner3:327246151591919627>") // Pegar um spinner aleatório
		var spinner = random[Loritta.random.nextInt(random.size)]

		var msg = context.sendMessage(context.getAsMention(true) + "$spinner Girando o fidget spinner...")
		thread {
			Thread.sleep((time * 1000).toLong());
			msg.delete().complete()
			context.sendMessage(context.getAsMention(true) + "$spinner Seu spinner girou por **$time** segundos!")
		}
    }
}