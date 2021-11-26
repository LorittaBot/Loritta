package net.perfectdreams.loritta.spicymorenitta.dashboard.styles

import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.selectors.className
import org.jetbrains.compose.web.css.selectors.hover
import org.jetbrains.compose.web.css.selectors.plus
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.transform

object GuildCardsStylesheet : StyleSheet(AppStylesheet) {
    val guildOverviewCardsGrid by style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("repeat(auto-fill, 150px)")
        gap(1.em)
        justifyContent(JustifyContent.Center)
    }

    @OptIn(ExperimentalComposeWebApi::class)
    val guildOverviewCard by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        flexDirection(FlexDirection.Column)
        backgroundColor(Color("#f2f3f5"))
        borderRadius(7.px)
        property("transition-duration", "0.5s")
        minWidth(100.px)
        minHeight(100.px)
        justifyContent(JustifyContent.Center)
        textAlign("center")
        property("aspect-ratio", "1/1") // Makes all grid cells square

        self + hover() style {
            property("box-shadow", "0px 0px 10px rgba(182, 182, 182, 0.7)")
            transform {
                scale(1.05)
            }
        }
    }

    val guildOverviewCardIcon by style {
        height(64.px)
        // TODO: Because the style is applied waaaay too late, the transition is applied on page refresh
        borderRadius(50.percent)
        property("transition-duration", "0.5s")
    }

    init {
        // https://kotlinlang.slack.com/archives/C01F2HV7868/p1637156519139700?thread_ts=1637156187.139600&cid=C01F2HV7868
        ".GuildCardsStylesheet-guildOverviewCard:hover .GuildCardsStylesheet-guildOverviewCardIcon" {
            borderRadius(25.percent)
        }
    }
}