package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox.declarations.RobloxCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import org.jsoup.Jsoup

class RobloxGameExecutor(loritta: LorittaCinnamon, val http: HttpClient) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val game = string("game", RobloxCommand.I18N_PREFIX.Game.Options.Game.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val searchParameter = args[options.game]
        val gameListRequest = http.get("https://games.roblox.com/v1/games/list") {
            parameter("model.keyword", searchParameter)
            parameter("model.startRows", 0)
            parameter("model.maxRows", 1)
        }

        val gamesResponse = JsonIgnoreUnknownKeys.decodeFromString<RobloxGamesResponse>(gameListRequest.bodyAsText())
        val games = gamesResponse.games

        // No games found that match the request
        if (games.isEmpty())
            context.fail(
                prefix = Emotes.Error,
                content = context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.UnknownGame(searchParameter))
            )

        val game = games.first()
        val gameId = game.placeId
        val universeId = game.universeId

        // Now we will request the game page, because the game page has some nifty info that we don't have by querying it via the games list endpoint
        val gamePageRequest = http.get("https://www.roblox.com/games/$gameId")
        val gameDocument = Jsoup.parse(gamePageRequest.bodyAsText())

        val placeId = gameDocument.getElementById("game-detail-page").attr("data-place-id")
        val gameName = gameDocument.getElementsByClass("game-name").text()
        val gameAuthor = gameDocument.getElementsByClass("game-creator")[0].getElementsByClass("text-name").text()
        val gameDescription = gameDocument.getElementsByClass("game-description")[0].text()
        val favoriteCount = gameDocument.getElementsByClass("game-favorite-count")[0].text()
        val gameStats = gameDocument.getElementsByClass("game-stat")

        val playing = gameStats[0].getElementsByClass("text-lead").text()
        // val favoriteCountFromPage = gameStats[1].getElementsByClass("text-lead").text()
        val visits = gameStats[2].getElementsByClass("text-lead").text()
        val created = gameStats[3].getElementsByClass("text-lead").text()
        val updated = gameStats[4].getElementsByClass("text-lead").text()
        val maxplayers = gameStats[5].getElementsByClass("text-lead").text()
        val genre = gameStats[6].getElementsByClass("text-lead").text()
        val upvotes = game.totalUpVotes
        val downvotes = game.totalDownVotes

        val mediaResponse = JsonIgnoreUnknownKeys.decodeFromString<RobloxMediaResponse>(
            http.get("https://games.roblox.com/v2/games/$universeId/media")
                .bodyAsText()
        )

        val firstMediaInfo = mediaResponse.data.firstOrNull {
            it.approved && it.assetTypeId == 1 /* 1 = image */ && it.imageId != null
        }

        val thumbnail = if (firstMediaInfo != null) {
            JsonIgnoreUnknownKeys.decodeFromString<RobloxBatchAssetResponse>(
                http.post("https://thumbnails.roblox.com/v1/batch") {
                    setBody(
                        TextContent(
                            Json.encodeToString(
                                listOf(
                                    RobloxAssetRequest(
                                        // Seems to a merge of all requests
                                        // And yes, this is required
                                        "${firstMediaInfo.imageId}::Asset:768x432:png:regular",
                                        "Asset",
                                        firstMediaInfo.imageId!!, // Should NEVER be null here!
                                        "",
                                        "png",
                                        "768x432"
                                    )
                                )
                            ),
                            ContentType.Application.Json
                        )
                    )
                }.bodyAsText()
            ).data.firstOrNull()?.imageUrl
        } else null

        context.sendMessage {
            embed {
                author(gameAuthor)
                title = "${Emotes.Roblox} $gameName"
                url = "https://www.roblox.com/games/$gameId"
                description = gameDescription.shortenWithEllipsis(1_000)
                field("${Emotes.LoriId} ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.PlaceId)}", placeId, true)
                field("\uD83E\uDD29 ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.Favorite)}", favoriteCount, true)
                field("\uD83D\uDC4D ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.Likes)}", upvotes.toString(), true)
                field("\uD83D\uDC4E ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.Dislikes)}", downvotes.toString(), true)
                field("\uD83C\uDFAE ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.Playing)}", playing, true)
                field("\uD83D\uDC3E ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.Visits)}", visits, true)
                field("\uD83C\uDF1F ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.CreatedAt)}", created, true)
                field("✨ ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.LastUpdated)}", updated, true)
                field("⛔ ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.MaxPlayers)}", maxplayers, true)
                field("${Emotes.LoriGameDie} ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.Genre)}", genre, true)

                image = thumbnail

                color = LorittaColors.RobloxRed.toKordColor()
            }
        }
    }

    // {"games":[{"creatorId":53537032,"creatorName":"Aesthetical","creatorType":"User","totalUpVotes":1098157,"totalDownVotes":179935,"universeId":73885730,"name":"Prison Life (Cars fixed!)","placeId":155615604,"playerCount":1561,"imageToken":"T_155615604_e166","isSponsored":false,"nativeAdData":"","isShowSponsoredLabel":false,"price":null,"analyticsIdentifier":null,"gameDescription":"Alright, I fixed the steering for the cars. In addition, driving cars for mobile should work now.\r\n\r\nSorry guys for the lack of updates. I started my first year of university in 2017, and while it's been an amazing experience, that meant that I didn't have much time to spend working on the game. Thankfully, I have been getting more time off recently, so now I have more time to spend developing. I'll make an announcement on my next project when the time comes. Thank you all for being dedicated fans of Prison Life!","genre":""}],"suggestedKeyword":null,"correctedKeyword":null,"filteredKeyword":null,"hasMoreRows":true,"nextPageExclusiveStartId":null,"featuredSearchUniverseId":null,"emphasis":false,"cutOffIndex":null,"algorithm":"GameSearchUsingSimilarQueryService","algorithmQueryType":"Bucketboost","suggestionAlgorithm":"GameSuggestions_V2","relatedGames":[],"esDebugInfo":null}
    @Serializable
    data class RobloxGamesResponse(
        val games: List<RobloxGame>,
        val suggestedKeyword: String?,
        val correctedKeyword: String?,
        val filteredKeyword: String?,
        val hasMoreRows: Boolean,
        val nextPageExclusiveStartId: String?,
        val featuredSearchUniverseId: String?,
        val emphasis: Boolean,
        val cutOffIndex: String?,
        val algorithm: String,
        val algorithmQueryType: String,
        val suggestionAlgorithm: String,
        val relatedGames: List<RobloxGame>,
        val esDebugInfo: String?
    )

    @Serializable
    data class RobloxGame(
        val creatorId: Long,
        val creatorName: String,
        val creatorType: String,
        val totalUpVotes: Long,
        val totalDownVotes: Long,
        val universeId: Long,
        val name: String,
        val placeId: Long,
        val playerCount: Long,
        val imageToken: String,
        val isSponsored: Boolean,
        val nativeAdData: String,
        val isShowSponsoredLabel: Boolean,
        val price: Long?,
        val analyticsIdentifier: String?,
        val gameDescription: String,
        val genre: String
    )

    @Serializable
    data class RobloxAssetRequest(
        val requestId: String,
        val type: String,
        val targetId: Long,
        val token: String,
        val format: String,
        val size: String
    )

    @Serializable
    data class RobloxBatchAssetResponse(
        val data: List<RobloxBatchAssetItem>
    )

    @Serializable
    data class RobloxBatchAssetItem(
        val requestId: String,
        val errorCode: Int,
        val errorMessage: String,
        val targetId: Long,
        val state: String,
        val imageUrl: String
    )

    @Serializable
    data class RobloxMediaResponse(
        val data: List<RobloxMediaItem>
    )

    @Serializable
    data class RobloxMediaItem(
        val assetTypeId: Int,
        val assetType: String,
        val imageId: Long?,
        val videoHash: String?,
        val videoTitle: String?,
        val approved: Boolean,
        val altText: String?
    )
}