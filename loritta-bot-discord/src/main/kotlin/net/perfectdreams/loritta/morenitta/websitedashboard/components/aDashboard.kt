package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.span
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

/**
 * A dashboard left sidebar entry href link that swaps the left sidebar contents and the right sidebar contents
 */
fun FlowContent.aDashboardSidebarEntry(
    i18nContext: I18nContext,
    href: String,
    text: String,
    selected: Boolean,
    new: Boolean
) {
    a(classes = "entry section-entry", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}$href") {
        if (selected)
            classes += "selected"

        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), .entries (innerHTML) -> .entries (innerHTML)"
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-replace-load"] = "#loading"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"

        text(text)

        if (new) {
            span(classes = "new-feature") {
                text("Novo!")
            }
        }
    }
}

/**
 * A dashboard left sidebar entry href link that swaps the left sidebar contents and the right sidebar contents that pretends to be a link
 */
fun FlowContent.aDashboardSidebarEntryButton(style: ButtonStyle, i18nContext: I18nContext, href: String, text: String, new: Boolean) {
    a(classes = "entry discord-button ${style.className}", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}$href") {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), #left-sidebar (innerHTML) -> #left-sidebar (innerHTML)"
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-replace-load"] = "#loading"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"

        text(text)

        if (new) {
            span(classes = "new-feature") {
                text("Novo!")
            }
        }
    }
}