package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.website.views.subviews.ProtectedView
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.DiscordUtils
import org.jooby.Request
import org.jooby.Response

abstract class ConfigureView : ProtectedView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path.matches(Regex("^/dashboard/configure/[0-9]+"))
	}

	override fun renderProtected(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String {
		val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").get()
		val split = path.split("/")
		if (4 > split.size) {
			return "Servidor não encontrado!"
		}
		val guildId = split[3]

		val shardId = DiscordUtils.getShardIdFromGuildId(guildId.toLong())

		val host = req.header("Host").valueOrNull() ?: return "Missing Host header!"

		val loriShardId = DiscordUtils.getLorittaClusterIdForShardId(shardId)
		val theNewUrl = DiscordUtils.getUrlForLorittaClusterId(loriShardId)

		if (host != theNewUrl) {
			res.redirect("https://$theNewUrl${req.path()}${req.urlQueryString}")
			return "Redirecting..."
		}

		val jdaGuild = lorittaShards.getGuildById(guildId)
		val serverConfig = loritta.getServerConfigForGuild(guildId)

		if (jdaGuild == null) {
			res.redirect(loritta.discordInstanceConfig.discord.addBotUrl)
			return "Redirecting..."
		}

		val id = userIdentification.id
		val member = jdaGuild.getMemberById(id)
		var canAccessDashboardViaPermission = false

		if (member != null) {
			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getOrCreateLorittaProfile(id.toLong()))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
		}

		val canBypass = loritta.config.isOwner(userIdentification.id) || canAccessDashboardViaPermission
		if (!canBypass && !(member?.hasPermission(Permission.ADMINISTRATOR) == true || member?.hasPermission(Permission.MANAGE_SERVER) == true)) {
			return "Você não tem permissão!"
		}

		variables["guild"] = jdaGuild
		variables["serverConfig"] = serverConfig
		variables["serverConfigJson"] = gson.toJson(WebsiteUtils.getServerConfigAsJson(jdaGuild, serverConfig, userIdentification))
		return renderConfiguration(req, res, path, variables, discordAuth, jdaGuild, serverConfig)
	}

	abstract fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String
}