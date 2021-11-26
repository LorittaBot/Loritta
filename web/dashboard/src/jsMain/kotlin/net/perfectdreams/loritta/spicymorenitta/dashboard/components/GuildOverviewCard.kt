package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.discord.PartialDiscordGuild
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.GuildCardsStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.Constants
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun GuildOverviewCard(guildData: PartialDiscordGuild) {
    A(href = Constants.LORITTA_WEBSITE_URL + "/guild/${guildData.id}/configure/", attrs = { classes(GuildCardsStylesheet.guildOverviewCard, AppStylesheet.resetLinkStyle) }) {
        Div {
            // TODO: Add default icon if not present
            val extension = if (guildData.name.startsWith("a_")) {
                "gif"
            } else "png"

            val discordIconUrl = "https://cdn.discordapp.com/icons/${guildData.id}/${guildData.icon}.$extension?size=128"
            Img(src = discordIconUrl ?: "") {
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