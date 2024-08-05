package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.internal.entities.MemberImpl
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

class GetMembersWithRolesInGuildRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/users-with-any-role/{roleList}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val guildId = call.parameters["guildId"] ?: return

		val guild = loritta.lorittaShards.getGuildById(guildId)

		if (guild == null) {
			call.respondJson(jsonObject())
			return
		}

		val roleList = call.parameters["roleList"] ?: return
		val roleIdsToBeMatchedAgainst = roleList.split(",")
			.map { it.toLong() }
			.toSet()

		val membersWithRoles = mutableListOf<Member>()

		for (member in guild.members) {
			// We cast and filter by the roleSet to avoid unnecessary sorting done by the Member.roles call
			member as MemberImpl

			// Check if "member" has any of the roles in the roleIdsToBeMatchedAgainst set
			for (role in member.roleSet) {
				if (role.idLong in roleIdsToBeMatchedAgainst) {
					membersWithRoles.add(member)
					break
				}
			}
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