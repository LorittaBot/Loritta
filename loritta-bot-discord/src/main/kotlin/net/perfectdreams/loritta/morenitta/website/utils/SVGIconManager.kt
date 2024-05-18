package net.perfectdreams.loritta.morenitta.website.utils

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import org.jsoup.nodes.Element
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser

class SVGIconManager(val m: LorittaWebsite) {
    val registeredSvgs = mutableMapOf<String, SVGIcon>()

    // ===[ DISCORD ]===
    val discordTextChannel = register("discordTextChannel", "discord/text-channel.svg", SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val discordNewsChannel = register("discordNewsChannel", "discord/news-channel.svg", SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS)

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
        val document = parser.parseInput(
            SVGIconManager::class.java.getResourceAsStream("/icons/$path")
                .bufferedReader()
                .readText(),
            "/"
        )

        val svgTag = document.getElementsByTag("svg")
            .first()!!

        svgTag.addClass("text-icon")
        // svgTag.addClass("icon-$name") // Also add the icon name to the SVG root, so we can individually style with CSS

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.getElementsByAttribute("fill")
                .removeAttr("fill")
        }

        if (SVGOptions.ADD_CURRENT_COLOR_FILLS in options) {
            // Adds "currentColor" fills
            svgTag.getElementsByTag("path")
                .filterIsInstance<Element>()
                .forEach {
                    it.attr("fill", "currentColor")
                }
        }

        val svgIcon = SVGIcon(svgTag)
        registeredSvgs[name] = svgIcon
        return svgIcon
    }

    class SVGIcon(val html: Element) {
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
        ADD_CURRENT_COLOR_FILLS
    }
}