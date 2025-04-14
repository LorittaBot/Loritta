package net.perfectdreams.loritta.website.backend.utils.locale

import net.perfectdreams.loritta.common.locale.BaseLocale

fun BaseLocale.formatLocaleKeyAsHtml(localeKey: String, onControlChar: (Int) -> (Unit), onStringBuild: (String) -> (Unit))
        = formatAsHtml(this[localeKey], onControlChar, onStringBuild)

fun formatAsHtml(updateString: String, onControlChar: (Int) -> (Unit), onStringBuild: (String) -> (Unit)) {
    var isControl = false
    var ignoreNext = false

    val genericStringBuilder = StringBuilder()

    println(updateString)

    for (ch in updateString) {
        if (ignoreNext) {
            ignoreNext = false
            continue
        }
        if (isControl) {
            ignoreNext = true
            isControl = false

            val num = ch.toString().toInt()

            if (genericStringBuilder.isNotEmpty()) {
                onStringBuild.invoke(genericStringBuilder.toString())
                genericStringBuilder.clear()
            }

            onControlChar.invoke(num)
            continue
        }
        if (ch == '{') {
            isControl = true
            continue
        }

        genericStringBuilder.append(ch)
    }

    onStringBuild.invoke(genericStringBuilder.toString())
}