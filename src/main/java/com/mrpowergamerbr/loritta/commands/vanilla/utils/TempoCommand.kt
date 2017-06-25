package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.io.StringReader
import java.net.URLEncoder


class TempoCommand : CommandBase() {
	override fun getLabel(): String {
		return "tempo"
	}

	override fun getUsage(): String {
		return "cidade"
	}

	override fun getAliases(): List<String> {
		return listOf("previsão", "previsao")
	}

	override fun getDescription(): String {
		return "Verifique a temperatura de uma cidade!"
	}

	override fun getExample(): List<String> {
		return listOf("São Paulo");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var cidade = context.args.joinToString(separator = " ");

			var cidadeResponse = HttpRequest.get("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(cidade, "UTF-8") + "&units=metric&lang=pt&APPID=" + Loritta.config.openWeatherMapKey).body()
			val reader = StringReader(cidadeResponse)
			val jsonReader = JsonReader(reader)
			val cidadeJsonResponse = JsonParser().parse(jsonReader).asJsonObject // Base

			if (cidadeJsonResponse.get("cod").asString == "200") { // Nós encontramos alguma coisa?
				var status = cidadeJsonResponse.get("list").asJsonArray.get(0).asJsonObject;

				var now = status.getAsJsonObject("main").get("temp").asDouble;
				var max = status.getAsJsonObject("main").get("temp_max").asDouble;
				var min = status.getAsJsonObject("main").get("temp_min").asDouble;
				var pressure = status.getAsJsonObject("main").get("pressure").asDouble;
				var humidity = status.getAsJsonObject("main").get("humidity").asDouble;
				var windSpeed = status.getAsJsonObject("wind").get("speed").asDouble;
				var embed = EmbedBuilder();

				var description = status.get("weather").asJsonArray.get(0).asJsonObject.get("description").asString
				var abbr = status.get("weather").asJsonArray.get(0).asJsonObject.get("icon").asString
				/* if (abbr == "sn") {
					description = "🌨 Nevando";
				}
				if (abbr == "sl" || abbr == "h") {
					description = "🌨 Granizo";
				}
				if (abbr == "t") {
					description = "⛈ Tempestade";
				}
				if (abbr == "hr") {
					description = "🌧 Chuva Forte";
				}
				if (abbr == "hl") {
					description = "🌧 Chuva Fraca";
				}
				if (abbr == "s") {
					description = "🚿 Garoando";
				}
				if (abbr == "hc") {
					description = "☁ Nuvens pesadas";
				}
				if (abbr.startsWith("02"))
					description = "⛅ Sol com nuvens";
				}
				if (abbr.startsWith("01")) {
					description = "☀ Ensolarado";
				} */
				embed.setTitle("Previsão do tempo para $cidade")
				embed.setDescription(description);
				embed.addField("🌡 Temperatura", "**Atual: **$now ºC\n**Máxima: **$max ºC\n**Mínima: **$min ºC", true);
				embed.addField("💦 Umidade", "$humidity%", true);
				embed.addField("🌬 Velocidade do Vento", "$windSpeed km/h", true);
				embed.addField("🏋 Pressão do Ar", "$pressure kPA", true);

				context.sendMessage(embed.build());
			} else {
				// Cidade inexistente!
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Não encontrei uma cidade chamada `$cidade`!")
			}
		} else {
			this.explain(context);
		}
	}
}