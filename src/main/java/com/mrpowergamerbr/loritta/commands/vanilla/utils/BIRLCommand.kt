package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class BIRLCommand : CommandBase() {
	override fun getLabel(): String {
		return "birl"
	}

	override fun getDescription(): String {
		return "Compila um código criado em BIRL (Bambam's \"It's show time\" Recursive Language)"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun getUsage(): String {
		return "stdin código (entre ```)";
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val joined = context.rawArgs.joinToString(" ").replace("\u200D", "");

			val split = joined.split("```")

			if (split.size > 1) {
				val code = split[1];

				println(code)

				val obj = JsonObject();
				obj["code"] = code;
				obj["stdin"] = split[0];
				val response = HttpRequest.post("https://birl.herokuapp.com/compile")
						.send(obj.toString())
						.body();

				val json = JsonParser().parse(response)

				val embed = EmbedBuilder()
				embed.setColor(Color(221, 45, 36));
				embed.setTitle("<:birl:331957235091636224> Bambam's \"It's show time\" Recursive Language")
				if (!json["error"].isJsonNull) {
					embed.setDescription(json["error"].string)
				} else {
					embed.addField("Resultado", "```${json["stdout"].string}```", false)
				}

				context.sendMessage(embed.build())
			} else {
				// Os \u200D são zero width joiners, usado para o Discord não parsear os code blocks
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "Códigos em BIRL devem estar entre blocos de código, por exemplo:```\n${context.config.commandPrefix}birl\n\u200D`\u200D`\u200D`HORA DO SHOW\n" +
						"    CE QUER VER ESSA PORRA? (\"Hello, World! Porra!\\n\");\n" +
						"    BORA CUMPADE 0;\n" +
						"BIRL`\u200D`\u200D`\n```Para mais informações: https://birl-language.github.io/")
			}
		} else {
			context.explain()
		}
	}
}