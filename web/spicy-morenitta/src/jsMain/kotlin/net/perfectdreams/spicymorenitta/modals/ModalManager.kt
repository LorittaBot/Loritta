package net.perfectdreams.spicymorenitta.modals

import androidx.compose.runtime.*
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.CloseModalButton
import net.perfectdreams.spicymorenitta.components.HtmlText
import net.perfectdreams.spicymorenitta.utils.htmx
import net.perfectdreams.spicymorenitta.utils.processNode
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import web.html.HTMLElement

class ModalManager(val m: SpicyMorenitta) {
    private var activeModal by mutableStateOf<Modal?>(null)

    fun closeModal() {
        activeModal = null
    }

    fun openModal(
        embeddedSpicyModal: EmbeddedSpicyModal
    ) {
        activeModal = Modal(
            this@ModalManager,
            embeddedSpicyModal.title,
            embeddedSpicyModal.canBeClosedByClickingOutsideTheWindow,
            {
                HtmlText(embeddedSpicyModal.bodyHtml)
            },
            embeddedSpicyModal.buttonsHtml.map { html ->
                {
                    HtmlText(html)
                }
            }
        )
    }

    fun openModalWithCloseButton(
        title: String,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        body,
        { modal ->
            CloseModalButton(modal)
        },
        *buttons
    )

    fun openCloseOnlyModal(
        title: String,
        body: @Composable (Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        body,
        { modal ->
            CloseModalButton(modal)
        }
    )

    fun openModal(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) {
        activeModal = Modal(
            this@ModalManager,
            title,
            canBeClosedByClickingOutsideTheWindow,
            body,
            buttons.toMutableList()
        )
    }

    fun setupModalRendering(element: Element) {
        renderComposable(element.unsafeCast<HTMLElement>()) {
            // Wrapped in a div to only trigger a recomposition within this div when a modal is updated
            Div {
                val activeModal = this@ModalManager.activeModal
                if (activeModal != null) {
                    // Open modal if there is one present
                    Div(attrs = {
                        classes("modal-wrapper")

                        if (activeModal.canBeClosedByClickingOutsideTheWindow) {
                            onClick {
                                // Close modal when clicking outside of the screen
                                if (it.target == it.currentTarget)
                                    activeModal.close()
                            }
                        }
                    }) {
                        key(activeModal) {
                            Div(attrs = {
                                classes("modal")

                                ref { htmlDivElement ->
                                    // Hook up any htmx behavior on the modal
                                    htmx.process(htmlDivElement)
                                    // Hook up any _hyperscript behavior on the modal
                                    processNode(htmlDivElement)
                                    // And finally hook up and custom component
                                    m.processCustomComponents(htmlDivElement.unsafeCast<org.w3c.dom.HTMLElement>())
                                    onDispose {}
                                }
                            }) {
                                Div(attrs = { classes("content") }) {
                                    Div(attrs = { classes("title") }) {
                                        Text(activeModal.title)
                                    }

                                    activeModal.body.invoke(activeModal)
                                }

                                if (activeModal.buttons.isNotEmpty()) {
                                    Div(attrs = { classes("buttons-wrapper") }) {
                                        activeModal.buttons.forEach {
                                            it.invoke(activeModal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}