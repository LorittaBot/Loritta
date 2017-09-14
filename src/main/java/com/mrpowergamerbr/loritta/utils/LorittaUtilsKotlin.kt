package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
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
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.StringReader
import java.net.URLEncoder
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

fun OffsetDateTime.humanize(): String {
	val fixedOffset = this.atZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime()
	val months = DateFormatSymbols().getMonths();
	return "${this.dayOfMonth} de ${months[this.month.value - 1]}, ${fixedOffset.year} às ${fixedOffset.hour.toString().padStart(2, '0')}:${fixedOffset.minute.toString().padStart(2, '0')}";
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

/**
 * Retorna a instância atual da Loritta
 */
val loritta: Loritta
	get() = LorittaLauncher.loritta

/**
 * Retorna a LorittaShards
 */
val lorittaShards: LorittaShards
	get() = LorittaLauncher.loritta.lorittaShards

/**
 * Salva um objeto usando o Datastore do MongoDB
 */
infix fun <T> Loritta.save(obj: T) {
	loritta.ds.save(obj)
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
			println(url + " ~ " + response)
			val reader = StringReader(response)
			val jsonReader = JsonReader(reader)
			val apiResponse = JsonParser().parse(jsonReader).asJsonObject // Base

			if (apiResponse.has("error")) {
				return NSFWResponse.ERROR
			}

			if (apiResponse.get("rating_label").asString == "adult") {
				return NSFWResponse.NSFW
			}
		} catch (e: Exception) {
			println("Ignorando verificação de conteúdo NSFW ($url) - Causa: ${e.message} - Resposta: $response")
			return NSFWResponse.EXCEPTION
		}
		return NSFWResponse.OK
	}

	@JvmStatic
	fun fillTrackMetadata(track: AudioTrackWrapper) {
		if (track.track.sourceManager.sourceName == "youtube") { // Se é do YouTube, então vamos preencher com algumas informações "legais"
			try {
				val playingTrack = track.track;
				val videoId = playingTrack.info.uri.substring(playingTrack.info.uri.length - 11..playingTrack.info.uri.length - 1)
				val response = HttpRequest.get("https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=snippet,statistics&key=${Loritta.config.youtubeKey}").body();
				val parser = JsonParser();
				val json = parser.parse(response).asJsonObject;
				val item = json["items"][0]
				val snippet = item["snippet"].obj
				val statistics = item["statistics"].obj

				var channelResponse = HttpRequest.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=${snippet.get("channelId").asString}&fields=items%2Fsnippet%2Fthumbnails&key=${Loritta.config.youtubeKey}").body();
				var channelJson = parser.parse(channelResponse).obj;

				track.metadata.put("viewCount", statistics["viewCount"].string)
				track.metadata.put("likeCount", statistics["likeCount"].string)
				track.metadata.put("dislikeCount", statistics["dislikeCount"].string)
				if (statistics.has("commentCount")) {
					track.metadata.put("commentCount", statistics["commentCount"].string)
				} else {
					track.metadata.put("commentCount", "Comentários desativados")
				}
				track.metadata.put("thumbnail", snippet["thumbnails"]["high"]["url"].string)
				track.metadata.put("channelIcon", channelJson["items"][0]["snippet"]["thumbnails"]["high"]["url"].string)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun createTrackInfoEmbed(context: CommandContext): MessageEmbed {
		return createTrackInfoEmbed(context.guild, context.locale, false)
	}

	@JvmStatic
	fun createTrackInfoEmbed(guild: Guild, locale: BaseLocale, stripSkipInfo: Boolean): MessageEmbed {
		val manager = loritta.getGuildAudioPlayer(guild)
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

		embed.addField("\uD83D\uDD52 ${locale.MUSICINFO_LENGTH.msgFormat()}", "`$elapsed`/`$fancy`", true);

		if (playingTrack.sourceManager.sourceName == "youtube") {
			val viewCount = if (metaTrack.metadata.containsKey("viewCount")) metaTrack.metadata["viewCount"] else "???"
			val likeCount = if (metaTrack.metadata.containsKey("likeCount")) metaTrack.metadata["likeCount"] else "???"
			val dislikeCount = if (metaTrack.metadata.containsKey("dislikeCount")) metaTrack.metadata["dislikeCount"] else "???"
			val commentCount = if (metaTrack.metadata.containsKey("commentCount")) metaTrack.metadata["commentCount"] else "???"

			// Se a source é do YouTube, então vamos pegar informações sobre o vídeo!
			embed.addField("\uD83D\uDCFA ${locale.get("MUSICINFO_VIEWS")}", viewCount, true);
			embed.addField("\uD83D\uDE0D ${locale.get("MUSICINFO_LIKES")}", likeCount, true);
			embed.addField("\uD83D\uDE20 ${locale.get("MUSICINFO_DISLIKES")}", dislikeCount, true);
			embed.addField("\uD83D\uDCAC ${locale.get("MUSICINFO_COMMENTS")}", commentCount, true);
			embed.setThumbnail(metaTrack.metadata.get("thumbnail"))
			embed.setAuthor("${playingTrack.info.author}", null, metaTrack.metadata["channelIcon"])
		}

		if (!stripSkipInfo)
			embed.addField("\uD83D\uDCAB ${locale.get("MUSICINFO_SKIPTITLE")}", locale.get("MUSICINFO_SKIPTUTORIAL"), false)

		return embed.build()
	}

	fun createPlaylistInfoEmbed(context: CommandContext): MessageEmbed {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		val embed = EmbedBuilder()

		embed.setTitle("\uD83C\uDFB6 ${context.locale.MUSICINFO_INQUEUE.msgFormat()}")
		embed.setColor(Color(93, 173, 236))

		val songs = manager.scheduler.queue.toList()
		val currentTrack = manager.scheduler.currentTrack
		if (currentTrack != null) {
			var text = "[${currentTrack.track.info.title}](${currentTrack.track.info.uri}) (${context.locale.MUSICINFO_REQUESTED_BY.msgFormat()} ${currentTrack.user.asMention})\n";
			text += songs.joinToString("\n", transform = { "[${it.track.info.title}](${it.track.info.uri}) (${context.locale.MUSICINFO_REQUESTED_BY.msgFormat()} ${it.user.asMention})" })
			if (text.length >= 2048) {
				text = text.substring(0, 2047);
			}
			embed.setDescription(text)
		} else {
			embed.setDescription(context.locale.MUSICINFO_NOMUSIC_SHORT.msgFormat());
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
						loritta.skipTrack(context)
					}
				}
			}
		}
	}

	/**
	 * Pega um post aleatório de uma página do Facebook
	 */
	fun getRandomPostFromPage(page: String): FacebookPostWrapper? {
		val response = HttpRequest
				.get("https://graph.facebook.com/v2.9/$page/posts?fields=attachments{url,subattachments,media,description}&access_token=${Loritta.config.facebookToken}&offset=${Loritta.random.nextInt(0, 1000)}")
				.body()

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
		val response = HttpRequest.get("https://graph.facebook.com/v2.9/$group/feed?fields=message,attachments{url,subattachments,media,description}&access_token=${Loritta.config.facebookToken}&offset=${Loritta.random.nextInt(0, 1000)}")
				.body()

		val json = JsonParser().parse(response)

		var url: String? = null;
		var description: String? = null;
		var image: BufferedImage? = null;

		for (post in json["data"].array) {
			var foundUrl = post["attachments"]["data"][0]["url"].string

			if (!foundUrl.contains("video")) {
				try { // Provavelmente não é o que nós queremos
					url = post["attachments"]["data"][0]["media"]["image"]["src"].string;
					description = if (post.obj.has("message")) post["message"].string else ""
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

	@JvmStatic
	fun getLastPostFromFeed(feedUrl: String): FeedEntry? {
		try {
			try {
				val rssFeed = HttpRequest.get(feedUrl)
						.header("Cache-Control", "max-age=0, no-cache") // Nunca pegar o cache
						.useCaches(false) // Também não usar cache
						.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0")
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
						SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
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

	@JvmStatic
	fun generateServersInFanClub() {
		val entries = mutableListOf<ServerFanClubEntry>()
		loritta.rawServersFanClub.forEach{
			val guild = lorittaShards.getGuildById(it.serverId)
			if (guild != null) {
				entries.add(ServerFanClubEntry(it.id,
						guild,
						it.inviteUrl,
						it.description))
			}
		}
		loritta.serversFanClub = entries
	}

	fun sendStackTrace(message: Message, t: Throwable) {
		if (message.isFromType(ChannelType.TEXT)) {
			sendStackTrace("[`${message.guild.name}` -> `${message.channel.name}`] **${message.author.name}**: `${message.rawContent}`", t)
		} else {
			sendStackTrace("[`Mensagem Direta`] **${message.author.name}**: `${message.rawContent}`", t)
		}
	}

	fun sendStackTrace(message: String, t: Throwable) {
		val guild = lorittaShards.getGuildById("297732013006389252")!!
		val textChannel = guild.getTextChannelById("336834673441243146")

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

		textChannel.sendMessage(messageBuilder.build()).queue()
	}

	val commandQueue = mutableListOf<String>()
	var lastUpdate = System.currentTimeMillis()

	@JvmStatic
	fun trackCommands(message: Message) {
		val guild = lorittaShards.getGuildById("297732013006389252")!!
		val textChannel = guild.getTextChannelById("336932935838203904")

		if (message.isFromType(ChannelType.TEXT)) {
			commandQueue.add("[`${message.guild.name.stripCodeMarks()}` -> `${message.channel.name.stripCodeMarks()}`] **${message.author.name.stripCodeMarks()}**: `${message.strippedContent.stripCodeMarks()}`")
		} else {
			commandQueue.add("[`Mensagem Direta`] **${message.author.name.stripCodeMarks()}**: `${message.strippedContent.stripCodeMarks()}`")
		}

		if (lastUpdate > 5000) {
			lastUpdate = System.currentTimeMillis()

			val toBeSent = commandQueue.joinToString("\n")

			commandQueue.clear()

			textChannel.sendMessage(toBeSent).queue()
		}
	}
}

data class FacebookPostWrapper(
		val url: String,
		val description: String,
		val image: BufferedImage)

data class FeedEntry(
		val title: String,
		val link: String,
		val date: Calendar,
		val description: String?,
		val entry: Element
)

class ServerFanClubEntry {
	val id: String
	val guild: Guild
	val inviteUrl: String
	val description: String
	val guildIcon: String
	val fancyJoinDate: String

	constructor(id: String, guild: Guild, inviteUrl: String, description: String) {
		this.id = id;
		this.guild = guild;
		this.inviteUrl = inviteUrl;
		this.description = description;
		this.guildIcon = guild.iconUrl.replace("jpg", "png")
		this.fancyJoinDate = guild.selfMember.joinDate.humanize()
	}
}

class PanelOptionWrapper(
		val obj: Any,
		val id: String,
		val description: String) {
	var result: Any

	init {
		val field = obj.javaClass.getDeclaredField(id)
		field.isAccessible = true
		result = false

		if (field.type == Boolean::class.javaPrimitiveType) {
			result = field.getBoolean(obj)
		}
	}
}

class ServerMiscInfo(
		val members: Long,
		val bots: Long,
		val joinDate: String
)