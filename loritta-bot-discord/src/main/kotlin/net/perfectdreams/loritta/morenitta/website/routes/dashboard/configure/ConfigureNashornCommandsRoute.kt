package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonArray
import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.evaluate
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.LegacyPebbleGuildDashboardRawHtmlView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll

class ConfigureNashornCommandsRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/nashorn") {
    override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
        val nashornCommands = loritta.newSuspendedTransaction {
            CustomGuildCommands.selectAll().where {
                CustomGuildCommands.guild eq serverConfig.id.value
            }.toList()
        }

        val variables = call.legacyVariables(loritta, locale)

        variables["saveType"] = "nashorn_commands"

        val feeds = JsonArray()
        nashornCommands.forEach {
            feeds.add(
                    jsonObject(
                            "jsLabel" to it[CustomGuildCommands.label],
                            "javaScript" to it[CustomGuildCommands.code]
                    )
            )
        }

        variables["commands"] = feeds.toString()

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
                evaluate("configure_nashorn.html", variables),
                "nashorn_commands"
            ).generateHtml()
        )
    }
}