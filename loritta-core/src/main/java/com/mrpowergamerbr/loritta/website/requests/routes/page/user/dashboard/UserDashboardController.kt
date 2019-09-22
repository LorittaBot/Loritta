package com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/user/@me/dashboard")
class UserDashboardController {
    @GET
    @LoriRequiresVariables(true)
    @LoriForceReauthentication(true)
    @LoriRequiresAuth(LoriAuthLevel.USER_AUTH)
    fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
        val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").get()

        val userId = userIdentification.id

        val user = runBlocking { lorittaShards.retrieveUserById(userId)!! }
        val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

        variables["profileUser"] = user
        variables["lorittaProfile"] = lorittaProfile
        variables["profileSettings"] = transaction(Databases.loritta) {
            lorittaProfile.settings
        }
        variables["profile_json"] = gson.toJson(
                WebsiteUtils.getProfileAsJson(lorittaProfile)
        )
        variables["saveType"] = "main"

        return evaluate("profile_dashboard.html", variables)
    }
}