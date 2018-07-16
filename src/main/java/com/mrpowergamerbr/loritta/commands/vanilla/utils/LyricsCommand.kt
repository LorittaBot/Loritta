package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup

class LyricsCommand : AbstractCommand("lyrics", listOf("letra", "letras"), category = CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("WIKIA_DESCRIPTION")
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
			val lyrics = retrieveLyricsFromLyricsWikia(artist, musicName)

			if (lyrics == null) {
				context.reply(
						LoriReply(
								"a música não existe carinha do barulho",
								Constants.ERROR
						)
				)
				return
			}

			val embed = EmbedBuilder().apply {
				this.setDescription(lyrics)
			}

			context.sendMessage(embed.build())
		}
	}

	fun retrieveLyricsFromLyricsWikia(artist: String, musicName: String): String? {
		// lyrics.wikia.com
		// Nota: Não, não podemos usar a API da wikia, ela é bloqueada nesta wikia por ter conteúdo com direitos autorais
		val response = Jsoup.connect("http://lyrics.wikia.com/wiki/${artist.encodeToUrl()}:${musicName.encodeToUrl()}")
				.ignoreHttpErrors(true)
				.execute()

		if (response.statusCode() == 404) {
			return null
		}

		val document = response.parse()
		val lyricsBody = document.getElementsByClass("lyricbox")

		if (lyricsBody.isEmpty())
			return null

		val lyrics = lyricsBody.first().text()

		return lyrics
	}
}