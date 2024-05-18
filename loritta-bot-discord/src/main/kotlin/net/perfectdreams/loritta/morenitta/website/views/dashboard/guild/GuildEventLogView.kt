package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.components.DiscordLikeToggles.toggleableSection
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureEventLogRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildEventLogView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    selectedType: String,
    val eventLog: ConfigureEventLogRoute.FakeEventLogConfig
) : GuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    guild,
    selectedType
) {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Website.Dashboard.EventLog
    }

    override fun DIV.generateRightSidebarContents() {
        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    // etherealGambiImg("https://stuff.loritta.website/loritta-daily-shop-allouette.png", classes = "hero-image", sizes = "(max-width: 900px) 100vw, 360px") {}

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18N_PREFIX.Title))
                        }

                        for (line in i18nContext.get(I18N_PREFIX.Description)) {
                            p {
                                text(line)
                            }
                        }
                    }
                }

                hr {}

                form {
                    id = "form-stuff"
                    method = FormMethod.post
                    attributes["loritta-synchronize-with-save-bar"] = "#save-bar"

                    toggleableSection(
                        "eventLogEnabled",
                        i18nContext.get(I18N_PREFIX.EnableEventLog),
                        null,
                        eventLog.isEnabled
                    ) {
                        div(classes = "field-wrappers") {
                            div(classes = "field-wrapper") {
                                div(classes = "field-title") {
                                    text(i18nContext.get(I18N_PREFIX.DefaultChannelWhereTheActionsWillBeSent))
                                }

                                div {
                                    style = "width: 100%;"

                                    discordChannelSelectMenu(
                                        lorittaWebsite,
                                        i18nContext,
                                        "eventLogChannelId",
                                        guild.textChannels,
                                        eventLog.eventLogChannelId,
                                        null
                                    )
                                }
                            }
                        }

                        hr {}

                        div(classes = "toggleable-sections") {
                            eventLogSection(
                                i18nContext,
                                "memberBanned",
                                i18nContext.get(I18N_PREFIX.Types.MemberBanned.Title),
                                null,
                                eventLog.memberBanned,
                                eventLog.memberBannedLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "memberUnbanned",
                                i18nContext.get(I18N_PREFIX.Types.MemberUnbanned.Title),
                                null,
                                eventLog.memberUnbanned,
                                eventLog.memberUnbannedLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "messageEdited",
                                i18nContext.get(I18N_PREFIX.Types.MessageEdited.Title),
                                i18nContext.get(I18N_PREFIX.Types.MessageEdited.Description),
                                eventLog.messageEdited,
                                eventLog.messageEditedLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "messageDeleted",
                                i18nContext.get(I18N_PREFIX.Types.MessageDeleted.Title),
                                i18nContext.get(I18N_PREFIX.Types.MessageDeleted.Description),
                                eventLog.messageDeleted,
                                eventLog.messageDeletedLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "nicknameChanges",
                                i18nContext.get(I18N_PREFIX.Types.NicknameChanges.Title),
                                null,
                                eventLog.nicknameChanges,
                                eventLog.nicknameChangesLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "avatarChanges",
                                i18nContext.get(I18N_PREFIX.Types.AvatarChanges.Title),
                                null,
                                eventLog.avatarChanges,
                                eventLog.avatarChangesLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "voiceChannelJoins",
                                i18nContext.get(I18N_PREFIX.Types.VoiceChannelJoins.Title),
                                null,
                                eventLog.voiceChannelJoins,
                                eventLog.voiceChannelJoinsLogChannelId
                            )

                            eventLogSection(
                                i18nContext,
                                "voiceChannelLeaves",
                                i18nContext.get(I18N_PREFIX.Types.VoiceChannelLeaves.Title),
                                null,
                                eventLog.voiceChannelLeaves,
                                eventLog.voiceChannelLeavesLogChannelId
                            )
                        }
                    }
                }
            }
        }

        hr {}

        // Maybe, with what little power you have... You can SAVE something else.
        div(classes = "save-bar-fill-screen-height") {}

        div(classes = "save-bar no-changes initial-state") {
            id = "save-bar"
            attributes["loritta-save-bar"] = "true"

            div(classes = "save-bar-small-text") {
                text("Deseja salvar?")
            }

            div(classes = "save-bar-large-text") {
                text("Cuidado! Você tem alterações que não foram salvas")
            }

            div(classes = "save-bar-buttons") {
                button(classes = "discord-button no-background-light-text") {
                    attributes["hx-get"] = ""
                    attributes["hx-select"] = "#form-stuff-wrapper"
                    attributes["hx-target"] = "#form-stuff-wrapper"
                    attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                    attributes["hx-disabled-elt"] = "this"
                    // We don't want to swap nor settle because that causes a flicker due to our custom select menu
                    attributes["hx-swap"] = "outerHTML settle:0ms swap:0ms"
                    attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                document.querySelector("#save-bar").classList.add("no-changes")
                                document.querySelector("#save-bar").classList.remove("has-changes")
                                window['spicy-morenitta'].playSoundEffect("recycle-bin")
                            }
                            """.trimIndent()

                    div(classes = "htmx-discord-like-loading-button") {
                        div {
                            text("Redefinir")
                        }

                        div(classes = "loading-text-wrapper") {
                            img(src = LoadingSectionComponents.list.random())

                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                        }
                    }
                }

                button(classes = "discord-button success") {
                    attributes["hx-post"] = ""
                    attributes["hx-include"] = "#form-stuff"
                    attributes["hx-select"] = "#form-stuff-wrapper"
                    attributes["hx-target"] = "#form-stuff-wrapper"
                    attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                    attributes["hx-disabled-elt"] = "this"
                    // We don't want to swap nor settle because that causes a flicker due to our custom select menu
                    attributes["hx-swap"] = "outerHTML settle:0ms swap:0ms"
                    attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                document.querySelector("#save-bar").classList.add("no-changes")
                                document.querySelector("#save-bar").classList.remove("has-changes")
                            }
                            """.trimIndent()

                    div(classes = "htmx-discord-like-loading-button") {
                        div {
                            text("Salvar")
                        }

                        div(classes = "loading-text-wrapper") {
                            img(src = LoadingSectionComponents.list.random())

                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.eventLogSection(
        i18nContext: I18nContext,
        checkboxName: String,
        title: String,
        description: String?,
        enabled: Boolean,
        eventLogChannelId: Long?
    ) {
        toggleableSection(
            checkboxName,
            title,
            description,
            enabled
        ) {
            div(classes = "field-wrappers") {
                div(classes = "field-wrapper") {
                    div(classes = "field-title") {
                        text(i18nContext.get(I18N_PREFIX.ChannelWhereTheActionsWillBeSent))
                    }

                    div {
                        style = "width: 100%;"

                        discordChannelSelectMenu(
                            lorittaWebsite,
                            i18nContext,
                            "${checkboxName}LogChannelId",
                            guild.textChannels,
                            eventLogChannelId
                        ) {
                            text(i18nContext.get(I18N_PREFIX.UseDefaultChannel))
                        }
                    }
                }
            }
        }
    }
}