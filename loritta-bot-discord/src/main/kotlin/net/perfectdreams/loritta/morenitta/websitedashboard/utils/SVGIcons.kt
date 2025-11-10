package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import org.jsoup.nodes.Element
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser
import kotlin.io.path.readText

object SVGIcons {
    val Star = register("star", "/svg_icons/phosphor/fill/star.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val StarOutline = register("star_outline", "/svg_icons/phosphor/regular/star.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Sparkles = register("sparkles", "/svg_icons/phosphor/fill/sparkles.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Heart = register("heart", "/svg_icons/phosphor/fill/heart.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Cat = register("cat", "/svg_icons/phosphor/fill/cat.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Code = register("code", "/svg_icons/phosphor/bold/code.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val House = register("house", "/svg_icons/phosphor/fill/house.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val HandWaving = register("hand_waving", "/svg_icons/phosphor/fill/hand-waving.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Key = register("key", "/svg_icons/phosphor/fill/key.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val IdentificationCard = register("identification_card", "/svg_icons/phosphor/fill/identification-card.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Seal = register("seal", "/svg_icons/phosphor/fill/seal.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Eye = register("seal", "/svg_icons/phosphor/fill/eye.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Gavel = register("seal", "/svg_icons/phosphor/fill/gavel.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ArrowFatLinesUp = register("seal", "/svg_icons/phosphor/fill/arrow-fat-lines-up.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val BoxArrowUp = register("seal", "/svg_icons/phosphor/fill/box-arrow-up.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Images = register("seal", "/svg_icons/phosphor/fill/images.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val DiamondsFour = register("seal", "/svg_icons/phosphor/fill/diamonds-four.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Knife = register("seal", "/svg_icons/phosphor/fill/knife.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Joystick = register("seal", "/svg_icons/phosphor/fill/joystick.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Ranking = register("seal", "/svg_icons/phosphor/fill/ranking.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val SignOut = register("sign-out", "/svg_icons/phosphor/fill/sign-out.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val DoorOpen = register("door-open", "/svg_icons/phosphor/fill/door-open.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ShoppingCart = register("shopping-cart", "/svg_icons/phosphor/fill/shopping-cart.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ShoppingBag = register("shopping-bag", "/svg_icons/phosphor/fill/shopping-bag.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Asterisk = register("asterisk", "/svg_icons/phosphor/fill/asterisk.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val List = register("list", "/svg_icons/phosphor/bold/list.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CheckFat = register("check-fat", "/svg_icons/phosphor/bold/check-fat.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CaretDown = register("caret-down", "/svg_icons/phosphor/bold/caret-down.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CaretUp = register("caret-up", "/svg_icons/phosphor/bold/caret-up.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CaretLeft = register("caret-left", "/svg_icons/phosphor/bold/caret-left.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val CaretRight = register("caret-right", "/svg_icons/phosphor/bold/caret-right.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ShootingStar = register("shooting-star", "/svg_icons/phosphor/fill/shooting-star.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Clock = register("clock", "/svg_icons/phosphor/fill/clock.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Timer = register("timer", "/svg_icons/phosphor/fill/timer.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val TimerAnimatedHand = register("timer", "/svg_icons/custom/timer-animated-hand.svg")
    val TrendUp = register("trend-up", "/svg_icons/phosphor/fill/trend-up.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Scroll = register("scroll", "/svg_icons/phosphor/fill/scroll.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val LockSimple = register("lock-simple", "/svg_icons/phosphor/fill/lock-simple.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ChartPie = register("chart-pie", "/svg_icons/phosphor/fill/chart-pie.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ChartDonut = register("chart-donut", "/svg_icons/phosphor/fill/chart-donut.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val SortDescending = register("sort-descending", "/svg_icons/phosphor/bold/sort-descending.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val EyeDropper = register("eyedropper", "/svg_icons/phosphor/fill/eyedropper.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val SquaresFour = register("squares-four", "/svg_icons/phosphor/fill/squares-four.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Certificate = register("certificate", "/svg_icons/phosphor/fill/certificate.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ArrowUp = register("arrow-up", "/svg_icons/phosphor/bold/arrow-up.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val ArrowDown = register("arrow-down", "/svg_icons/phosphor/bold/arrow-down.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Bell = register("bell-simple", "/svg_icons/phosphor/fill/bell-simple.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)

    val SlashCommand = register("slash-command", "/svg_icons/discord/slash-command.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val TextChannel = register("text-channel", "/svg_icons/discord/text-channel.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val PrivateTextChannel = register("private-text-channel", "/svg_icons/discord/private-text-channel.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val AnnouncementChannel = register("announcement-channel", "/svg_icons/discord/announcement-channel.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val PrivateAnnouncementChannel = register("private-announcement-channel", "/svg_icons/discord/private-announcement-channel.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val RulesChannel = register("rules-channel", "/svg_icons/discord/rules-channel.svg", SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val RoleShield = register("role-shield", "/svg_icons/discord/role-shield.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)

    val PrefixCommand = register("prefix-command", "/svg_icons/custom/prefix-command.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Ban = register("ban", "/svg_icons/custom/ban.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Coy = register("coy", "/svg_icons/custom/coy.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Pickaxe = register("pickaxe", "/svg_icons/custom/pickaxe.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)

    val YouTube = register("youtube", "/svg_icons/brands/youtube.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Twitch = register("twitch", "/svg_icons/brands/twitch.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val Bluesky = register("bluesky", "/svg_icons/brands/bluesky.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val GamerSafer = register("gamersafer", "/svg_icons/brands/gamersafer.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)
    val PlayStation = register("playstation", "/svg_icons/brands/playstation.svg", SVGOptions.REMOVE_FILLS, SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT)

    val FolderColored = register("folder-colored", "/svg_icons/twemoji/1f4c1.svg")

    fun register(name: String, path: String, vararg options: SVGOptions): SVGIcon {
        val svgFile = SVGIcons::class.getPathFromResources(path) ?: error("Could not find SVG file $path")
        val svgText = svgFile.readText(Charsets.UTF_8)

        val parser = Parser.htmlParser()
        parser.settings(ParseSettings(true, true)) // tag, attribute preserve case, if not stuff like viewBox breaks!

        val document = parser.parseInput(svgText, "/")

        val svgTag = document.getElementsByTag("svg")
            .first()!!

        if (SVGOptions.REPLACE_FILLS_WITH_CURRENT_COLOR in options) {
            // Replace "fill" tags into "currentColor"
            val fill = svgTag.getElementsByAttribute("fill")

            if (fill.attr("fill") != "none")
                fill.attr("fill", "currentColor")
        }

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.getElementsByAttribute("fill")
                .removeAttr("fill")
        }

        if (SVGOptions.SET_CURRENT_COLOR_FILL_ON_ROOT in options) {
            svgTag.attr("fill", "currentColor")
        }

        return SVGIcon(svgTag)
    }

    class SVGIcon(val html: Element)

    enum class SVGOptions {
        REPLACE_FILLS_WITH_CURRENT_COLOR,
        SET_CURRENT_COLOR_FILL_ON_ROOT,
        REMOVE_FILLS
    }
}