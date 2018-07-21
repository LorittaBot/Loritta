package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mongodb.MongoWaitQueueFullException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.audio.AudioTrackWrapper
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import net.dv8tion.jda.core.utils.MiscUtil
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.lang3.time.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.StringReader
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

fun <R : Any> R.logger(): Lazy<Logger> {
	return lazy { LoggerFactory.getLogger(getClassName(this.javaClass)) }
}
fun <T : Any> getClassName(clazz: Class<T>): String {
	return clazz.name.removeSuffix("\$Companion")
}

fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius);
}

fun Graphics.drawStringWrap(text: String, x: Int, y: Int, maxX: Int = 9999, maxY: Int = 9999) {
	ImageUtils.drawTextWrap(text, x, y, maxX, maxY, this.fontMetrics, this)
}

fun Array<String>.remove(index: Int): Array<String> {
	return ArrayUtils.remove(this, index)
}

val User.patreon: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

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
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

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

val User.donator: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("334711262262853642")
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
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

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

val User.supervisor: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

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

val User.support: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

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
	if (obj is ServerConfig) {
		loritta.serversColl.replaceOne(
				Filters.eq("_id", obj.guildId),
				obj,
				updateOptions
		)
		return
	}
	if (obj is LorittaProfile) {
		loritta.usersColl.replaceOne(
				Filters.eq("_id", obj.userId),
				obj,
				updateOptions
		)
		return
	}
	if (obj is StoredMessage) {
		loritta.storedMessagesColl.replaceOne(
				Filters.eq("_id", obj.messageId),
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
	val logger by logger()

	fun handleIfBanned(context: CommandContext, profile: LorittaProfile): Boolean {
		if (profile.isBanned) {
			LorittaLauncher.loritta.ignoreIds.add(context.userHandle.id)

			// Se um usuário está banido...
			try {
				context.userHandle
						.openPrivateChannel()
						.complete()
						.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.locale["USER_IS_LORITTABANNED", profile.banReason]).complete()
			} catch (e: ErrorResponseException) {
				// Usuário tem as DMs desativadas
				context.event.textChannel!!.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.locale["USER_IS_LORITTABANNED", profile.banReason]).complete()
			}
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
				.header("X-Mashape-Key", Loritta.config.mashapeKey)
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
				val playingTrack = track.track;
				val videoId = playingTrack.info.uri.substring(playingTrack.info.uri.length - 11 until playingTrack.info.uri.length)
				val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=snippet,statistics&key=${loritta.youtubeKey}").body();
				val parser = JsonParser();
				val json = parser.parse(response).asJsonObject;
				val item = json["items"][0]
				val snippet = item["snippet"].obj
				val statistics = item["statistics"].obj
				val likeCount = statistics["likeCount"].nullString
				val dislikeCount = statistics["dislikeCount"].nullString

				var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${loritta.youtubeKey}").body();
				var channelJson = parser.parse(channelResponse).obj;

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
		return createTrackInfoEmbed(context.guild, context.locale, context.config.musicConfig.voteToSkip)
	}

	@JvmStatic
	fun createTrackInfoEmbed(guild: Guild, locale: BaseLocale, stripSkipInfo: Boolean): MessageEmbed {
		val manager = loritta.audioManager.getGuildAudioPlayer(guild)
		val playingTrack = manager.player.playingTrack
		val metaTrack = manager.scheduler.currentTrack
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

		embed.addField("\uD83D\uDD52 ${locale["MUSICINFO_LENGTH"]}", "`$elapsed`/`$fancy`", true);

		if (playingTrack.sourceManager.sourceName == "youtube" && metaTrack != null) {
			val viewCount = if (metaTrack.metadata.containsKey("viewCount")) metaTrack.metadata["viewCount"] else "???"
			val likeCount = if (metaTrack.metadata.containsKey("likeCount")) metaTrack.metadata["likeCount"] else "???"
			val dislikeCount = if (metaTrack.metadata.containsKey("dislikeCount")) metaTrack.metadata["dislikeCount"] else "???"
			val commentCount = if (metaTrack.metadata.containsKey("commentCount")) metaTrack.metadata["commentCount"] else "???"

			// Se a source é do YouTube, então vamos pegar informações sobre o vídeo!
			embed.addField("\uD83D\uDCFA ${locale["MUSICINFO_VIEWS"]}", viewCount, true);
			embed.addField("\uD83D\uDE0D ${locale["MUSICINFO_LIKES"]}", likeCount, true);
			embed.addField("\uD83D\uDE20 ${locale["MUSICINFO_DISLIKES"]}", dislikeCount, true);
			embed.addField("\uD83D\uDCAC ${locale["MUSICINFO_COMMENTS"]}", commentCount, true);
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

		embed.setTitle("\uD83C\uDFB6 ${context.locale["MUSICINFO_INQUEUE"]}")
		embed.setColor(Color(93, 173, 236))

		val songs = manager.scheduler.queue.toList()
		val currentTrack = manager.scheduler.currentTrack
		if (currentTrack != null) {
			var text = "[${currentTrack.track.info.title}](${currentTrack.track.info.uri}) (${context.locale["MUSICINFO_REQUESTED_BY"]} ${currentTrack.user.asMention})\n";
			text += songs.joinToString("\n", transform = { "[${it.track.info.title}](${it.track.info.uri}) (${context.locale["MUSICINFO_REQUESTED_BY"]} ${it.user.asMention})" })
			if (text.length >= 2048) {
				text = text.substring(0, 2047);
			}
			embed.setDescription(text)
		} else {
			embed.setDescription(context.locale["MUSICINFO_NOMUSIC_SHORT"]);
		}
		return embed.build();
	}

	fun handleMusicReaction(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.reactionEmote.name != "\uD83E\uDD26") { // Se é diferente de facepalm...
			if (context.handle == e.member) { // Então só deixe quem exectou o comando mexer!
				if (e.reactionEmote.name == "\uD83D\uDD22") {
					msg.editMessage(LorittaUtilsKotlin.createPlaylistInfoEmbed(context)).complete()
					msg.reactions.forEach {
						if (it.reactionEmote.name != "\uD83E\uDD26") {
							it.removeReaction().complete()
						}
					}
					e.reaction.removeReaction(e.user).complete()
					msg.addReaction("\uD83D\uDCBF").complete();
				} else if (e.reactionEmote.name == "\uD83D\uDCBF") {
					val embed = LorittaUtilsKotlin.createTrackInfoEmbed(context)
					msg.reactions.forEach {
						if (it.reactionEmote.name != "\uD83E\uDD26") {
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

			if (count > 0 && conf.musicConfig.voteToSkip && loritta.audioManager.getGuildAudioPlayer(e.guild).scheduler.currentTrack === atw) {
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
						loritta.audioManager.skipTrack(context)
					}
				}
			}
		}
	}

	/**
	 * Pega um post aleatório de uma página do Facebook
	 */
	fun getRandomPostsFromPage(page: String, limit: Int = 5): List<FacebookPostWrapper> {
		val response = HttpRequest
				.get("https://graph.facebook.com/v2.9/$page/posts?fields=attachments{url,subattachments,media,description}&access_token=${Loritta.config.facebookToken}&offset=${Loritta.RANDOM.nextInt(0, 1000)}&limit=$limit")
				.body()

		val json = jsonParser.parse(response)

		var url: String? = null;
		var description: String? = null;

		val posts = mutableListOf<FacebookPostWrapper>()

		for (post in json["data"].array) {
			var foundUrl = post["attachments"]["data"][0]["url"].string;

			if (!foundUrl.contains("video")) {
				try { // Provavelmente não é o que nós queremos
					url = post["attachments"]["data"][0]["media"]["image"]["src"].string;
					description = post["attachments"]["data"][0]["description"].string
					posts.add(FacebookPostWrapper(url, description))
				} catch (e: Exception) {}
			}
		}
		return posts
	}

	/**
	 * Pega um post aleatório de um grupo do Facebook
	 */
	fun getRandomPostsFromGroup(group: String): List<FacebookPostWrapper> {
		val response = HttpRequest.get("https://graph.facebook.com/v2.9/$group/feed?fields=message,attachments{url,subattachments,media,description}&access_token=${Loritta.config.facebookToken}&offset=${Loritta.RANDOM.nextInt(0, 1000)}")
				.body()
		val json = jsonParser.parse(response)

		var url: String? = null;
		var description: String? = null;

		val posts = mutableListOf<FacebookPostWrapper>()

		for (post in json["data"].array) {
			var foundUrl = post["attachments"]["data"][0]["url"].string

			if (!foundUrl.contains("video")) {
				try { // Provavelmente não é o que nós queremos
					url = post["attachments"]["data"][0]["media"]["image"]["src"].string;
					description = if (post.obj.has("message")) post["message"].string else ""
					posts.add(FacebookPostWrapper(url, description))
				} catch (e: Exception) {}
			}
		}
		return posts
	}

	@JvmStatic
	fun getLastPostFromFeed(feedUrl: String): FeedEntry? {
		try {
			try {
				val rssFeed = HttpRequest.get(feedUrl)
						.header("Cache-Control", "max-age=0, no-cache") // Nunca pegar o cache
						.useCaches(false) // Também não usar cache
						.userAgent(Constants.USER_AGENT)
						.body();

				// Parsear a nossa RSS feed
				val jsoup = Jsoup.parse(rssFeed, "", Parser.xmlParser())

				var title: String? = null
				var link: String? = null
				var entryItem: Element? = null
				var dateRss: String? = null
				var description: String? = null;
				var rssCalendar: Calendar? = null

				if (jsoup.select("feed").attr("xmlns") == "http://www.w3.org/2005/Atom") {
					// Atom Feed
					title = jsoup.select("feed entry title").first().text()
					link = jsoup.select("feed entry link").first().attr("href")
					entryItem = jsoup.select("feed entry").first()
					if (jsoup.select("feed entry published").isNotEmpty()) {
						dateRss = jsoup.select("feed entry published").first().text();
					} else if (jsoup.select("feed entry updated").isNotEmpty()) {
						dateRss = jsoup.select("feed entry updated").first().text();
					}
					rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(dateRss);
					// Enquanto a maioria das feeds RSS colocam title e link... a maioria não coloca a descrição corretamente
					// Então vamos verificar de duas maneiras
					if (jsoup.select("feed entry description").isNotEmpty()) {
						description = jsoup.select("feed entry description").first().text()
					} else if (jsoup.select("feed entry content").isNotEmpty()) {
						description = jsoup.select("feed entry content").first().text()
					}
				} else if (jsoup.select("rdf|RDF").attr("xmlns") == "http://purl.org/rss/1.0/") {
					// RDF Feed (usada pela Steam)
					title = jsoup.select("item title").first().text()
					link = jsoup.select("item link").first().text()
					entryItem = jsoup.select("item").first()
					dateRss = jsoup.select("item pubDate").first().text();
					val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
					val date = sdf.parse(dateRss)
					rssCalendar = DateUtils.toCalendar(date)
					if (!jsoup.select("item description").isEmpty()) {
						description = jsoup.select("item description").first().text()
					}
				} else if (jsoup.select("channel").isNotEmpty()) {
					// Provavelemente é uma feed RSS então :)
					title = jsoup.select("channel item title").first().text()
					link = jsoup.select("channel item link").first().text()
					entryItem = jsoup.select("channel item").first()
					dateRss = jsoup.select("channel item pubDate").first().text();
					val sdf = if (!dateRss.matches(Regex("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+"))) {
						if (dateRss[3] == ',') {
							SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
						} else {
							SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
						}
					} else { // Algumas rss feeds usam este estilo de data (bug?)
						SimpleDateFormat("dd/mm/yyyy HH:mm:ss", Locale.ENGLISH)
					}
					val date = sdf.parse(dateRss)
					rssCalendar = DateUtils.toCalendar(date)
					if (!jsoup.select("channel item description").isEmpty()) {
						description = jsoup.select("channel item description").first().text()
					}
				} else {
					// Faço a mínima ideia do que seja isto.
					return null;
				}

				if (dateRss == null) {
					return null;
				}

				if (description != null) {
					description = Jsoup.clean(description, "", Whitelist.simpleText(), Document.OutputSettings().escapeMode(Entities.EscapeMode.xhtml))
				}

				return FeedEntry(title, link, rssCalendar, description, entryItem)
			} catch (urlEx: HttpRequest.HttpRequestException) {
				return null
			} // Ignorar silenciosamente...
		} catch (e: Exception) {
			println(feedUrl)
			e.printStackTrace()
			return null
		}
	}

	fun sendStackTrace(message: Message, t: Throwable) {
		if (message.isFromType(ChannelType.TEXT)) {
			sendStackTrace("[`${message.guild.name.stripCodeMarks()}` -> `${message.channel.name.stripCodeMarks()}`] **${message.author.name.stripCodeMarks()}**: `${message.contentRaw.stripCodeMarks()}`", t)
		} else {
			sendStackTrace("[`Mensagem Direta`] **${message.author.name}**: `${message.contentRaw}`", t)
		}
	}

	var stackTraceCount = 0
	var stackTraceDelay = 0L

	fun sendStackTrace(message: String, t: Throwable) {
		if (t is MongoWaitQueueFullException) // I don't care!!! ~ Desativado para evitar floods de mensagens no #stacktraces ao recarregar a Loritta pelo JRebel
			return

		if (t is ErrorResponseException && t.errorCode == -1) // Ignorar socket timeouts (provavelmente é a shard do Discord que está morrendo)
			return

		val guild = lorittaShards.getGuildById("297732013006389252")

		if (guild == null) {
			logger.error("Não foi possível enviar stacktrace: Guild inexistente!")
			return
		}

		val textChannel = guild.getTextChannelById("336834673441243146")

		if (textChannel == null) {
			logger.error("Não foi possível enviar stacktrace: Canal de texto inexistente!")
			return
		}

		val messageBuilder = MessageBuilder()
		messageBuilder.append(message)
		val builder = EmbedBuilder()
		builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")
		var description = "Irineu, você não sabe e nem eu!"
		if (t is ExecutionException) {
			description = "A thread que executava este comando agora está nos céus... *+angel* (Provavelmente seu script atingiu o limite máximo de memória utilizada!)"
		} else {
			val message = t.cause?.message
			if (t != null && t.cause != null && message != null) {
				description = message.trim { it <= ' ' }
			} else if (t != null) {
				description = ExceptionUtils.getStackTrace(t).substring(0, Math.min(2000, ExceptionUtils.getStackTrace(t).length))
			}
		}
		builder.setDescription("```$description```")
		builder.setFooter("Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null)
		builder.setColor(Color.RED)

		messageBuilder.setEmbed(builder.build())

		if ((System.currentTimeMillis() - stackTraceDelay) > 5000) {
			stackTraceCount = 0
			stackTraceDelay = System.currentTimeMillis()
		}

		if (stackTraceCount == 4) {
			stackTraceCount++
			logger.info("Ignorando exceptions devido ao grande número de exceptions recebidas em um curto período de tempo!")
			return
		}

		textChannel.sendMessage(messageBuilder.build()).queue()
		stackTraceCount++
	}

	var executedCommands = 0;

	fun startRandomSong(guild: Guild, conf: ServerConfig) {
		val diff = System.currentTimeMillis() - loritta.audioManager.songThrottle.getOrDefault(guild.id, 0L)

		if (5000 > diff)
			return  // bye

		if (conf.musicConfig.musicGuildId == null || conf.musicConfig.musicGuildId!!.isEmpty())
			return

		val voiceChannel = guild.getVoiceChannelById(conf.musicConfig.musicGuildId) ?: return

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