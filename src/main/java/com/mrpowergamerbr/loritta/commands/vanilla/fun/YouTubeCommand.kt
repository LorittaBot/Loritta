package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.YouTubeUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.temmieyoutube.YouTubeItem
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.util.*

class YouTubeCommand : CommandBase() {
	val indexes = listOf("1⃣",
			"2⃣",
			"3⃣",
			"4⃣",
			"5⃣")
	override fun getLabel(): String {
		return "youtube"
	}

	override fun getAliases() : List<String> {
		return listOf("yt")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("YOUTUBE_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return Arrays.asList("shantae tassel town")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun onlyInMusicInstance(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 1) {
			var query = context.args.joinToString(" ");
			val items = YouTubeUtils.searchVideosOnYouTube(query)

			if (items.isNotEmpty()) {
				var format = "";
				var youtubeKey = Loritta.config.youtubeKey;
				for (i in 0..Math.min(5, items.size) - 1) {
					var item = items[i];
					var response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${item.id.videoId}&part=contentDetails&key=${youtubeKey}").body();
					var parser = JsonParser();
					var json = parser.parse(response).asJsonObject;
					var strDuration = json["items"].array[0]["contentDetails"]["duration"].string
					var duration = java.time.Duration.parse(strDuration)
					var inSeconds = duration.get(java.time.temporal.ChronoUnit.SECONDS); // Nós não podemos pegar o tempo diretamente porque é "unsupported"
					var final = String.format("%02d:%02d", ((inSeconds/60)%60), (inSeconds%60));
					format += "${indexes[i]} `[${final}]` **[${item.snippet.title}](https://youtu.be/${item.id.videoId})**\n";

					context.metadata.put(i.toString(), item);
				}
				var embed = EmbedBuilder();
				embed.setColor(Color(217, 66, 52));
				embed.setDescription(format);
				embed.setTitle("<:youtube:314349922885566475> ${context.locale.YOUTUBE_RESULTS_FOR.msgFormat(query)}");
				var mensagem = context.sendMessage(context.getAsMention(true), embed.build());
				// Adicionar os reactions
				for (i in 0..Math.min(5, items.size) - 1) {
					mensagem.addReaction(indexes[i]).complete();
				}
				// mensagem.addReaction("❌").complete();
				return;
			} else {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.YOUTUBE_COULDNT_FIND.msgFormat(query))
			}
		} else {
			context.explain()
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user == context.userHandle) { // Somente quem executou o comando pode interagir!
			if (context.metadata.contains("playMusic")) {
				var item = context.metadata.get("playMusic") as YouTubeItem;

				if (loritta.checkAndLoad(context, "https://youtu.be/${item.id.videoId}")) {
					context.metadata.remove("playMusic")
				}
				return;
			}
			var item: YouTubeItem;
			if (e.reactionEmote.name == "1⃣") {
				item = context.metadata.get("0") as YouTubeItem;
			} else if (e.reactionEmote.name == "2⃣") {
				item = context.metadata.get("1") as YouTubeItem;
			} else if (e.reactionEmote.name == "3⃣") {
				item = context.metadata.get("2") as YouTubeItem;
			} else if (e.reactionEmote.name == "4⃣") {
				item = context.metadata.get("3") as YouTubeItem;
			} else {
				item = context.metadata.get("4") as YouTubeItem;
			}

			// Remover todos os reactions
			msg.clearReactions().complete();

			var embed = EmbedBuilder();
			embed.setTitle("<:youtube:314349922885566475> ${item.snippet.title}");
			embed.setDescription(item.snippet.description);
			var channelId = item.snippet.channelId;
			embed.setThumbnail("http://i.ytimg.com/i/$channelId/1.jpg");
			embed.addField("Link", "https://youtu.be/${item.id.videoId}", true);
			embed.addField("${context.locale.YOUTUBE_CHANNEL.msgFormat()}", "${item.snippet.channelTitle}", true);
			embed.setColor(Color(217, 66, 52));

			// Criar novo embed!
			msg.editMessage(embed.build()).complete();

			if (context.config.musicConfig.isEnabled) {
				// Se o sistema de músicas está ativado...
				msg.addReaction("▶").complete(); // Vamos colocar um ícone para tocar!
				context.metadata.put("playMusic", item);
			}
		}
	}
}