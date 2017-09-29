package com.mrpowergamerbr.loritta.commands.vanilla.roblox

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URLEncoder

class RbUserCommand : CommandBase() {
	override fun getLabel(): String {
		return "rbuser"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("RBUSER_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ROBLOX
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExample(): List<String> {
		return listOf("cazum8", "lol738236")
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val username = context.args.joinToString(separator = " ")

			val body = HttpRequest.get("https://www.roblox.com/search/users/results?keyword=${URLEncoder.encode(username, "UTF-8")}&maxRows=12&startIndex=0")
					.body()

			val response = jsonParser.parse(body).obj

			if (response["UserSearchResults"].isJsonArray) {
				val users = response["UserSearchResults"].array

				if (users.size() > 0) {
					val user = users[0]

					val userId = user["UserId"].long
					val name = user["Name"].string
					val blurb = user["Blurb"].string
					val isOnline = user["IsOnline"].bool

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
					val forumPosts = stats[2].getElementsByClass("text-lead")[0].text()

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

					var bufferedImage = BufferedImage(333, 250, BufferedImage.SCALE_SMOOTH)

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
						setTitle("<:roblox:352208393571663873>${if (isOnline) "<:online:313956277808005120>" else "<:offline:313956277237710868>"}$name")
						if (blurb.isNotEmpty()) {
							setDescription(blurb)
						}
						setColor(Color(226, 35, 26))
						addField("\uD83D\uDCBB ${context.locale.get("RBUSER_ID_DO_ROBLOX")}", userId.toString(), true)
						addField("\uD83D\uDCC5 ${context.locale.get("RBUSER_JOIN_DATE")}", joinDate, true)
						addField("\uD83D\uDC40 ${context.locale.get("RBUSER_PLACE_VISITS")}", placeVisits, true)
						addField("\uD83D\uDCDD ${context.locale.get("RBUSER_FORUM_POSTS")}", forumPosts, true)
						addField("\uD83D\uDE4B ${context.locale.get("RBUSER_SOCIAL")}", "**\uD83D\uDC3E ${context.locale.get("RBUSER_FOLLOWING")}**: $totalFollowing\n**<:twitt_starstruck:352216844603752450> ${context.locale.get("RBUSER_FOLLOWERS")}**: $totalFollowers\n**\uD83D\uDE0E ${context.locale.get("RBUSER_FRIENDS")}**: $totalFriends\n", true)
						setImage("attachment://roblox.png")
						setThumbnail(avatar)
					}

					context.sendFile(bufferedImage, "roblox.png", embed.build())
					return
				}
			}
			context.sendMessage(Constants.ERROR + " **|** " + context.locale.get("RBUSER_COULDNT_FIND", username) + " \uD83D\uDE22")
		} else {
			context.explain()
		}
	}
}