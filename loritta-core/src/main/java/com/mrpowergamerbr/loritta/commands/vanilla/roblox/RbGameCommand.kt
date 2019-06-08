package com.mrpowergamerbr.loritta.commands.vanilla.roblox

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.api.EmbedBuilder
import org.jsoup.Jsoup

class RbGameCommand : AbstractCommand("rbgame", listOf("rbjogo", "rbgameinfo"), CommandCategory.ROBLOX) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["RBGAME_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ")

			val url = "https://www.roblox.com/games/moreresultscached?StartRows=0&MaxRows=40&IsUserLoggedIn=false&NumberOfColumns=8&IsInHorizontalScrollMode=false&DeviceTypeId=1&Keyword=${query.encodeToUrl()}&AdSpan=56&AdAlignment=0&v=2&IsSecure=&UseFakeResults=False&SuggestedCorrection=none&SuggestionKeyword=&SuggestionReplacedKeyword="

			val body = HttpRequest.get(url)
					.body()

			val document = Jsoup.parse(body)
			val gameCardLink = document.getElementsByClass("game-card-link").firstOrNull()

			if (gameCardLink == null) {
				context.reply(
						LoriReply(
								message = locale["RBGAME_CouldntFind"],
								prefix = Constants.ERROR
						)
				)
				return
			}

			val gameUrl = gameCardLink.attr("href")

			val gameBody = HttpRequest.get(gameUrl)
					.body()

			val embed = EmbedBuilder()
					.setColor(Constants.ROBLOX_RED)

			val gameDocument = Jsoup.parse(gameBody)

			val placeId = gameDocument.getElementById("game-detail-page").attr("data-place-id")
			val gameName = gameDocument.getElementsByClass("game-name").text()
			val gameAuthor = gameDocument.getElementsByClass("game-creator")[0].getElementsByClass("text-name").text()
			val gameDescription = gameDocument.getElementsByClass("game-description")[0].text()
			val favoriteCount = gameDocument.getElementsByClass("game-favorite-count")[0].text()
			val thumbnail = gameDocument.getElementsByClass("carousel-thumb").firstOrNull()

			if (thumbnail != null) {
				embed.setImage(thumbnail.attr("src"))
			}

			val gameStats = gameDocument.getElementsByClass("game-stat")

			val playing = gameStats[0].getElementsByClass("text-lead").text()
			val visits = gameStats[1].getElementsByClass("text-lead").text()
			val created = gameStats[2].getElementsByClass("text-lead").text()
			val updated = gameStats[3].getElementsByClass("text-lead").text()
			val maxplayers = gameStats[4].getElementsByClass("text-lead").text()
			val genre = gameStats[5].getElementsByClass("text-lead").text()
			val allowedgear = gameStats[6].getElementsByClass("text-lead").text()

			val voteBody = HttpRequest.get("https://www.roblox.com/games/votingservice/$placeId")
					.body()

			val voteDocument = Jsoup.parse(voteBody)

			val voteSection = voteDocument.getElementById("voting-section")
			val upvotes = voteSection.attr("data-total-up-votes")
			val downvotes = voteSection.attr("data-total-down-votes")

			embed.setTitle("<:roblox_logo:412576693803286528> $gameName", gameUrl)
			embed.addField("\uD83D\uDCBB ${locale["RBUSER_ID_DO_ROBLOX"]}", placeId, true)
			embed.addField("<:starstruck:540988091117076481> ${locale["RBGAME_Favorites"]}", favoriteCount, true)
			embed.addField("\uD83D\uDC4D ${locale["MUSICINFO_LIKES"]}", upvotes, true)
			embed.addField("\uD83D\uDC4E ${locale["MUSICINFO_DISLIKES"]}", downvotes, true)
			embed.addField("\uD83C\uDFAE ${locale["RBGAME_Playing"]}", playing, true)
			embed.addField("\uD83D\uDC3E ${locale["RBUSER_PLACE_VISITS"]}", visits, true)
			embed.addField("\uD83C\uDF1F ${locale["SERVERINFO_CREATED_IN"]}", created, true)
			embed.addField("✨ ${locale["RBGAME_LastUpdate"]}", updated, true)
			embed.addField("⛔ ${locale["RBGAME_MaxPlayers"]}", maxplayers, true)
			embed.addField("\uD83C\uDFB2 ${locale["RBGAME_Genre"]}", genre, true)

			embed.setAuthor(gameAuthor)
			embed.setDescription(gameDescription.substringIfNeeded(0 until 250))

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			context.explain()
		}
	}
}