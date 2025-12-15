package net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects

import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.InlineNullableUserDisplay.inlineNullableUserDisplay
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.shipBuyButton
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.UserId

class PostShipEffectsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/ship-effects") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val userSearch = DiscordUserInputResult.parse(call.parameters.getOrFail("userQuery"))

        if (userSearch is DiscordUserInputResult.DiscordParseFailure) {
            when (userSearch) {
                DiscordUserInputResult.InvalidDiscriminator -> call.respondHtmlFragment {
                    div(classes = "input-result") {
                        div(classes = "validation error") {
                            div(classes = "icon") {
                                i("fa-solid fa-triangle-exclamation")
                            }

                            div {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.InvalidDiscriminator))
                            }
                        }
                    }

                    shipBuyButton(i18nContext, false)
                }

                DiscordUserInputResult.MissingDiscriminator -> call.respondHtmlFragment {
                    div(classes = "input-result") {
                        div(classes = "validation error") {
                            div(classes = "icon") {
                                i("fa-solid fa-triangle-exclamation")
                            }

                            div {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.MissingDiscriminator))
                            }
                        }
                    }

                    shipBuyButton(i18nContext, false)
                }

                DiscordUserInputResult.Empty -> call.respondHtmlFragment {
                    div(classes = "input-result") {
                        div(classes = "validation error") {
                            div(classes = "icon") {
                                i("fa-solid fa-triangle-exclamation")
                            }

                            div {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.Tip))
                            }
                        }
                    }

                    shipBuyButton(i18nContext, false)
                }
            }
            return
        }

        if (userSearch is DiscordUserInputResult.DiscordParseSuccess) {
            val cachedUserInfo = when (userSearch) {
                is DiscordUserInputResult.DiscordIdInput -> {
                    website.loritta.pudding.users.getCachedUserInfoById(userSearch.userId)
                }

                is DiscordUserInputResult.DiscordPomeloInput -> {
                    website.loritta.pudding.users.getCachedUserInfoByPomeloName(userSearch.name)
                }

                is DiscordUserInputResult.DiscordTagInput -> {
                    val (name, discriminator) = userSearch.tag.split("#")

                    website.loritta.pudding.users.getCachedUserInfoByNameAndDiscriminator(name, discriminator)
                }
            }

            if (cachedUserInfo != null) {
                call.respondHtmlFragment {
                    div(classes = "input-result") {
                        div(classes = "validation success") {
                            inlineNullableUserDisplay(cachedUserInfo.id.value.toLong(), cachedUserInfo)

                            input {
                                this.type = InputType.hidden
                                this.name = "receivingEffectUserId"
                                this.value = cachedUserInfo.id.value.toString()
                            }
                        }
                    }

                    shipBuyButton(i18nContext, true)
                }
            } else {
                // TODO: Query via Discord's API too
                call.respondHtmlFragment {
                    div(classes = "input-result") {
                        div(classes = "validation error") {
                            div(classes = "icon") {
                                i("fa-solid fa-triangle-exclamation")
                            }

                            div {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.UnknownUser))
                            }
                        }
                    }

                    shipBuyButton(i18nContext, false)
                }
            }
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