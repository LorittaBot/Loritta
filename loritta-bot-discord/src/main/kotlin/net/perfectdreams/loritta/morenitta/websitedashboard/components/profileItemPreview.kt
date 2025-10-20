package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.fileInput
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.serializable.Background

fun FlowContent.profileItemPreview(
    i18nContext: I18nContext,
    profileDesignId: String,
    backgroundId: String,
) {
    div(classes = "canvas-preview-wrapper-wrapper") {
        style = "width: fit-content; margin-left: auto; margin-right: auto;"
        div(classes = "canvas-preview-wrapper") {
            div(classes = "canvas-preview-profile-design") {
                img(src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=$profileDesignId&background=$backgroundId") {
                    style = "width: 300px; aspect-ratio: 4/3;"
                }
            }
        }
    }
}