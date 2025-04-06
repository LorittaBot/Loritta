package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

object DashboardSaveBar {
    /**
     * @param i18nContext the i18nContext
     * @param hasChanges if the save bar has "changes" by default
     */
    fun DIV.lorittaSaveBar(
        i18nContext: I18nContext,
        hasChanges: Boolean,
        resetButtonAttributes: BUTTON.() -> (Unit),
        saveButtonAttributes: BUTTON.() -> (Unit)
    ) {
        // Maybe, with what little power you have... You can SAVE something else.
        div(classes = "save-bar-fill-screen-height") {}

        div(classes = "save-bar") {
            if (hasChanges) {
                classes += "has-changes"
            } else {
                classes += "initial-state"
                classes += "no-changes"
            }

            id = "save-bar"
            attributes["data-component-mounter"] = "loritta-save-bar"
            attributes["spicy-oob-attribute-swap"] = "spicy-initial-save-bar-has-changes"
            attributes["spicy-initial-save-bar-has-changes"] = hasChanges.toString()

            div(classes = "save-bar-small-text") {
                text("Deseja salvar?")
            }

            div(classes = "save-bar-large-text") {
                text("Cuidado! Você tem alterações que não foram salvas")
            }

            div(classes = "save-bar-buttons") {
                id = "save-bar-buttons"
                button(classes = "discord-button no-background-light-text") {
                    resetButtonAttributes.invoke(this)
                    id = "save-bar-reset-button"
                    attributes["hx-get"] = ""
                    attributes["hx-select"] = "#module-config-wrapper"
                    attributes["hx-target"] = "#module-config-wrapper"
                    attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                    attributes["hx-disabled-elt"] = "this"
                    // We don't want to swap nor settle because that causes a flicker due to our custom select menu
                    attributes["hx-swap"] = "outerHTML settle:0ms swap:0ms"
                    attributes["spicy-oob-attribute-swap"] = "hx-get,hx-post,hx-put,hx-patch,hx-delete"
                    attributes["spicy-save-bar-has-changes"] = hasChanges.toString()

                    // TODO: This causes the sound effect to be played twice, because of the HX-Trigger header
                    // language=JavaScript
                    attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                const saveBar = document.querySelector("#save-bar")
                                const initialHasChanges = saveBar.getAttribute("spicy-initial-save-bar-has-changes") === "true"
                                if (!initialHasChanges) {
                                    saveBar.classList.add("no-changes")
                                    saveBar.classList.remove("has-changes")
                                }
                                window['spicy-morenitta'].playSoundEffect("recycle-bin")
                            }
                            """.trimIndent()

                    div(classes = "htmx-discord-like-loading-button") {
                        div {
                            text("Redefinir")
                        }

                        div(classes = "loading-text-wrapper") {
                            img(src = LoadingSectionComponents.list.random())

                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                        }
                    }
                }

                button(classes = "discord-button success") {
                    saveButtonAttributes.invoke(this)
                    id = "save-bar-save-button"
                    attributes["hx-swap"] = "outerHTML settle:0ms swap:0ms"
                    attributes["hx-select"] = "#module-config-wrapper"
                    attributes["hx-target"] = "#module-config-wrapper"
                    attributes["hx-include"] = "#module-config"
                    attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                    attributes["hx-disabled-elt"] = "this"
                    // We don't want to swap nor settle because that causes a flicker due to our custom select menu
                    attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                    attributes["spicy-oob-attribute-swap"] = "hx-get,hx-post,hx-put,hx-patch,hx-delete"
                    attributes["hx-on::after-request"] = """
                            if (event.detail.successful) {
                                document.querySelector("#save-bar").classList.add("no-changes")
                                document.querySelector("#save-bar").classList.remove("has-changes")
                            }
                            """.trimIndent()

                    div(classes = "htmx-discord-like-loading-button") {
                        div {
                            text("Salvar")
                        }

                        div(classes = "loading-text-wrapper") {
                            img(src = LoadingSectionComponents.list.random())

                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                        }
                    }
                }
            }
        }
    }
}