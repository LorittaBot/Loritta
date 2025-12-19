package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

/**
 * Small reusable translation progress bar used in language overview and in the left sidebar
 */
fun FlowContent.languageProgressBar(translatedCount: Int, totalCount: Int) {
    val pct = if (totalCount == 0) 0.0 else (translatedCount.toDouble() / totalCount.toDouble()) * 100.0

    div {
        style = "display: flex; gap: 16px; align-items: center;"

        div {
            style = "width: 100%; height: 16px; background-color: var(--user-info-wrapper-background-color); border-radius: 7px; overflow: hidden; border: 1px solid var(--card-border-color);"

            div {
                style = "width: $pct%; background-color: var(--loritta-green); height: 100%;"
            }
        }

        div {
            style = "flex-shrink: 0;"
            text("${"%.2f".format(pct)}% ($translatedCount / $totalCount)")
        }
    }
}

