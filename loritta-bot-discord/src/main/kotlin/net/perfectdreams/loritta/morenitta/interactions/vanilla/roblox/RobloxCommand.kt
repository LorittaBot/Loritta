package net.perfectdreams.loritta.morenitta.interactions.vanilla.roblox

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.getResizedInstance
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import org.jsoup.Jsoup
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

class RobloxCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Roblox
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Roblox
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */, CommandCategory.ROBLOX) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)


        subcommand(I18N_PREFIX.User.Label, I18N_PREFIX.User.Description) {
            alternativeLegacyAbsoluteCommandPaths.add("rbuser")

            executor = RobloxUserExecutor()
        }

        subcommand(I18N_PREFIX.Game.Label, I18N_PREFIX.Game.Description) {
            alternativeLegacyAbsoluteCommandPaths.add("rbgame")


            executor = RobloxGameExecutor()
        }
    }

    inner class RobloxUserExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val username = string("username", I18N_PREFIX.User.Options.Username.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val username = args[options.username]
            val userProfileRequest = loritta.http.get("https://www.roblox.com/users/profile") {
                parameter("username", username)
            }

            // Unknown user
            if (userProfileRequest.status == HttpStatusCode.NotFound)
                context.fail(false) {
                    styled(
                        context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.UnknownPlayer(username)),
                        Emotes.Error
                    )
                }

            val userId = userProfileRequest.request.url.toString().split("/").getOrNull(4)
                ?.toLongOrNull() ?: error("Couldn't find the User ID") // This should never happen unless if Roblox changes where the ID is in the URL

            val body = userProfileRequest.bodyAsText()

            val userProfileDocument = Jsoup.parse(body)

            // Yes this is correct, it can be null, but for some reason it complains that it can never be not null
            val isRobloxPremium = userProfileDocument.select(".header-title .icon-premium-medium").isNotEmpty()
            // The favoriteGamesContainer *can* be null if the user doesn't has favorite games
            val favoriteGamesContainer = userProfileDocument.select(".favorite-games-container").firstOrNull() // If null, then the user doesn't have Premium (oof)
            val profileHeader = userProfileDocument.select("[profile-header-data]").first()!!

            val friendsCount = profileHeader.attr("data-friendscount")
            val followersCount = profileHeader.attr("data-followerscount")
            val followingsCount = profileHeader.attr("data-followingscount")

            val avatarBodyImageUrlJob = GlobalScope.async {
                Jsoup.parse(
                    loritta.http.get("https://www.roblox.com/thumbnail/user-avatar") {
                        parameter("userId", userId)
                        parameter("thumbnailFormatId", 124)
                        parameter("width", 300)
                        parameter("height", 300)
                    }.bodyAsText()
                ).getElementsByTag("img").first()!!.attr("src")
            }

            val userDataJob = GlobalScope.async {
                JsonIgnoreUnknownKeys.decodeFromString<RobloxUserResponse>(
                    loritta.http.get("https://users.roblox.com/v1/users/$userId").bodyAsText()
                )
            }

            val userBadgesJob = GlobalScope.async {
                JsonIgnoreUnknownKeys.decodeFromString<List<RobloxBadge>>(
                    loritta.http.get("https://accountinformation.roblox.com/v1/users/$userId/roblox-badges").bodyAsText()
                )
            }

            val userFriendsJob = GlobalScope.async {
                JsonIgnoreUnknownKeys.decodeFromString<RobloxFriendsResponse>(
                    loritta.http.get("https://friends.roblox.com/v1/users/$userId/friends").bodyAsText()
                )
            }

            val userCollectionsJob = GlobalScope.async {
                JsonIgnoreUnknownKeys.decodeFromString<CollectionsItemsResponse>(
                    loritta.http.get("https://www.roblox.com/users/profile/robloxcollections-json") {
                        parameter("userId", userId)
                    }.bodyAsText()
                )
            }

            val userAssetsJob = GlobalScope.async {
                JsonIgnoreUnknownKeys.decodeFromString<PlayerAssetsResponse>(
                    loritta.http.get("https://www.roblox.com/users/profile/playerassets-json?assetTypeId=21&userId=$userId") {
                        parameter("assetTypeId", "21")
                        parameter("userId", userId)
                    }.bodyAsText()
                )
            }

            val avatarBodyImageUrl = avatarBodyImageUrlJob.await()
            val userData = userDataJob.await()
            val userCollections = userCollectionsJob.await()
            val userBadges = userBadgesJob.await()
            val userAssets = userAssetsJob.await()

            val bufferedImage = BufferedImage(333, 165, BufferedImage.TYPE_INT_ARGB)
            val graphics = bufferedImage.createGraphics()

            run {
                var x = 0
                val y = 0

                val jobs = userCollections.collectionItems.mapNotNull {
                    if (x > 275) // Break, the list is too big
                        return@mapNotNull null

                    val realX = x
                    val realY = y

                    val async = GlobalScope.async {
                        val thumbnail = ImageIO.read(URL(it.thumbnail.url)).getResizedInstance(55, 55, InterpolationType.BILINEAR)
                        graphics.drawImage(thumbnail, realX, realY, null)
                    }

                    x += 55

                    async
                }

                // Wait until all images are downloaded and drawn
                jobs.awaitAll()
            }

            run {
                var x = 0
                val y = 55

                val jobs = userBadges.mapNotNull {
                    if (x > 275) // Break, the list is too big
                        return@mapNotNull null

                    val realX = x
                    val realY = y

                    val async = GlobalScope.async {
                        val thumbnail = ImageIO.read(URL(it.imageUrl)).getResizedInstance(55, 55, InterpolationType.BILINEAR)
                        graphics.drawImage(thumbnail, realX, realY, null)
                    }

                    x += 55

                    async
                }

                // Wait until all images are downloaded and drawn
                jobs.awaitAll()
            }

            run {
                var x = 0
                val y = 110

                val jobs = userAssets.assets.mapNotNull {
                    if (x > 275) // Break, the list is too big
                        return@mapNotNull null

                    val realX = x
                    val realY = y

                    val async = GlobalScope.async {
                        val thumbnail = ImageIO.read(URL(it.thumbnail.url)).getResizedInstance(55, 55, InterpolationType.BILINEAR)
                        graphics.drawImage(thumbnail, realX, realY, null)
                    }

                    x += 55

                    async
                }

                // Wait until all images are downloaded and drawn
                jobs.awaitAll()
            }

            val baos = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "png", baos)
            val bais = baos.toByteArray().inputStream()

            context.reply(false) {
                embed {
                    title = buildString {
                        this.append(Emotes.Roblox.toString())
                        if (isRobloxPremium) {
                            this.append(" ")
                            this.append(Emotes.RobloxPremium)
                        }
                        this.append(" ")
                        this.append(userData.name)
                    }
                    url = "https://roblox.com/users/${userId}/profile"

                    if (userData.description.isNotBlank()) {
                        description = userData.description
                    }

                    field("${Emotes.LoriId} ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.UserId)}", "`$userId`", true)
                    field("${Emotes.LoriCalendar} ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.JoinDate)}", "<t:${userData.created.epochSeconds}:F>", true)
                    field(
                        "\uD83D\uDE4B ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.Social)}",
                        """🐾 **${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.Following)}:** $followingsCount
                        |🤩 **${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.Followers)}:** $followersCount
                        |😎 **${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.Friends)}:** $friendsCount
                    """.trimMargin(),
                        true
                    )

                    // Favorite Games
                    if (favoriteGamesContainer != null) {
                        val builder = buildString {
                            favoriteGamesContainer.getElementsByClass("game-card-link").forEach {
                                val gameLink = it.attr("href")
                                val title = it.getElementsByClass("game-name-title")
                                    .first()!!
                                    .attr("title")

                                // This is a "hack" because Roblox loves adding dumb stuff that doesn't really matter
                                // Example: https://www.roblox.com/games/refer?SortFilter=5&PlaceId=31610786&Position=1&SortPosition=1&PageId=703dbc9a-117a-4742-ae56-cd9d63d6be9a&PageType=Profile
                                // We can reduce that to just https://roblox.com/games/31610786
                                val placeId = gameLink.parseUrlEncodedParameters(Charsets.UTF_8)["PlaceId"]

                                append("[$title](https://roblox.com/games/$placeId)\n")
                            }
                        }

                        // The user may not have any favorite games!
                        if (builder.isNotBlank())
                            field("\uD83D\uDD79️ ${context.i18nContext.get(RobloxCommand.I18N_PREFIX.User.FavoriteGames)}", builder, false)
                    }


                    thumbnail = avatarBodyImageUrl
                    image = "attachment://roblox.png"

                    color = LorittaColors.RobloxRed.rgb
                }

                files += FileUpload.fromData(bais, "roblox.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (context.args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.username to args.joinToString(separator = " ")
            )
        }
    }

    inner class RobloxGameExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val game = string("game", I18N_PREFIX.Game.Options.Game.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val searchParameter = args[options.game]
            val gameListRequest = loritta.http.get("https://games.roblox.com/v1/games/list") {
                parameter("model.keyword", searchParameter)
                parameter("model.startRows", 0)
                parameter("model.maxRows", 1)
            }

            val gamesResponse = JsonIgnoreUnknownKeys.decodeFromString<RobloxGamesResponse>(gameListRequest.bodyAsText())
            val games = gamesResponse.games

            // No games found that match the request
            if (games.isEmpty())
                context.fail(false) {
                    styled(
                        content = context.i18nContext.get(RobloxCommand.I18N_PREFIX.Game.UnknownGame(searchParameter)),
                        prefix = Emotes.Error
                    )
                }

            val game = games.first()
            val gameId = game.placeId
            val universeId = game.universeId

            // Now we will request the game page, because the game page has some nifty info that we don't have by querying it via the games list endpoint
            val gamePageRequest = loritta.http.get("https://www.roblox.com/games/$gameId")
            val gameDocument = Jsoup.parse(gamePageRequest.bodyAsText())

            val placeId = gameDocument.getElementById("game-detail-page")?.attr("data-place-id")!!
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
                loritta.http.get("https://games.roblox.com/v2/games/$universeId/media")
                    .bodyAsText()
            )

            val firstMediaInfo = mediaResponse.data.firstOrNull {
                it.approved && it.assetTypeId == 1 /* 1 = image */ && it.imageId != null
            }

            val thumbnail = if (firstMediaInfo != null) {
                JsonIgnoreUnknownKeys.decodeFromString<RobloxBatchAssetResponse>(
                    loritta.http.post("https://thumbnails.roblox.com/v1/batch") {
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

            context.reply(false) {
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

                    color = LorittaColors.RobloxRed.rgb
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (context.args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.game to args.joinToString(separator = " ")
            )
        }
    }

    // Example data: {"description":"####### ######## ####################################################### Brasil!","created":"2013-01-22T11:00:23.88Z","isBanned":false,"id":37271405,"name":"SonicteamPower","displayName":"SonicteamPower"}
    @Serializable
    data class RobloxUserResponse(
        val description: String,
        val created: Instant,
        val isBanned: Boolean,
        val id: Long,
        val name: String,
        val displayName: String,
        val externalAppDisplayName: String?,
        val hasVerifiedBadge: Boolean
    )

    // [{"id":2,"name":"Friendship","description":"This badge is given to players who have embraced the Roblox community and have made at least 20 friends. People who have this badge are good people to know and can probably help you out if you are having trouble.","imageUrl":"https://images.rbxcdn.com/5eb20917cf530583e2641c0e1f7ba95e.png"},{"id":12,"name":"Veteran","description":"This badge recognizes members who have played Roblox for one year or more. They are stalwart community members who have stuck with us over countless releases, and have helped shape Roblox into the game that it is today. These medalists are the true steel, the core of the Robloxian history ... and its future.","imageUrl":"https://images.rbxcdn.com/b7e6cabb5a1600d813f5843f37181fa3.png"},{"id":6,"name":"Homestead","description":"The homestead badge is earned by having your personal place visited 100 times. Players who achieve this have demonstrated their ability to build cool things that other Robloxians were interested enough in to check out. Get a jump-start on earning this reward by inviting people to come visit your place.","imageUrl":"https://images.rbxcdn.com/b66bc601e2256546c5dd6188fce7a8d1.png"},{"id":7,"name":"Bricksmith","description":"The Bricksmith badge is earned by having a popular personal place. Once your place has been visited 1000 times, you will receive this award. Robloxians with Bricksmith badges are accomplished builders who were able to create a place that people wanted to explore a thousand times. They no doubt know a thing or two about putting bricks together.","imageUrl":"https://images.rbxcdn.com/49f3d30f5c16a1c25ea0f97ea8ef150e.png"},{"id":18,"name":"Welcome To The Club","description":"This badge is awarded to players who have ever belonged to the illustrious Builders Club. These players are part of a long tradition of Roblox greatness.","imageUrl":"https://images.rbxcdn.com/6c2a598114231066a386fa716ac099c4.png"}]
    @Serializable
    data class RobloxBadge(
        val id: Long,
        val name: String,
        val description: String,
        val imageUrl: String
    )

    // {"data":[{"isOnline":false,"presenceType":0,"isDeleted":false,"friendFrequentScore":0,"friendFrequentRank":1,"description":null,"created":"0001-01-01T06:00:00Z","isBanned":false,"externalAppDisplayName":null,"id":610201316,"name":"KaikeCarlos1","displayName":"KaikeCarlos1"},{"isOnline":false,"presenceType":0,"isDeleted":false,"friendFrequentScore":0,"friendFrequentRank":1,"description":null,"created":"0001-01-01T06:00:00Z","isBanned":false,"externalAppDisplayName":null,"id":2490318475,"name":"Podeuso","displayName":"Podeuso"},{"isOnline":false,"presenceType":0,"isDeleted":false,"friendFrequentScore":0,"friendFrequentRank":1,"description":null,"created":"0001-01-01T06:00:00Z","isBanned":false,"externalAppDisplayName":null,"id":93959327,"name":"Rickinho3GamerBR","displayName":"Rick3"},{"isOnline":false,"presenceType":0,"isDeleted":false,"friendFrequentScore":0,"friendFrequentRank":1,"description":null,"created":"0001-01-01T06:00:00Z","isBanned":false,"externalAppDisplayName":null,"id":2435130932,"name":"srtabread","displayName":"srtabread"}]}
    @Serializable
    data class RobloxFriendsResponse(
        val data: List<RobloxFriend>
    )

    @Serializable
    data class RobloxFriend(
        val isOnline: Boolean,
        val presenceType: Int,
        val isDeleted: Boolean,
        val friendFrequentScore: Int,
        val friendFrequentRank: Int,
        val description: String?,
        val created: String,
        val isBanned: Boolean,
        val externalAppDisplayName: String?,
        val id: Long,
        val name: String,
        val displayName: String
    )

    @Serializable
    data class CollectionsItemsResponse(
        @SerialName("CollectionsItems")
        val collectionItems: List<Asset>
    )

    @Serializable
    data class Asset(
        @SerialName("Id")
        val id: Long,
        @SerialName("AssetSeoUrl")
        val assetSeoUrl: String,
        @SerialName("Thumbnail")
        val thumbnail: Thumbnail,
        @SerialName("Name")
        val name: String,
        @SerialName("FormatName")
        val formatName: String?,
        @SerialName("Description")
        val description: String?,
        @SerialName("AssetRestrictionIcon")
        val assetRestrictionIcon: AssetRestrictionIcon?,
        @SerialName("HasPremiumBenefit")
        val hasPremiumBenefit: Boolean
    )

    @Serializable
    data class Thumbnail(
        @SerialName("Final")
        val final: Boolean,
        @SerialName("Url")
        val url: String,
        @SerialName("RetryUrl")
        val retryUrl: String?,
        @SerialName("UserId")
        val userId: Long,
        @SerialName("EndpointType")
        val endpointType: String?,
    )

    @Serializable
    data class AssetRestrictionIcon(
        @SerialName("TooltipText")
        val tooltipText: String?,
        @SerialName("CssTag")
        val cssTag: String?,
        @SerialName("LoadAssetRestrictionIconCss")
        val loadAssetRestrictionIconCss: Boolean,
        @SerialName("HasTooltip")
        val hasTooltip: Boolean,
    )

    @Serializable
    data class PlayerAssetsResponse(
        @SerialName("Title")
        val title: String,
        @SerialName("Label")
        val label: String,
        @SerialName("ModalAssetViewType")
        val modalAssetViewType: Int,
        @SerialName("MaxNumberOfVisibleAssets")
        val maxNumberOfVisibleAssets: Int,
        @SerialName("Assets")
        val assets: List<Asset>,
        @SerialName("UserId")
        val userId: Long,
        @SerialName("IsSeeAllHeaderButtonVisible")
        val isSeeAllHeaderButtonVisible: Boolean,
        @SerialName("AssetTypeInventoryUrl")
        val assetTypeInventoryUrl: String,
        @SerialName("ProfileLangResources")
        val profileLangResources: JsonObject
    )

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