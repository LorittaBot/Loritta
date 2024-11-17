package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.profilepresets

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PostApplyProfilePresetRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/profile-presets/{presetId}/apply") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val presetId = call.parameters.getOrFail("presetId").toLong()

		loritta.transaction {
			val presetData = UserCreatedProfilePresets.selectAll()
				.where {
					UserCreatedProfilePresets.id eq presetId and (UserCreatedProfilePresets.createdBy eq userIdentification.id.toLong())
				}
				.firstOrNull()

			if (presetData == null)
				return@transaction Result.PresetNotFound

			val profile = loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())
			UserCreatedProfilePresets.update({ UserCreatedProfilePresets.id eq presetData[UserCreatedProfilePresets.id] }) {
				it[UserCreatedProfilePresets.lastUsedAt] = Instant.now()
			}
			profile.settings.activeProfileDesignInternalName = presetData[UserCreatedProfilePresets.profileDesign]
			profile.settings.activeBackgroundInternalName = presetData[UserCreatedProfilePresets.background]

			return@transaction Result.Success
		}

		// call.response.header("HX-Redirect", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/profile-presets")

		call.respondBodyAsHXTrigger(
			HttpStatusCode.OK
		) {
			playSoundEffect = "config-saved"
			closeSpicyModal = true
			showSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Predefinição aplicada!")
		}
	}

	private sealed class Result {
		data object Success : Result()
		data object PresetNotFound : Result()
	}
}