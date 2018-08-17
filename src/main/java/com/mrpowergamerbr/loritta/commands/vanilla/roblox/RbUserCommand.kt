package com.mrpowergamerbr.loritta.commands.vanilla.roblox

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.image.BufferedImage
import java.net.URLEncoder

class RbUserCommand : AbstractCommand("rbuser", listOf("rbplayer"), CommandCategory.ROBLOX) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("RBUSER_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExample(): List<String> {
		return listOf("cazum8", "lol738236")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val username = context.args.joinToString(separator = " ")

			val body = HttpRequest.get("https://www.roblox.com/search/users/results?keyword=${URLEncoder.encode(username, "UTF-8")}&maxRows=12&startIndex=0")
					.body()

			val response = jsonParser.parse(body).obj

			var userId: Long? = null
			var name: String? = null
			var blurb: String? = null
			var isOnline: Boolean? = null

			if (response["Keyword"].string == "###########") {
				// oh man, censored :(
				// fuck you roblox >:c
				val altRobloxQuery = HttpRequest.get("https://www.roblox.com/users/profile?username=${URLEncoder.encode(username, "UTF-8")}")

				altRobloxQuery.ok()

				if (altRobloxQuery.code() != 404) {
					val jsoup = Jsoup.parse(altRobloxQuery.body())
					userId = altRobloxQuery.url().path.split("/")[2].toLong()
					name = jsoup.getElementsByClass("header-title").text()
					blurb = jsoup.getElementsByAttribute("data-statustext").attr("data-statustext")
					isOnline = jsoup.getElementsByClass("avatar-status").isNotEmpty()
				}
			} else {
				if (response["UserSearchResults"].isJsonArray) {
					val users = response["UserSearchResults"].array

					if (users.size() > 0) {
						val user = users[0]

						userId = user["UserId"].long
						name = user["Name"].string
						blurb = user["Blurb"].string
						isOnline = user["IsOnline"].bool
					}
				}
			}

			if (userId == null || name == null || blurb == null || isOnline == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.locale["RBUSER_COULDNT_FIND", username] + " \uD83D\uDE22")
				return
			}

			val avatarBody = HttpRequest.get("https://www.roblox.com/search/users/avatar?isHeadshot=false&userIds=$userId")
					.body()

			val avatarResponse = jsonParser.parse(avatarBody).obj

			// {"PlayerAvatars":[{"Thumbnail":{"Final":true,"Url":"https://t0.rbxcdn.com/fff65b7dc56eefa902fe543b2665da42","RetryUrl":null,"UserId":37271405,"EndpointType":"Avatar"},"UserId":37271405},{"Thumbnail":{"Final":true,"Url":"https://t1.rbxcdn.com/2083a073d0cc644478d06d266c2cc4d6","RetryUrl":null,"UserId":315274565,"EndpointType":"Avatar"},"UserId":315274565}]}
			val avatar = avatarResponse["PlayerAvatars"].array[0]["Thumbnail"]["Url"].string

			val page = Jsoup.connect("https://www.roblox.com/users/$userId/profile")
					.get()
					.body()

			val stats = page.getElementsByClass("profile-stat")

			val joinDate = stats[0].getElementsByClass("text-lead")[0].text()
			val placeVisits = stats[1].getElementsByClass("text-lead")[0].text()

			// SEGUINDO
			val followingBody = HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=Following&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
					.body()

			val followingResponse = jsonParser.parse(followingBody)

			val totalFollowing = followingResponse["TotalFriends"].int

			// SEGUIDORES
			val followersBody = HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=Followers&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
					.body()

			val followersResponse = jsonParser.parse(followersBody)

			val totalFollowers = followersResponse["TotalFriends"].int

			// AMIGOS
			val friendsBody = HttpRequest.get("https://www.roblox.com/users/friends/list-json?currentPage=0&friendsType=AllFriends&imgHeight=100&imgWidth=100&pageSize=18&userId=$userId")
					.body()

			val friendsResponse = jsonParser.parse(friendsBody)

			val totalFriends = friendsResponse["TotalFriends"].int

			var bufferedImage = BufferedImage(333, 250, BufferedImage.TYPE_INT_ARGB)

			var x = 0
			var y = 0

			run {
				val robloxCollectionsResponse = HttpRequest.get("https://www.roblox.com/users/profile/robloxcollections-json?userId=$userId")
						.body()

				val robloxCollections = jsonParser.parse(robloxCollectionsResponse).obj

				for (coll in robloxCollections["CollectionsItems"].array) {
					if (x >= 333) {
						y += 55
						x = 0
					}

					val thumbnailUrl = coll["Thumbnail"]["Url"].string

					val thumbnail = LorittaUtils.downloadImage(thumbnailUrl).getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH)
					bufferedImage.graphics.drawImage(thumbnail, x, y, null)
					x += 55
				}
			}

			y += 55
			x = 0
			run {
				val robloxCollectionsResponse = HttpRequest.get("https://www.roblox.com/users/profile/playerassets-json?assetTypeId=21&userId=$userId")
						.body()

				val robloxCollections = jsonParser.parse(robloxCollectionsResponse).obj

				for (coll in robloxCollections["Assets"].array) {
					if (x >= 333) {
						y += 55
						x = 0
					}

					val thumbnailUrl = coll["Thumbnail"]["Url"].string

					val thumbnail = LorittaUtils.downloadImage(thumbnailUrl).getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH)
					bufferedImage.graphics.drawImage(thumbnail, x, y, null)
					x += 55
				}
			}

			y += 55
			x = 0
			run {
				val gameCardsThumbnail = page.getElementsByClass("game-card-thumb")
				for (gameCard in gameCardsThumbnail) {
					if (x >= 333) {
						y += 55
						x = 0
					}

					val thumbnailUrl = gameCard.attr("src")

					if (thumbnailUrl != null) {
						val thumbnail = LorittaUtils.downloadImage(thumbnailUrl)?.getScaledInstance(55, 55, BufferedImage.SCALE_SMOOTH) ?: continue

						bufferedImage.graphics.drawImage(thumbnail, x, y, null)
						x += 55
					}
				}
			}

			bufferedImage = bufferedImage.getSubimage(0, 0, 333, y + 55)
			val embed = EmbedBuilder().apply {
				setTitle("<:roblox_logo:412576693803286528>${if (isOnline) "<:online:313956277808005120>" else "<:offline:313956277237710868>"}$name")
				if (blurb.isNotEmpty()) {
					setDescription(blurb)
				}
				setColor(Constants.ROBLOX_RED)
				addField("\uD83D\uDCBB ${context.locale.get("RBUSER_ID_DO_ROBLOX")}", userId.toString(), true)
				addField("\uD83D\uDCC5 ${context.locale.get("RBUSER_JOIN_DATE")}", joinDate, true)
				addField("\uD83D\uDC40 ${context.locale.get("RBUSER_PLACE_VISITS")}", placeVisits, true)
				addField("\uD83D\uDE4B ${context.locale.get("RBUSER_SOCIAL")}", "**\uD83D\uDC3E ${context.locale.get("RBUSER_FOLLOWING")}**: $totalFollowing\n**<:twitt_starstruck:352216844603752450> ${context.locale.get("RBUSER_FOLLOWERS")}**: $totalFollowers\n**\uD83D\uDE0E ${context.locale.get("RBUSER_FRIENDS")}**: $totalFriends\n", true)
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