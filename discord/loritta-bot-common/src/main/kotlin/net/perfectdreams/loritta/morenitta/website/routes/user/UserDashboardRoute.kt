package net.perfectdreams.loritta.morenitta.website.routes.user

import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class UserDashboardRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val userId = userIdentification.id
        val variables = call.legacyVariables(loritta, locale)

        val user = loritta.lorittaShards.retrieveUserById(userId)!!
        val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

        variables["profileUser"] = user
        variables["lorittaProfile"] = lorittaProfile
        variables["profileSettings"] = loritta.newSuspendedTransaction {
            lorittaProfile.settings
        }
        variables["profile_json"] = gson.toJson(
            WebsiteUtils.getProfileAsJson(lorittaProfile)
        )
        variables["saveType"] = "main"

        call.respondHtml(evaluate("profile_dashboard.html", variables))
    }
}