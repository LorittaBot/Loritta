package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

object LoadingSectionComponents {
    val list = listOf(
        "https://cdn.discordapp.com/emojis/957368372025262120.gif?size=160&quality=lossless",
        "https://cdn.discordapp.com/emojis/958906311414796348.gif?size=160&quality=lossless",
        "https://cdn.discordapp.com/emojis/959551356769820712.gif?size=160&quality=lossless",
        "https://cdn.discordapp.com/emojis/959557654341103696.gif?size=160&quality=lossless",
        "https://cdn.discordapp.com/emojis/985919207147470858.gif?size=160&quality=lossless"
    )

    /**
     * A loading section
     */
    fun FlowContent.loadingSection(i18nContext: I18nContext) {
        div(classes = "loading-section") {
            img(src = list.random())

            div {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
            }
        }
    }

    /**
     * A loading section that fills the entire height of the parent div, centralizing the loading section
     */
    fun FlowContent.fillContentLoadingSection(i18nContext: I18nContext) {
        div(classes = "loading-section fill-content-loading-section") {
            // Set the img src to a random GIF when we aren't intersecting
            attributes["_"] = """
                on intersection(intersecting) having threshold 0.5
                    if intersecting
                    else
                        set gifs to [${list.joinToString(",") { "\"$it\"" }}]
                        set gif to random in gifs
                        set x to the first <img/> in me
                        set x's @src to gif
                end""".trimIndent()
            img(src = list.random())

            div {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
            }
        }
    }
}