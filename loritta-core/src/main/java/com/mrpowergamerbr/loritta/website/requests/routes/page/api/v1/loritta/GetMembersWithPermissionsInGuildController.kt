package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import net.dv8tion.jda.api.Permission
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/guild/:guildId/users-with-any-permission/:permissionList")
class GetMembersWithPermissionsInGuildController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, guildId: String, permissionList: String) {
		res.type(MediaType.json)

		val guild = lorittaShards.getGuildById(guildId)

		if (guild == null) {
			res.send(
					jsonObject()
			)
			return
		}

		val permissions = permissionList.split(",").map { Permission.valueOf(it) }

		val membersWithPermission = guild.members.filter {  member ->
			val permissionTheUserHas = permissions.filter { permission ->
				member.hasPermission(permission)
			}

			permissionTheUserHas.isNotEmpty()
		}

		res.send(
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