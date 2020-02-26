package net.perfectdreams.loritta.plugin.funky.commands

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase
import net.perfectdreams.loritta.utils.Emotes
import org.jsoup.parser.Parser
import java.awt.Color

object YouTubeCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("youtube", "yt")) {
		description { it["commands.audio.youtube.description"] }
		examples { listOf("shantae tassel town", "hampsterdance") }

		executesDiscord {
			if (args.isEmpty()) {
				this.explain()
				return@executesDiscord
			}

			val search = args.joinToString(" ")
			val results = try {
				m.funkyManager.lavalinkRestClient.searchTrackOnYouTube(search)
			} catch (e: Exception) {
				reply(
						LorittaReply(
								"Nada encontrado...",
								Emotes.LORI_CRYING
						)
				)
				return@executesDiscord
			}

			if (results.isEmpty()) {
				reply(
						LorittaReply(
								"Nada encontrado...",
								Emotes.LORI_CRYING
						)
				)
				return@executesDiscord
			}


			val format = buildString {
				for (i in 0 until Math.min(9, results.size)) {
					val item = results[i]

					val info = item["info"].obj
					val strDuration = info["length"].long

					val duration = java.time.Duration.ofMillis(strDuration)
					val inSeconds = duration.get(java.time.temporal.ChronoUnit.SECONDS) // Nós não podemos pegar o tempo diretamente porque é "unsupported"
					val final = String.format("%02d:%02d", ((inSeconds / 60) % 60), (inSeconds % 60))
					this.append("${Constants.INDEXES[i]} \uD83C\uDFA5 `[${final}]` **[${Parser.unescapeEntities(info["title"].string, false)}](https://youtu.be/${info["identifier"].string})**\n")
				}
			}

			val embed = EmbedBuilder()
			embed.setColor(Color(217, 66, 52))
			embed.setDescription(format)
			embed.setTitle("<:youtube:314349922885566475> Resultados para `$search`")
			val message = sendMessage(embed.build())

			message.onReactionAddByAuthor(this) {
				val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

				// Caso seja uma reaçõa inválida ou que não tem no metadata, ignore!
				val result = results.getOrNull(idx)
				if (idx == -1 || result == null)
					return@onReactionAddByAuthor

				message.delete().queue()

				val videoMessage = reply(
						LorittaReply(
								"https://youtu.be/${result["info"]["identifier"].string}",
								"\uD83D\uDCFA"
						)
				)
			}

			for (i in 0 until Math.min(9, results.size)) {
				message.addReaction(Constants.INDEXES[i]).queue()
			}
		}
	}
}