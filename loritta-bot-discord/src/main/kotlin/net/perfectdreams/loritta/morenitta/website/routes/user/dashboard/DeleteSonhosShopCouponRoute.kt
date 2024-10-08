package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.SonhosShopView.Companion.sonhosBundles
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.SonhosBundle
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll

class DeleteSonhosShopCouponRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/sonhos-shop/coupon") {
	override suspend fun onDashboardAuthenticatedRequest(
		call: ApplicationCall,
		locale: BaseLocale,
		i18nContext: I18nContext,
		discordAuth: TemmieDiscordAuth,
		userIdentification: LorittaJsonWebSession.UserIdentification,
		colorTheme: ColorTheme
	) {
		val bundles = loritta.transaction {
			loritta.transaction {
				SonhosBundles.selectAll()
					.where { SonhosBundles.active eq true }
					.toList()
			}.map {
				SonhosBundle(
					it[SonhosBundles.id].value,
					it[SonhosBundles.active],
					it[SonhosBundles.price],
					it[SonhosBundles.sonhos],
					it[SonhosBundles.bonus]
				)
			}
		}

		call.response.headerHXTrigger {
			this.playSoundEffect = "recycle-bin"
			this.showSpicyToast(
				EmbeddedSpicyToast.Type.SUCCESS,
				"Cupom removido!"
			)
		}

		call.respondHtml(
			createHTML()
				.div {
					sonhosBundles(i18nContext, bundles, null)
				}
		)
	}
}