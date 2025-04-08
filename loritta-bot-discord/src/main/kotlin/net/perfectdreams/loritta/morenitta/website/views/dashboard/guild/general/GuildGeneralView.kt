package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.general

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildGeneralConfigBootstrap
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildGeneralView(
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
    val bootstrap: GuildGeneralConfigBootstrap
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
    override fun DIV.generateRightSidebarContents() {
        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    div(classes = "hero-image") {
                        div(classes = "welcomer-web-animation") {
                            etherealGambiImg(src = "https://stuff.loritta.website/loritta-welcomer-heathecliff.png", sizes = "350px") {
                                style = "height: 100%; width: 100%;"
                            }

                            span(classes = "welcome-wumpus-message") {
                                text("Welcome, ")
                                span(classes = "discord-mention") {
                                    text("@Wumpus")
                                }
                                text("!")

                                img(src = "https://cdn.discordapp.com/emojis/417813932380520448.png?v=1", classes = "discord-inline-emoji")
                            }
                        }
                    }

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Welcomer.Title))
                        }

                        p {
                            text("Anuncie quem está entrando e saindo do seu servidor da maneira que você queria! Envie mensagens para novatos via mensagem direta com informações sobre o seu servidor para não encher o chat com informações repetidas e muito mais!")
                        }
                    }
                }

                hr {}

                div {
                    id = "module-config-wrapper"

                    div {
                        attributes["data-component-mounter"] = "test-general"
                        attributes["config"] = Json.encodeToString(bootstrap)
                    }
                }
            }
        }
    }
}