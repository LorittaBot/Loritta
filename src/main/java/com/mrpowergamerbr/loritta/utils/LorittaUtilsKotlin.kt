package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.text.DateFormatSymbols
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

inline fun OffsetDateTime.humanize(): String {
	val months = DateFormatSymbols().getMonths();
	return "${this.dayOfMonth} de ${months[this.month.value - 1]}, ${this.year} às ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}";
}

inline fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

inline fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius);
}

/**
 * Retorna a instância atual da Loritta
 */
inline fun loritta(): Loritta {
	return LorittaLauncher.getInstance();
}

object LorittaUtilsKotlin {
	@JvmStatic
	fun fillTrackMetadata(track: AudioTrackWrapper) {
		if (track.track.sourceManager.sourceName == "youtube") { // Se é do YouTube, então vamos preencher com algumas informações "legais"
			val playingTrack = track.track;
			val videoId = playingTrack.info.uri.substring(playingTrack.info.uri.length - 11..playingTrack.info.uri.length - 1)
			val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=snippet,statistics&key=${Loritta.config.youtubeKey}").body();
			val parser = JsonParser();
			val json = parser.parse(response).asJsonObject;
			val item = json.get("items").asJsonArray.get(0)
			val snippet = item.get("snippet").asJsonObject
			val statistics = item.get("statistics").asJsonObject

			var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${Loritta.config.youtubeKey}").body();
			var channelJson = parser.parse(channelResponse).asJsonObject;

			track.metadata.put("viewCount", statistics.get("viewCount").asString)
			track.metadata.put("likeCount", statistics.get("likeCount").asString)
			track.metadata.put("dislikeCount", statistics.get("dislikeCount").asString)
			track.metadata.put("commentCount", statistics.get("commentCount").asString)
			track.metadata.put("thumbnail", snippet.get("thumbnails").asJsonObject.get("high").asJsonObject.get("url").asString)
			track.metadata.put("channelIcon", channelJson.get("items").asJsonArray[0].asJsonObject.get("snippet").asJsonObject.get("thumbnails").asJsonObject.get("high").asJsonObject.get("url").asString)
		}
	}

	fun createTrackInfoEmbed(context: CommandContext): MessageEmbed {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		val playingTrack = manager.player.playingTrack;
		val metaTrack = manager.scheduler.currentTrack;
		val embed = EmbedBuilder()
		embed.setTitle("\uD83C\uDFB5 ${playingTrack.info.title}", playingTrack.info.uri)
		embed.setColor(Color(93, 173, 236))
		val millis = playingTrack.duration

		val fancy = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
		);

		val elapsedMillis = playingTrack.position;

