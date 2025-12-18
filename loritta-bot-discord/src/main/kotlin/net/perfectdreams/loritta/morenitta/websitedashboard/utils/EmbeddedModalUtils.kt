package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.luna.modals.EmbeddedModal
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink

/**
 * Creates an embedded modal
 */
fun createEmbeddedModal(
    title: String,
    size: EmbeddedModal.Size,
    canBeClosedByClickingOutsideTheWindow: Boolean,
    body: DIV.() -> (Unit),
    buttons: List<FlowContent.() -> (Unit)>
): EmbeddedModal {
    return EmbeddedModal(
        title,
        size,
        canBeClosedByClickingOutsideTheWindow,
        createHTML(false).div { body() },
        buttons.map { createHTML(false).span { it() } }
    )
}

fun createEmbeddedConfirmPurchaseModal(
    i18nContext: I18nContext,
    price: Long,
    userSonhos: Long,
    confirmPurchaseButtonBehavior: BUTTON.() -> (Unit)
): EmbeddedModal {
    return createEmbeddedModal(
        i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Title),
        EmbeddedModal.Size.SMALL,
        true,
        {
            style = "text-align: center;"

            img {
                src = "https://stuff.loritta.website/lori-nota-fiscal.png"
                width = "300"
            }

            i18nContext.get(
                I18nKeysData.Website.Dashboard.PurchaseModal.Description(
                    price,
                    userSonhos
                )
            ).forEach {
                p {
                    text(it)
                }
            }
        },
        listOf(
            {
                defaultModalCloseButton(i18nContext)
            },
            {
                discordButton(ButtonStyle.PRIMARY) {
                    if (price > userSonhos) {
                        openModalOnClick(createEmbeddedNotEnoughSonhosModal(i18nContext, price))

                        text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                    } else {
                        confirmPurchaseButtonBehavior.invoke(this)

                        div(classes = "htmx-discord-like-loading-button") {
                            div {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                            }

                            div(classes = "loading-text-wrapper") {
                                img(src = LoadingSectionComponents.list.random())

                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                            }
                        }
                    }
                }
            }
        )
    )
}

fun createEmbeddedNotEnoughSonhosModal(i18nContext: I18nContext, price: Long): EmbeddedModal {
    return createEmbeddedModal(
        i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Title),
        EmbeddedModal.Size.MEDIUM,
        true,
        {
            div {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Description(price)))
            }
        },
        listOf {
            defaultModalCloseButton(i18nContext)
        }
    )
}

fun createEmbeddedConfirmDeletionModal(
    i18nContext: I18nContext,
    confirmDeletionButtonBehavior: BUTTON.() -> (Unit)
): EmbeddedModal {
    return createEmbeddedModal(
        "Você tem certeza?",
        EmbeddedModal.Size.SMALL,
        true,
        {
            text("Você quer deletar meeesmo?")
        },
        listOf(
            {
                defaultModalCloseButton(i18nContext)
            },
            {
                discordButton(ButtonStyle.DANGER) {
                    confirmDeletionButtonBehavior.invoke(this)

                    text("Excluir")
                }
            }
        )
    )
}

fun createEmbeddedDisableAdBlockModal(i18nContext: I18nContext): EmbeddedModal {
    return createEmbeddedModal(
        i18nContext.get(I18nKeysData.Website.Dashboard.DisableAdBlock.Title),
        EmbeddedModal.Size.MEDIUM,
        true,
        {
            div {
                style = "text-align: center;"

                img {
                    src = "https://stuff.loritta.website/that-wasnt-very-cash-money-of-you/that-wasnt-very-cash-money-of-you.png"
                    width = "300"
                }
            }

            for (line in i18nContext.get(I18nKeysData.Website.Dashboard.DisableAdBlock.PleaseDisable)) {
                p {
                    text(line)
                }
            }
        },
        listOf(
            {
                defaultModalCloseButton(i18nContext)
            },
            {
                discordButtonLink(ButtonStyle.PRIMARY, LorittaDashboardWebServer.INSTANCE.loritta.config.loritta.website.url.removeSuffix("/") + "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/donate") {
                    text("Premium")
                }
            }
        )
    )
}

/**
 * Adds a "show modal" to the DOM
 */
fun FlowContent.blissShowModal(modal: EmbeddedModal) {
    script(type = "application/json") {
        attributes["bliss-show-modal"] = "true"
        attributes["bliss-modal"] = BlissHex.encodeToHexString(Json.encodeToString(modal))
    }
}

/**
 * Adds a "close modal" to the DOM
 */
fun FlowContent.blissCloseModal() {
    script(type = "application/json") {
        attributes["bliss-close-modal"] = "true"
    }
}

/**
 * Adds a "close all modals" to the DOM
 */
fun FlowContent.blissCloseAllModals() {
    script(type = "application/json") {
        attributes["bliss-close-all-modals"] = "true"
    }
}

/**
 * Creates the default generic "close modal" button
 */
fun FlowContent.defaultModalCloseButton(i18nContext: I18nContext, text: StringI18nData = I18nKeysData.Website.Dashboard.Modal.Close) {
    discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
        attributes["bliss-close-modal-on-click"] = "true"
        text(i18nContext.get(text))
    }
}

/**
 * Opens a modal on click
 */
fun FlowContent.openModalOnClick(modal: EmbeddedModal) {
    attributes["bliss-modal"] = BlissHex.encodeToHexString(Json.encodeToString(modal))
    attributes["bliss-open-modal-on-click"] = "true"
}