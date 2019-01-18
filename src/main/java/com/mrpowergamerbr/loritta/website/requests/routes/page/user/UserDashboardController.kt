package com.mrpowergamerbr.loritta.website.requests.routes.page.user

import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriForceReauthentication
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.evaluate
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/user/:userId/dashboard")
class UserDashboardController {
    @GET
    @LoriRequiresVariables(true)
    @LoriForceReauthentication(true)
    fun handle(req: Request, res: Response, userId: String, @Local variables: MutableMap<String, Any?>): String {
        val user = lorittaShards.getUserById(userId)!!
        val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

        var userIdentification: TemmieDiscordAuth.UserIdentification? = null

        if (!req.session().isSet("discordAuth")) {
            variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
        } else {
            try {
                val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
                discordAuth.isReady(true)
                userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
                val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)

                // variables["selfProfile"] = Loritta.GSON.toJson(profile)
            } catch (e: Exception) {
                // variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
            }
        }

        variables["profileUser"] = user
        variables["lorittaProfile"] = lorittaProfile
        variables["profileSettings"] = transaction(Databases.loritta) {
            lorittaProfile.settings
        }

        return evaluate("profile_dashboard.html", variables)
    }
}