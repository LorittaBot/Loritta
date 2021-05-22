package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.nullArray
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.awt.Canvas
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class LyricsCommand : AbstractCommand("lyrics", listOf("letra", "letras"), category = CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.lyrics.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.lyrics.examples")

	// TODO: Fix Usage

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
                                "${locale["commands.command.lyrics.couldntFind"]} ${locale["commands.command.lyrics.sorryForTheInconvenience"]} \uD83D\uDE2D",
                                Constants.ERROR
                        )
				)
				return
			}

			val lyrics = songInfo.lyrics

			// Vamos pegar a versão "compactada" da lyric
			val compactLyrics = getCompactLyricsFromLyrics(lyrics.split("\n"))
			// Para ficar melhor para ver, nós iremos separar em colunas
			val columns = divideLyricsInColumns(compactLyrics)

			val useHighResolution = 3 > columns.size // Para evitar OutOfMemoryExceptions, vamos fazer fallback de resolução
			val fontSize = if (useHighResolution) 18f else 9f
			val initialImageHeight = if (useHighResolution) 22 else 11
			val blankHeight = if (useHighResolution) 6 else 3
			val outlinePadding = if (useHighResolution) 2 else 1

			val lyricFont = Constants.VOLTER.deriveFont(fontSize)
			val fallbackFont = Constants.JACKEY.deriveFont(fontSize)

			val c = Canvas() // Canvas funciona até em headless mode, e é um jeito para a gente conseguir pegar as font metrics da fonte!
			val lyricFontMetrics = c.getFontMetrics(lyricFont)
			val fallbackFontMetrics = c.getFontMetrics(fallbackFont)

			// Fazer por colunas é mais... difícil do que parece na verdade!
			var imageWidth = 0
			for (column in columns) {
				val biggestString = column.maxByOrNull { getStringWidth(it, lyricFont, lyricFontMetrics, fallbackFontMetrics) }!!
				imageWidth += getStringWidth(biggestString, lyricFont, lyricFontMetrics, fallbackFontMetrics) + 4
			}

			var imageHeight = initialImageHeight

			for (line in columns.sortedByDescending { it.size }[0]) { // Agora nós iremos pegar a coluna que tem mais letras
				if (line.isBlank()) {
					imageHeight += blankHeight
					continue
				}
				imageHeight += lyricFontMetrics.height
			}

			val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
			val graphics = image.graphics

			val cover = getCoverArt(songInfo.albumUrl)

			val averageColor = cover.getScaledInstance(1, 1, BufferedImage.SCALE_AREA_AVERAGING)
					.toBufferedImage()
					.getRGB(0, 0)

			drawCoverArt(graphics, cover, imageWidth, imageHeight, Math.max(imageWidth, imageHeight))

			graphics.color = Color(0, 0, 0, 220)
			graphics.fillRect(0, 0, imageWidth, imageHeight)

			graphics.font = lyricFont
			graphics.color = Color.BLACK
			var x = 2
			var y = initialImageHeight

			for (column in columns) {
				val originalX = x
				for (line in column) {
					if (line.isBlank()) {
						y += blankHeight
						continue
					}

					for (ch in line) {
						graphics.color = Color.BLACK
						if (lyricFont.canDisplay(ch)) {
							graphics.font = lyricFont
						} else {
							graphics.font = fallbackFont
						}

						graphics.drawString(ch.toString(), x - outlinePadding, y)
						graphics.drawString(ch.toString(), x + outlinePadding, y)
						graphics.drawString(ch.toString(), x, y + outlinePadding)
						graphics.drawString(ch.toString(), x, y - outlinePadding)

						graphics.color = Color.WHITE
						graphics.drawString(ch.toString(), x, y)
						x += graphics.fontMetrics.charWidth(ch)
					}
					y += graphics.fontMetrics.height
					x = originalX
				}


				val biggestString = column.maxByOrNull { it.length }!!
				x += getStringWidth(biggestString, lyricFont, lyricFontMetrics, fallbackFontMetrics) + 2
				y = initialImageHeight
			}

			val embed = EmbedBuilder().apply {
				setTitle("\uD83C\uDFB6\uD83D\uDCC4 ${songInfo.artistName} - ${songInfo.songName}")
				setImage("attachment://lyrics.png")
				setColor(averageColor)
			}

			context.sendFile(
					image,
					"lyrics.png",
					context.getAsMention(true),
					embed.build()
			)
		} else {
			context.explain()
		}
	}

	fun getCompactLyricsFromLyrics(lyrics: List<String>): List<String> {
		val compactLyrics = mutableListOf<String>() // Letras versão "compacta"
		var count = 0
		var lastLine: String? = null

		for (line in lyrics) {
			val line = line.trim()
			if (line == lastLine) {
				count++
			} else {
				if (lastLine != null && count != 0) {
					compactLyrics.removeAt(compactLyrics.size - 1)
					compactLyrics.add(lastLine + " (${count + 1}x)")
				}
				count = 0
				compactLyrics.add(line)
			}
			lastLine = line
		}

		return compactLyrics
	}

	fun divideLyricsInColumns(lyrics: List<String>): List<List<String>> {
		val columns = mutableListOf<List<String>>()
		var column = mutableListOf<String>()

		for (line in lyrics) {
			if (column.filter { !it.isBlank() }.size == 40 && line.isNotBlank()) {
				columns.add(column)
				column = mutableListOf()
			}
			column.add(line)
		}
		columns.add(column)
		return columns
	}

	fun getCoverArt(coverArtUrl: String?): BufferedImage {
		var cover = if (coverArtUrl != null) {
			LorittaUtils.downloadImage(coverArtUrl)
		} else null

		if (cover == null) {
			// Baixar imagem de outras fontes
			// Usar cover art padrão
			cover = ImageIO.read(File(Loritta.ASSETS, "lobby_mural1.png"))
		}

		return cover!!
	}

	fun drawCoverArt(graphics: Graphics, coverArt: BufferedImage, imageWidth: Int, imageHeight: Int, size: Int) {
		val resized = coverArt.getScaledInstance(size, size, BufferedImage.SCALE_SMOOTH)
		// Colocar algo no centro é meio complicado, mas nada impossível!
		val centerX = imageWidth / 2
		val centerY = imageHeight / 2
		val halfSize = size / 2

		graphics.drawImage(resized, centerX - halfSize, centerY - halfSize, null)
	}

	fun getStringWidth(str: String, lyricFont: Font, lyricFontMetrics: FontMetrics, fallbackFontMetrics: FontMetrics): Int {
		var maxSize = 0
		for (ch in str) {
			maxSize += if (lyricFont.canDisplay(ch)) {
				lyricFontMetrics.charWidth(ch)
			} else {
				fallbackFontMetrics.charWidth(ch)
			}
		}
		return maxSize
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