package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import net.perfectdreams.loritta.morenitta.Loritta
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.commands.vanilla.utils.PackageInfoCommand.PackageSource.CORREIOS
import net.perfectdreams.loritta.morenitta.commands.vanilla.utils.PackageInfoCommand.PackageSource.CTT
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.correios.CorreiosResponse
import net.perfectdreams.loritta.morenitta.utils.correios.EncomendaResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.awt.Color
import java.util.*

class PackageInfoCommand : AbstractCommand("packageinfo", listOf("correios", "ctt"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.packageinfo.description")

	override fun getExamples(): List<String> {
		return Arrays.asList("correios")
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size == 1) {
			OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "package track")
			val packageId = context.args[0]

			try {
				var pair = getCorreiosInfo(packageId)
				var packageSource = CORREIOS
				if (pair == null) {
					pair = getCttInfo(packageId)
					packageSource = CTT
				}

				if (pair == null) {
					context.reply(
							LorittaReply(
									message = locale["commands.command.packageinfo.couldntFind", packageId],
									prefix = Constants.ERROR
							)
					)
					return
				}

				var embed = EmbedBuilder()

				var color = when (packageSource) {
					CORREIOS -> Color(253, 220, 1)
					CTT -> Color(223, 0, 36)
				}

				embed.setColor(color)

				var emoji = when (packageSource) {
					CORREIOS -> "<:correios:375314171644084234>"
					CTT -> "<:ctt:385499134263689220>"
				}

				embed.setTitle("${emoji} " + pair.first) // A categoria é o tipo da encomenda

				embed.setDescription("```diff\n${pair.second}```")

				context.sendMessage(context.getAsMention(true), embed.build())
			} catch (e: Exception) {
				context.reply(
						LorittaReply(
								message = locale["commands.command.packageinfo.invalid", packageId],
								prefix = Constants.ERROR
						)
				)
			}
		} else {
			context.explain()
		}
	}

	fun getCorreiosInfo(packageId: String): Pair<String, String>? {
		// Eu encontrei a API REST do Correios usando engenharia reversa(tm) no SRO Mobile
		var response = HttpRequest.post("http://webservice.correios.com.br/service/rest/rastro/rastroMobile")
				.contentType("application/xml")
				.userAgent("Dalvik/2.1.0 (Linux; U; Android 7.1.2; MotoG3-TE Build/NJH47B)")
				.acceptJson() // Sim, o request é em XML mas a response é em JSON
				// E sim, não importa qual é o usuário/senha/token, ele sempre retorna algo válido
				.send("<rastroObjeto><usuario>LorittaBot</usuario><senha>LorittaSuperFofa</senha><tipo>L</tipo><resultado>T</resultado><objetos>$packageId</objetos><lingua>101</lingua><token>Loritta-Discord</token></rastroObjeto>")

		var correios = Loritta.GSON.fromJson(response.body(), CorreiosResponse::class.java)

		var objeto = correios.objeto[0]

		if (objeto.categoria == "ERRO: Objeto não encontrado na base de dados dos Correios.") {
			return null
		}

		var str = ""
		for (evento in objeto.evento) {
			str += "+ " + evento.data + " - " + evento.hora
			str += " - " + evento.unidade.local + " (" + evento.unidade.cidade + ")\n- " + evento.descricao + "\n\n"
		}

		return Pair(objeto.categoria, str)
	}

	fun getCttInfo(packageId: String): Pair<String, String>? {
		val packageHtml = HttpRequest.get("http://pesquisarencomendas.com/ws/?ref=" + packageId).body()

		val encRes = Loritta.GSON.fromJson(packageHtml, EncomendaResponse::class.java)

		if (encRes.error != null) {
			return null
		}

		var str = ""
		for (update in encRes.locations) {
			str += "+ " + update.date.replace("-", "/") + " - " + update.time + " - ${update.location}\n"
			str += "- " + update.state + "\n\n"
		}

		return Pair("Encomenda", str)
	}

	enum class PackageSource {
		CORREIOS, CTT
	}
}