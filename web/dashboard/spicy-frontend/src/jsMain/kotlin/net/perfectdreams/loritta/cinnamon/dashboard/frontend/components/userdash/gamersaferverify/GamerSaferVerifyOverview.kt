package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.gamersaferverify

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildGamerSaferVerifyScreen
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.DiscordGuild
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun GamerSaferVerifyOverview(m: LorittaDashboardFrontend, screen: ConfigureGuildGamerSaferVerifyScreen, i18nContext: I18nContext, guild: DiscordGuild, ) {
    Div(attrs = {
        attr("style", "text-align: center;")
    }) {
        H1 {
            Text("GamerSafer")
        }
    }

    Div {
        for (text in i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.Description)) {
            P {
                Text(text)
            }
        }

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    gap(1.em)
                    justifyContent(JustifyContent.Center)
                }
            }
        ) {
            A(href = "https://discord.com/api/oauth2/authorize?client_id=1037108339538153584&permissions=8&redirect_uri=https%3A%2F%2Fdefender.gamersafer.systems%2Fapi%2Fauth%2Fsignin%3Fsource%3Dloritta&response_type=code&scope=identify%20applications.commands%20bot") {
                DiscordButton(
                    DiscordButtonType.PRIMARY
                ) {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.AddBot))
                }
            }

            A(href = "https://docs.gamersafer.com/") {
                DiscordButton(
                    DiscordButtonType.PRIMARY
                ) {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.Docs))
                }
            }

            A(href = "https://discord.com/invite/65UjScXNFg") {
                DiscordButton(
                    DiscordButtonType.PRIMARY
                ) {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.DiscordGamerSafer))
                }
            }
        }
    }
}