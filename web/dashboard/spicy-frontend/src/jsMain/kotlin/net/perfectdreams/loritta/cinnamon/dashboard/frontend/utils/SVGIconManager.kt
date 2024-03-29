package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils


import org.jetbrains.compose.web.attributes.AutoComplete.Companion.name
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

// Inspired by Loritta's Showtime "SVGIconManager" class
object SVGIconManager {
    val sparkles by lazy { register(svgSparkles, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val heart by lazy { register(svgHeart, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val exclamationTriangle by lazy { register(svgExclamationTriangle, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val exclamationCircle by lazy { register(svgExclamationCircle, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val bars by lazy { register(svgBars, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val times by lazy { register(svgTimes, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val cogs by lazy { register(svgGears, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val user by lazy { register(svgUser, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val idCard by lazy { register(svgIdCard, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val images by lazy { register(svgImages, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val moneyBillWave by lazy { register(svgMoneyBillWave, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val store by lazy { register(svgStore, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val shoppingCart by lazy { register(svgShoppingCart, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val asterisk by lazy { register(svgAsterisk, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val star by lazy { register(svgStar, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val chevronLeft by lazy { register(svgChevronLeft, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val chevronDown by lazy { register(svgChevronDown, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val clock by lazy { register(svgClock, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val terminal by lazy { register(svgTerminal, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val addressCard by lazy { register(svgAddressCard, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val ban by lazy { register(svgBan, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val rightToBracket by lazy { register(svgRightToBracket, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val eye by lazy { register(svgEye, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val youtube by lazy { register(svgYouTube, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val twitch by lazy { register(svgTwitch, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val award by lazy { register(svgAward, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val briefcase by lazy { register(svgBriefcase, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val sortAmountUp by lazy { register(svgArrowUpWideShort, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val shuffle by lazy { register(svgShuffle, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val list by lazy { register(svgList, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val gamerSafer by lazy { register(svgGamerSaferLogo, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val paperPlane by lazy { register(svgPaperPlane, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val diagramNext by lazy { register(svgDiagramNext, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val check by lazy { register(svgCheck, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val eyeDropper by lazy { register(svgEyeDropper, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val fileImport by lazy { register(svgFileImport, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val arrowUpRightFromSquare by lazy { register(svgArrowUpRightFromSquare, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val pencil by lazy { register(svgPencil, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val coloredFolder by lazy { register(svgColoredFolder) }
    val coloredStar by lazy { register(svgColoredStar) }
    val code by lazy { register(svgCode, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val discordTextChannel by lazy { register(svgDiscordTextChannel, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val discordNewsChannel by lazy { register(svgDiscordNewsChannel, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val faceSmile by lazy { register(svgFaceSmile, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val roleShield by lazy { register(svgRoleShield, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }
    val xmark by lazy { register(svgXmark, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS) }

    /**
     * Loads and registers a SVG with [name] and [path]
     */
    fun register(html: String, vararg options: SVGOptions): SVGIcon {
        val parser = DOMParser()
        val document = parser.parseFromString(html, "image/svg+xml")

        val svgTag = document.getElementsByTagName("svg")
            .asList()
            .first()

        // TODO: Check if we really need this
        // svgTag.addClass("icon") // Add the "icon" class name to the SVG root, this helps us styling it via CSS
        //    .addClass("icon-$name") // Also add the icon name to the SVG root, so we can individually style with CSS

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.querySelectorAll("[fill]")
                .asList()
                .filterIsInstance<Element>()
                .forEach {
                    it.removeAttribute("fill")
                }
        }

        if (SVGOptions.ADD_CURRENT_COLOR_FILLS in options) {
            // Adds "currentColor" fills
            svgTag.querySelectorAll("path")
                .asList()
                .filterIsInstance<Element>()
                .forEach {
                    it.setAttribute("fill", "currentColor")
                }
        }

        val svgIcon = SVGIcon(svgTag)
        return svgIcon
    }

    class SVGIcon(val element: Element)

    enum class SVGOptions {
        REMOVE_FILLS,
        ADD_CURRENT_COLOR_FILLS,
    }
}

// ===[ NEEDS TO BE TOP LEVEL! ]===
@JsModule("./icons/twemoji-modified/sparkles.svg")
@JsNonModule
external val svgSparkles: dynamic

@JsModule("./icons/fontawesome6/solid/heart.svg")
@JsNonModule
external val svgHeart: dynamic

@JsModule("./icons/fontawesome6/solid/triangle-exclamation.svg")
@JsNonModule
external val svgExclamationTriangle: dynamic

@JsModule("./icons/fontawesome6/solid/circle-exclamation.svg")
@JsNonModule
external val svgExclamationCircle: dynamic

@JsModule("./icons/fontawesome6/solid/xmark.svg")
@JsNonModule
external val svgTimes: dynamic

@JsModule("./icons/fontawesome6/solid/bars.svg")
@JsNonModule
external val svgBars: dynamic

@JsModule("./icons/fontawesome6/solid/gears.svg")
@JsNonModule
external val svgGears: dynamic

@JsModule("./icons/fontawesome6/solid/user.svg")
@JsNonModule
external val svgUser: dynamic

@JsModule("./icons/fontawesome6/solid/id-card.svg")
@JsNonModule
external val svgIdCard: dynamic

@JsModule("./icons/fontawesome6/solid/images.svg")
@JsNonModule
external val svgImages: dynamic

@JsModule("./icons/fontawesome6/solid/money-bill-wave.svg")
@JsNonModule
external val svgMoneyBillWave: dynamic

@JsModule("./icons/fontawesome6/solid/store.svg")
@JsNonModule
external val svgStore: dynamic

@JsModule("./icons/fontawesome6/solid/cart-shopping.svg")
@JsNonModule
external val svgShoppingCart: dynamic

@JsModule("./icons/fontawesome6/solid/asterisk.svg")
@JsNonModule
external val svgAsterisk: dynamic

@JsModule("./icons/fontawesome6/solid/star.svg")
@JsNonModule
external val svgStar: dynamic

@JsModule("./icons/fontawesome6/solid/chevron-left.svg")
@JsNonModule
external val svgChevronLeft: dynamic

@JsModule("./icons/fontawesome6/solid/chevron-down.svg")
@JsNonModule
external val svgChevronDown: dynamic

@JsModule("./icons/fontawesome6/solid/clock.svg")
@JsNonModule
external val svgClock: dynamic

@JsModule("./icons/fontawesome6/solid/terminal.svg")
@JsNonModule
external val svgTerminal: dynamic

@JsModule("./icons/fontawesome6/solid/address-card.svg")
@JsNonModule
external val svgAddressCard: dynamic

@JsModule("./icons/fontawesome6/solid/ban.svg")
@JsNonModule
external val svgBan: dynamic

@JsModule("./icons/fontawesome6/solid/right-to-bracket.svg")
@JsNonModule
external val svgRightToBracket: dynamic

@JsModule("./icons/fontawesome6/solid/eye.svg")
@JsNonModule
external val svgEye: dynamic

@JsModule("./icons/fontawesome6/brands/youtube.svg")
@JsNonModule
external val svgYouTube: dynamic

@JsModule("./icons/fontawesome6/brands/twitch.svg")
@JsNonModule
external val svgTwitch: dynamic

@JsModule("./icons/fontawesome6/solid/award.svg")
@JsNonModule
external val svgAward: dynamic

@JsModule("./icons/fontawesome6/solid/briefcase.svg")
@JsNonModule
external val svgBriefcase: dynamic

@JsModule("./icons/fontawesome6/solid/arrow-up-wide-short.svg")
@JsNonModule
external val svgArrowUpWideShort: dynamic

@JsModule("./icons/fontawesome6/solid/shuffle.svg")
@JsNonModule
external val svgShuffle: dynamic

@JsModule("./icons/fontawesome6/solid/list.svg")
@JsNonModule
external val svgList: dynamic

@JsModule("./icons/gamersafer-logo.svg")
@JsNonModule
external val svgGamerSaferLogo: dynamic

@JsModule("./icons/fontawesome6/solid/paper-plane.svg")
@JsNonModule
external val svgPaperPlane: dynamic

@JsModule("./icons/fontawesome6/solid/diagram-next.svg")
@JsNonModule
external val svgDiagramNext: dynamic

@JsModule("./icons/fontawesome6/solid/check.svg")
@JsNonModule
external val svgCheck: dynamic

@JsModule("./icons/fontawesome6/solid/eye-dropper.svg")
@JsNonModule
external val svgEyeDropper: dynamic

@JsModule("./icons/fontawesome6/solid/file-import.svg")
@JsNonModule
external val svgFileImport: dynamic

@JsModule("./icons/fontawesome6/solid/arrow-up-right-from-square.svg")
@JsNonModule
external val svgArrowUpRightFromSquare: dynamic

@JsModule("./icons/fontawesome6/solid/pencil.svg")
@JsNonModule
external val svgPencil: dynamic

@JsModule("./icons/twemoji-master/1f4c1.svg")
@JsNonModule
external val svgColoredFolder: dynamic

@JsModule("./icons/twemoji-master/2b50.svg")
@JsNonModule
external val svgColoredStar: dynamic

@JsModule("./icons/fontawesome6/solid/code.svg")
@JsNonModule
external val svgCode: dynamic

@JsModule("./icons/fontawesome6/solid/face-smile.svg")
@JsNonModule
external val svgFaceSmile: dynamic

@JsModule("./icons/fontawesome6/solid/xmark.svg")
@JsNonModule
external val svgXmark: dynamic

@JsModule("./icons/discord/text-channel.svg")
@JsNonModule
external val svgDiscordTextChannel: dynamic

@JsModule("./icons/discord/news-channel.svg")
@JsNonModule
external val svgDiscordNewsChannel: dynamic

@JsModule("./icons/discord/role-shield.svg")
@JsNonModule
external val svgRoleShield: dynamic