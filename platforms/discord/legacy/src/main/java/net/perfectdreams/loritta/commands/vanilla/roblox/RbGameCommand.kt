package net.perfectdreams.loritta.commands.vanilla.roblox

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import org.jsoup.Jsoup
import java.time.Instant
import java.time.format.DateTimeFormatter

class RbGameCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("rbgame", "rbjogo", "rbgameinfo"), CommandCategory.ROBLOX) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.rbgame"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val context = this

            if (context.args.isNotEmpty()) {
                val query = context.args.joinToString(" ")

                val url = "https://games.roblox.com/v1/games/list?model.keyword=${query.encodeToUrl()}&model.startRows=0&model.maxRows=40"

                val body = HttpRequest.get(url)
                    .body()

                val json = JsonParser.parseString(body)
                val res = json.obj["games"].array

                if (res.size() == 0) {
                    context.reply(
                        LorittaReply(
                            message = locale["$LOCALE_PREFIX.couldntFind", query],
                            prefix = Constants.ERROR
                        )
                    )
                    return@executesDiscord
                }

                val gameUrl = "https://www.roblox.com/games/refer?IsLargeGameTile=true&LocalTimestamp=${
                    DateTimeFormatter.ISO_INSTANT.format(
                        Instant.now()).encodeToUrl()}&PageType=GameSearch&PlaceId=${res[0]["placeId"]}&Position=1"

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
                // val favoriteCountFromPage = gameStats[1].getElementsByClass("text-lead").text()
                val visits = gameStats[2].getElementsByClass("text-lead").text()
                val created = gameStats[3].getElementsByClass("text-lead").text()
                val updated = gameStats[4].getElementsByClass("text-lead").text()
                val maxplayers = gameStats[5].getElementsByClass("text-lead").text()
                val genre = gameStats[6].getElementsByClass("text-lead").text()
                // val allowedgear = gameStats[7].getElementsByClass("text-lead").text()

                val voteBody = HttpRequest.get("https://www.roblox.com/games/votingservice/$placeId")
                    .body()

                val voteDocument = Jsoup.parse(voteBody)

                val voteSection = voteDocument.getElementById("voting-section")
                val upvotes = voteSection.attr("data-total-up-votes")
                val downvotes = voteSection.attr("data-total-down-votes")

                embed.setTitle("<:roblox_logo:412576693803286528> $gameName", gameUrl)
                embed.addField("\uD83D\uDCBB ${locale["commands.command.rbuser.robloxId"]}", placeId, true)
                embed.addField("<:starstruck:540988091117076481> ${locale["$LOCALE_PREFIX.favorites"]}", favoriteCount, true)
                embed.addField("\uD83D\uDC4D ${locale["$LOCALE_PREFIX.likes"]}", upvotes, true)
                embed.addField("\uD83D\uDC4E ${locale["$LOCALE_PREFIX.dislikes"]}", downvotes, true)
                embed.addField("\uD83C\uDFAE ${locale["$LOCALE_PREFIX.playing"]}", playing, true)
                embed.addField("\uD83D\uDC3E ${locale["commands.command.rbuser.visits"]}", visits, true)
                embed.addField("\uD83C\uDF1F ${locale["$LOCALE_PREFIX.createdAt"]}", created, true)
                embed.addField("✨ ${locale["$LOCALE_PREFIX.lastUpdated"]}", updated, true)
                embed.addField("⛔ ${locale["$LOCALE_PREFIX.maxPlayers"]}", maxplayers, true)
                embed.addField("\uD83C\uDFB2 ${locale["$LOCALE_PREFIX.genre"]}", genre, true)

                embed.setAuthor(gameAuthor)
                embed.setDescription(gameDescription.substringIfNeeded(0 until 250))

                context.sendMessage(context.getUserMention(true), embed.build())
            } else {
                context.explain()
            }
        }
    }
}