package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

abstract class ConfigureView : ProtectedView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path().matches(Regex("^/dashboard/configure/[0-9]+"))
	}

	override fun renderProtected(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String {
		val split = req.path().split("/");
		if (4 > split.size) {
			return "Servidor não encontrado!"
		}
		val guildId = split[3]

		val temmieGuild = discordAuth.getUserGuilds().firstOrNull { it.id == guildId }

		if (discordAuth.getUserIdentification().id != Loritta.config.ownerId && (temmieGuild == null || !LorittaWebsite.canManageGuild(temmieGuild))) {
			return "Você não tem permissão!"
		}

		val serverConfig = loritta.getServerConfigForGuild(guildId)

		val jdaGuild = lorittaShards.getGuildById(guildId)!!
		variables["guild"] = jdaGuild
		variables["serverConfig"] = serverConfig

		return renderConfiguration(req, res, variables, discordAuth, jdaGuild, serverConfig)
	}

	abstract fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String
}