package net.perfectdreams.loritta.commands.vanilla.roblox

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.time.Instant
import java.time.format.DateTimeFormatter

class RbUserCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta,listOf("rbuser", "rbplayer"), CommandCategory.ROBLOX) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.rbuser"
        private val json = Json {
            ignoreUnknownKeys = true
        }

        // Example data: {"description":"####### ######## ####################################################### Brasil!","created":"2013-01-22T11:00:23.88Z","isBanned":false,"id":37271405,"name":"SonicteamPower","displayName":"SonicteamPower"}
        @Serializable
        data class RobloxUserResponse(
            val description: String,
            val created: String,
            val isBanned: Boolean,
            val id: Long,
            val name: String,
            val displayName: String
        )

        // [{"id":2,"name":"Friendship","description":"This badge is given to players who have embraced the Roblox community and have made at least 20 friends. People who have this badge are good people to know and can probably help you out if you are having trouble.","imageUrl":"https://images.rbxcdn.com/5eb20917cf530583e2641c0e1f7ba95e.png"},{"id":12,"name":"Veteran","description":"This badge recognizes members who have played Roblox for one year or more. They are stalwart community members who have stuck with us over countless releases, and have helped shape Roblox into the game that it is today. These medalists are the true steel, the core of the Robloxian history ... and its future.","imageUrl":"https://images.rbxcdn.com/b7e6cabb5a1600d813f5843f37181fa3.png"},{"id":6,"name":"Homestead","description":"The homestead badge is earned by having your personal place visited 100 times. Players who achieve this have demonstrated their ability to build cool things that other Robloxians were interested enough in to check out. Get a jump-start on earning this reward by inviting people to come visit your place.","imageUrl":"https://images.rbxcdn.com/b66bc601e2256546c5dd6188fce7a8d1.png"},{"id":7,"name":"Bricksmith","description":"The Bricksmith badge is earned by having a popular personal place. Once your place has been visited 1000 times, you will receive this award. Robloxians with Bricksmith badges are accomplished builders who were able to create a place that people wanted to explore a thousand times. They no doubt know a thing or two about putting bricks together.","imageUrl":"https://images.rbxcdn.com/49f3d30f5c16a1c25ea0f97ea8ef150e.png"},{"id":18,"name":"Welcome To The Club","description":"This badge is awarded to players who have ever belonged to the illustrious Builders Club. These players are part of a long tradition of Roblox greatness.","imageUrl":"https://images.rbxcdn.com/6c2a598114231066a386fa716ac099c4.png"}]
        @Serializable
        data class RobloxBadge(
            val id: Long,
            val name: String,
            val description: String,
            val imageUrl: String
        )
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")
		
		localizedExamples("$LOCALE_PREFIX.examples")

        usage {
            argument(ArgumentType.TEXT) {
                optional = false
            }
        }

        executesDiscord {
            val context = this

            if (context.args.isNotEmpty()) {
                val username = context.args.joinToString(separator = " ")

                var userId: Long? = null
                var isRobloxPremium = false
                var favoriteGamesContainer: Element? = null

                val altRobloxQuery = HttpRequest.get("https://www.roblox.com/users/profile?username=${URLEncoder.encode(username, "UTF-8")}")

                val pageStatusCode = altRobloxQuery.code()

                if (pageStatusCode != 404) {
                    userId = altRobloxQuery.url().path.split("/")[2].toLong()
                    val document = Jsoup.parse(altRobloxQuery.body())
                    // If null, then the user doesn't have Premium (oof)
                    isRobloxPremium = document.selectFirst(".header-title .icon-premium-medium") != null
                    favoriteGamesContainer = document.getElementsByClass("favorite-games-container").firstOrNull()
                }

                // The favoriteGamesContainer *can* be null if the user doesn't has favorite games
                if (userId == null) {
                    context.sendMessage(Constants.ERROR + " **|** " + locale["$LOCALE_PREFIX.couldntFind", username] + " \uD83D\uDE22")
                    return@executesDiscord
                }

                val avatarBodyTask = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    HttpRequest.get("https://www.roblox.com/thumbnail/user-avatar?userId=$userId&thumbnailFormatId=124&width=300&height=300")
                        .body()
                }
                val usersApiRequest = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    com.mrpowergamerbr.loritta.utils.loritta.http.get<String>("https://users.roblox.com/v1/users/$userId")
                }
                val robloxBadgesRequest = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    com.mrpowergamerbr.loritta.utils.loritta.http.get<String>("https://accountinformation.roblox.com/v1/users/$userId/roblox-badges")
                }
                val followingBodyTask = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=Following&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
                        .body()
                }
                val followersBodyTask = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=Followers&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
                        .body()
                }
                val friendsBodyTask = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=AllFriends&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
                        .body()
                }

                var bufferedImage = BufferedImage(333, 220, BufferedImage.TYPE_INT_ARGB)

                val collections = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    var x = 0
                    val y = 0

                    val robloxCollectionsResponse = HttpRequest.get("https://www.roblox.com/users/profile/robloxcollections-json?userId=$userId")
                        .body()

                    val robloxCollections = JsonParser.parseString(robloxCollectionsResponse).obj

                    val entries = robloxCollections["CollectionsItems"].array.map {
                        if (x > 275) // Break, the list is too big
                            return@async

                        val realX = x
                        val realY = y

                        val async = async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                            val thumbnailUrl = it["Thumbnail"]["Url"].string

                            val thumbnail = LorittaUtils.downloadImage(thumbnailUrl)?.getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH)
                                ?: return@async null
                            bufferedImage.graphics.drawImage(thumbnail, realX, realY, null)
                        }

                        x += 55

                        return@map async
                    }

                    entries.awaitAll()
                }

                val robloxBadges = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    var x = 0
                    val y = 55

                    val robloxBadgesResponse = robloxBadgesRequest.await()
                    val robloxBadges = json.decodeFromString<List<RobloxBadge>>(robloxBadgesResponse)

                    val entries = robloxBadges.map {
                        if (x > 275) // Break, the list is too big
                            return@async

                        val realX = x
                        val realY = y

                        val async = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                            val thumbnailUrl = it.imageUrl

                            val thumbnail = LorittaUtils.downloadImage(thumbnailUrl)?.getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH)
                                ?: return@async null
                            bufferedImage.graphics.drawImage(thumbnail, realX, realY, null)
                        }

                        x += 55

                        return@map async
                    }

                    entries.awaitAll()
                }

                val playerAssets = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                    var x = 0
                    val y = 110

                    val robloxCollectionsResponse = HttpRequest.get("https://www.roblox.com/users/profile/playerassets-json?assetTypeId=21&userId=$userId")
                        .body()

                    val robloxCollections = JsonParser.parseString(robloxCollectionsResponse).obj

                    val entries = robloxCollections["Assets"].array.map {
                        if (x > 275) // Break, the list is too big
                            return@async

                        val realX = x
                        val realY = y

                        val async = GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
                            val thumbnailUrl = it["Thumbnail"]["Url"].string

                            val thumbnail = LorittaUtils.downloadImage(thumbnailUrl)?.getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH)
                                ?: return@async null
                            bufferedImage.graphics.drawImage(thumbnail, realX, realY, null)
                        }

                        x += 55

                        return@map async
                    }

                    entries.awaitAll()
                }

                collections.await()
                robloxBadges.await()
                playerAssets.await()

                val usersApiResponse = usersApiRequest.await()
                val robloxUserResponse = json.decodeFromString<RobloxUserResponse>(usersApiResponse)

                bufferedImage = bufferedImage.getSubimage(0, 0, 333, 110 + 55)

                val avatarResponse = Jsoup.parse(avatarBodyTask.await())

                // The avatar is the first img tag in the page
                val avatar = avatarResponse.getElementsByTag("img").first().attr("src")

                // Convert the date to a Instant
                // Roblox's dates are in ISO format: "2013-01-22T11:00:23.88Z"
                val joinDateAsInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(robloxUserResponse.created))
                val joinDateAsEpochMilli = joinDateAsInstant.toEpochMilli()

                // SEGUINDO
                val followingResponse = JsonParser.parseString(followingBodyTask.await())

                val totalFollowing = followingResponse["TotalFriends"].int

                // SEGUIDORES
                val followersResponse = JsonParser.parseString(followersBodyTask.await())

                val totalFollowers = followersResponse["TotalFriends"].int

                // AMIGOS
                val friendsResponse = JsonParser.parseString(friendsBodyTask.await())

                val totalFriends = friendsResponse["TotalFriends"].int

                val embed = EmbedBuilder().apply {
                    setTitle("<:roblox_logo:412576693803286528> ${if (isRobloxPremium) (Emotes.ROBLOX_PREMIUM.toString() + " ") else ""}${robloxUserResponse.name}", "https://roblox.com/users/${userId}/profile")
                    if (robloxUserResponse.description.isNotBlank()) {
                        setDescription(robloxUserResponse.description)
                    }
                    setColor(Constants.ROBLOX_RED)
                    addField("\uD83D\uDCBB ${locale["$LOCALE_PREFIX.robloxId"]}", userId.toString(), true)
                    addField("\uD83D\uDCC5 ${locale["$LOCALE_PREFIX.joinDate"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(joinDateAsEpochMilli, locale), true)
                    // addField("\uD83D\uDC40 ${locale["$LOCALE_PREFIX.placeVisits"]}", placeVisits, true)
                    addField("\uD83D\uDE4B ${locale["$LOCALE_PREFIX.social"]}", "**\uD83D\uDC3E ${locale["$LOCALE_PREFIX.following"]}**: $totalFollowing\n**<:starstruck:540988091117076481> ${locale["$LOCALE_PREFIX.followers"]}**: $totalFollowers\n**\uD83D\uDE0E ${locale["$LOCALE_PREFIX.friends"]}**: $totalFriends\n", true)

                    // Favorite Games
                    if (favoriteGamesContainer != null) {
                        val builder = buildString {
                            favoriteGamesContainer.getElementsByClass("game-card-link").forEach {
                                val gameLink = it.attr("href")
                                val title = it.getElementsByClass("game-name-title")
                                    .first()
                                    .attr("title")

                                // This is a "hack" because Roblox loves adding dumb stuff that doesn't really matter
                                // Example: https://www.roblox.com/games/refer?SortFilter=5&PlaceId=31610786&Position=1&SortPosition=1&PageId=703dbc9a-117a-4742-ae56-cd9d63d6be9a&PageType=Profile
                                // We can reduce that to just https://roblox.com/games/31610786
                                val placeId = gameLink.parseUrlEncodedParameters(Charsets.UTF_8)["PlaceId"]

                                append("[$title](https://roblox.com/games/$placeId)\n")
                            }
                        }

                        addField("\uD83D\uDD79️ ${locale["$LOCALE_PREFIX.favoriteGames"]}", builder, false)
                    }

                    setImage("attachment://roblox.png")
                    setThumbnail(avatar)
                }.build()

                val image = ByteArrayInputStream(JVMImage(bufferedImage).toByteArray())

                context.sendFile(image, "roblox.png", context.getUserMention(true), embed)
                return@executesDiscord
            } else {
                context.explain()
            }
        }
    }
}
