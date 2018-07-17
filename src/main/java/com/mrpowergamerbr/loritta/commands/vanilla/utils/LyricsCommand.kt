package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.awt.Canvas
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

class LyricsCommand : AbstractCommand("lyrics", listOf("letra", "letras"), category = CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("LYRICS_Description")
	}

	override fun getUsage(): String {
		return "url conteúdo"
	}

	override fun getExample(): List<String> {
		return listOf("she - Atomic")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val args = context.rawArgs

		val joined = args.joinToString(" ")
		val split = joined.split(" - ")

		split.forEach {
			println(it)
		}
		if (split.size == 2) {
			context.sendMessage("pesquisando...")
			val artist = split[0]
			val musicName = split[1]
			// Nós iremos verificar as lyrics em mútiplos websites, o primeiro que der certo, nós iremos usar
			val songInfo = retrieveSongInfoFromLyricWikia(artist, musicName)
					?: retrieveSongInfoFromSongLyrics(artist, musicName)
					?: retrieveSongInfoFromLyricsOvh(artist, musicName)
					?: retrieveSongInfoFromLetrasMus(artist, musicName)
					?: retrieveSongInfoFromVagalume(artist, musicName) // Pesquisa no Vagalume, isto deve ser a última opção!

			if (songInfo == null) {
				context.reply(
						LoriReply(
								"a música não existe carinha do barulho",
								Constants.ERROR
						)
				)
				return
			}

			val lyrics = songInfo.lyrics
			val lines = lyrics.split("\n")
			val compactLyrics = mutableListOf<String>() // Letras versão "compacta"
			var count = 0
			var lastLine: String? = null

			for (line in lines) {
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

			// Para ficar melhor para ver, nós iremos separar em colunas
			val columns = mutableListOf<List<String>>()
			var column = mutableListOf<String>()

			for (line in compactLyrics) {
				if (column.filter { !it.isBlank() }.size == 40 && line.isNotBlank()) {
					columns.add(column)
					column = mutableListOf()
				}
				column.add(line)
			}
			columns.add(column)

			val lyricFont = Constants.VOLTER.deriveFont(18f)
			val fallbackFont = FileInputStream(File(Loritta.ASSETS + "jackeyfont.ttf")).use {
				Font.createFont(Font.TRUETYPE_FONT, it)
			}.deriveFont(18f)

			val c = Canvas() // Canvas funciona até em headless mode, e é um jeito para a gente conseguir pegar as font metrics da fonte!
			val lyricFontMetrics = c.getFontMetrics(lyricFont)
			val fallbackFontMetrics = c.getFontMetrics(fallbackFont)

			// Fazer por colunas é mais... difícil do que parece na verdade!
			var imageWidth = 0
			for (column in columns) {
				val biggestString = column.maxBy {
					var size = 0
					for (ch in it) {
						size += if (lyricFont.canDisplay(ch)) {
							lyricFontMetrics.charWidth(ch)
						} else {
							fallbackFontMetrics.charWidth(ch)
						}
					}
					size
				}!!

				var maxSize = 0
				for (ch in biggestString) {
					maxSize += if (lyricFont.canDisplay(ch)) {
						lyricFontMetrics.charWidth(ch)
					} else {
						fallbackFontMetrics.charWidth(ch)
					}
				}

				imageWidth += maxSize + 4
			}

			var imageHeight = 22

			for (line in columns.sortedByDescending { it.size }[0]) { // A primeira coluna sempre *deverá* ter mais linhas
				if (line.isBlank()) {
					imageHeight += 6
					continue
				}
				imageHeight += lyricFontMetrics.height
			}

			val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
			val graphics = image.graphics

			if (songInfo.albumUrl != null) {
				val cover = LorittaUtils.downloadImage(songInfo.albumUrl)
				if (cover != null) {
					val biggerSize = Math.max(imageWidth, imageHeight)
					val loriImage = LorittaImage(cover)
					loriImage.resize(biggerSize, biggerSize, false)
					// Colocar algo no centro é meio complicado, mas nada impossível!
					val centerX = imageWidth / 2
					val centerY = imageHeight / 2
					val halfSize = biggerSize / 2

					graphics.drawImage(loriImage.bufferedImage, centerX - halfSize, centerY - halfSize, null)
				}
			}

			graphics.color = Color(0, 0, 0, 220)
			graphics.fillRect(0, 0, imageWidth, imageHeight)

			graphics.font = lyricFont
			graphics.color = Color.BLACK
			var x = 2
			var y = 22

			for (column in columns) {
				var originalX = x
				for (line in column) {
					if (line.isBlank()) {
						y += 6
						continue
					}

					for (ch in line) {
						graphics.color = Color.BLACK
						if (lyricFont.canDisplay(ch)) {
							graphics.font = lyricFont
						} else {
							graphics.font = fallbackFont
						}

						graphics.drawString(ch.toString(), x - 2, y)
						graphics.drawString(ch.toString(), x + 2, y)
						graphics.drawString(ch.toString(), x, y + 2)
						graphics.drawString(ch.toString(), x, y - 2)

						graphics.color = Color.WHITE
						graphics.drawString(ch.toString(), x, y)
						x += graphics.fontMetrics.charWidth(ch)
					}
					y += graphics.fontMetrics.height
					x = originalX
				}


				val biggestString = column.maxBy { it.length }!!
				var maxSize = 0

				for (ch in biggestString) {
					maxSize += if (lyricFont.canDisplay(ch)) {
						lyricFontMetrics.charWidth(ch)
					} else {
						fallbackFontMetrics.charWidth(ch)
					}
				}

				x += maxSize + 2
				y = 22
			}

			context.sendFile(image, "lyrics.png", "${columns.size} colunas de letras")
		}
	}

	fun retrieveSongInfoFromLyricWikia(artist: String, musicName: String): SongInfo? {
		// lyrics.wikia.com
		// Nota: Não, não podemos usar a API da wikia, ela é bloqueada nesta wikia por ter conteúdo com direitos autorais
		val response = Jsoup.connect("http://lyrics.wikia.com/wiki/${artist.replace(" ", "_").encodeToUrl()}:${musicName.replace(" ", "_").encodeToUrl()}")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404) {
			return null
		}

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

							if (albumElement != null) {
								albumArtUrl = albumElement.attr("href")

								println(albumArtUrl)
							}
						}
					}
				}
			}
		}

		return SongInfo(
				albumArtUrl,
				Jsoup.clean(lyrics, "localhost", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
		)
	}

	fun retrieveSongInfoFromSongLyrics(artist: String, musicName: String): SongInfo? {
		val response = Jsoup.connect("http://www.songlyrics.com/${artist.replace(" ", "-").encodeToUrl()}/${musicName.replace(" ", "-").encodeToUrl()}-lyrics/")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404) {
			return null
		}

		val document = response.parse()
		val songLyricsDiv = document.getElementById("songLyricsDiv") ?: return null

		val lyrics = songLyricsDiv.html()

		return SongInfo(
				null,
				lyrics.replace("<br>", "\n")
		)
	}

	fun retrieveSongInfoFromLyricsOvh(artist: String, musicName: String): SongInfo? {
		val request = HttpRequest.get("https://api.lyrics.ovh/v1/${artist.encodeToUrl()}/${musicName.encodeToUrl()}")
		request.ok()

		if (request.code() == 404)
			return null

		val payload = jsonParser.parse(request.body())

		return SongInfo(
				null,
				payload["lyrics"].string
		)
	}

	fun retrieveSongInfoFromLetrasMus(artist: String, musicName: String): SongInfo? {
		val response = Jsoup.connect("https://www.letras.mus.br/${artist.replace(" ", "-").encodeToUrl()}/${musicName.replace(" ", "-").encodeToUrl()}/")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404) {
			return null
		}

		val document = response.parse()
		val songLyricsDiv = document.getElementsByTag("article").firstOrNull() ?: return null

		val lyrics = songLyricsDiv.html()

		return SongInfo(
				null,
				Jsoup.clean(lyrics.replace("<br>", "\n"), "localhost", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
		)
	}

	fun retrieveSongInfoFromVagalume(artist: String, musicName: String): SongInfo? {
		val request = HttpRequest.get("https://api.vagalume.com.br/search.artmus?apikey=${Loritta.config.vagalumeKey}&q=${artist.encodeToUrl()} ${musicName.encodeToUrl()}&limit=1")
				.body()

		val payload = jsonParser.parse(request)
		val response = payload["response"].obj

		val numFound = response["numFound"].int

		if (numFound == 0)
			return null

		val firstResult = response["docs"].array[0].obj

		if (firstResult["title"].nullString == null)
			return null

		val id = firstResult["id"]

		val musicRequest = HttpRequest.get("https://api.vagalume.com.br/search.php?musid=$id&apikey=${Loritta.config.vagalumeKey}")
				.body()

		val musicPayload = jsonParser.parse(musicRequest)
		val music = musicPayload["mus"].array[0]
		val artistId = musicPayload["art"]["id"].string

		val artistRequest = HttpRequest.get("https://api.vagalume.com.br/image.php?bandID=$artistId&limit=1")
				.body()

		val artistPayload = jsonParser.parse(artistRequest).obj

		val images = artistPayload["images"].nullArray

		val artistUrl: String? = images?.firstOrNull()?.get("url").nullString

		return SongInfo(
				artistUrl,
				music["text"].string
		)
	}

	class SongInfo(val albumUrl: String?, val lyrics: String)
}