package com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.dao.ShipEffect
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.tables.ShipEffects
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/user/@me/dashboard/ship-effects")
class ShipEffectsController {
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
        variables["saveType"] = "ship_effects"
        variables["profile_json"] = gson.toJson(
                WebsiteUtils.getProfileAsJson(lorittaProfile)
        )
        val shipEffects = transaction(Databases.loritta) {
            ShipEffect.find {
                (ShipEffects.buyerId eq user.idLong) and
                (ShipEffects.expiresAt greaterEq System.currentTimeMillis())
            }.toMutableList()
        }

        variables["ship_effects_json"] =
                gson.toJson(
                        shipEffects.map {
                            jsonObject(
                                    "buyerId" to it.buyerId,
                                    "user1Id" to it.user1Id,
                                    "user2Id" to it.user2Id,
                                    "editedShipValue" to it.editedShipValue,
                                    "expiresAt" to it.expiresAt
                            )
                        }
                )

        variables["profile_json"] = gson.toJson(
                WebsiteUtils.getProfileAsJson(lorittaProfile)
        )

        return evaluate("profile_dashboard_ship_effects.html", variables)
    }
}