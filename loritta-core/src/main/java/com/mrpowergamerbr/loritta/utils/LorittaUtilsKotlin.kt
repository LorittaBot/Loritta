package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mongodb.MongoWaitQueueFullException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.audio.AudioTrackWrapper
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getVoiceChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MiscUtil
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import org.apache.commons.lang3.ArrayUtils
import org.jsoup.nodes.Element
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.StringReader
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit

fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius)
}

fun Graphics.drawStringWrap(text: String, x: Int, y: Int, maxX: Int = 9999, maxY: Int = 9999) {
	ImageUtils.drawTextWrap(text, x, y, maxX, maxY, this.fontMetrics, this)
}

fun Array<String>.remove(index: Int): Array<String> {
	return ArrayUtils.remove(this, index)
}

val User.patreon: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("364201981016801281")
			val member = lorittaGuild.getMember(this)

			if (member != null && role != null) {
				if (member.roles.contains(role))
					return true
			}
		}
		return false
	}

val User.lorittaSupervisor: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("351473717194522647")
			val member = lorittaGuild.getMember(this)

			if (member != null && role != null) {
				if (member.roles.contains(role))
					return true
			}
		}
		return false
	}

val User.artist: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("341343754336337921")
			val member = lorittaGuild.getMember(this)

			if (member != null && role != null) {
				if (member.roles.contains(role))
					return true
			}
		}
		return false
	}

val User.support: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("399301696892829706")
			val member = lorittaGuild.getMember(this)

			if (member != null && role != null) {
				if (member.roles.contains(role))
					return true
			}
		}
		return false
	}

/**
 * Retorna a instância atual da Loritta
 */
val loritta get() = LorittaLauncher.loritta

/**
 * Retorna a LorittaShards
 */
val lorittaShards get() = LorittaLauncher.loritta.lorittaShards

val gson get() = Loritta.GSON
val jsonParser get() = Loritta.JSON_PARSER

/**
 * Salva um objeto usando o Datastore do MongoDB
 */
infix fun <T> Loritta.save(obj: T) {
	val updateOptions = ReplaceOptions().upsert(true)
	if (obj is MongoServerConfig) {
		loritta.serversColl.replaceOne(
				Filters.eq("_id", obj.guildId),
				obj,
				updateOptions
		)
		return
	}
	throw RuntimeException("Trying to save $obj but no collection for that type exists!")
}

fun String.isValidSnowflake(): Boolean {
	try {
		MiscUtil.parseSnowflake(this)
		return true
	} catch (e: NumberFormatException) {
		return false
	}
}

enum class NSFWResponse {
	OK, ERROR, NSFW, EXCEPTION
}

object LorittaUtilsKotlin {
	val logger = KotlinLogging.logger {}

	fun handleIfBanned(context: CommandContext, profile: Profile): Boolean {
		if (profile.isBanned) {
			LorittaLauncher.loritta.ignoreIds.add(context.userHandle.idLong)

			// Se um usuário está banido...
			context.userHandle
					.openPrivateChannel()
					.queue (
							{ it.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() },
							{ context.event.textChannel!!.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() }
					)
			return true
		}
		return false
	}

	fun handleIfBanned(context: LorittaCommandContext, profile: Profile): Boolean {
		if (context !is DiscordCommandContext)
			throw UnsupportedOperationException("I don't know how to handle a $context yet!")

		if (profile.isBanned) {
			LorittaLauncher.loritta.ignoreIds.add(context.userHandle.idLong)

			// Se um usuário está banido...
			context.userHandle
					.openPrivateChannel()
					.queue (
							{ it.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() },
							{ context.event.textChannel!!.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() }
					)
			return true
		}
		return false
	}

	fun <T:Comparable<T>>shuffle(items:MutableList<T>):List<T>{
		val rg : Random = Random()
		for (i in 0..items.size - 1) {
			val randomPosition = rg.nextInt(items.size)
			val tmp : T = items[i]
			items[i] = items[randomPosition]
			items[randomPosition] = tmp
		}
		return items
	}

	fun getImageStatus(url: String): NSFWResponse {
		var response = HttpRequest.get("https://mdr8.p.mashape.com/api/?url=" + URLEncoder.encode(url, "UTF-8"))
				.header("X-Mashape-Key", loritta.config.mashape.apiKey)
				.header("Accept", "application/json")
				.acceptJson()
				.body()

		// Nós iremos ignorar caso a API esteja sobrecarregada
		try {
			val reader = StringReader(response)
			val jsonReader = JsonReader(reader)
			val apiResponse = jsonParser.parse(jsonReader).asJsonObject // Base

			if (apiResponse.has("error")) {
				return NSFWResponse.ERROR
			}

			if (apiResponse.get("rating_label").asString == "adult") {
				return NSFWResponse.NSFW
			}
		} catch (e: Exception) {
			logger.info("Ignorando verificação de conteúdo NSFW ($url) - Causa: ${e.message} - Resposta: $response")
			return NSFWResponse.EXCEPTION
		}
		return NSFWResponse.OK
	}

