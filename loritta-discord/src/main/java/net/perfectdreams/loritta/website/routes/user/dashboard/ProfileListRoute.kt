package net.perfectdreams.loritta.website.routes.user.dashboard

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ProfileSettings
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileDesign
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.transactions.transaction

class ProfileListRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/profiles") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val userId = userIdentification.id
		val variables = call.legacyVariables(locale)

		val user = lorittaShards.retrieveUserById(userId)!!
		val lorittaProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userId)

		variables["profileUser"] = user
		variables["lorittaProfile"] = WebsiteUtils.transformProfileToJson(lorittaProfile)
		variables["saveType"] = "profile_list"

		val profileSettings = transaction(Databases.loritta) {
			lorittaProfile.settings
		}

		variables["available_profiles_json"] = gson.toJson(
				com.mrpowergamerbr.loritta.utils.loritta.profileDesignManager.publicDesigns.map {
					getProfileAsJson(userIdentification, it.clazz, profileSettings, it)
				}
		)

		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)

		call.respondHtml(evaluate("profile_dashboard_profile_list.html", variables))
	}

	companion object {
		fun getProfileAsJson(userIdentification: LorittaJsonWebSession.UserIdentification, profile: Class<*>, settings: ProfileSettings, profileDesign: ProfileDesign): JsonObject {
			return jsonObject(
					"internalName" to profile.simpleName,
					"shortName" to profileDesign.internalType,
					"rarity" to profileDesign.rarity.toString(),
					"alreadyBought" to if (profile == NostalgiaProfileCreator::class.java) true else settings.boughtProfiles.contains(profile.simpleName),
					"activated" to (settings.activeProfile == profile.simpleName),
					"availableToBuyViaDreams" to profileDesign.availableToBuyViaDreams,
					"availableToBuyViaMoney" to profileDesign.availableToBuyViaMoney
			)
		}
	}
}