package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.website.evaluateKotlin
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
import net.dv8tion.jda.api.entities.Member
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/donate")
class DonateController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local locale: BaseLocale, @Local variables: MutableMap<String, Any?>) {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		val activeDonators = mutableListOf<Member>()
		val inactiveDonators = mutableListOf<Member>()

		if (lorittaGuild != null) {
			val rolePatreons = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
			val roleDonators = lorittaGuild.getRoleById("435856512787677214") // Doadores
			activeDonators.addAll(lorittaGuild.getMembersWithRoles(rolePatreons))
			inactiveDonators.addAll(lorittaGuild.getMembersWithRoles(roleDonators))
		}

		val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").getOrNull()

		val result = evaluateKotlin("donate.kts", "onLoad", locale, userIdentification, activeDonators, inactiveDonators)
		val builder = StringBuilder()
		builder.appendHTML().div { result.invoke(this) }

		variables["donate_html"] = builder.toString()

		res.send(evaluate("donate.html", variables))
	}
}