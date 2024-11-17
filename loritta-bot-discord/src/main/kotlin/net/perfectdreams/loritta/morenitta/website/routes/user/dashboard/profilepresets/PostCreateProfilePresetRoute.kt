package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.profilepresets

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXPushURL
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.ProfilePresetsListView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostCreateProfilePresetRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/profile-presets/create") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val parameters = call.receiveParameters()
		val activeProfileDesignId = parameters.getOrFail("activeProfileDesignId")
		val activeBackgroundId = parameters.getOrFail("activeBackgroundId")
		val presetName = parameters.getOrFail("presetName").trim()

		if (presetName.length !in 0..50)
			error("Preset name too long")

		val result = loritta.transaction {
			val totalPresets = UserCreatedProfilePresets.selectAll()
				.where {
					UserCreatedProfilePresets.createdBy eq userIdentification.id.toLong()
				}
				.count()

			if (totalPresets + 1 > ProfilePresetsListView.MAX_PROFILE_PRESETS)
				return@transaction Result.TooManyPresets

			UserCreatedProfilePresets.insert {
				it[UserCreatedProfilePresets.createdBy] = userIdentification.id.toLong()
				it[UserCreatedProfilePresets.createdAt] = Instant.now()
				it[UserCreatedProfilePresets.name] = presetName
				it[UserCreatedProfilePresets.profileDesign] = activeProfileDesignId
				it[UserCreatedProfilePresets.background] = activeBackgroundId
			}

			val profilePresets = loritta.transaction {
				UserCreatedProfilePresets.selectAll()
					.where {
						UserCreatedProfilePresets.createdBy eq userIdentification.id.toLong()
					}
					.toList()
			}

			return@transaction Result.Success(profilePresets)
		}

		when (result) {
			is Result.Success -> {
				val view = ProfilePresetsListView(
					loritta.newWebsite!!,
					i18nContext,
					locale,
					getPathWithoutLocale(call),
					loritta.getLegacyLocaleById(locale.id),
					userIdentification,
					UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
					colorTheme,
					result.profilePresets
				)

				call.response.headerHXTrigger {
					playSoundEffect = "config-saved"
					closeSpicyModal = true
					showSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Pré-definição criada!")
				}

				call.response.headerHXPushURL("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets")

				call.respondHtml(
					view.generateHtml()
				)
			}
			Result.TooManyPresets -> {
				call.response.headerHXTrigger {
					playSoundEffect = "config-error"
					closeSpicyModal = true
					showSpicyToast(EmbeddedSpicyToast.Type.WARN, "Você já tem muitas pré-definições criadas!")
				}
			}
		}
	}

	private sealed class Result {
		data class Success(val profilePresets: List<ResultRow>) : Result()
		data object TooManyPresets : Result()
	}
}