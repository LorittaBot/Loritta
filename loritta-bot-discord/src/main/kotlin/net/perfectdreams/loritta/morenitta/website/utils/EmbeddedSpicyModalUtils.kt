package net.perfectdreams.loritta.morenitta.website.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import java.net.URLEncoder

object EmbeddedSpicyModalUtils {
    fun modalButtonListOnlyCloseModalButton(i18nContext: I18nContext): List<BUTTON.() -> Unit> {
        return listOf {
            defaultModalCloseButton(i18nContext)
        }
    }

    fun FlowContent.closeModalOnClick() {
        attributes["hx-on:click"] = "window['spicy-morenitta'].closeModal()"
    }

    fun BUTTON.defaultModalCloseButton(i18nContext: I18nContext) {
        classes += "no-background-theme-dependent-dark-text"
        type = ButtonType.button
        closeModalOnClick()
        text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
    }

    fun createSpicyModal(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: TagConsumer<String>.() -> (Unit),
        buttons: List<BUTTON.() -> (Unit)>
    ): EmbeddedSpicyModal {
        return EmbeddedSpicyModal(
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
    }

    fun createSpicyToast(
        type: EmbeddedSpicyToast.Type,
        title: String,
        description: DIV.() -> (Unit) = {},
    ): EmbeddedSpicyToast {
        return EmbeddedSpicyToast(
            type,
            title,
            createHTML().div { apply(description) },
        )
    }

    fun FlowContent.openEmbeddedModalOnClick(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: TagConsumer<String>.() -> (Unit),
        buttons: List<BUTTON.() -> (Unit)>
    ) {
        openEmbeddedModalOnClick(
            createSpicyModal(
                title,
                canBeClosedByClickingOutsideTheWindow,
                body,
                buttons
            )
        )
    }

    fun FlowContent.openEmbeddedModalOnClick(embeddedSpicyModal: EmbeddedSpicyModal) {
        attributes["hx-on:click"] = "window['spicy-morenitta'].openEmbeddedModal(this)"
        // We encode it using encodeURIComponent to avoid issues when nested modals breaking due to URL encoding (not really sure why they break tho)
        // While Base64 IS smaller than encodeURIComponent, encodeURIComponent compresses way better than Base64
        // ATTENTION! If a stacktrace points to this line, it is probably an issue on the EmbeddedSpicyModal itself, NOT ON HERE
        // Check if the "body" is wrapped in a div or something!!
        attributes["loritta-embedded-spicy-modal"] = encodeURIComponent(Json.encodeToString<EmbeddedSpicyModal>(embeddedSpicyModal))
    }

    fun createEmbeddedConfirmPurchaseModal(
        i18nContext: I18nContext,
        price: Long,
        userSonhos: Long,
        confirmPurchaseButtonBehavior: BUTTON.() -> (Unit)
    ): EmbeddedSpicyModal {
        return EmbeddedSpicyModal(
            i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Title),
            true,
            createHTML().div {
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
                createHTML().button(classes = "discord-button") {
                    defaultModalCloseButton(i18nContext)
                },
                createHTML().button(classes = "discord-button primary") {
                    type = ButtonType.button
                    if (price > userSonhos) {
                        openEmbeddedNotEnoughSonhosModalOnClick(i18nContext, price)
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                    } else {
                        attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                        attributes["hx-disabled-elt"] = "this"
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
            )
        )
    }

    fun FlowContent.openEmbeddedConfirmPurchaseModalOnClick(
        i18nContext: I18nContext,
        price: Long,
        userSonhos: Long,
        confirmPurchaseButtonBehavior: BUTTON.() -> (Unit)
    ) {
        openEmbeddedModalOnClick(createEmbeddedConfirmPurchaseModal(i18nContext, price, userSonhos, confirmPurchaseButtonBehavior))
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
                defaultModalCloseButton(i18nContext)
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

    /**
     * Sets the [call] to use htmx's `HX-Trigger` header
     */
    fun ApplicationResponse.headerHXTrigger(
        block: SpicyMorenittaTriggers.() -> (Unit)
    )  = headerHXTrigger(SpicyMorenittaTriggers().apply(block))

    /**
     * Sets the [call] to use htmx's `HX-Trigger` header
     */
    fun ApplicationResponse.headerHXTrigger(
        triggers: SpicyMorenittaTriggers
    ) {
        this.header(
            "HX-Trigger",
            triggers.build().toString()
        )
    }

    /**
     * Sets the [call] to use htmx's `HX-Trigger` header
     */
    fun ApplicationResponse.headerHXPushURL(newUrl: String) {
        this.header(
            "HX-Push-URL",
            newUrl
        )
    }

    /**
     * Sets the [call] to use the response body as htmx's HX-Trigger
     */
    suspend fun ApplicationCall.respondBodyAsHXTrigger(
        status: HttpStatusCode,
        block: SpicyMorenittaTriggers.() -> (Unit)
    ) {
        // Enables the feature
        this.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
        val triggers = SpicyMorenittaTriggers().apply(block)
        val json = triggers.build().toString()
        this.respondJson(
            json,
            status
        )
    }
}