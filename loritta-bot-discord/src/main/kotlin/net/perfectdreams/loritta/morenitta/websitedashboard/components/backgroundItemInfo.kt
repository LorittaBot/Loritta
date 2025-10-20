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

fun FlowContent.backgroundItemInfo(
    i18nContext: I18nContext,
    locale: BaseLocale,
    backgroundId: String,
    activeProfileDesignId: String,
    activeBackgroundId: String,
) {
    profileItemPreview(i18nContext, activeProfileDesignId, backgroundId)

    div {
        style = "text-align: center;"

        h1 {
            text(locale["backgrounds.$backgroundId.title"])
        }
    }

    div {
        text(locale["backgrounds.$backgroundId.description"])
    }

    itemInfoButtonsWrapper {
        discordButton(ButtonStyle.PRIMARY) {
            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/backgrounds/${backgroundId}"
            attributes["bliss-swap:200"] = "body (innerHTML) -> #trinket-info-content (innerHTML)"
            attributes["bliss-indicator"] = "this"

            if (activeBackgroundId == backgroundId) {
                disabled = true
            }

            div {
                text("Ativar")
            }

            div(classes = "loading-text-wrapper") {
                text("Carregando...")
            }
        }

        if (backgroundId == Background.CUSTOM_BACKGROUND_ID) {
            // For custom backgrounds, we'll add a special button to send a custom background
            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                openModalOnClick(
                    createEmbeddedModal(
                        "Background Personalizado",
                        true,
                        {
                            fileInput {
                                name = "file"
                            }
                        },
                        listOf(
                            {
                                defaultModalCloseButton(i18nContext)
                            },
                            {
                                discordButton(ButtonStyle.PRIMARY) {
                                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/backgrounds/upload"
                                    attributes["bliss-include-json"] = "[name='file']"

                                    text("Enviar")
                                }
                            }
                        )
                    )
                )
                text("Enviar Imagem")
            }
        }
    }
}