		val elapsed = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(elapsedMillis),
				TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis))
		);

		embed.addField("\uD83D\uDD52 Duração", "`$elapsed`/`$fancy`", true);

		if (playingTrack.sourceManager.sourceName == "youtube") {
			// Se a source é do YouTube, então vamos pegar informações sobre o vídeo!
			embed.addField("\uD83D\uDCFA Visualizações", metaTrack.metadata.get("viewCount"), true);
			embed.addField("\uD83D\uDE0D Gostei", metaTrack.metadata.get("likeCount"), true);
			embed.addField("\uD83D\uDE20 Não Gostei", metaTrack.metadata.get("dislikeCount"), true);
			embed.addField("\uD83D\uDCAC Comentários", metaTrack.metadata.get("commentCount"), true);
			embed.setThumbnail(metaTrack.metadata.get("thumbnail"))
			embed.setAuthor("${playingTrack.info.author}", null, metaTrack.metadata.get("channelIcon"))
		}

		embed.addField("\uD83D\uDCAB Quer pular a música?", "**Então use \uD83E\uDD26 nesta mensagem!** (Se 75% das pessoas no canal de música reagirem com \uD83E\uDD26, eu irei pular a música!)", false)
		return embed.build()
	}

	fun createPlaylistInfoEmbed(context: CommandContext): MessageEmbed {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		val embed = EmbedBuilder()

		embed.setTitle("\uD83C\uDFB6 Na fila...")
		embed.setColor(Color(93, 173, 236))

		val songs = manager.scheduler.queue.toList()
		val currentTrack = manager.scheduler.currentTrack
		if (currentTrack != null) {
			var text = "[${currentTrack.track.info.title}](${currentTrack.track.info.uri}) (pedido por ${currentTrack.user.asMention})\n";
			text += songs.joinToString("\n", transform = { "[${it.track.info.title}](${it.track.info.uri}) (pedido por ${it.user.asMention})" })
			embed.setDescription(text)
		} else {
			embed.setDescription("Nenhuma música...");
		}
		return embed.build();
	}

	fun handleMusicReaction(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.reactionEmote.name != "\uD83E\uDD26") { // Se é diferente de facepalm...
			if (context.handle == e.member) { // Então só deixe quem exectou o comando mexer!
				if (e.reactionEmote.name == "\uD83D\uDD22") {
					msg.editMessage(LorittaUtilsKotlin.createPlaylistInfoEmbed(context)).complete()
					msg.reactions.forEach {
						if (it.emote.name != "\uD83E\uDD26") {
							it.removeReaction().complete()
						}
					}
					e.reaction.removeReaction(e.user).complete()
					msg.addReaction("\uD83D\uDCBF").complete();
				} else if (e.reactionEmote.name == "\uD83D\uDCBF") {
					val embed = LorittaUtilsKotlin.createTrackInfoEmbed(context)
					msg.reactions.forEach {
						if (it.emote.name != "\uD83E\uDD26") {
							it.removeReaction().complete()
						}
					}
					e.reaction.removeReaction(e.user).queue()
					msg.editMessage(embed).complete()
					msg.addReaction("\uD83D\uDD22").queue();
				}
			}
		} else { // Se for facepalm...
			val atw = context.metadata.get("currentTrack") as AudioTrackWrapper
			val count = e.reaction.users.complete().filter { !it.isBot }.size
			val conf = context.config

			if (count > 0 && conf.musicConfig.voteToSkip && LorittaLauncher.loritta.getGuildAudioPlayer(e.guild).scheduler.currentTrack === atw) {
				val vc = e.guild.getVoiceChannelById(conf.musicConfig.musicGuildId)

				if (e.reactionEmote.name != "\uD83E\uDD26") { // Só permitir reactions de "facepalm"
					return
				}

				if (e.member.voiceState.channel !== vc) {
					e.reaction.removeReaction(e.user).complete()
					return
				}

				if (vc != null) {
					val inChannel = vc.members.filter{ !it.user.isBot }.size
					val required = Math.round(inChannel.toDouble() * (conf.musicConfig.required.toDouble() / 100))

					if (count >= required) {
						LorittaLauncher.getInstance().skipTrack(e.guild)
						e.textChannel.sendMessage("🤹 Música pulada!").complete()
					}
				}
			}
		}
	}

	/**
	 * Pega um post aleatório de uma página do Facebook
	 */
	fun getRandomPostFromPage(page: String): FacebookPostWrapper? {
		val response = HttpRequest.get("https://graph.facebook.com/v2.9/$page/posts?fields=attachments{url,subattachments,media,description}&access_token=${Loritta.config.facebookToken}&offset=${Loritta.random.nextInt(0, 1000)}").body();

		val json = JsonParser().parse(response)

		var url: String? = null;
		var description: String? = null;
		var image: BufferedImage? = null;

		for (post in json["data"].array) {
			var foundUrl = post["attachments"]["data"][0]["url"].string;

			if (!foundUrl.contains("video")) {
				try { // Provavelmente não é o que nós queremos
					url = post["attachments"]["data"][0]["media"]["image"]["src"].string;
					description = post["attachments"]["data"][0]["description"].string
					image = LorittaUtils.downloadImage(url, 4000)
					if (image != null) {
						return FacebookPostWrapper(url, description, image)
					}
				} catch (e: Exception) {}
			}
		}
		return null;
	}

	/**
	 * Pega um post aleatório de um grupo do Facebook
	 */
	fun getRandomPostFromGroup(group: String): FacebookPostWrapper? {
		val response = HttpRequest.get("https://graph.facebook.com/v2.9/$group/feed?fields=message,attachments{url,subattachments,media,description}&access_token=${Loritta.config.facebookToken}&offset=${Loritta.random.nextInt(0, 1000)}").body();
		val json = JsonParser().parse(response)

		var url: String? = null;
		var description: String? = null;
		var image: BufferedImage? = null;

		for (post in json["data"].array) {
			var foundUrl = post["attachments"]["data"][0]["url"].string;

			if (!foundUrl.contains("video")) {
				try { // Provavelmente não é o que nós queremos
					url = post["attachments"]["data"][0]["media"]["image"]["src"].string;
					description = post["message"].string
					image = LorittaUtils.downloadImage(url, 4000)
					if (image != null) {
						return FacebookPostWrapper(url, description, image)
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
		return null;
	}
}

data class FacebookPostWrapper(
		val url: String,
		val description: String,
		val image: BufferedImage)