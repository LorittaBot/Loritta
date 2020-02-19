package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetMembersWithRolesInGuildRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/users-with-any-role/{roleList}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val guildId = call.parameters["guildId"] ?: return

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			call.respondJson(jsonObject())
			return
		}

		val roleList = call.parameters["roleList"] ?: return

		val roles = roleList.split(",").map { guild.getRoleById(it) }

		val membersWithRoles = guild.members.filter {  member ->
			val rolesTheUserHas = roles.filter { role ->
				member.roles.contains(role)
			}

			rolesTheUserHas.isNotEmpty()
		}

		call.respondJson(
				jsonObject(
						"members" to membersWithRoles.map {
							jsonObject(
									"id" to it.id
							)
						}.toJsonArray()
				)
		)
	}
}