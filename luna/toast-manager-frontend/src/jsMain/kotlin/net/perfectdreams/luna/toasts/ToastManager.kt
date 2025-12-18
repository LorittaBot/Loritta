package net.perfectdreams.luna.toasts

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.html.HTMLTag
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.unsafe
import web.animations.ANIMATION_END
import web.animations.AnimationEvent
import web.cssom.ClassName
import web.dom.ElementId
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLElement
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ToastManager(
    val onToastListRendered: (HTMLElement) -> (Unit) = {},
    val onToastAdded: (ToastWithAnimationState) -> (Unit) = {}
) {
    lateinit var toastListElement: HTMLElement

    fun showToast(embeddedToast: EmbeddedToast) {
        val descriptionHtml = embeddedToast.descriptionHtml

        showToast(
            when (embeddedToast.type) {
                EmbeddedToast.Type.INFO -> Toast.Type.INFO
                EmbeddedToast.Type.SUCCESS -> Toast.Type.SUCCESS
                EmbeddedToast.Type.WARN -> Toast.Type.WARN
            },
            embeddedToast.title,
            {
                if (descriptionHtml != null) {
                    unsafe {
                        raw(descriptionHtml)
                    }
                }
            }
        )
    }

    fun showToast(toastType: Toast.Type, title: String, body: HTMLTag.() -> (Unit) = {}) {
        val toast = Toast(
            toastType,
            title,
            body
        )

        val toastWithAnimationState = ToastWithAnimationState(toast, Random.nextLong(0, Long.MAX_VALUE), MutableStateFlow(ToastWithAnimationState.State.ADDED))
        onToastAdded(toastWithAnimationState)

        val toastElement = document.createElement("div").apply {
            id = ElementId("toast-${toastWithAnimationState.toastId}")
            val toastStyle = when (toastWithAnimationState.toast.type) {
                Toast.Type.INFO -> "info"
                Toast.Type.SUCCESS -> "success"
                Toast.Type.WARN -> "warn"
            }

            className = ClassName("toast $toastStyle added")

            addEventHandler(AnimationEvent.ANIMATION_END) {
                when (toastWithAnimationState.state.value) {
                    ToastWithAnimationState.State.ADDED -> {
                        this.classList.remove(ClassName("added"))
                        toastWithAnimationState.state.value = ToastWithAnimationState.State.DEFAULT
                    }

                    ToastWithAnimationState.State.DEFAULT -> {
                        // I'm just happy to be here
                    }
                    ToastWithAnimationState.State.REMOVED -> {
                        this.remove()
                    }
                }
            }

            appendChild(
                document.createElement("div").apply {
                    classList.value = ClassName("toast-title")
                    textContent = toastWithAnimationState.toast.title
                }
            )

            appendChild(
                document.createElement("div").apply {
                    classList.value = ClassName("toast-description")
                    this as org.w3c.dom.HTMLElement
                    this.append.div {
                        body()
                    }
                }
            )
        }

        toastListElement.append(toastElement)

        GlobalScope.launch {
            delay(7.seconds)
            toastWithAnimationState.state.value = ToastWithAnimationState.State.REMOVED
            toastElement.classList.add(ClassName("removed"))
        }
    }

    fun render(element: HTMLElement) {
        val toastListElement = document.createElement("div").apply {
            classList.value = ClassName("toast-list")
        }

        this.toastListElement = toastListElement
        element.append(toastListElement)

        onToastListRendered(toastListElement)
    }

    class ToastWithAnimationState(
        val toast: Toast,
        val toastId: Long,
        val state: MutableStateFlow<State>,
    ) {
        enum class State {
            ADDED,
            DEFAULT,
            REMOVED
        }
    }
}