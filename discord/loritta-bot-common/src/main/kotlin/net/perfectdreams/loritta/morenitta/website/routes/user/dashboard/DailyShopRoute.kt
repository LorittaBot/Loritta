package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class DailyShopRoute(loritta: LorittaBot) :
    RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/daily-shop") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        val variables = call.legacyVariables(loritta, locale)

        variables["saveType"] = "daily_shop"

        call.respondHtml(evaluate("profile_dashboard_daily_shop.html", variables))
    }

    override suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale) {
        if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
            call.respondHtml(
                createHTML().html {
                    head {
                        fun setMetaProperty(property: String, content: String) {
                            meta(content = content) { attributes["property"] = property }
                        }
                        title("Login • Loritta")
                        setMetaProperty("og:site_name", "Loritta")
                        setMetaProperty("og:title", "Loja Diária")
                        setMetaProperty(
                            "og:description",
                            "Bem-vind@ a loja diária de itens! O lugar para comprar itens para o seu \"+perfil\" da Loritta!\n\nTodo o dia as 00:00 UTC (21:00 no horário do Brasil) a loja é atualizada com novos itens! Então volte todo o dia para verificar ^-^"
                        )
                        setMetaProperty(
                            "og:image",
                            loritta.config.loritta.website.url + "assets/img/loritta_daily_shop.png"
                        )
                        setMetaProperty("og:image:width", "320")
                        setMetaProperty("og:ttl", "660")
                        setMetaProperty("og:image:width", "320")
                        setMetaProperty("theme-color", "#7289da")
                        meta("twitter:card", "summary_large_image")
                    }
                    body {
                        p {
                            +"Parabéns, você encontrou um easter egg!"
                        }
                    }
                }
            )
        }
        return super.onUnauthenticatedRequest(call, locale)
    }
}