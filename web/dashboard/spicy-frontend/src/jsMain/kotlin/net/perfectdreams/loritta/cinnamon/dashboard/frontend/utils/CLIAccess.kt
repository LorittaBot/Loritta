/**
 * A class that exposes useful things to JavaScript, useful for debugging!
 *
 * You can access it via `window["spicy-frontend"].net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.CLIAccess`
 */
package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.Language
import net.perfectdreams.i18nhelper.core.TextBundle
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend

@OptIn(ExperimentalJsExport::class)
@JsExport
object CLIAccess {
    private val disallowedRemaps = setOf(
        "website.dashboard.localePathId"
    )

    @JsName("transformToPseudoLocalization")
    fun transformToPseudoLocalization() {
        when (val r = LorittaDashboardFrontend.INSTANCE.globalState.i18nContext) {
            is Resource.Failure -> {}
            is Resource.Loading -> {}
            is Resource.Success -> {
                val originalI18nContext = r.value
                val oldLanguage = originalI18nContext.language
                val newLanguage = Language(
                    oldLanguage.info,
                    TextBundle(
                        oldLanguage.textBundle.strings.map {
                            if (it.key !in disallowedRemaps)
                                it.key to convertWord(it.value)
                            else
                                it.key to it.value
                        }.toMap(),
                        oldLanguage.textBundle.lists.map {
                            it.key to it.value.map { convertWord(it) }
                        }.toMap()
                    )
                )
                LorittaDashboardFrontend.INSTANCE.globalState.i18nContext = Resource.Success(
                    I18nContext(
                        originalI18nContext.formatter,
                        newLanguage
                    )
                )
                println("Transformed current i18nContext into pseudo-localization!")
            }
        }
    }

    @JsName("showToast")
    fun showToast(title: String) {
        LorittaDashboardFrontend.INSTANCE.globalState.showToast(Toast.Type.INFO, title)
    }

    @JsName("shouldChannelSelectMenuPermissionCheckAlwaysFail")
    fun shouldChannelSelectMenuPermissionCheckAlwaysFail(enabled: Boolean) {
        LorittaDashboardFrontend.shouldChannelSelectMenuPermissionCheckAlwaysFail = enabled
    }

    /**
     * Converts the [before] to a string for a pseudolocalization locale, used to test localization
     */
    private fun convertWord(before: String): String {
        // http://www.pseudolocalize.com/
        var after = ""

        var withinControlDepth = 0

        before.forEach { c ->
            // If we replace the stuff inside of the {...} blocks, this can cause issues when trying to format
            // the string (example: {0,number}, number is replaced and this causes an error)
            if (c == '{')
                withinControlDepth++
            if (c == '}')
                withinControlDepth--

            if (withinControlDepth == 0) {
                val out = when (c) {
                    'a' -> 'á'
                    'b' -> 'β'
                    'c' -> 'ç'
                    'd' -> 'δ'
                    'e' -> 'è'
                    'f' -> 'ƒ'
                    'g' -> 'ϱ'
                    'h' -> 'λ'
                    'i' -> 'ï'
                    'j' -> 'J'
                    'k' -> 'ƙ'
                    'l' -> 'ℓ'
                    'm' -> '₥'
                    'n' -> 'ñ'
                    'o' -> 'ô'
                    'p' -> 'ƥ'
                    'q' -> '9'
                    'r' -> 'ř'
                    's' -> 'ƨ'
                    't' -> 'ƭ'
                    'u' -> 'ú'
                    'v' -> 'Ʋ'
                    'w' -> 'ω'
                    'x' -> 'ж'
                    'y' -> '¥'
                    'z' -> 'ƺ'
                    'A' -> 'Â'
                    'B' -> 'ß'
                    'C' -> 'Ç'
                    'D' -> 'Ð'
                    'E' -> 'É'
                    'F' -> 'F'
                    'G' -> 'G'
                    'H' -> 'H'
                    'I' -> 'Ì'
                    'J' -> 'J'
                    'K' -> 'K'
                    'L' -> '£'
                    'M' -> 'M'
                    'N' -> 'N'
                    'O' -> 'Ó'
                    'P' -> 'Þ'
                    'Q' -> 'Q'
                    'R' -> 'R'
                    'S' -> '§'
                    'T' -> 'T'
                    'U' -> 'Û'
                    'V' -> 'V'
                    'W' -> 'W'
                    'X' -> 'X'
                    'Y' -> 'Ý'
                    'Z' -> 'Z'
                    else -> c
                }

                after += out
            } else {
                after += c
            }
        }

        return after
    }
}