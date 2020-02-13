package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.parallax

import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.PUT
import org.jooby.mvc.Path

@Path("/api/v1/parallax/guilds/:guildId/members/:memberId/roles/:roleId")
class AddRoleToMemberController {
	@PUT
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, guildId: String, memberId: String, roleId: String) {
		res.type(MediaType.json)

		try {
			val guild = lorittaShards.getGuildById(guildId)!!
			val member = guild.getMemberById(memberId)!!
			val role = guild.getRoleById(roleId)!!

			if (guild.selfMember.canInteract(role)) {
				guild.addRoleToMember(member, role).complete()

				res.status(Status.CREATED)
				res.send("")
			}
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}
}