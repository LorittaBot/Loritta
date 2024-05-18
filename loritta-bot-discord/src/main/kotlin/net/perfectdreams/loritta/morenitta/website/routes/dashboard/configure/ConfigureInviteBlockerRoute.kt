package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.evaluate
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.LegacyPebbleGuildDashboardRawHtmlView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureInviteBlockerRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/invite-blocker") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {	val inviteBlockerConfig = loritta.newSuspendedTransaction {
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
						inviteBlockerConfig?.whitelistedChannels?.toTypedArray() ?: arrayOf()
				)
		)

		call.respondHtml(
			LegacyPebbleGuildDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				"Painel de Controle",
				evaluate("invite_blocker.html", variables),
				"invite_blocker"
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