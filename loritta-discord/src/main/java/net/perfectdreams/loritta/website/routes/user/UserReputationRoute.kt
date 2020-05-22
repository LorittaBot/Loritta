package net.perfectdreams.loritta.website.routes.user

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.request.header
import io.ktor.request.path
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

class UserReputationRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/{userId}/rep") {
	override suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale) {
		if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
			createReputationPage(call, locale, null, null)
			return
		}

		super.onUnauthenticatedRequest(call, locale)
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		createReputationPage(call, locale, discordAuth, userIdentification)
	}

	suspend fun createReputationPage(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth?, userIdentification: LorittaJsonWebSession.UserIdentification?) {
		loritta as Loritta
		val userId = call.parameters["userId"] ?: return

		val user = lorittaShards.retrieveUserById(userId)!!

		// Vamos agora pegar todas as reputações
		val reputations = loritta.newSuspendedTransaction {
			Reputation.find { Reputations.receivedById eq user.idLong }.sortedByDescending { it.receivedAt }
		}

		val lastReputationGiven = if (userIdentification != null) {
			loritta.newSuspendedTransaction {
				Reputation.find {
					(Reputations.givenById eq userIdentification.id.toLong()) or
							(Reputations.givenByEmail eq userIdentification.email!!) or
							(Reputations.givenByIp eq call.request.trueIp)
				}.sortedByDescending { it.receivedAt }.firstOrNull()
			}
		} else null

		val backgroundUrl = loritta.getUserProfileBackgroundUrl(loritta.getOrCreateLorittaProfile(userId))

		val html = ScriptingUtils.evaluateWebPageFromTemplate(
				File(
						"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/user/user_reputation.kts"
				),
				mapOf(
						"path" to call.request.path().split("/").drop(2).joinToString("/"),
						"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
						"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale),
						"userIdentification" to ScriptingUtils.WebsiteArgumentType(TemmieDiscordAuth.UserIdentification::class.createType(nullable = true), userIdentification, "LorittaJsonWebSession.UserIdentification"),
						"user" to ScriptingUtils.WebsiteArgumentType(User::class.createType(nullable = true), user),
						"lastReputationGiven" to ScriptingUtils.WebsiteArgumentType(Reputation::class.createType(nullable = true), lastReputationGiven),
						"reputations" to ScriptingUtils.WebsiteArgumentType(
								List::class.createType(listOf(KTypeProjection.invariant(Reputation::class.createType()))),
								reputations
						),
						"guildId" to ScriptingUtils.WebsiteArgumentType(String::class.createType(nullable = true), call.parameters["guild"]),
						"channelId" to ScriptingUtils.WebsiteArgumentType(String::class.createType(nullable = true), call.parameters["channel"]),
						"backgroundUrl" to backgroundUrl
				)
		)

		call.respondHtml(html)
	}
}