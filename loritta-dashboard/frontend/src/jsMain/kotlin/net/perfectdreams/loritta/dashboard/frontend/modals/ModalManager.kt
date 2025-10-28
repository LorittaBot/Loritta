package net.perfectdreams.loritta.dashboard.frontend.modals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButton
import net.perfectdreams.loritta.dashboard.frontend.compose.components.DiscordButtonType
import net.perfectdreams.loritta.dashboard.frontend.compose.components.RawHtml
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import web.cssom.ClassName
import web.dom.document
import web.html.HTMLElement

class ModalManager(val m: LorittaDashboardFrontend) {
    // A stack, the last modal in the stack is the one that is shown
    var modals = mutableStateListOf<Modal>()

    fun closeModal(modal: Modal) = this.modals.remove(modal)

    fun closeModal() {
        this.modals.removeLastOrNull()
    }

    fun closeAllModals() {
        this.modals.clear()
    }

    fun openModal(modal: EmbeddedModal) {
        val buttons = modal.buttonsHtml.map {
            @Composable { modal: Modal ->
                RawHtml(it)
            }
        }.toTypedArray()

        openModal(
            modal.title,
            modal.canBeClosedByClickingOutsideTheWindow,
            {
                RawHtml(modal.bodyHtml)
            },
            *buttons
        )
    }

    fun openModalWithOnlyCloseButton(
        title: String,
        body: @Composable (Modal) -> (Unit)
    ) {
        openModalWithCloseButton(
            title,
            body
        )
    }

    fun openModalWithCloseButton(
        title: String,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) {
        openModal(
            title,
            true,
            body,
            { modal ->
                DiscordButton(DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, attrs = {
                    onClick {
                        modal.close()
                    }
                }) {
                    Text("Fechar")
                }
            },
            *buttons
        )
    }

    fun openModal(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) {
        modals.add(
            Modal(
                this@ModalManager,
                title,
                canBeClosedByClickingOutsideTheWindow,
                body,
                buttons.toMutableList()
            )
        )
    }

    fun render(element: HTMLElement) {
        renderComposable(element) {
            // Wrapped in a div to only trigger a recomposition within this div when a modal is updated
            Div {
                val activeModal = this@ModalManager.modals.lastOrNull()

                if (activeModal != null) {
                    LaunchedEffect(Unit) {
                        document.body.classList.add(ClassName("modal-open"))
                    }

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
                            }) {
                                Div(attrs = {
                                    classes("content")

                                    ref {
                                        Bliss.processAttributes(it)
                                        onDispose {}
                                    }
                                }) {
                                    Div(attrs = { classes("title") }) {
                                        Text(activeModal.title)
                                    }

                                    activeModal.body.invoke(activeModal)
                                }

                                if (activeModal.buttons.isNotEmpty()) {
                                    Div(attrs = {
                                        classes("buttons-wrapper")

                                        ref {
                                            Bliss.processAttributes(it)
                                            onDispose {}
                                        }
                                    }) {
                                        activeModal.buttons.forEach {
                                            it.invoke(activeModal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LaunchedEffect(Unit) {
                        document.body.classList.remove(ClassName("modal-open"))
                    }
                }
            }
        }
    }
}