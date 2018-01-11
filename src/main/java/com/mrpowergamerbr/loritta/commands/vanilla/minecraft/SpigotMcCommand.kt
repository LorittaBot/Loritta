package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class SpigotMcCommand : AbstractCommand("spigotmc", category = CommandCategory.MINECRAFT) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("SPIGOTMC_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "query"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("ProtocolSupportStuff")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size > 0) {
			val query = context.args.joinToString(" ")

			val embed = EmbedBuilder()
			embed.setTitle("<:spigotmc:375314413357629440> Spigot")
			embed.setColor(Color(227, 156, 17))

			val response = HttpRequest.get("https://api.spiget.org/v2/search/resources/${URLEncoder.encode(query, "UTF-8")}")
					.userAgent("LorittaBot")
					.body()

			val json = JSON_PARSER.parse(response)

			if (json.isJsonObject) {
				// Erro!
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("SPIGOTMC_COULDNT_FIND", query))
				return
			} else {
				val array = json.array

				if (array.size() == 1) {
					// Se for apenas um...
					val resourceId = array[0]["id"].string

					context.sendMessage(context.getAsMention(true), createResourceEmbed(context, resourceId).build())
				} else {
					var format = "";
					for (i in 0..Math.min(5, array.size()) - 1) {
						var item = json[i]
						format += "${Constants.INDEXES[i]} **[${item["name"].string}](https://www.spigotmc.org/${item["id"].string})**\n";

						context.metadata.put(i.toString(), item["id"].string);
					}
					embed.setDescription(format);
					embed.setTitle("<:spigotmc:375314413357629440> ${context.locale["YOUTUBE_RESULTS_FOR", query]}");
					var mensagem = context.sendMessage(context.getAsMention(true), embed.build());
					// Adicionar os reactions
					for (i in 0..Math.min(5, array.size()) - 1) {
						mensagem.addReaction(Constants.INDEXES[i]).complete();
					}
				}
			}
		} else {
			this.explain(context);
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user == context.userHandle) { // Somente quem executou o comando pode interagir!
			var resourceId: String;
			if (e.reactionEmote.name == "1⃣") {
				resourceId = context.metadata.get("0") as String
			} else if (e.reactionEmote.name == "2⃣") {
				resourceId = context.metadata.get("1") as String
			} else if (e.reactionEmote.name == "3⃣") {
				resourceId = context.metadata.get("2") as String
			} else if (e.reactionEmote.name == "4⃣") {
				resourceId = context.metadata.get("3") as String
			} else {
				resourceId = context.metadata.get("4") as String
			}

			// Criar novo embed!
			msg.editMessage(createResourceEmbed(context, resourceId).build()).complete();

			// Remover todos os reactions
			msg.clearReactions().complete();
		}
	}

	fun createResourceEmbed(context: CommandContext, resourceId: String) : EmbedBuilder {
		val embed = EmbedBuilder()
		embed.setTitle("<:spigotmc:375314413357629440> Spigot")
		embed.setColor(Color(227, 156, 17))

		val resource = getResourceInfo(resourceId)
		val author = JSON_PARSER.parse(HttpRequest.get("https://api.spiget.org/v2/resources/$resourceId/author").body())

		embed.setAuthor(author["name"].string, null, "https://www.spigotmc.org/${author["icon"]["url"].string}")
		embed.setTitle("<:spigotmc:375314413357629440> ${resource.name}", "https://www.spigotmc.org/resources/$resourceId/")
		embed.setDescription(resource.tag.replace("*", "\\*").replace("_", "\\_").replace("~", "\\~"))
		embed.setThumbnail("https://www.spigotmc.org/${resource.icon}")
		if (resource.contributors.isNotEmpty()) embed.addField(context.locale.get("SPIGOTMC_CONTRIBUTORS"), resource.contributors, true)
		embed.addField(context.locale.get("SPIGOTMC_DOWNLOADS"), resource.downloads.toString(), true)
		if (resource.testedVersions.isNotEmpty()) embed.addField(context.locale.get("SPIGOTMC_TESTED_VERSIONS"), resource.testedVersions.joinToString(separator = ", "), true)

		val releaseEpoch = resource.releaseDate.toLong()
		val releaseInstant = Instant.ofEpochSecond(releaseEpoch)
		ZonedDateTime.ofInstant(releaseInstant, ZoneOffset.UTC)

		val updateEpoch = resource.updateDate.toLong()
		val updateInstant = Instant.ofEpochSecond(updateEpoch)
		ZonedDateTime.ofInstant(updateInstant, ZoneOffset.UTC)

		embed.addField(context.locale.get("SPIGOTMC_RELEASED"), releaseInstant.atOffset(ZoneOffset.UTC).humanize(), true)
		embed.addField(context.locale.get("SPIGOTMC_UPDATED"), updateInstant.atOffset(ZoneOffset.UTC).humanize(), true)

		embed.addField(context.locale.get("SPIGOTMC_DOWNLOAD"), "https://www.spigotmc.org/${resource.downloadLink}", true)

		return embed
	}

	fun getResourceInfo(id: String): SpigotResource {
		val response = HttpRequest.get("https://api.spiget.org/v2/resources/$id")
				.userAgent("LorittaBot")
				.body()

		val json = JSON_PARSER.parse(response).obj

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