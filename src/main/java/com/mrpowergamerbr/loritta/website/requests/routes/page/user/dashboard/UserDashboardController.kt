package com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard

import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.*
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

        val user = lorittaShards.getUserById(userId)!!
        val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

        variables["profileUser"] = user
        variables["lorittaProfile"] = lorittaProfile
        variables["profile_json"] = gson.toJson(
                WebsiteUtils.getProfileAsJson(lorittaProfile)
        )
        variables["saveType"] = "main"

        return evaluate("profile_dashboard.html", variables)
    }
}