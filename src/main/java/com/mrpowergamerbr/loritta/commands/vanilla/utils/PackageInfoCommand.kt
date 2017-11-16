package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.correios.CorreiosResponse
import com.mrpowergamerbr.loritta.utils.correios.EncomendaResponse
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.nodes.Document
import java.util.*

class PackageInfoCommand : CommandBase("correios") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PACKAGEINFO_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("correios")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun getAliases(): List<String> {
		return listOf("packageinfo", "ctt")
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			val packageId = context.args[0]
			// DU892822537BR
			val doc: Document? = null
			try {
				if (packageId.endsWith("PT")) { // Portugal
					val packageHtml = HttpRequest.get("http://pesquisarencomendas.com/ws/?ref=" + packageId).body()

					val encRes = Loritta.GSON.fromJson(packageHtml, EncomendaResponse::class.java)

					var base = ""

					for (update in encRes.locations) {
						base += String.format("%s %s - %s - %s\n", update.date.replace(":", "/") /* deixar mais bonito */, update.time, update.location, update.state)
					}

					context.sendMessage(context.getAsMention(true) + "**Status para pacote \"" + packageId + "\"**\n" +
							"```" + base + "```")
				} else {
					// Eu encontrei a API REST do Correios usando engenharia reversa(tm) no SRO Mobile
					var response = HttpRequest.post("http://webservice.correios.com.br/service/rest/rastro/rastroMobile")
							.contentType("application/xml")
							.userAgent("Dalvik/2.1.0 (Linux; U; Android 7.1.2; MotoG3-TE Build/NJH47B)")
							.acceptJson() // Sim, o request é em XML mas a response é em JSON
							// E sim, não importa qual é o usuário/senha/token, ele sempre retorna algo válido
							.send("<rastroObjeto><usuario>LorittaBot</usuario><senha>LorittaSuperFofa</senha><tipo>L</tipo><resultado>T</resultado><objetos>$packageId</objetos><lingua>101</lingua><token>Loritta-Discord</token></rastroObjeto>")

					var correios = Loritta.GSON.fromJson(response.body(), CorreiosResponse::class.java);

					var objeto = correios.objeto[0];

					if (objeto.categoria == "ERRO: Objeto não encontrado na base de dados dos Correios.") {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.PACKAGEINFO_COULDNT_FIND.msgFormat(packageId))
						return;
					}

					var embed = EmbedBuilder();
					embed.setTitle("<:correios:375314171644084234> " + objeto.categoria) // A categoria é o tipo da encomenda

					var str = "";
					for (evento in objeto.evento) {
						str += "+ " + evento.data + " - " + evento.hora;
						str += " - " + evento.unidade.local + " (" + evento.unidade.cidade + ")\n- " + evento.descricao + "\n\n";
					}

					embed.setDescription("```diff\n$str```");

					context.sendMessage(embed.build());
				}
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.PACKAGEINFO_INVALID.msgFormat(packageId))
			}

		} else {
			context.explain()
		}
	}
}