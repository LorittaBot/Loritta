package net.perfectdreams.loritta.website.backend.utils

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import org.jsoup.nodes.Element
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser

class SVGIconManager(val showtime: LorittaWebsiteBackend) {
    val registeredSvgs = mutableMapOf<String, SVGIcon>()

    val terminal = register("terminal", "fontawesome5/solid/terminal.svg")
    val paintBrush = register("paint-brush", "fontawesome5/solid/paint-brush.svg")
    val gift = register("gift", "fontawesome5/solid/gift.svg")
    val star = register("star", "fontawesome5/solid/star.svg")
    val newspaper = register("newspaper", "fontawesome5/solid/newspaper.svg")
    val heart = register("heart", "fontawesome5/solid/heart.svg")
    val sparkles = register("sparkles", "twemoji-master/2728.svg", SVGOptions.REMOVE_FILLS)
    val gavel = register("gavel", "fontawesome5/solid/gavel.svg")
    val pencil = register("pencil", "fontawesome5/solid/pencil-alt.svg")
    val edit = register("edit", "fontawesome5/solid/edit.svg")
    val idCard = register("id-card", "fontawesome5/solid/id-card.svg")
    val bug = register("bug", "fontawesome5/solid/bug.svg")
    val robot = register("robot", "fontawesome5/solid/robot.svg")
    val key = register("key", "twemoji-master/1f511.svg", SVGOptions.REMOVE_FILLS)
    val oldKey = register("old-key", "twemoji-master/1f5dd.svg", SVGOptions.REMOVE_FILLS)
    val users = register("users", "fontawesome5/solid/users.svg")
    val usersCog = register("users-cog", "fontawesome5/solid/users-cog.svg")
    val moneyCheck = register("money-check", "fontawesome5/solid/money-check-alt.svg")
    val commentDollar = register("comment-dollar", "fontawesome5/solid/comment-dollar.svg")
    val chartLine = register("chart-line", "fontawesome5/solid/chart-line.svg")
    val handHoldingMoney = register("hand-holding-money", "fontawesome5/solid/hand-holding-usd.svg")
    val userEdit = register("user-edit", "fontawesome5/solid/user-edit.svg")
    val language = register("language", "fontawesome5/solid/language.svg")
    val phone = register("phone", "fontawesome5/solid/phone.svg")
    val server = register("server", "fontawesome5/solid/server.svg")
    val chevronDown = register("chevron-down", "fontawesome5/solid/chevron-down.svg")
    val sun = register("sun", "fontawesome5/solid/sun.svg")
    val moon = register("moon", "fontawesome5/solid/moon.svg")
    val globe = register("globe", "fontawesome5/solid/globe-americas.svg")
    val bars = register("bars", "fontawesome5/solid/bars.svg")
    val signIn = register("sign-in", "fontawesome5/solid/sign-in-alt.svg")
    val search = register("search", "fontawesome5/solid/search.svg")
    val ad = register("ad", "fontawesome5/solid/ad.svg")
    val dollarSign = register("dollar-sign", "fontawesome5/solid/dollar-sign.svg")
    val bell = register("bell", "fontawesome5/solid/bell.svg")
    val comment = register("comment", "fontawesome5/solid/comment-alt.svg")
    val comments = register("comments", "fontawesome5/solid/comments.svg")
    val briefcase = register("briefcase", "fontawesome5/solid/briefcase.svg")
    val rocket = register("rocket", "fontawesome5/solid/rocket.svg")
    val doorOpen = register("door-open", "fontawesome5/solid/door-open.svg")
    val book = register("book", "fontawesome5/solid/book.svg")
    val dizzy = register("dizzy", "fontawesome5/solid/dizzy.svg")
    val cogs = register("cogs", "fontawesome5/solid/cogs.svg")
    val plus = register("plus", "fontawesome5/solid/plus.svg")
    val bullhorn = register("bullhorn", "fontawesome5/solid/bullhorn.svg")
    val music = register("music", "fontawesome5/solid/music.svg")
    val headphones = register("headphones", "fontawesome5/solid/headphones.svg")
    val shirt = register("shirt", "fontawesome5/solid/tshirt.svg")
    val smallDiamond = register("small-diamond", "twemoji-master/1f539.svg", SVGOptions.REMOVE_FILLS)
    val xmark = register("check", "fontawesome6/solid/xmark.svg")
    val check = register("xmark", "fontawesome6/solid/check.svg")

    // https://thenounproject.com/ahmadpp4/collection/emoticons/
    val owo = register("owo", "loritta/owo.svg")

    // ===[ BRANDS ]===
    val perfectDreams = register("perfectdreams", "perfectdreams-logo-black-with-yellow-star.svg", SVGOptions.DO_NOT_ADD_ICON_CLASS)
    val discord = register("discord", "fontawesome5/brands/discord.svg")
    val instagram = register("instagram", "fontawesome5/brands/instagram.svg")
    val twitter = register("twitter", "fontawesome5/brands/twitter.svg")
    val github = register("github", "fontawesome5/brands/github.svg")
    val youtube = register("youtube", "fontawesome5/brands/youtube.svg")
    val lastfm = register("lastfm", "fontawesome5/brands/lastfm.svg")
    val reddit = register("reddit", "fontawesome5/brands/reddit.svg")
    val tiktok = register("tiktok", "fontawesome5/brands/tiktok.svg")

    /**
     * Loads and registers a SVG with [name] and [path]
     *
     * The SVG also checks for name conflicts and stores all registered icons in [registeredSvgs]
     */
    fun register(name: String, path: String, vararg options: SVGOptions): SVGIcon {
        if (name in registeredSvgs)
            throw RuntimeException("There is already a SVG with name $name!")

        val parser = Parser.htmlParser()
        parser.settings(ParseSettings(true, true)) // tag, attribute preserve case, if not stuff like viewBox breaks!
        println(path)
        val document = parser.parseInput(
            SVGIconManager::class.java.getResourceAsStream("/icons/$path")
                .bufferedReader()
                .readText(),
            "/"
        )

        val svgTag = document.getElementsByTag("svg")
            .first()!!

        if (SVGOptions.DO_NOT_ADD_ICON_CLASS !in options) {
            svgTag.addClass("icon") // Add the "icon" class name to the SVG root, this helps us styling it via CSS
        }

        svgTag.addClass("icon-$name") // Also add the icon name to the SVG root, so we can individually style with CSS

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.getElementsByAttribute("fill")
                .removeAttr("fill")
        }

        val svgIcon = SVGIcon(svgTag)
        registeredSvgs[name] = svgIcon
        return svgIcon
    }

    class SVGIcon(private val html: Element) {
        fun apply(content: HTMLTag, block: Element.() -> (Unit) = {}) {
            content.unsafe {
                val clonedElement = html.clone()
                block.invoke(clonedElement)
                raw(clonedElement.toString())
            }
        }
    }

    enum class SVGOptions {
        REMOVE_FILLS,
        DO_NOT_ADD_ICON_CLASS
    }
}