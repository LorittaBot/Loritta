package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class SpigotMcCommand : AbstractCommand("spigotmc", category = CommandCategory.MINECRAFT) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("SPIGOTMC_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "query"
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("EssentialsX", "FastAsyncWorldEdit", "ProtocolSupport", "ProtocolSupportStuff")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")

			val embed = EmbedBuilder()
			embed.setTitle("<:spigotmc:375314413357629440> Spigot")
			embed.setColor(Color(227, 156, 17))

			val response = HttpRequest.get("https://api.spiget.org/v2/search/resources/${URLEncoder.encode(query, "UTF-8")}")
					.userAgent("LorittaBot")
					.body()

			val json = jsonParser.parse(response)

			if (json.isJsonObject) {
				// Erro!
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale.get("SPIGOTMC_COULDNT_FIND", query))
				return
			} else {
				val array = json.array

				if (array.size() == 1) {
					// Se for apenas um...
					val resourceId = array[0]["id"].string

					context.sendMessage(context.getAsMention(true), createResourceEmbed(context, resourceId, locale).build())
				} else {
					var format = ""
					for (i in 0..Math.min(5, array.size()) - 1) {
						var item = json[i]
						format += "${Constants.INDEXES[i]} **[${item["name"].string}](https://www.spigotmc.org/${item["id"].string})**\n"

						context.metadata.put(i.toString(), item["id"].string)
					}
					embed.setDescription(format)
					embed.setTitle("<:spigotmc:375314413357629440> ${context.legacyLocale["YOUTUBE_RESULTS_FOR", query]}")
					val mensagem = context.sendMessage(context.getAsMention(true), embed.build())

					mensagem.onReactionAddByAuthor(context) {
						val resourceId: String
						when {
							it.reactionEmote.name == "1⃣" -> resourceId = context.metadata.get("0") as String
							it.reactionEmote.name == "2⃣" -> resourceId = context.metadata.get("1") as String
							it.reactionEmote.name == "3⃣" -> resourceId = context.metadata.get("2") as String
							it.reactionEmote.name == "4⃣" -> resourceId = context.metadata.get("3") as String
							else -> resourceId = context.metadata.get("4") as String
						}

						// Criar novo embed!
						mensagem.editMessage(createResourceEmbed(context, resourceId, context.legacyLocale).build()).queue()

						// Remover todos os reactions
						mensagem.clearReactions().queue()
					}

					// Adicionar os reactions
					for (i in 0..Math.min(5, array.size()) - 1) {
						mensagem.addReaction(Constants.INDEXES[i]).queue()
					}
				}
			}
		} else {
			this.explain(context)
		}
	}

	fun createResourceEmbed(context: CommandContext, resourceId: String, locale: LegacyBaseLocale) : EmbedBuilder {
		val embed = EmbedBuilder()
		embed.setTitle("<:spigotmc:375314413357629440> Spigot")
		embed.setColor(Color(227, 156, 17))

		val resource = getResourceInfo(resourceId)
		val author = jsonParser.parse(HttpRequest.get("https://api.spiget.org/v2/resources/$resourceId/author").body())

		embed.setAuthor(author["name"].string, null, "https://www.spigotmc.org/${author["icon"]["url"].string}")
		embed.setTitle("<:spigotmc:375314413357629440> ${resource.name}", "https://www.spigotmc.org/resources/$resourceId/")
		embed.setDescription(resource.tag.replace("*", "\\*").replace("_", "\\_").replace("~", "\\~"))
		embed.setThumbnail("https://www.spigotmc.org/${resource.icon}")
		if (resource.contributors.isNotEmpty()) embed.addField(context.legacyLocale.get("SPIGOTMC_CONTRIBUTORS"), resource.contributors, true)
		embed.addField(context.legacyLocale.get("SPIGOTMC_DOWNLOADS"), resource.downloads.toString(), true)
		if (resource.testedVersions.isNotEmpty()) embed.addField(context.legacyLocale.get("SPIGOTMC_TESTED_VERSIONS"), resource.testedVersions.joinToString(separator = ", "), true)

		val releaseEpoch = resource.releaseDate.toLong()
		val releaseInstant = Instant.ofEpochSecond(releaseEpoch)
		ZonedDateTime.ofInstant(releaseInstant, ZoneOffset.UTC)

		val updateEpoch = resource.updateDate.toLong()
		val updateInstant = Instant.ofEpochSecond(updateEpoch)
		ZonedDateTime.ofInstant(updateInstant, ZoneOffset.UTC)

		embed.addField(context.legacyLocale.get("SPIGOTMC_RELEASED"), releaseInstant.atOffset(ZoneOffset.UTC).humanize(locale), true)
		embed.addField(context.legacyLocale.get("SPIGOTMC_UPDATED"), updateInstant.atOffset(ZoneOffset.UTC).humanize(locale), true)

		embed.addField(context.legacyLocale.get("SPIGOTMC_DOWNLOAD"), "https://www.spigotmc.org/${resource.downloadLink}", true)

		return embed
	}

	fun getResourceInfo(id: String): SpigotResource {
		val response = HttpRequest.get("https://api.spiget.org/v2/resources/$id")
				.userAgent("LorittaBot")
				.body()

		val json = jsonParser.parse(response).obj

		return SpigotResource(
				json["name"].string,
				json["tag"].string,
				json["icon"]["url"].string,
				if (json.has("contributors")) json["contributors"].string else "",
				Loritta.GSON.fromJson<List<String>>(json["testedVersions"]),
				json["downloads"].int,
				json["file"]["url"].string,
				json["releaseDate"].int,
				json["updateDate"].int,
				json["rating"]["average"].double)
	}

	data class SpigotResource(
			val name: String,
			val tag: String,
			val icon: String,
			val contributors: String,
			val testedVersions: List<String>,
			val downloads: Int,
			val downloadLink: String,
			val releaseDate: Int,
			val updateDate: Int,
			val avg: Double
	)
}