	@JvmStatic
	fun fillTrackMetadata(track: AudioTrackWrapper) {
		if (track.track.sourceManager.sourceName == "youtube") { // Se é do YouTube, então vamos preencher com algumas informações "legais"
			try {
				val playingTrack = track.track
				val videoId = playingTrack.info.uri.substring(playingTrack.info.uri.length - 11 until playingTrack.info.uri.length)
				val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=snippet,statistics&key=${loritta.youtubeKey}").body()
				val parser = JsonParser()
				val json = parser.parse(response).asJsonObject
				val item = json["items"][0]
				val snippet = item["snippet"].obj
				val statistics = item["statistics"].obj
				val likeCount = statistics["likeCount"].nullString
				val dislikeCount = statistics["dislikeCount"].nullString

				var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${loritta.youtubeKey}").body()
				var channelJson = parser.parse(channelResponse).obj

				track.metadata.put("viewCount", statistics["viewCount"].string)

				if (likeCount != null)
					track.metadata.put("likeCount", likeCount)
				if (dislikeCount != null)
					track.metadata.put("dislikeCount", dislikeCount)

				if (statistics.has("commentCount")) {
					track.metadata.put("commentCount", statistics["commentCount"].string)
				} else {
					track.metadata.put("commentCount", "Comentários desativados")
				}
				track.metadata.put("thumbnail", snippet["thumbnails"]["high"]["url"].string)
				track.metadata.put("channelIcon", channelJson["items"][0]["snippet"]["thumbnails"]["high"]["url"].string)
			} catch (e: Exception) {
				logger.error("Erro ao pegar informações sobre ${track.track}!", e)
			}
		}
	}

	fun createTrackInfoEmbed(context: CommandContext): MessageEmbed {
		return createTrackInfoEmbed(context.guild, context.legacyLocale, context.config.musicConfig.voteToSkip)
	}

	@JvmStatic
	fun createTrackInfoEmbed(guild: Guild, locale: LegacyBaseLocale, stripSkipInfo: Boolean): MessageEmbed {
		val manager = loritta.audioManager.getGuildAudioPlayer(guild)
		val playingTrack = manager.player.playingTrack
		val metaTrack = manager.scheduler.currentTrack
		val embed = EmbedBuilder()
		embed.setTitle("\uD83C\uDFB5 ${playingTrack.info.title}", playingTrack.info.uri)
		embed.setColor(Color(93, 173, 236))
		val millis = manager.player.playingTrack.duration

		val fancy = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
		)

		val elapsedMillis = manager.player.trackPosition

