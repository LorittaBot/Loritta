package net.perfectdreams.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import java.awt.Color
import java.net.URLEncoder

class TempoCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("weather", "tempo", "previsão", "previsao", "clima"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.weather"
	}

	override fun command() = create {
		usage {
			argument(ArgumentType.TEXT) {}
		}

		localizedDescription("$LOCALE_PREFIX.description")

		examples {
			+ "São Paulo"
		}

		executesDiscord {
			val context = this

			if (context.args.isNotEmpty()) {
				val cidade = context.args.joinToString(separator = " ")

				val cidadeResponse = HttpRequest.get("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(cidade, "UTF-8") + "&units=metric&lang=pt&APPID=" + com.mrpowergamerbr.loritta.utils.loritta.config.openWeatherMap.apiKey).body()
				val cidadeJsonResponse = JsonParser.parseString(cidadeResponse).asJsonObject

				if (cidadeJsonResponse.get("cod").asString == "200") {
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

					embed.setTitle(locale["$LOCALE_PREFIX.forecastFor", realCityName, countryShort])
					embed.setDescription(icon + description)
					embed.setColor(Color(0, 210, 255))
					embed.addField("🌡 ${context.locale["$LOCALE_PREFIX.temperature"]}", "**${context.locale["$LOCALE_PREFIX.current"]}: **$now ºC\n**${context.locale["$LOCALE_PREFIX.max"]}: **$max ºC\n**${context.locale["$LOCALE_PREFIX.min"]}: **$min ºC", true)
					embed.addField("💦 ${context.locale["$LOCALE_PREFIX.humidity"]}", "$humidity%", true)
					embed.addField("🌬 ${context.locale["$LOCALE_PREFIX.windSpeed"]}", "$windSpeed km/h", true)
					embed.addField("🏋 ${context.locale["$LOCALE_PREFIX.airPressure"]}", "$pressure kPA", true)

					context.sendMessage(embed.build())
				} else {
					context.reply(
							LorittaReply(
									locale["$LOCALE_PREFIX.couldntFind", context.args[0]],
									Constants.ERROR
							)
					)
				}
			} else {
				explain()
			}
		}
	}
}