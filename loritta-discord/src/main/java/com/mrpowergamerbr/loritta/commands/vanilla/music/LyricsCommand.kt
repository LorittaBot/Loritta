package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class LyricsCommand : AbstractCommand("lyrics", listOf("letra", "letras"), category = CommandCategory.MUSIC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.music.lyrics.description"]
	}

	override fun getUsage(): String {
		return "artista - nome da música"
	}

	override fun getExamples(): List<String> {
		return listOf(
				"she - Atomic",
				"she - Chiptune Memories",
				"C418 - tsuki no koibumi",
				"MC Hariel - Tá Fácil Dizer Que Me Ama",
				"Jack Ü - Jungle Bae",
				"Pusher - Clear",
				"Sega - Sonic Boom",
				"Macklemore & Ryan Lewis - White Walls"
		)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val args = context.rawArgs

		val joined = args.joinToString(" ")
		val split = joined.split(" - ")

		if (split.size == 2) {
			val artist = split[0]
			val musicName = split[1]
			// Nós iremos verificar as lyrics em mútiplos websites, o primeiro que der certo, nós iremos usar
			// É necessário posicionar na ordem de mais confiável -> menos confiável
			val songInfo = retrieveSongInfoFromLyricWikia(artist, musicName)
					?: retrieveSongInfoFromGenius(artist, musicName)
					?: retrieveSongInfoFromSongLyrics(artist, musicName)
					?: retrieveSongInfoFromLetrasMus(artist, musicName)
					?: retrieveSongInfoFromVagalume(artist, musicName) // Pesquisa no Vagalume, isto deve ser a última opção!

			if (songInfo == null) {
				context.reply(
                        LorittaReply(
                                "${locale["commands.music.lyrics.couldntFind"]} ${locale["commands.music.lyrics.sorryForTheInconvenience"]} \uD83D\uDE2D",
                                Constants.ERROR
                        )
				)
				return
			}

			val lyrics = songInfo.lyrics

			val embed = EmbedBuilder()
			embed.setTitle("\uD83C\uDFB6\uD83D\uDCC4 ${songInfo.artistName} - ${songInfo.songName}")
			embed.setColor(Color.red)

			val lines = lyrics.split("\n")



			var currentFieldText = StringBuilder()

			try {

				for (line in lines) {
					if (currentFieldText.length + line.length > 1024) {
						embed.addField("", "${currentFieldText.toString()}", false)
						currentFieldText = StringBuilder()
					}

					currentFieldText.append("$line\n")
				}

				embed.addField("", "${currentFieldText.toString()}", false)

				context.sendMessage(embed.build())

			} catch (e: Exception) {
				val embed3 = EmbedBuilder()
				embed3.setTitle("\uD83C\uDFB6\uD83D\uDCC4 ${songInfo.artistName} - ${songInfo.songName}")
				embed3.setColor(Color.red)

				embed3.addField("", "${lyrics.slice(IntRange(0, 999))}", false)
				embed3.addField("", "${lyrics.slice(IntRange(1000, 1999))}", false)
				embed3.addField("", "${lyrics.slice(IntRange(2000, 2999))}", false)
				embed3.addField("", "${lyrics.slice(IntRange(3000, 3999))}", false)
				embed3.addField("", "${lyrics.slice(IntRange(4000, 4999))}", false)
				embed3.setFooter("${locale["commands.music.lyrics.goToNextPage"]}")

				val message = context.sendMessage(embed3.build())
				message.addReaction("▶").queue()

				message.onReactionAddByAuthor(context.userHandle.idLong) {

					if (it.reactionEmote.name == "▶") {

						val embed2 = EmbedBuilder()
						embed2.setTitle("\uD83C\uDFB6\uD83D\uDCC4 ${songInfo.artistName} - ${songInfo.songName}")
						embed2.setColor(Color.red)
						embed2.setDescription(lyrics.slice(IntRange(5000, lyrics.length - 1)))

						message.edit(
								"",
								embed2.build(),
								true
						)

						return@onReactionAddByAuthor
					}

				}
			}

		} else {
			context.explain()
		}
	}

	fun retrieveSongInfoFromLyricWikia(artist: String, musicName: String): SongInfo? {
		// lyrics.wikia.com
		// Nota: Não, não podemos usar a API da wikia, ela é bloqueada nesta wikia por ter conteúdo com direitos autorais
		val response = Jsoup.connect("http://lyrics.wikia.com/wiki/${artist.replace(" ", "_").encodeToUrl()}:${musicName.replace(" ", "_").encodeToUrl()}")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404)
			return null

		val document = response.parse()
		val lyricsBody = document.getElementsByClass("lyricbox")

		if (lyricsBody.isEmpty())
			return null

		val lyrics = lyricsBody.first().html()

		var albumArtUrl: String? = null

		val songHeader = document.getElementById("song-header-container")
		if (songHeader != null) {
			val albumUrlElement = songHeader.getElementsByTag("a").lastOrNull()

			if (albumUrlElement != null) {
				val albumUrl = albumUrlElement.attr("href")

				if (!albumUrl.contains("redlink=1")) {
					val albumResponse = Jsoup.connect("http://lyrics.wikia.com${albumUrl}")
							.ignoreHttpErrors(true)
							.execute()

					if (albumResponse.statusCode() == 200) {
						val albumDocument = albumResponse.parse()
						val contentText = albumDocument.getElementsByClass("plainlinks").firstOrNull()

						if (contentText != null) {
							val albumElement = contentText.getElementsByClass("image-thumbnail").firstOrNull()

							albumArtUrl = albumElement?.attr("href")
						}
					}
				}
			}
		}

		val songTitle = document.getElementById("song-header-title")?.text()

		return SongInfo(
				artist,
				songTitle ?: musicName,
				albumArtUrl,
				Jsoup.clean(lyrics, "localhost", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
		)
	}

	fun retrieveSongInfoFromGenius(artist: String, musicName: String): SongInfo? {
		val response = Jsoup.connect("https://genius.com/${artist.replace(" ", "-").encodeToUrl()}-${musicName.replace(" ", "-").encodeToUrl()}-lyrics/")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404)
			return null

		val document = response.parse()
		val songLyricsDiv = document.getElementsByClass("lyrics").firstOrNull() ?: return null
		val artistName = document.getElementsByClass("header_with_cover_art-primary_info-primary_artist").firstOrNull()?.text()
		val songName = document.getElementsByClass("header_with_cover_art-primary_info-title").firstOrNull()?.text()
		val coverArtUrl = document.getElementsByClass("cover_art-image").firstOrNull()?.attr("src")
		val lyrics = songLyricsDiv.children().html()

		val prettyPrintedBodyFragment = Jsoup.clean(lyrics, "", Whitelist.none().addTags("br", "p"), Document.OutputSettings().prettyPrint(true))
		return SongInfo(
				artistName ?: artist,
				songName ?: musicName,
				coverArtUrl,
				Jsoup.clean(prettyPrintedBodyFragment, "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
		)
	}

	fun retrieveSongInfoFromSongLyrics(artist: String, musicName: String): SongInfo? {
		val response = Jsoup.connect("http://www.songlyrics.com/${artist.replace(" ", "-").encodeToUrl()}/${musicName.replace(" ", "-").encodeToUrl()}-lyrics/")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404)
			return null

		val document = response.parse()
		val songLyricsDiv = document.getElementById("songLyricsDiv") ?: return null

		val lyrics = songLyricsDiv.html()

		return SongInfo(
				artist,
				musicName,
				null,
				lyrics.replace("<br>", "\n")
		)
	}

	fun retrieveSongInfoFromLetrasMus(artist: String, musicName: String): SongInfo? {
		val response = try {
			Jsoup.connect("https://www.letras.mus.br/${artist.replace(" ", "-").encodeToUrl()}/${musicName.replace(" ", "-").encodeToUrl()}/")
					.ignoreHttpErrors(true)
					.execute()
		} catch (e: IOException) { // O letras.mus tem um bug de redirecionamento infinito, vamos ignorar caso isto aconteça
			return null
		}

		if (response.statusCode() == 404)
			return null

		val document = response.parse()
		val headTitle = document.getElementsByClass("cnt-head_title").firstOrNull() ?: return null
		val h1 = headTitle.getElementsByTag("h1") ?: return null
		val h2 = headTitle.getElementsByTag("h2") ?: return null

		val songTitle = h1.text()
		if (!songTitle.contains(musicName, true))
			return null

		val songLyricsDiv = document.getElementsByClass("cnt-letra").firstOrNull() ?: return null

		val lyrics = songLyricsDiv.html()
		val avatarUrl = headTitle.getElementsByTag("img").firstOrNull()?.attr("src")

		return SongInfo(
				h2.text() ?: artist,
				songTitle ?: musicName,
				avatarUrl,
				Jsoup.clean(lyrics
						.replace("<br>", "\n")
						.replace("<p>", "\n"),
						"localhost",
						Whitelist.none(),
						Document.OutputSettings().prettyPrint(false)
				)
		)
	}

	fun retrieveSongInfoFromVagalume(artist: String, musicName: String): SongInfo? {
		val request = HttpRequest.get("https://api.vagalume.com.br/search.artmus?apikey=lolidkthekey&q=${artist.encodeToUrl()} ${musicName.encodeToUrl()}&limit=1")
				.body()

		val payload = JsonParser.parseString(request)
		val response = payload["response"].obj

		val numFound = response["numFound"].int

		if (numFound == 0)
			return null

		val firstResult = response["docs"].array[0].obj

		if (firstResult["title"].nullString == null)
			return null

		val id = firstResult["id"]

		val musicRequest = HttpRequest.get("https://api.vagalume.com.br/search.php?musid=$id&apikey=lolidkthekey")
				.body()

		val musicPayload = JsonParser.parseString(musicRequest)
		val music = musicPayload["mus"].array[0]
		val artistId = musicPayload["art"]["id"].string

		val artistRequest = HttpRequest.get("https://api.vagalume.com.br/image.php?bandID=$artistId&limit=1")
				.body()

		val artistPayload = JsonParser.parseString(artistRequest).obj

		val images = artistPayload["images"].nullArray

		val artistUrl: String? = images?.firstOrNull()?.get("url").nullString

		return SongInfo(
				artist,
				musicName,
				artistUrl,
				music["text"].string
		)
	}

	class SongInfo(val artistName: String, val songName: String, val albumUrl: String?, val lyrics: String)
}
