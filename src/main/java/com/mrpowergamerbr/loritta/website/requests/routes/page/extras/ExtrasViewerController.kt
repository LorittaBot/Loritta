package com.mrpowergamerbr.loritta.website.requests.routes.page.extras

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.File

@Path("/:localeId/extras/:pageId")
class ExtrasViewerController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		val extraType = req.path().split("/").getOrNull(3)?.replace(".", "")?.replace("/", "")

		if (extraType != null) {
			if (File(LorittaWebsite.FOLDER, "extras/$extraType.html").exists()) {
				variables["extraType"] = extraType
				if (extraType == "banned-users") {
					val bannedUsers = loritta.mongo.getDatabase("loritta").getCollection("users").find(
							Filters.eq("banned", true)
					).toMutableList()

					var html = ""
					for (profile in bannedUsers) {
						val userId = profile.getString("_id")
						val banReason = profile.getString("banReason") ?: "???"
						val user = try {
							lorittaShards.getUserById(userId)
						} catch (e: Exception) {
							null
						}

						html += """
							<tr>
							<td>${user?.id ?: userId}</td>
							<td>${if (user != null) "${user.name}#${user.discriminator}" else "???"}</td>
							<td>$banReason</td>
							</tr>
						""".trimIndent()
					}

					variables["tableContents"] = html
				}
				return res.send(evaluate("extras/$extraType.html", variables))
			}
		}

		res.status(404)
		return res.send(evaluate("404.html"))
	}
}