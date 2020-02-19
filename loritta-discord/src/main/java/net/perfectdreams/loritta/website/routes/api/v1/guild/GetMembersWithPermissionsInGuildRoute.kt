package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetMembersWithPermissionsInGuildRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/users-with-any-permission/{permissionList}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val guildId = call.parameters["guildId"] ?: return

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			call.respondJson(jsonObject())
			return
		}

		val permissionList = call.parameters["permissionList"] ?: return

		val permissions = permissionList.split(",").map { Permission.valueOf(it) }

		val membersWithPermission = guild.members.filter {  member ->
			val permissionTheUserHas = permissions.filter { permission ->
				member.hasPermission(permission)
			}

			permissionTheUserHas.isNotEmpty()
		}

		call.respondJson(
				jsonObject(
						"members" to membersWithPermission.map {
							jsonObject(
									"id" to it.id
							)
						}.toJsonArray()
				)
		)
	}
}