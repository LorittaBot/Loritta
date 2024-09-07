package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.views.dashboard.DashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

abstract class GuildDashboardView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    val guild: Guild,
    val selectedType: String,
) : DashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme
) {
    // TODO - htmx-adventures: Proper titles?
    override fun getTitle() = "Painel de Controle"

    override fun NAV.generateLeftSidebarContents() {
        div(classes = "entries") {
            div(classes = "guild-icon-wrapper") {
                style = "text-align: center;"

                img(src = guild.iconUrl)
            }

            div(classes = "entry guild-name") {
                text(guild.name)
            }

            a(href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard") {
                // TODO - htmx-adventures: Can't we reuse the appendEntry somehow?
                //  we need to replace the entire inner-wrapper because we are switching from two different "dashboards", the left sidebar is completely different
                attributes["hx-select"] = "#wrapper"
                attributes["hx-target"] = "#wrapper"
                attributes["hx-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard"
                attributes["hx-indicator"] = "#right-sidebar-wrapper"
                attributes["hx-push-url"] = "true"
                // show:top - Scroll to the top
                // settle:0ms - We don't want the settle animation beccause it is a full page swap
                // swap:0ms - We don't want the swap animation because it is a full page swap
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                attributes["_"] = """
                                                            on click
                                                                remove .is-open from #left-sidebar
                                                                add .is-closed to #left-sidebar
                                                            end
                                                """.trimIndent()

                button(classes = "discord-button no-background-theme-dependent-dark-text") {
                    style = "width: 100%;"
                    type = ButtonType.button
                    text("Voltar ao Painel de Usuário")
                }
            }

            hr(classes = "divider") {}

            appendEntry("/guild/${guild.id}/configure", false, locale["modules.sectionNames.general"], "fa fa-cogs", false)
            appendEntry("/guild/${guild.id}/configure/moderation", false, locale["modules.sectionNames.moderation"], "fas fa-exclamation-circle", false)
            appendEntry("/guild/${guild.id}/configure/commands", true, locale["modules.sectionNames.commands"], "fa fa-terminal", false)
            appendEntry("/guild/${guild.id}/configure/permissions", false, locale["modules.sectionNames.permissions"], "fa fa-address-card", false)

            appendEntry(
                "${lorittaWebsite.loritta.config.loritta.website.spicyMorenittaDashboardUrl.removeSuffix("/")}/guilds/${guild.id}/configure/gamersafer-verify",
                false,
                "GamerSafer",
                "fas fa-list",
                false
            )

            hr(classes = "divider") {}

            div(classes = "category") {
                + legacyBaseLocale.strings["DASHBOARD_Notifications"]!!
            }

            appendEntry("/guild/${guild.id}/configure/welcomer", true, locale["modules.sectionNames.welcomer"], "fa fa-sign-in-alt", false)
            appendEntry("/guild/${guild.id}/configure/event-log", true, locale["modules.sectionNames.eventLog"], "fa fa-eye", false)
            appendEntry("/guild/${guild.id}/configure/youtube", true, "YouTube", "fab fa-youtube", false)
            appendEntry("/guild/${guild.id}/configure/twitch", true, "Twitch", "fab fa-twitch", false)
            appendEntry("/guild/${guild.id}/configure/bluesky", true, "Bluesky", "fab fa-bluesky", true)

            hr(classes = "divider") {}

            div(classes = "category") {
                + legacyBaseLocale.strings["CommandCategory_MISC_Name"]!!
            }

            appendEntry("/guild/${guild.id}/configure/level", false, locale["modules.sectionNames.levelUp"], "fas fa-award", false)
            appendEntry("/guild/${guild.id}/configure/autorole", false, locale["modules.sectionNames.autorole"], "fa fa-briefcase", false)
            appendEntry("/guild/${guild.id}/configure/invite-blocker", false, locale["modules.sectionNames.inviteBlocker"], "fa fa-ban", false)
            appendEntry("/guild/${guild.id}/configure/member-counter", false, locale["modules.sectionNames.memberCounter"], "fas fa-sort-amount-up", false)
            appendEntry(
                "${lorittaWebsite.loritta.config.loritta.website.spicyMorenittaDashboardUrl.removeSuffix("/")}/guilds/${guild.id}/configure/starboard",
                false,
                locale["modules.sectionNames.starboard"],
                "fa fa-star",
                false
            )
            appendEntry("/guild/${guild.id}/configure/miscellaneous", false, "+${legacyBaseLocale.strings["CommandCategory_MISC_Name"]}", "fas fa-random", false)
            appendEntry("/guild/${guild.id}/configure/audit-log", false, locale["modules.auditLog.title"], "fas fa-list", false)

            hr(classes = "divider") {}

            div(classes = "category") {
                + "Premium"
            }

            appendEntry("/guild/${guild.id}/configure/premium", false, locale["modules.sectionNames.premiumKeys"], "fas fa-gift", false)

            appendEntry("/guild/${guild.id}/configure/badge", false, locale["modules.sectionNames.customBadge"], "fas fa-certificate", false)

            appendEntry("/guild/${guild.id}/configure/daily-multiplier", false, locale["modules.sectionNames.dailyMultiplier"], "fas fa-times", false)

            hr(classes = "divider") {}

            div(classes = "category") {
                + legacyBaseLocale.strings["DASHBOARD_YourFeatures"]!!
            }
            appendEntry(
                "${lorittaWebsite.loritta.config.loritta.website.spicyMorenittaDashboardUrl.removeSuffix("/")}/guilds/${guild.id}/configure/custom-commands",
                false,
                locale["modules.sectionNames.customCommands"],
                "fas fa-code",
                false
            )

            hr(classes = "divider") {}

            appendEntry(
                "/support",
                false,
                locale["website.navbar.support"],
                "fas fa-question-circle",
                false
            )
        }
    }
}