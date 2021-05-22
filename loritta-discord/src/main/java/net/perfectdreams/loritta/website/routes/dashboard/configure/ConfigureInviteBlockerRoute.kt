package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureInviteBlockerRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/invite-blocker") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta

		val inviteBlockerConfig = loritta.newSuspendedTransaction {
			serverConfig.inviteBlockerConfig
		}

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "invite_blocker"
		variables["whitelistedChannels"] = (inviteBlockerConfig?.whitelistedChannels?.filter { guild.getTextChannelById(it) != null } ?: listOf()).joinToString(separator = ";")
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeInviteBlockerConfig(
						inviteBlockerConfig?.enabled ?: false,
						inviteBlockerConfig?.whitelistServerInvites ?: false,
						inviteBlockerConfig?.deleteMessage ?: false,
						inviteBlockerConfig?.tellUser ?: false,
						inviteBlockerConfig?.warnMessage ?: "{@user} Você não pode enviar convites de outros servidores aqui!",
						inviteBlockerConfig?.whitelistedChannels ?: arrayOf()
				)
		)

		call.respondHtml(evaluate("invite_blocker.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val inviteBlockerConfig: FakeInviteBlockerConfig) {
		class FakeInviteBlockerConfig(
				val isEnabled: Boolean,
				val whitelistServerInvites: Boolean,
				val deleteMessage: Boolean,
				val tellUser: Boolean,
				val warnMessage: String,
				val whitelistedChannels: Array<Long>
		)
	}
}