package net.perfectdreams.loritta.spicymorenitta.dashboard.styles

import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.deg
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexShrink
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.selectors.hover
import org.jetbrains.compose.web.css.selectors.plus
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.textDecoration
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.transform
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width

object AppStylesheet : StyleSheet() {
    init {
        "body" style {
            margin(0.px)
        }

        "*" style {
            boxSizing("border-box")
        }
    }

    val wrapper by style {
        display(DisplayStyle.Flex)
        minHeight(100.vh)
        fontFamily("Lato", "Arial", "sans-serif")
        backgroundColor(Color("#fcfcfc"))
    }

    val guildsSidebar by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Start)
        flexDirection(FlexDirection.Column)
        width(72.px)
        backgroundColor(Color.red)
    }

    val leftSidebar by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(25.percent)
        maxWidth(300.px)
        flexShrink(0) // Do not shink the sidebar!
        property("box-shadow", "rgba(0, 101, 255, 0.7) 0px 0px 15px") // Shadow
        property("z-index", 1) // Make the left sidebar be on top of the right sidebar, fixes the shadow
        color(Color.white)
        background("rgba(0, 0, 0, 0) linear-gradient(rgb(0, 168, 255) 0%, rgb(0, 100, 255) 100%) repeat scroll 0% 0%")
        height(100.vh)
        position(Position.Sticky)
        // Required for sticky position to work
        top(0.px)
        left(0.px)
    }

    val leftSidebarEntries by style {
        paddingLeft(1.em)
        paddingRight(1.em)
        height(100.percent)
        overflow("auto")
    }

    val leftSidebarDivider by style {
        width(100.percent)

        border {
            width = 1.px
            style = LineStyle.Solid
            color = Color("#ffffff57")
        }
    }

    val leftSidebarCategory by style {
        fontWeight("bold")
        property("text-transform", "uppercase")
        fontSize(18.px)
        marginTop(0.25.em)
        marginBottom(0.25.em)
    }

    val leftSidebarEntry by style {
        opacity(0.8)
        display(DisplayStyle.Flex)
        property("transition-duration", "0.2s")
        alignItems(AlignItems.Center)
        gap(0.5.em)
        paddingTop(0.2.em)
        paddingBottom(0.2.em)
        marginTop(0.2.em)
        marginBottom(0.2.em)
        fontSize(16.px)

        hover() + self style {
            opacity(1.0)
        }
    }

    val leftSidebarEntryIcon by style {
        height(24.px)
        display(DisplayStyle.Block)
    }

    val leftSidebarUserInfo by style {
        background("rgba(0, 0, 0, 0) linear-gradient(rgb(0, 79, 201) 0%, rgb(0, 69, 176) 100%) repeat scroll 0% 0%")
        height(60.px)
    }

    val rightSidebar by style {
        display(DisplayStyle.Flex)
        width(100.percent)
        maxWidth(70.em)
        property("margin-left", "auto")
        property("margin-right", "auto")
    }

    val sectionHeaderImage by style {
        backgroundColor(Color("#00a7ff"))
        width(100.percent)
        height(200.px)
        borderRadius(7.px)
        position(Position.Relative)
        overflow("hidden")
    }

    @OptIn(ExperimentalComposeWebApi::class)
    val guildSidebarIcon by style {
        width(48.px)
        height(auto)
        borderRadius(50.percent)
        property("transition-duration", "0.5s")

        self + hover() style {
            borderRadius(35.percent)
        }
    }

    val guildSidebarLine by style {
        height(5.px)
        width(100.percent)
        backgroundColor(Color.black)
    }

    val guildSidebarItem by style {
        marginBottom(8.px)
        width(100.percent)
        textAlign("center")
    }

    // https://blog.logrocket.com/creating-beautiful-tooltips-with-only-css/
    @OptIn(ExperimentalComposeWebApi::class)
    val guildSidebarTooltip by style {
        position(Position.Absolute)
        top(50.percent)
        transform {
            translateY((-50).percent)
        }
        left(100.percent)
        marginLeft(15.px)
        backgroundColor(Color.black)
        color(Color.white)
        property("transition-duration", "0.5s, 0.1s")
        property("transition-property", "border-radius, opacity")
    }

    val container by style { // container is a class
        display(DisplayStyle.Flex)
        padding(20.px)

        // custom property (or not supported out of the box)
        property("font-family", "Arial, Helvetica, sans-serif")
    }

    // Resets the default link style
    val resetLinkStyle by style {
        property("color", "inherit")
        textDecoration("none")
    }

    val sidebarAd by style {
        width(160.px)
        maxWidth(160.px)
        minWidth(160.px)
        backgroundColor(rgba(200, 200, 200, 255))
    }
}