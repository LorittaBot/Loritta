package net.perfectdreams.loritta.plugin.htmlprovider

import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.sweetmorenitta.views.DailyView
import net.perfectdreams.loritta.sweetmorenitta.views.DonateView
import net.perfectdreams.loritta.sweetmorenitta.views.HomeView
import net.perfectdreams.loritta.sweetmorenitta.views.SupportView
import net.perfectdreams.loritta.sweetmorenitta.views.user.UserReputationView
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import net.perfectdreams.loritta.website.utils.RouteKey

class JVMHtmlProvider : LorittaHtmlProvider {
    override fun render(page: String, arguments: List<Any?>): String {
        if (page == RouteKey.SUPPORT) {
            return SupportView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.HOME) {
            return HomeView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.USER_REPUTATION) {
            return UserReputationView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String,
                    arguments[2] as LorittaJsonWebSession.UserIdentification?,
                    arguments[3] as User?,
                    arguments[4] as Reputation?,
                    arguments[5] as List<Reputation>,
                    arguments[6] as Long?,
                    arguments[7] as Long?,
                    arguments[8] as String
            ).generateHtml()
        }

        if (page == RouteKey.DONATE) {
            return DonateView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String,
                    arguments[2] as LorittaJsonWebSession.UserIdentification?,
                    arguments[3] as JsonArray
            ).generateHtml()
        }

        if (page == RouteKey.DAILY) {
            return DailyView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String
            ).generateHtml()
        }

        throw RuntimeException("Can't process page \"$page\"")
    }
}