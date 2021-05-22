package net.perfectdreams.loritta.plugin.htmlprovider

import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.Reputation
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.sweetmorenitta.views.BlogPostView
import net.perfectdreams.loritta.sweetmorenitta.views.BlogView
import net.perfectdreams.loritta.sweetmorenitta.views.CommunityGuidelinesView
import net.perfectdreams.loritta.sweetmorenitta.views.DailyView
import net.perfectdreams.loritta.sweetmorenitta.views.DonateView
import net.perfectdreams.loritta.sweetmorenitta.views.Error404View
import net.perfectdreams.loritta.sweetmorenitta.views.FanArtArtistView
import net.perfectdreams.loritta.sweetmorenitta.views.FanArtsView
import net.perfectdreams.loritta.sweetmorenitta.views.HomeView
import net.perfectdreams.loritta.sweetmorenitta.views.SponsorRedirectView
import net.perfectdreams.loritta.sweetmorenitta.views.SponsorsView
import net.perfectdreams.loritta.sweetmorenitta.views.SupportView
import net.perfectdreams.loritta.sweetmorenitta.views.UserBannedView
import net.perfectdreams.loritta.sweetmorenitta.views.landingpages.BrazilianBotLandingPageView
import net.perfectdreams.loritta.sweetmorenitta.views.user.UserReputationView
import net.perfectdreams.loritta.utils.Sponsor
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.website.blog.Post
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import net.perfectdreams.loritta.website.utils.RouteKey
import org.jetbrains.exposed.sql.ResultRow

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

        if (page == RouteKey.SPONSOR_REDIRECT) {
            return SponsorRedirectView(
                arguments[1] as BaseLocale,
                arguments[0] as String,
                arguments[2] as Sponsor
            ).generateHtml()
        }

        if (page == RouteKey.ERROR_404) {
            return Error404View(
                arguments[1] as BaseLocale,
                arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.SPONSORS) {
            return SponsorsView(
                arguments[1] as BaseLocale,
                arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.USER_BANNED) {
            return UserBannedView(
                arguments[1] as BaseLocale,
                arguments[0] as String,
                arguments[2] as Profile,
                arguments[3] as ResultRow
            ).generateHtml()
        }

        if (page == RouteKey.COMMUNITY_GUIDELINES) {
            return CommunityGuidelinesView(
                arguments[1] as BaseLocale,
                arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.FAN_ARTS) {
            return FanArtsView(
                arguments[1] as BaseLocale,
                arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.FAN_ART_ARTIST) {
            return FanArtArtistView(
                arguments[1] as BaseLocale,
                arguments[0] as String,
                arguments[2] as FanArtArtist,
                arguments[3] as JDAUser?
            ).generateHtml()
        }

        if (page == RouteKey.BLOG) {
            return BlogView(
                arguments[1] as BaseLocale,
                arguments[0] as String,
                arguments[2] as List<Post>
            ).generateHtml()
        }

        if (page == RouteKey.BLOG_POST) {
            return BlogPostView(
                arguments[1] as BaseLocale,
                arguments[0] as String,
                arguments[2] as Post
            ).generateHtml()
        }

        if (page == RouteKey.BRAZILIAN_BOT) {
            return BrazilianBotLandingPageView(
                arguments[1] as BaseLocale,
                arguments[0] as String
            ).generateHtml()
        }

        throw RuntimeException("Can't process page \"$page\"")
    }
}