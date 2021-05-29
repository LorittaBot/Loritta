package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureWelcomerRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/welcomer") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta

		val welcomerConfig = loritta.newSuspendedTransaction {
			serverConfig.welcomerConfig
		}

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "welcomer"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeWelcomerConfig(
						welcomerConfig != null,
						welcomerConfig?.tellOnJoin ?: false,
						welcomerConfig?.tellOnRemove ?: false,
						welcomerConfig?.tellOnBan ?: false,
						welcomerConfig?.tellOnPrivateJoin ?: false,
						welcomerConfig?.joinMessage ?: "\uD83D\uDC49 {@user} entrou no servidor!",
						welcomerConfig?.removeMessage ?: "\uD83D\uDC48 {user.name} saiu do servidor!",
						welcomerConfig?.joinPrivateMessage ?: "Obrigado por entrar na {guild} {@user}! Espero que você curta o nosso servidor!",
						welcomerConfig?.bannedMessage ?: "{user} foi banido do servidor!",
						welcomerConfig?.deleteJoinMessagesAfter ?: 0L,
						welcomerConfig?.deleteRemoveMessagesAfter ?: 0L
				)
		)

		call.respondHtml(evaluate("welcomer.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val joinLeaveConfig: FakeWelcomerConfig) {
		class FakeWelcomerConfig(
				val isEnabled: Boolean,
				val tellOnJoin: Boolean,
				val tellOnLeave: Boolean,
				val tellOnBan: Boolean,
				val tellOnPrivate: Boolean,
				val joinMessage: String,
				val removeMessage: String,
				val joinPrivateMessage: String,
				val bannedMessage: String,
				val deleteJoinMessagesAfter: Long,
				val deleteRemoveMessagesAfter: Long
		)
	}
}