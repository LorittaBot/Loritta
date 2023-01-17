package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureInviteBlockerRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/invite-blocker") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as LorittaBot

		val inviteBlockerConfig = loritta.newSuspendedTransaction {
			serverConfig.inviteBlockerConfig
		}

		val variables = call.legacyVariables(loritta, locale)

		variables["saveType"] = "invite_blocker"
		variables["whitelistedChannels"] = (inviteBlockerConfig?.whitelistedChannels?.filter { guild.getGuildMessageChannelById(it) != null } ?: listOf()).joinToString(separator = ";")
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

		call.respondHtml(
			LegacyPebbleRawHtmlView(
				loritta,
				locale,
				getPathWithoutLocale(call),
				"Painel de Controle",
				evaluate("invite_blocker.html", variables)
			).generateHtml()
		)
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