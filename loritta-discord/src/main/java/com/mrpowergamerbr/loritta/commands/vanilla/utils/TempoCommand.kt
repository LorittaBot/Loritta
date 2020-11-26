package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.Color
import java.net.URLEncoder

class TempoCommand : AbstractCommand("weather", listOf("tempo", "previsão", "previsao"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "cidade"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.utils.weather.description"]
	}

	override fun getExamples(): List<String> {
		return listOf("São Paulo")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val cidade = context.args.joinToString(separator = " ")

			val cidadeResponse = HttpRequest.get("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(cidade, "UTF-8") + "&units=metric&lang=pt&APPID=" + loritta.config.openWeatherMap.apiKey).body()
			val cidadeJsonResponse = JsonParser.parseString(cidadeResponse).asJsonObject // Base

			if (cidadeJsonResponse.get("cod").asString == "200") { // Nós encontramos alguma coisa?
				val status = cidadeJsonResponse.get("list").asJsonArray.get(0).asJsonObject

				val now = status.getAsJsonObject("main").get("temp").asDouble
				val max = status.getAsJsonObject("main").get("temp_max").asDouble
				val min = status.getAsJsonObject("main").get("temp_min").asDouble
				val pressure = status.getAsJsonObject("main").get("pressure").asDouble
				val humidity = status.getAsJsonObject("main").get("humidity").asDouble
				val windSpeed = status.getAsJsonObject("wind").get("speed").asDouble
				val realCityName = cidadeJsonResponse.get("city").asJsonObject.get("name").asString
				val countryShort = if (cidadeJsonResponse["city"].obj.has("country")) cidadeJsonResponse.get("city").asJsonObject.get("country").asString else realCityName
				var icon = ""

				val embed = EmbedBuilder()

				val description = status.get("weather").asJsonArray.get(0).asJsonObject.get("description").asString
				val abbr = status.get("weather").asJsonArray.get(0).asJsonObject.get("icon").asString

				if (abbr.startsWith("01")) {
					icon = "☀ "
				}
				if (abbr.startsWith("02")) {
					icon = "⛅ "
				}
				if (abbr.startsWith("03")) {
					icon = "☁ "
				}
				if (abbr.startsWith("04")) {
					icon = "☁ "
				}
				if (abbr.startsWith("09")) {
					icon = "\uD83D\uDEBF "
				}
				if (abbr.startsWith("10")) {
					icon = "\uD83C\uDF27 "
				}
				if (abbr.startsWith("11")) {
					icon = "⛈ "
				}
				if (abbr.startsWith("13")) {
					icon = "\uD83C\uDF28 "
				}
				if (abbr.startsWith("50")) {
					icon = "\uD83C\uDF2B "
				}

				embed.setTitle(locale.toNewLocale()["commands.utils.weather.forecastFor", realCityName, countryShort])
				embed.setDescription(icon + description)
				embed.setColor(Color(0, 210, 255))
				embed.addField("🌡 ${context.legacyLocale.toNewLocale()["commands.utils.weather.temperature"]}", "**${context.locale["commands.utils.weather.current"]}: **$now ºC\n**${context.legacyLocale["TEMPO_MAX"]}: **$max ºC\n**${context.legacyLocale["TEMPO_MIN"]}: **$min ºC", true)
				embed.addField("💦 ${context.legacyLocale.toNewLocale()["commands.utils.weather.humidity"]}", "$humidity%", true)
				embed.addField("🌬 ${context.legacyLocale.toNewLocale()["commands.utils.weather.windSpeed"]}", "$windSpeed km/h", true)
				embed.addField("🏋 ${context.legacyLocale.toNewLocale()["commands.utils.weather.airPressure"]}", "$pressure kPA", true)

				context.sendMessage(embed.build())
			} else {
				// Cidade inexistente!
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.utils.weather.couldntFind", cidade])
			}
		} else {
			this.explain(context)
		}
	}
}