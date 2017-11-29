package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.util.*

class GameJoltCommand : CommandBase("gamejolt") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["GAMEJOLT_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("undertale yellow")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val embed = EmbedBuilder()
			val query = context.args.joinToString(" ")
			val response = HttpRequest.get("https://gamejolt.com/site-api/web/search?q=$query")
					.body()

			val json = Loritta.JSON_PARSER.parse(response).obj
			val games = json["payload"]["games"].array

			var format = "";
			if (games.size() == 1) {
				context.sendMessage(createResourceEmbed(context, games[0].obj).build())
				return
			}
			for (i in 0 until Math.min(5, games.size())) {
				val game = games[i]
				val id = game["id"]
				val title = game["title"].string
				val path = game["slug"].string
				val url = "https://gamejolt.com/games/$path/$id"

				format += "${Constants.INDEXES[i]} **[${title}]($url)**\n"

				context.metadata.put(i.toString(), game)
			}
			embed.setColor(Color(47, 127, 111))
			embed.setDescription(format)
			embed.setTitle("<:gamejolt:362325764181590017> ${context.locale["YOUTUBE_RESULTS_FOR", query]}")
			var mensagem = context.sendMessage(context.getAsMention(true), embed.build())
			// Adicionar os reactions
			for (i in 0 until Math.min(5, games.size())) {
				mensagem.addReaction(Constants.INDEXES[i]).complete();
			}
		} else {
			context.explain()
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user == context.userHandle) { // Somente quem executou o comando pode interagir!
			var game: JsonObject;
			if (e.reactionEmote.name == "1⃣") {
				game = context.metadata["0"] as JsonObject
			} else if (e.reactionEmote.name == "2⃣") {
				game = context.metadata["1"] as JsonObject
			} else if (e.reactionEmote.name == "3⃣") {
				game = context.metadata["2"] as JsonObject
			} else if (e.reactionEmote.name == "4⃣") {
				game = context.metadata["3"] as JsonObject
			} else {
				game = context.metadata["4"] as JsonObject
			}

			// Criar novo embed!
			msg.editMessage(createResourceEmbed(context, game).build()).complete();

			// Remover todos os reactions
			msg.clearReactions().complete();
		}
	}

	fun createResourceEmbed(context: CommandContext, game: JsonObject) : EmbedBuilder {
		val embed = EmbedBuilder()
		embed.setColor(Color(47, 127, 111))

		val id = game["id"]
		val developerName = game["developer"]["name"].string
		val developerDisplayName = game["developer"]["display_name"].string
		val developerAvatar = game["developer"]["img_avatar"].string
		val developerUsername = game["developer"]["username"].string
		val title = game["title"].string
		val path = game["slug"].string
		val url = "https://gamejolt.com/games/$path/$id"
		val imgThumbnail = game["img_thumbnail"].string
		val isWindowsCompat = game["compatibility"].obj.has("os_windows")
		val isLinuxCompat = game["compatibility"].obj.has("os_linux")
		val isMacCompat = game["compatibility"].obj.has("os_mac")

		val descriptionJson = HttpRequest.get("https://gamejolt.com/site-api/web/discover/games/overview/$id")
				.body()

		val gameDescription = Loritta.JSON_PARSER.parse(descriptionJson)["payload"]["metaDescription"].string
		embed.setAuthor(developerDisplayName, "https://gamejolt.com/@$developerUsername", developerAvatar)
		embed.setTitle("<:gamejolt:362325764181590017> $title", url)
		embed.setDescription(gameDescription.substringIfNeeded())
		embed.setImage(imgThumbnail)

		return embed
	}
}