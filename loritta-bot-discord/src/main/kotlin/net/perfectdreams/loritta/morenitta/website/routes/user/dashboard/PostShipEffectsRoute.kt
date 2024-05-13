package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.input
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.components.InlineNullableUserDisplay.inlineNullableUserDisplay
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.user.ShipEffectsView.Companion.buyShipEffectButton
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.component1
import kotlin.collections.component2

class PostShipEffectsRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/ship-effects") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()

		val userSearch = DiscordUserInputResult.parse(params.getOrFail("userSearch"))

		if (userSearch is DiscordUserInputResult.DiscordParseFailure) {
			when (userSearch) {
				DiscordUserInputResult.InvalidDiscriminator -> call.respondHtml(
					createHTML()
						.div(classes = "validation error") {
							buyShipEffectButton(i18nContext, true, true)

							div(classes = "icon") {
								i("fa-solid fa-triangle-exclamation")
							}

							div {
								text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.InvalidDiscriminator))
							}
						}
				)

				DiscordUserInputResult.MissingDiscriminator -> call.respondHtml(
					createHTML()
						.div(classes = "validation error") {
							buyShipEffectButton(i18nContext, true, true)

							div(classes = "icon") {
								i("fa-solid fa-triangle-exclamation")
							}

							div {
								text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.MissingDiscriminator))
							}
						}
				)

				DiscordUserInputResult.Empty -> call.respondHtml(
					createHTML()
						.div(classes = "validation error") {
							buyShipEffectButton(i18nContext, true, true)

							div(classes = "icon") {
								i("fa-solid fa-triangle-exclamation")
							}

							div {
								text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.Tip))
							}
						}
				)
			}
			return
		}

		if (userSearch is DiscordUserInputResult.DiscordParseSuccess) {
			val cachedUserInfo = when (userSearch) {
				is DiscordUserInputResult.DiscordIdInput -> {
					loritta.pudding.users.getCachedUserInfoById(userSearch.userId)
				}

				is DiscordUserInputResult.DiscordPomeloInput -> {
					loritta.pudding.users.getCachedUserInfoByPomeloName(userSearch.name)
				}

				is DiscordUserInputResult.DiscordTagInput -> {
					val (name, discriminator) = userSearch.tag.split("#")

					loritta.pudding.users.getCachedUserInfoByNameAndDiscriminator(name, discriminator)
				}
			}

			if (cachedUserInfo != null) {
				call.respondHtml(
					createHTML()
						.div(classes = "validation success") {
							buyShipEffectButton(i18nContext, false, true)

							inlineNullableUserDisplay(cachedUserInfo.id.value.toLong(), cachedUserInfo)

							input {
								this.type = InputType.hidden
								this.name = "receivingEffectUserId"
								this.value = cachedUserInfo.id.value.toString()
							}
						}
				)
			} else {
				// TODO: Query via Discord's API too
				call.respondHtml(
					createHTML()
						.div(classes = "validation error") {
							buyShipEffectButton(i18nContext, true, true)

							div(classes = "icon") {
								i("fa-solid fa-triangle-exclamation")
							}

							div {
								text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.UnknownUser))
							}
						}
				)
			}
			return
		}
	}

	sealed class DiscordUserInputResult {
		companion object {
			fun parse(input: String): DiscordUserInputResult {
				if (input.isBlank())
					return Empty

				val trimmedInput = input.trim() // The user may have copied the text with spaces, so let's remove it
					.replace("@", "") // Usernames cannot have @, so let's remove it

				val valueAsLong = trimmedInput.toLongOrNull()
				if (valueAsLong == null) {
					val split = trimmedInput.split("#")
					if (split.size != 2)
						return DiscordPomeloInput(
							split
								.first()
								.substringBefore(" ")
								.lowercase() // All pomelo names are in lowercase
						)

					val (name, discriminator) = split
					val trimmedName = name.trim()
					val trimmedDiscriminator = discriminator.trim()

					if (trimmedDiscriminator.isBlank()) {
						return MissingDiscriminator
					}

					if (trimmedDiscriminator.length != 4)
						return InvalidDiscriminator

					val discriminatorAsInt = trimmedDiscriminator.toIntOrNull()
					if (discriminatorAsInt == null || discriminatorAsInt !in 1..9999)
						return InvalidDiscriminator

					return DiscordTagInput("${trimmedName}#${trimmedDiscriminator}")
				}

				return DiscordIdInput(UserId(valueAsLong))
			}
		}

		sealed class DiscordParseFailure : DiscordUserInputResult()
		object Empty : DiscordParseFailure()
		object MissingDiscriminator : DiscordParseFailure()
		object InvalidDiscriminator : DiscordParseFailure()
		sealed class DiscordParseSuccess : DiscordUserInputResult()
		class DiscordTagInput(val tag: String) : DiscordParseSuccess()
		class DiscordPomeloInput(val name: String) : DiscordParseSuccess()
		class DiscordIdInput(val userId: UserId) : DiscordParseSuccess()
	}
}