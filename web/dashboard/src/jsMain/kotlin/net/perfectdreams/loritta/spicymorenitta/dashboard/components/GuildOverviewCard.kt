package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.GuildCardsStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.Constants
import net.perfectdreams.loritta.webapi.data.PartialDiscordGuild
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun GuildOverviewCard(guildData: PartialDiscordGuild) {
    A(href = Constants.LORITTA_WEBSITE_URL + "/guild/${guildData.id}/configure/", attrs = { classes(GuildCardsStylesheet.guildOverviewCard, AppStylesheet.resetLinkStyle) }) {
        Div {
            Img(src = guildData.icon ?: "") {
                classes(GuildCardsStylesheet.guildOverviewCardIcon)
            }
        }

        Div {
            Div {
                var hasBadges = false
                Div(attrs = {
                    style {
                        display(DisplayStyle.LegacyInlineFlex)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    // Verified badge takes priority in the Discord Client
                    if (guildData.features.contains("VERIFIED")) {
                        hasBadges = true
                        DiscordVerifiedBadge()
                    } else if (guildData.features.contains("PARTNERED")) {
                        hasBadges = true
                        DiscordPartnerBadge()
                    }

                    Span {
                        if (hasBadges)
                            Span {
                                Text(" ")
                            }

                        Text(guildData.name)
                    }
                }
            }
        }
    }
}