		val elapsed = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(elapsedMillis),
				TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis))
		)

		embed.addField("\uD83D\uDD52 ${locale["MUSICINFO_LENGTH"]}", "`$elapsed`/`$fancy`", true)

		if (playingTrack.sourceManager.sourceName == "youtube" && metaTrack != null) {
			val viewCount = if (metaTrack.metadata.containsKey("viewCount")) metaTrack.metadata["viewCount"] else "???"
			val likeCount = if (metaTrack.metadata.containsKey("likeCount")) metaTrack.metadata["likeCount"] else "???"
			val dislikeCount = if (metaTrack.metadata.containsKey("dislikeCount")) metaTrack.metadata["dislikeCount"] else "???"
			val commentCount = if (metaTrack.metadata.containsKey("commentCount")) metaTrack.metadata["commentCount"] else "???"

			// Se a source é do YouTube, então vamos pegar informações sobre o vídeo!
			embed.addField("\uD83D\uDCFA ${locale["MUSICINFO_VIEWS"]}", viewCount, true)
			embed.addField("\uD83D\uDE0D ${locale["MUSICINFO_LIKES"]}", likeCount, true)
			embed.addField("\uD83D\uDE20 ${locale["MUSICINFO_DISLIKES"]}", dislikeCount, true)
			embed.addField("\uD83D\uDCAC ${locale["MUSICINFO_COMMENTS"]}", commentCount, true)
			embed.setThumbnail(metaTrack.metadata["thumbnail"])
			embed.setAuthor(playingTrack.info.author, null, metaTrack.metadata["channelIcon"])
		}

		if (!stripSkipInfo)
			embed.addField("\uD83D\uDCAB ${locale["MUSICINFO_SKIPTITLE"]}", locale["MUSICINFO_SKIPTUTORIAL"], false)

		return embed.build()
	}

	fun createPlaylistInfoEmbed(context: CommandContext): MessageEmbed {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)
		val embed = EmbedBuilder()

		embed.setTitle("\uD83C\uDFB6 ${context.legacyLocale["MUSICINFO_INQUEUE"]}")
		embed.setColor(Color(93, 173, 236))

		val songs = manager.scheduler.queue.toList()
		val currentTrack = manager.scheduler.currentTrack
		if (currentTrack != null) {
			var text = "[${currentTrack.track.info.title}](${currentTrack.track.info.uri}) (${context.legacyLocale["MUSICINFO_REQUESTED_BY"]} ${currentTrack.user.asMention})\n"
			text += songs.joinToString("\n", transform = { "[${it.track.info.title}](${it.track.info.uri}) (${context.legacyLocale["MUSICINFO_REQUESTED_BY"]} ${it.user.asMention})" })
			if (text.length >= 2048) {
				text = text.substring(0, 2047)
			}
			embed.setDescription(text)
		} else {
			embed.setDescription(context.legacyLocale["MUSICINFO_NOMUSIC_SHORT"])
		}
		return embed.build()
	}

	suspend fun handleMusicReaction(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.reactionEmote.name != "\uD83E\uDD26") { // Se é diferente de facepalm...
			if (context.handle == e.member) { // Então só deixe quem exectou o comando mexer!
				if (e.reactionEmote.isEmote("\uD83D\uDD22")) {
					msg.editMessage(LorittaUtilsKotlin.createPlaylistInfoEmbed(context)).queue {
						val filteredReactions = msg.reactions.filter { it.reactionEmote.name != "\uD83E\uDD26" }
						for (reaction in filteredReactions) {
							if (msg.reactions.indexOf(reaction) == (filteredReactions.size - 1)) {
								reaction.removeReaction().queue {
									e.reaction.removeReaction(e.user).queue {
										msg.addReaction("\uD83D\uDCBF").queue()
									}
								}
							} else {
								reaction.removeReaction().queue()
							}
						}
					}
				} else if (e.reactionEmote.isEmote("\uD83E\uDD26")) {
					msg.editMessage(LorittaUtilsKotlin.createTrackInfoEmbed(context)).queue {
						val filteredReactions = msg.reactions.filter { it.reactionEmote.name != "\uD83E\uDD26" }
						for (reaction in filteredReactions) {
							if (msg.reactions.indexOf(reaction) == (filteredReactions.size - 1)) {
								reaction.removeReaction().queue {
									e.reaction.removeReaction(e.user).queue {
										msg.addReaction("\uD83D\uDD22").queue()
									}
								}
							} else {
								reaction.removeReaction().queue()
							}
						}
					}
				}
			}
		} else { // Se for facepalm...
			val atw = context.metadata.get("currentTrack") as AudioTrackWrapper
			val list = e.reaction.retrieveUsers().await()
			val count = list.count { !it.isBot }

			val conf = context.config

			if (count > 0 && conf.musicConfig.voteToSkip && loritta.audioManager.getGuildAudioPlayer(e.guild).scheduler.currentTrack === atw) {
				val vc = e.guild.getVoiceChannelByNullableId(conf.musicConfig.musicGuildId)

				if (e.reactionEmote.name != "\uD83E\uDD26") { // Só permitir reactions de "facepalm"
					return
				}

				if (e.member?.voiceState?.channel !== vc) {
					e.reaction.removeReaction(e.user).queue()
					return
				}

				if (vc != null) {
					val inChannel = vc.members.filter { !it.user.isBot }.size
					val required = Math.round(inChannel.toDouble() * (conf.musicConfig.required.toDouble() / 100))

					if (count >= required) {
						loritta.audioManager.skipTrack(context)
					}
				}
			}
		}
	}

	var executedCommands = 0

	fun startRandomSong(guild: Guild, conf: MongoServerConfig) {
		val diff = System.currentTimeMillis() - loritta.audioManager.songThrottle.getOrDefault(guild.id, 0L)

		if (5000 > diff)
			return  // bye

		if (conf.musicConfig.musicGuildId == null || conf.musicConfig.musicGuildId!!.isEmpty())
			return

		val voiceChannel = guild.getVoiceChannelByNullableId(conf.musicConfig.musicGuildId) ?: return

		if (!guild.selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT))
			return

		if (voiceChannel.members.isEmpty())
			return

		if (conf.musicConfig.autoPlayWhenEmpty && !conf.musicConfig.urls.isEmpty()) {
			val trackUrl = conf.musicConfig.urls[Loritta.RANDOM.nextInt(0, conf.musicConfig.urls.size)]

			// Nós iremos colocar o servidor em um throttle, para evitar várias músicas sendo colocadas ao mesmo tempo devido a VEVO sendo tosca
			loritta.audioManager.songThrottle.put(guild.id, System.currentTimeMillis())

			// E agora carregue a música
			loritta.audioManager.loadAndPlayNoFeedback(guild, conf, trackUrl) // Só vai meu parça
		}
	}
}

data class FacebookPostWrapper(
		val url: String,
		val description: String)

data class FeedEntry(
		val title: String,
		val link: String,
		val date: Calendar,
		val description: String?,
		val entry: Element
)
