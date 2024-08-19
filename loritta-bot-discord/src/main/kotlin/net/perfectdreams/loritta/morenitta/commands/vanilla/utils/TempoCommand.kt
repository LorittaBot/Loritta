package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.awt.Color
import java.net.URLEncoder

class TempoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "weather", listOf("tempo", "previsão", "previsao", "clima", "temperatura"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.weather.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.weather.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val cidade = context.args.joinToString(separator = " ")

			val cidadeResponse = HttpRequest.get("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(cidade, "UTF-8") + "&units=metric&lang=pt&APPID=" + loritta.config.loritta.openWeatherMap.key).body()
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

				embed.setTitle(locale["commands.command.weather.forecastFor", realCityName, countryShort])
				embed.setDescription(icon + description)
				embed.setColor(Color(0, 210, 255))
				embed.addField("🌡 ${context.locale["commands.command.weather.temperature"]}", "**${context.locale["commands.command.weather.current"]}: **$now ºC\n**${context.locale["commands.command.weather.max"]}: **$max ºC\n**${context.locale["commands.command.weather.min"]}: **$min ºC", true)
				embed.addField("💦 ${context.locale["commands.command.weather.humidity"]}", "$humidity%", true)
				embed.addField("🌬 ${context.locale["commands.command.weather.windSpeed"]}", "$windSpeed km/h", true)
				embed.addField("🏋 ${context.locale["commands.command.weather.airPressure"]}", "$pressure kPA", true)

				context.sendMessageEmbeds(embed.build())
			} else {
				// Cidade inexistente!
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.command.weather.couldntFind", cidade])
			}
		} else {
			this.explain(context)
		}
	}
}