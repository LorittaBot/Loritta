package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.views.subviews.ProtectedView
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

abstract class ConfigureView : ProtectedView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path.matches(Regex("^/dashboard/configure/[0-9]+"))
	}

	override fun renderProtected(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String {
		val split = path.split("/");
		if (4 > split.size) {
			return "Servidor não encontrado!"
		}
		val guildId = split[3]

		val temmieGuild = discordAuth.getUserGuilds().firstOrNull { it.id == guildId }
		val jdaGuild = lorittaShards.getGuildById(guildId)
		val serverConfig = loritta.getServerConfigForGuild(guildId)

		if (jdaGuild == null) {
			return "Eu não estou neste servidor ou as minhas shards ainda não reiniciaram!"
		}

		val id = discordAuth.getUserIdentification().id
		val member = jdaGuild.getMemberById(id)
		var canAccessDashboardViaPermission = false

		if (member != null) {
			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))

			canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)
		}

		val canBypass = discordAuth.getUserIdentification().id == Loritta.config.ownerId || canAccessDashboardViaPermission
		if ((!canBypass) && (temmieGuild == null || !LorittaWebsite.canManageGuild(temmieGuild))) {
			return "Você não tem permissão!"
		}

		variables["guild"] = jdaGuild
		variables["serverConfig"] = serverConfig
		return renderConfiguration(req, res, path, variables, discordAuth, jdaGuild, serverConfig)
	}

	abstract fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String
}