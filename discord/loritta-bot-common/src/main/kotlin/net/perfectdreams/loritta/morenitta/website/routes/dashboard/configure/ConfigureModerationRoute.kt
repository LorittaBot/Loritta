package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureModerationRoute(loritta: LorittaBot) :
    RequiresGuildAuthLocalizedRoute(loritta, "/configure/moderation") {
    override suspend fun onGuildAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification,
        guild: Guild,
        serverConfig: ServerConfig
    ) {
        loritta as LorittaBot

        val moderationConfig = loritta.newSuspendedTransaction {
            serverConfig.moderationConfig
        }

        val variables = call.legacyVariables(loritta, locale)

        variables["saveType"] = "moderation"
        variables["serverConfig"] = FakeServerConfig(
            FakeServerConfig.FakeModerationConfig(
                moderationConfig?.sendPunishmentViaDm ?: false,
                moderationConfig?.sendPunishmentToPunishLog ?: false,
                moderationConfig?.punishLogMessage ?: ""
            )
        )

        call.respondHtml(evaluate("configure_moderation.html", variables))
    }

    /**
     * Fake Server Config for Pebble, in the future this will be removed
     */
    private class FakeServerConfig(val moderationConfig: FakeModerationConfig) {
        class FakeModerationConfig(
            val sendPunishmentViaDm: Boolean,
            val sendToPunishLog: Boolean,
            val punishmentLogMessage: String
        )
    }
}