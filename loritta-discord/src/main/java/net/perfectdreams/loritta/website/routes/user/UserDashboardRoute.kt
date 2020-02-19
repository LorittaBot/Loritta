package net.perfectdreams.loritta.website.routes.user

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.transactions.transaction

class UserDashboardRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val userId = userIdentification.id
		val variables = call.legacyVariables(locale)

		val user =  lorittaShards.retrieveUserById(userId)!!
		val lorittaProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userId)

		variables["profileUser"] = user
		variables["lorittaProfile"] = lorittaProfile
		variables["profileSettings"] = transaction(Databases.loritta) {
			lorittaProfile.settings
		}
		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)
		variables["saveType"] = "main"

		call.respondHtml(evaluate("profile_dashboard.html", variables))
	}
}