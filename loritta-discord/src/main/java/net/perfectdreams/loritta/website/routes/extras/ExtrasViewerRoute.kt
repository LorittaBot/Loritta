package net.perfectdreams.loritta.website.routes.extras

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BlacklistedUsers
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class ExtrasViewerRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/extras/{pageId}") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val extraType = call.parameters["pageId"]?.replace(".", "")?.replace("/", "")

		if (extraType != null) {
			if (File(LorittaWebsite.FOLDER, "extras/$extraType.html").exists()) {
				val variables = call.legacyVariables(locale)
				variables["extraType"] = extraType
				call.respondHtml(evaluate("extras/$extraType.html", variables))
			}
		}
	}
}