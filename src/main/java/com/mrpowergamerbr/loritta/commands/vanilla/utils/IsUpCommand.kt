package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.net.UnknownHostException
import java.util.*

class IsUpCommand : CommandBase() {
	override fun getLabel(): String {
		return "isup"
	}

	override fun getDescription(): String {
		return "Verifica se um website está online!"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("http://loritta.website/")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var url = context.args[0];

			if (!url.startsWith("http", true)) {
				url = "http://" + url;
			}
			url = url.toLowerCase();

			try {
				var response = HttpRequest.get(url).code();

				if (response == 200) {
					context.sendMessage(context.getAsMention(true) + "É só você, para mim `$url` está online! (**Código:**  $response)");
				} else {
					context.sendMessage(context.getAsMention(true) + "Não é só você! Para mim `$url` também está offline! (**Código:** $response)");
				}
			} catch (e: Exception) {
				var reason = e.message;
				if (e.cause is UnknownHostException) {
					reason = "`$url não existe!`";
				}
				context.sendMessage(context.getAsMention(true) + "Não é só você! Para mim `$url` também está offline! (**Erro:**: $reason)");
			}
		} else {
			this.explain(context);
		}
	}
}