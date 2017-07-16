package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import org.apache.commons.lang3.time.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit

fun OffsetDateTime.humanize(): String {
	val months = DateFormatSymbols().getMonths();
	return "${this.dayOfMonth} de ${months[this.month.value - 1]}, ${this.year} às ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}";
}

fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius);
}

/**
 * Retorna a instância atual da Loritta
 */
val loritta: Loritta
	get() = LorittaLauncher.loritta

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
			if (text.length >= 2048) {
				text = text.substring(0, 2047);
			}
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
						loritta.skipTrack(e.textChannel)
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

	@JvmStatic
	fun getLastPostFromFeed(feedUrl: String): FeedEntry? {
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
			} else if (jsoup.select("channel").isNotEmpty()) {
				// Provavelemente é uma feed RSS então :)
				title = jsoup.select("channel item title").first().text()
				link = jsoup.select("channel item link").first().text()
				entryItem = jsoup.select("channel item").first()
				dateRss = jsoup.select("channel item pubDate").first().text();
				val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
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
		} catch (urlEx: HttpRequest.HttpRequestException) { return null } // Ignorar silenciosamente...
	}

	@JvmStatic
	fun getServersInFanClub(): List<ServerFanClubEntry> {
		val fanClubServers = ArrayList<ServerFanClubEntry>()

		var guild = LorittaLauncher.loritta.lorittaShards.getGuildById("323548939246632961")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("animeszone", guild, "https://discord.me/animeszone",
					"<p>Variedade de comandos, eventos, League of Legends, shitpost, animes, mangas, wallpapers, músicas, histórias confessionário, frases e muito mais!</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("300213099405901825")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("starsbrasil", guild, "https://discord.gg/QRPsEuW",
					"<p>Animes, Filmes, Variedade de musica, Games, Cargos por level, Cargos personalizados, Sorteios, coisas aleatórias e muito mais pra vir!</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("278636362163552257")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("brprogrammers", guild, "https://discord.gg/ypuZAqp",
					"<p>Quer aprender a programar?</p>\n"
							+ "<p>Já sabe programar e quer conversar com outros programadores sobre a sua linguagem favorita?</p>\n"
							+ "<p>Sabe fazer um bot do discord e quer compartilhar conhecimento junto com outros programadores de bots para todos termos bots brs cada vez melhores?</p>\n"
							+ "<p>Quer aprender a criar um bot no discord?</p>\n"
							+ "<p>Tudo isso você pode fazer e encontrar no BR PROGRAMMERS! Um servidor com mais de 200 usuários contribuindo para melhorar cada vez mais</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("313077442959114240")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("gamingstars", guild, "https://discord.gg/k9Uu2Yd",
					"<p>Gostaria de convidá-lo(a) para vem fazer parte do servidor do Gaming Stars, este servidor tem foco em games mas também temos alguns assuntos extra como música eletrônica por exemplo,somos tão apaixonados por games e música eletrônica que temos ah Dubstep Stars para você descubrir ou aumentar seu amor ah música.</p>\n"
							+ "<p>Temos muitos planos pela frente ainda como ter nossos produtos ao venda (em breve)</p>\n"
							+ "\n"
							+ "<p>Venha fazer parte da nossa \"família\" estamos lhe esperando. ✨\uD83C\uDF55</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("207609115671920642")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("leaguebr", guild, "https://discord.gg/4YayeC8",
					"<p>O servidor mais amigável de League of Legends que você já encontrará. atendendo às necessidades da comunidade. Fornecemos canais e recursos para interessar a galera. Venha participar da festa!</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("330541042849808384")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("batata", guild, "https://discordapp.com/invite/362GWPp",
					"<p>Guild sem um foco, gostamos de fazer uma variedades de coisas (Como ouvir músicas, conversar, etc) Entre e faça parte da Batata, estamos te esperando lá! =)</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("210258135565205515")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("zueiragamer", guild, "https://discord.gg/KD6QJgS",
					"<p>Nosso objetivo é juntar cada vêz mais comunidades de jogos em geral. Mas a nossa comunidade não é só de jogadores, para todos que quiserem entrar, as portas estarão sempre abertas! \uD83D\uDE04</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("327605170257264641")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("gamingnetwork", guild, "https://discord.gg/sCTvW3c",
					"<p>Gostaria de convida-lo para o Gaming Network 2.0 . Um servidor com foco em juntar a comunidade de Games e do Discord para se conhecerem e se unirem.</p><p>Além de Games temos Música e muita Zoeira. E temos a Loritta para alegrar ainda mais nosso servidor.</p>"))
		}

		guild = LorittaLauncher.loritta.lorittaShards.getGuildById("248288318322638851")

		if (guild != null) {
			fanClubServers.add(ServerFanClubEntry("scarlegameplay", guild, "https://discord.gg/6Z7fRNN",
					"""<p>Discord de um youtuber legal e divertido, tendo em vista trazer muita diversão a todos, muitas brincadeira em call, tendo varias salas para conversar, salas somente para brincar com o bots e com temas diferenciados para evitar bagunças, Servidor sem pornografia, respeitando todos, venha conhecer e se divertir no nosso Discord!</p>"""))
		}

		return fanClubServers
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