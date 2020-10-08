package net.perfectdreams.loritta.embededitor.editors

import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
import kotlinx.html.style
import net.perfectdreams.loritta.embededitor.select
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document

object EmbedPillEditor : EditorBase {
    val pillCallback: ELEMENT_CONFIGURATION = { m, discordMessage, currentElement, renderInfo ->
        currentElement.onClickFunction = {
            document.body!!.append {
                input(InputType.color) {
                    id = "color-selector"
                    style = "display: none;"

                    onInputFunction = {
                        val value = (it.target as HTMLInputElement)
                                .value
                                .removePrefix("#")

                        val rgb = hexToRgb(value)
                                .toPackedInt()

                        m.generateMessageAndUpdateJson(
                                m.activeMessage!!.copy(
                                        embed = m.activeMessage!!.embed!!
                                                .copy(
                                                        color = rgb
                                                )
                                )
                        )

                        document.select<HTMLInputElement>("#color-selector").remove()
                    }
                }
            }

            document.select<HTMLInputElement>("#color-selector")
                    .click()
        }
    }

    fun hexToRgb(hex: String): Color {
        val bigint = hex.toInt(16);
        val r = (bigint shr 16) and 255
        val g = (bigint shr 8) and 255
        val b = bigint and 255

        return Color(r, g, b)
    }

    data class Color(
            val red: Int,
            val green: Int,
            val blue: Int
    ) {
        fun toPackedInt(): Int {
            var rgb = red
            rgb = (rgb shl 8) + green
            rgb = (rgb shl 8) + blue
            return rgb
        }
    }
}