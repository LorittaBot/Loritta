package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.correios.CorreiosResponse
import com.mrpowergamerbr.loritta.utils.correios.EncomendaResponse
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.nodes.Document
import java.util.*

class PackageInfoCommand : CommandBase() {
	override fun getLabel(): String {
		return "correios"
	}

	override fun getDescription(): String {
		return "Mostra o status de uma encomenda dos correios, funciona com os Correios (Brasil) e a CTT (Portugal)"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("correios")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			val packageId = context.args[0]
			// DU892822537BR
			val doc: Document? = null
			try {
				if (packageId.endsWith("PT")) { // Portugal
					val packageHtml = HttpRequest.get("http://pesquisarencomendas.com/ws/?ref=" + packageId).body()

					val encRes = Loritta.gson.fromJson(packageHtml, EncomendaResponse::class.java)

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

					var correios = Loritta.gson.fromJson(response.body(), CorreiosResponse::class.java);

					var objeto = correios.objeto[0];

					if (objeto.categoria == "ERRO: Objeto não encontrado na base de dados dos Correios.") {
						context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "**Não consegui encontrar o objeto `$packageId$` no banco de dados do Correios!**")
						return;
					}

					var embed = EmbedBuilder();
					embed.setTitle("<:correios:329622124140429313> " + objeto.categoria) // A categoria é o tipo da encomenda

					var str = "";
					for (evento in objeto.evento) {
						str += "+ " + evento.data + " - " + evento.hora;
						str += " - " + evento.unidade.local + " (" + evento.unidade.cidade + ")\n- " + evento.descricao + "\n\n";
					}

					embed.setDescription("```diff\n$str```");

					context.sendMessage(embed.build());
				}
			} catch (e: Exception) {
				context.sendMessage(context.getAsMention(true) + "**Código de rastreio inválido!**")
			}

		} else {
			context.explain()
		}
	}
}

fun main(args: Array<String>) {
	// Eu encontrei a API REST do Correios usando engenharia reversa(tm) no SRO Mobile
	var response = HttpRequest.post("http://webservice.correios.com.br/service/rest/rastro/rastroMobile")
			.contentType("application/xml")
			.userAgent("Dalvik/2.1.0 (Linux; U; Android 7.1.2; MotoG3-TE Build/NJH47B)")
			.acceptJson() // Sim, o request é em XML mas a response é em JSON
			// E sim, não importa qual é o usuário/senha/token, ele sempre retorna algo válido
			.send("<rastroObjeto><usuario>LorittaBot</usuario><senha>LorittaSuperFofa</senha><tipo>L</tipo><resultado>T</resultado><objetos>A</objetos><lingua>101</lingua><token>Loritta-Discord</token></rastroObjeto>")

	var body = response.body();

	println(response.code());
	println(body);

	var correios = Loritta.gson.fromJson(body, CorreiosResponse::class.java);

	println(correios.objeto[0].nome)

	for (evento in correios.objeto[0].evento) {
		println(evento.descricao);
	}
}