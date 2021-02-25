package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullInt
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostSearchUsersRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/users/search") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }
		val isRegexPattern = json["isRegExPattern"].nullBool ?: false
		val limit = json["limit"].nullInt

		val userName = json["username"].string
		val discriminator = json["discriminator"].nullString

		val filter: ((User) -> Boolean) = if (isRegexPattern) {
			val regex = Regex(userName);

			{
				(if (discriminator != null) discriminator == it.discriminator else true) && it.name.contains(regex)
			}
		} else {
			{
				(if (discriminator != null) discriminator == it.discriminator else true) && it.name == userName
			}
		}

		val array = lorittaShards.getUsers()
				.asSequence() // With asSequence, the sequence will respect the "take" and won't process more than the defined limit
				.filter { filter.invoke(it) }
				.map {
					jsonObject(
							"id" to it.idLong,
							"name" to it.name,
							"discriminator" to it.discriminator,
							"avatarId" to it.avatarId
					)
				}
				.run {
					if (limit != null)
						this.take(limit)
					else this
				}
				.toList()
				.toJsonArray()

		call.respondJson(array)
	}
}