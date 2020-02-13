package com.mrpowergamerbr.loritta.commands.vanilla.roblox

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jsoup.Jsoup
import java.awt.image.BufferedImage
import java.net.URLEncoder

class RbUserCommand : AbstractCommand("rbuser", listOf("rbplayer"), CommandCategory.ROBLOX) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("RBUSER_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExamples(): List<String> {
		return listOf("cazum8", "lol738236")
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val username = context.args.joinToString(separator = " ")

			var userId: Long? = null
			var name: String? = null
			var blurb: String? = null
			var isOnline: Boolean? = null

			val altRobloxQuery = HttpRequest.get("https://www.roblox.com/users/profile?username=${URLEncoder.encode(username, "UTF-8")}")

			altRobloxQuery.ok()

			if (altRobloxQuery.code() != 404) {
				val jsoup = Jsoup.parse(altRobloxQuery.body())
				userId = altRobloxQuery.url().path.split("/")[2].toLong()
				name = jsoup.getElementsByClass("header-title").text()
				blurb = jsoup.getElementsByAttribute("data-statustext").attr("data-statustext")
				isOnline = jsoup.getElementsByClass("avatar-status").isNotEmpty()
			}

			if (userId == null || name == null || blurb == null || isOnline == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.legacyLocale["RBUSER_COULDNT_FIND", username] + " \uD83D\uDE22")
				return
			}

			val avatarBodyTask = GlobalScope.async(loritta.coroutineDispatcher) {
				HttpRequest.get("https://www.roblox.com/search/users/avatar?isHeadshot=false&userIds=$userId")
						.body()
			}
			val pageTask = GlobalScope.async(loritta.coroutineDispatcher) {
				Jsoup.connect("https://www.roblox.com/users/$userId/profile")
						.get()
						.body()
			}
			val followingBodyTask = GlobalScope.async(loritta.coroutineDispatcher) {
				HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=Following&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
						.body()
			}
			val followersBodyTask = GlobalScope.async(loritta.coroutineDispatcher) {
				HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=Followers&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
						.body()
			}
			val friendsBodyTask = GlobalScope.async(loritta.coroutineDispatcher) {
				HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=AllFriends&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
						.body()
			}

			var bufferedImage = BufferedImage(333, 220, BufferedImage.TYPE_INT_ARGB)

			var x = 0
			var y = 0

			val collections = GlobalScope.async(loritta.coroutineDispatcher) {
				val robloxCollectionsResponse = HttpRequest.get("https://www.roblox.com/users/profile/robloxcollections-json?userId=$userId")
						.body()

				val robloxCollections = jsonParser.parse(robloxCollectionsResponse).obj


				val entries = robloxCollections["CollectionsItems"].array.map {
					if (x > 275) {
						y += 55
						x = 0
					}

					val realX = x
					val realY = y

					val async = GlobalScope.async(loritta.coroutineDispatcher) {
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

			y += 55
			x = 0

			val playerAssets = GlobalScope.async(loritta.coroutineDispatcher) {
				val robloxCollectionsResponse = HttpRequest.get("https://www.roblox.com/users/profile/playerassets-json?assetTypeId=21&userId=$userId")
						.body()

				val robloxCollections = jsonParser.parse(robloxCollectionsResponse).obj

				val entries = robloxCollections["Assets"].array.map {
					if (x > 275) {
						y += 55
						x = 0
					}

					val realX = x
					val realY = y

					val async = GlobalScope.async(loritta.coroutineDispatcher) {
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

			val page = pageTask.await()

			y += 55
			x = 0

			val gameCards = GlobalScope.async(loritta.coroutineDispatcher) {
				val gameCardsThumbnail = page.getElementsByClass("game-cards").first().getElementsByClass("game-card-thumb")

				val entries = gameCardsThumbnail.mapNotNull { gameCard ->
					if (x > 275) {
						y += 55
						x = 0
					}

					val thumbnailUrl = gameCard.attr("src")

					if (thumbnailUrl != null) {
						val realX = x
						val realY = y

						val async = GlobalScope.async(loritta.coroutineDispatcher) {
							val thumbnail = LorittaUtils.downloadImage(thumbnailUrl)?.getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH)
									?: return@async null

							bufferedImage.graphics.drawImage(thumbnail, realX, realY, null)
						}

						x += 55

						return@mapNotNull async
					} else { return@mapNotNull null }
				}

				entries.awaitAll()
			}

			collections.await()
			playerAssets.await()
			gameCards.await()

			bufferedImage = bufferedImage.getSubimage(0, 0, 333, y + 55)

			val avatarResponse = jsonParser.parse(avatarBodyTask.await()).obj

			// {"PlayerAvatars":[{"Thumbnail":{"Final":true,"Url":"https://t0.rbxcdn.com/fff65b7dc56eefa902fe543b2665da42","RetryUrl":null,"UserId":37271405,"EndpointType":"Avatar"},"UserId":37271405},{"Thumbnail":{"Final":true,"Url":"https://t1.rbxcdn.com/2083a073d0cc644478d06d266c2cc4d6","RetryUrl":null,"UserId":315274565,"EndpointType":"Avatar"},"UserId":315274565}]}
			val avatar = avatarResponse["PlayerAvatars"].array[0]["Thumbnail"]["Url"].string

			val stats = page.getElementsByClass("profile-stat")

			val joinDate = stats[0].getElementsByClass("text-lead")[0].text()
			val placeVisits = stats[1].getElementsByClass("text-lead")[0].text()

			// SEGUINDO
			val followingResponse = jsonParser.parse(followingBodyTask.await())

			val totalFollowing = followingResponse["TotalFriends"].int

			// SEGUIDORES
			val followersResponse = jsonParser.parse(followersBodyTask.await())

			val totalFollowers = followersResponse["TotalFriends"].int

			// AMIGOS
			val friendsResponse = jsonParser.parse(friendsBodyTask.await())

			val totalFriends = friendsResponse["TotalFriends"].int

			val embed = EmbedBuilder().apply {
				setTitle("<:roblox_logo:412576693803286528>${if (isOnline) "<:online:313956277808005120>" else "<:offline:313956277237710868>"}$name")
				if (blurb.isNotEmpty()) {
					setDescription(blurb)
				}
				setColor(Constants.ROBLOX_RED)
				addField("\uD83D\uDCBB ${context.legacyLocale.get("RBUSER_ID_DO_ROBLOX")}", userId.toString(), true)
				addField("\uD83D\uDCC5 ${context.legacyLocale.get("RBUSER_JOIN_DATE")}", joinDate, true)
				addField("\uD83D\uDC40 ${context.legacyLocale.get("RBUSER_PLACE_VISITS")}", placeVisits, true)
				addField("\uD83D\uDE4B ${context.legacyLocale.get("RBUSER_SOCIAL")}", "**\uD83D\uDC3E ${context.legacyLocale.get("RBUSER_FOLLOWING")}**: $totalFollowing\n**<:starstruck:540988091117076481> ${context.legacyLocale.get("RBUSER_FOLLOWERS")}**: $totalFollowers\n**\uD83D\uDE0E ${context.legacyLocale.get("RBUSER_FRIENDS")}**: $totalFriends\n", true)
				setImage("attachment://roblox.png")
				setThumbnail(avatar)
			}

			context.sendFile(bufferedImage, "roblox.png", embed.build())
			return
		} else {
			context.explain()
		}
	}
}