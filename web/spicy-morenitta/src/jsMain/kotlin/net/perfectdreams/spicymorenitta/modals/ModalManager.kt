package net.perfectdreams.spicymorenitta.modals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import js.core.jso
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.CloseModalButton
import org.w3c.dom.Element
import react.*
import react.dom.DangerouslySetInnerHTML
import react.dom.client.createRoot
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML.div
import web.cssom.ClassName
import web.html.HTMLDivElement

class ModalManager(val m: SpicyMorenitta) {
    private var activeModal by mutableStateOf<Modal?>(null)
    private val callbacks = mutableListOf<(Modal?) -> (Unit)>()

    fun closeModal() {
        activeModal = null
        notifySubscribers()
    }

    fun openModal(
        embeddedSpicyModal: EmbeddedSpicyModal
    ) {
        activeModal = Modal(
            this@ModalManager,
            embeddedSpicyModal.title,
            embeddedSpicyModal.canBeClosedByClickingOutsideTheWindow,
            {
                div {
                    dangerouslySetInnerHTML = jso<DangerouslySetInnerHTML> {
                        this.__html = embeddedSpicyModal.bodyHtml
                    }
                }
            },
            embeddedSpicyModal.buttonsHtml.map { html ->
                {
                    dangerouslySetInnerHTML = jso<DangerouslySetInnerHTML> {
                        this.__html = html
                    }
                }
            }
        )
        notifySubscribers()
    }

    fun openModalWithCloseButton(
        title: String,
        body: HTMLAttributes<HTMLDivElement>.() -> (Unit),
        vararg buttons: HTMLAttributes<HTMLDivElement>.(Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        body,
        { modal ->
            CloseModalButton {
                this.modal = modal
            }
        },
        *buttons
    )

    fun openCloseOnlyModal(
        title: String,
        body: HTMLAttributes<HTMLDivElement>.() -> (Unit)
    ) = openModal(
        title,
        true,
        body,
        { modal ->
            CloseModalButton {
                this.modal = modal
            }
        }
    )

    fun openModal(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: HTMLAttributes<HTMLDivElement>.() -> (Unit),
        vararg buttons: HTMLAttributes<HTMLDivElement>.(Modal) -> (Unit)
    ) {
        activeModal = Modal(
            this@ModalManager,
            title,
            canBeClosedByClickingOutsideTheWindow,
            body,
            buttons.toMutableList()
        )
        notifySubscribers()
    }

    // The reason why we have a subscription system is that the toast list component has a copy of the active toast list due to the way React works
    // Because we can't set state inside the component, so somehow we need to update the list inside of the component
    /**
     * Subscribes to changes to the active toasts list
     *
     * @param callback the code that will be executed on update
     * @return the created callback
     */
    fun subscribe(callback: (Modal?) -> (Unit)): (Modal?) -> Unit {
        callbacks.add(callback)
        return callback
    }

    /**
     * Unsubscribes from changes of the active toasts list
     *
     * @param callback the code that will be executed on update
     */
    fun unsubscribe(callback: (Modal?) -> (Unit)) {
        callbacks.remove(callback)
    }

    /**
     * Notifies subscribers about changes to the active toasts list
     */
    private fun notifySubscribers() {
        for (callback in this.callbacks) {
            // This looks stupid, but yes we need to create a NEW LIST because if we don't, React won't know how to rerender
            callback.invoke(this.activeModal)
        }
    }

    fun setupModalRendering(element: Element) {
        val ModalContainer = FC<Props> {
            var stateActiveModal by useState<Modal>()

            useEffectOnceWithCleanup {
                println("useEffectOnce")
                val callback = subscribe {
                    println("Callback $it")
                    stateActiveModal = it
                }

                this.onCleanup {
                    println("Cleaning up...")
                    unsubscribe(callback)
                }
            }

            // Wrapped in a div to only trigger a recomposition within this div when a modal is updated
            div {
                val activeModal = stateActiveModal
                var elementRef = useRef<HTMLDivElement>(null)

                useEffect {
                    val element = elementRef.current

                    if (element != null) {
                        // Hook up any htmx behavior on the modal
                        // htmx.process(element)
                        // Hook up any _hyperscript behavior on the modal
                        // processNode(element)
                        // And finally hook up and custom component
                        // m.processCustomComponents(element.unsafeCast<HTMLElement>())
                    }
                }

                if (activeModal != null) {
                    // Open modal if there is one present
                    div {
                        className = ClassName("modal-wrapper")

                        if (activeModal.canBeClosedByClickingOutsideTheWindow) {
                            onClick = {
                                // Close modal when clicking outside the screen
                                if (it.target == it.currentTarget)
                                    activeModal.close()
                            }
                        }

                        div {
                            this.key = activeModal.hashCode().toString()

                            this.className = ClassName("modal")

                            // Set that the elementRef will have the reference of THIS DIV
                            // It is a bit weird but that's how React works
                            this.ref = elementRef

                            div {
                                this.className = ClassName("content")

                                div {
                                    this.className = ClassName("title")
                                    + activeModal.title
                                }

                                activeModal.body.invoke(this)
                            }

                            if (activeModal.buttons.isNotEmpty()) {
                                div {
                                    this.className = ClassName("buttons-wrapper")

                                    activeModal.buttons.forEach {
                                        it.invoke(this, activeModal)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        createRoot(element.unsafeCast<HTMLDivElement>()).render(ModalContainer.create())

        /* renderComposable(element) {
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
                                    m.processCustomComponents(htmlDivElement)
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
        } */
    }
}