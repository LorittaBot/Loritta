package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.profilepresets

import io.ktor.server.application.*
import io.ktor.server.util.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.ProfilePresetsListView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

class DeleteProfilePresetRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/profile-presets/{presetId}") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val presetId = call.parameters.getOrFail("presetId").toLong()

		val profilePresets = loritta.transaction {
			UserCreatedProfilePresets.deleteWhere {
				UserCreatedProfilePresets.id eq presetId and (UserCreatedProfilePresets.createdBy eq userIdentification.id.toLong())
			}

				UserCreatedProfilePresets.selectAll()
					.where {
						UserCreatedProfilePresets.createdBy eq userIdentification.id.toLong()
					}
					.toList()
		}

		val view = ProfilePresetsListView(
			loritta.newWebsite!!,
			i18nContext,
			locale,
			getPathWithoutLocale(call),
			loritta.getLegacyLocaleById(locale.id),
			userIdentification,
			UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
			colorTheme,
			profilePresets
		)

		call.response.headerHXTrigger {
			playSoundEffect = "config-saved"
			closeSpicyModal = true
			showSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Pré-definição deletada!")
		}

		call.respondHtml(
			view.generateHtml()
		)
	}
}