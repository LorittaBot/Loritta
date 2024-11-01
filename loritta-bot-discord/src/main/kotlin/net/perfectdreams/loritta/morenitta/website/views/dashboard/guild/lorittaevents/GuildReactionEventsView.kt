package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.lorittaevents

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEvent
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordLikeToggles.discordToggle
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildReactionEventsConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildReactionEventsView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    private val activeEvent: ReactionEvent?,
    private val reactionEventsConfig: GuildReactionEventsConfig,
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
    "reaction_events"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    // img(src = "https://stuff.loritta.website/monica-ata-bluetero.jpeg", classes = "hero-image") {}

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.Title))
                        }

                        for (str in i18nContext.language
                            .textBundle
                            .lists
                            .getValue(I18nKeys.Website.Dashboard.ReactionEvents.Description.key)
                        ) {
                            p {
                                handleI18nString(
                                    str,
                                    appendAsFormattedText(i18nContext, emptyMap()),
                                ) {
                                    when (it) {
                                        "commandMention" -> {
                                            TextReplaceControls.ComposableFunctionResult {
                                                span(classes = "discord-mention") {
                                                    text("/evento entrar")
                                                }
                                            }
                                        }

                                        else -> TextReplaceControls.AppendControlAsIsResult
                                    }
                                }
                            }
                        }

                        if (activeEvent != null) {
                            div(classes = "alert alert-success") {
                                handleI18nString(
                                    i18nContext,
                                    I18nKeys.Website.Dashboard.ReactionEvents.EventStatus.EventIsHappening,
                                    appendAsFormattedText(i18nContext, emptyMap()),
                                ) {
                                    when (it) {
                                        "eventName" -> {
                                            TextReplaceControls.ComposableFunctionResult {
                                                b {
                                                    text(activeEvent.createEventTitle(i18nContext))
                                                }
                                            }
                                        }
                                        else -> TextReplaceControls.AppendControlAsIsResult
                                    }
                                }
                            }
                        } else {
                            div(classes = "alert alert-danger") {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.EventStatus.EventIsNotHappening))
                            }
                        }
                    }
                }

                hr {}

                div {
                    id = "module-config-wrapper"
                    form {
                        id = "module-config"
                        attributes["loritta-synchronize-with-save-bar"] = "#save-bar"

                        discordToggle(
                            "enabled",
                            i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.EnableReactionEvents),
                            i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.DescriptionReactionEvents),
                            reactionEventsConfig.enabled,
                            {}
                        )
                    }
                }

                hr {}

                lorittaSaveBar(
                    i18nContext,
                    false,
                    {}
                ) {
                    attributes["hx-put"] = ""
                }
            }
        }
    }
}