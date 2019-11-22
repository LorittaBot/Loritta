package com.mrpowergamerbr.loritta.website.requests.routes.page.user

import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriForceReauthentication
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

@Path("/:localeId/user/:userId/rep")
class UserReputationController {
	@GET
	@LoriRequiresVariables(true)
	@LoriForceReauthentication(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		val userId = req.param("userId").value()
		val user = runBlocking { lorittaShards.retrieveUserById(userId)!! }
		val userIdentification = variables["userIdentification"] as TemmieDiscordAuth.UserIdentification?

		// Vamos agora pegar todas as reputações
		val reputations = transaction(Databases.loritta) {
			Reputation.find { Reputations.receivedById eq user.idLong }.sortedByDescending { it.receivedAt }
		}

		val lastReputationGiven = if (userIdentification != null) {
			transaction(Databases.loritta) {
				Reputation.find {
					(Reputations.givenById eq userIdentification.id.toLong()) or
							(Reputations.givenByEmail eq userIdentification.email!!) or
							(Reputations.givenByIp eq req.trueIp)
				}.sortedByDescending { it.receivedAt }.firstOrNull()
			}
		} else { null }

		val html = runBlocking {
			ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/user/user_reputation.kts"
					),
					mapOf(
							"path" to req.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!),
							"userIdentification" to ScriptingUtils.WebsiteArgumentType(TemmieDiscordAuth.UserIdentification::class.createType(nullable = true), userIdentification, "TemmieDiscordAuth.UserIdentification"),
							"user" to ScriptingUtils.WebsiteArgumentType(User::class.createType(nullable = true), user),
							"lastReputationGiven" to ScriptingUtils.WebsiteArgumentType(Reputation::class.createType(nullable = true), lastReputationGiven),
							"reputations" to ScriptingUtils.WebsiteArgumentType(
									List::class.createType(listOf(KTypeProjection.invariant(Reputation::class.createType()))),
									reputations
							),
							"guildId" to ScriptingUtils.WebsiteArgumentType(String::class.createType(nullable = true), req.param("guild").valueOrNull()),
							"channelId" to ScriptingUtils.WebsiteArgumentType(String::class.createType(nullable = true), req.param("channel").valueOrNull())
					)
			)
		}

		res.send(html)
	}
}