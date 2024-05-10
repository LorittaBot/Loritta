package net.perfectdreams.loritta.morenitta.website.utils

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import java.net.URLEncoder

object EmbeddedSpicyModalUtils {
    fun FlowContent.closeModalOnClick() {
        attributes["hx-on:click"] = "window['spicy-morenitta'].closeModal()"
    }

    fun FlowContent.openEmbeddedModalOnClick(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: TagConsumer<String>.() -> (Unit),
        buttons: List<BUTTON.() -> (Unit)>
    ) {
        attributes["hx-on:click"] = "window['spicy-morenitta'].openEmbeddedModal(this)"
        // We encode it using encodeURIComponent to avoid issues when nested modals breaking due to URL encoding (not really sure why they break tho)
        // While Base64 IS smaller than encodeURIComponent, encodeURIComponent compresses way better than Base64
        attributes["loritta-embedded-spicy-modal"] = encodeURIComponent(
            Json.encodeToString<EmbeddedSpicyModal>(
                EmbeddedSpicyModal(
                    title,
                    canBeClosedByClickingOutsideTheWindow,
                    createHTML().apply(body).finalize(),
                    buttons.map {
                        createHTML()
                            .button(classes = "discord-button") {
                                type = ButtonType.button
                                apply(it)
                            }
                    }
                )
            )
        )
    }

    fun FlowContent.openEmbeddedConfirmPurchaseModalOnClick(
        i18nContext: I18nContext,
        price: Long,
        userSonhos: Long,
        confirmPurchaseButtonBehavior: BUTTON.() -> (Unit)
    ) {
        openEmbeddedModalOnClick(
            i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Title),
            true,
            {
                div {
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
                }
            },
            listOf(
                {
                    classes += "no-background-theme-dependent-dark-text"

                    closeModalOnClick()
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
                },
                {
                    classes += "primary"
                    if (price > userSonhos) {
                        openEmbeddedNotEnoughSonhosModalOnClick(i18nContext, price)
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                    } else {
                        classes += "htmx-discord-like-loading-button"
                        attributes["hx-indicator"] = "this"
                        attributes["hx-disabled-elt"] = "this"
                        confirmPurchaseButtonBehavior.invoke(this)

                        div {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                        }

                        div(classes = "loading-text-wrapper") {
                            img(src = LoadingSectionComponents.list.random())

                            text("Carregando...")
                        }
                    }
                }
            )
        )
    }

    fun FlowContent.openEmbeddedNotEnoughSonhosModalOnClick(
        i18nContext: I18nContext,
        price: Long
    ) {
        openEmbeddedModalOnClick(
            i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Title),
            true,
            {
                div {
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Description(price)))
                }
            },
            listOf {
                classes += "no-background-theme-dependent-dark-text"

                closeModalOnClick()
                text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
            }
        )
    }

    fun encodeURIComponent(uri: String): String {
        return URLEncoder.encode(uri, "UTF-8")
            .replace("\\+".toRegex(), "%20") // replace '+' with '%20'
            .replace("%21".toRegex(), "!")
            .replace("%27".toRegex(), "'")
            .replace("%28".toRegex(), "(")
            .replace("%29".toRegex(), ")")
            .replace("%7E".toRegex(), "~")
    }
}