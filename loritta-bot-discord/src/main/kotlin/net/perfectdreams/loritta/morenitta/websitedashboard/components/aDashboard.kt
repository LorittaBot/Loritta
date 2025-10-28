package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.span
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

fun FlowContent.sectionEntry(href: String? = null, selected: Boolean, block: A.() -> Unit) {
    a(classes = "entry section-entry", href = href) {
        if (selected)
            classes += "selected"

        block()
    }
}

fun FlowContent.sectionEntryContent(text: String, icon: SVGIcons.SVGIcon, new: Boolean) {
    div(classes = "section-icon") {
        svgIcon(icon)
    }

    div(classes = "section-text") {
        text(text)
    }

    if (new) {
        span(classes = "new-feature") {
            text("Novo!")
        }
    }
}

/**
 * A dashboard left sidebar entry href link that swaps the left sidebar contents and the right sidebar contents
 */
fun FlowContent.aDashboardSidebarEntry(
    i18nContext: I18nContext,
    href: String,
    text: String,
    icon: SVGIcons.SVGIcon,
    selected: Boolean,
    new: Boolean
) {
    sectionEntry("/${i18nContext.get(I18nKeysData.Website.LocalePathId)}$href", selected) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), .entries (innerHTML) -> .entries (innerHTML), #that-wasnt-very-cash-money-of-you-fnuy (innerHTML) -> #that-wasnt-very-cash-money-of-you-fnuy (innerHTML)"
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"
        attributes["bliss-component"] = "close-left-sidebar-on-click"
        attributes["loritta-cancel-if-save-bar-active"] = "true"

        sectionEntryContent(text, icon, new)
    }
}

/**
 * A dashboard left sidebar entry href link that swaps the left sidebar contents and the right sidebar contents that pretends to be a link
 */
fun FlowContent.aDashboardSidebarEntryButton(
    style: ButtonStyle,
    i18nContext: I18nContext,
    href: String,
    text: String
) {
    a(classes = "entry discord-button ${style.className}", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}$href") {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), #left-sidebar (innerHTML) -> #left-sidebar (innerHTML), #that-wasnt-very-cash-money-of-you-fnuy (innerHTML) -> #that-wasnt-very-cash-money-of-you-fnuy (innerHTML)"
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "#right-sidebar-wrapper"
        attributes["bliss-component"] = "close-left-sidebar-on-click"

        div(classes = "section-text") {
            text(text)
        }
    }
}