package com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ProfileSettings
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.profile.DefaultProfileCreator
import com.mrpowergamerbr.loritta.profile.MSNProfileCreator
import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.profile.OrkutProfileCreator
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/user/@me/dashboard/profiles")
class ProfileListController {
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
        variables["lorittaProfile"] = WebsiteUtils.transformProfileToJson(lorittaProfile)
        variables["saveType"] = "profile_list"

        val profileSettings = transaction(Databases.loritta) {
            lorittaProfile.settings
        }

        variables["available_profiles_json"] = gson.toJson(
                getProfiles().map {
                    getProfileAsJson(userIdentification, it.first, it.second, profileSettings, it.third)
                }
        )

        variables["profile_json"] = gson.toJson(
                WebsiteUtils.getProfileAsJson(lorittaProfile)
        )

        return evaluate("profile_dashboard_profile_list.html", variables)
    }

    companion object {
        fun getProfiles(): List<Triple<Class<*>, String, Double>> {
            return listOf(
                    Triple(NostalgiaProfileCreator::class.java, "default", 0.0),
                    Triple(DefaultProfileCreator::class.java, "modern", 2000.0),
                    Triple(MSNProfileCreator::class.java, "msn", 7500.0),
                    Triple(OrkutProfileCreator::class.java, "orkut", 7500.0)
            )
        }

        fun getProfileAsJson(userIdentification: SimpleUserIdentification, profile: Class<*>, shortName: String, settings: ProfileSettings, price: Double): JsonObject {
            return jsonObject(
                    "internalName" to profile.simpleName,
                    "shortName" to shortName,
                    "price" to price,
                    "alreadyBought" to if (profile == NostalgiaProfileCreator::class.java) true else settings.boughtProfiles.contains(profile.simpleName),
                    "activated" to (settings.activeProfile == profile.simpleName)
            )
        }
    }
}