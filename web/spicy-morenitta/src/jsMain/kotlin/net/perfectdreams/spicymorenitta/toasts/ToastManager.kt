package net.perfectdreams.spicymorenitta.toasts

import js.core.jso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import org.w3c.dom.Element
import react.*
import react.dom.DangerouslySetInnerHTML
import react.dom.client.createRoot
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML.div
import web.cssom.ClassName
import web.html.HTMLDivElement
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ToastManager(private val m: SpicyMorenitta) {
    private val activeToasts = mutableListOf<ToastWithAnimationState>()
    private val callbacks = mutableListOf<(List<ToastWithAnimationState>) -> (Unit)>()

    fun showToast(embeddedSpicyToast: EmbeddedSpicyToast) {
        showToast(
            Toast.Type.valueOf(embeddedSpicyToast.type.name),
            embeddedSpicyToast.title
        ) {
            val descriptionHtml = embeddedSpicyToast.descriptionHtml
            if (descriptionHtml != null)
                dangerouslySetInnerHTML = jso<DangerouslySetInnerHTML> {
                    this.__html = descriptionHtml
                }
        }
    }

    fun showToast(toastType: Toast.Type, title: String, body: HTMLAttributes<HTMLDivElement>.() -> (Unit) = {}) {
        val toast = Toast(
            toastType,
            title,
            body
        )
        val toastWithAnimationState = ToastWithAnimationState(toast, Random.nextLong(0, Long.MAX_VALUE), ToastWithAnimationState.State.ADDED)

        this.activeToasts.add(toastWithAnimationState)
        notifySubscribers()

        // TODO - htmx-adventures: Don't use GlobalScope
        GlobalScope.launch {
            delay(7.seconds)
            toastWithAnimationState.state = ToastWithAnimationState.State.REMOVED
            notifySubscribers()
        }
    }

    // The reason why we have a subscription system is that the toast list component has a copy of the active toast list due to the way React works
    // Because we can't set state inside the component, so somehow we need to update the list inside of the component
    /**
     * Subscribes to changes to the active toasts list
     *
     * @param callback the code that will be executed on update
     * @return the created callback
     */
    fun subscribe(callback: (List<ToastWithAnimationState>) -> (Unit)): (List<ToastWithAnimationState>) -> Unit {
        callbacks.add(callback)
        return callback
    }

    /**
     * Unsubscribes from changes of the active toasts list
     *
     * @param callback the code that will be executed on update
     */
    fun unsubscribe(callback: (List<ToastWithAnimationState>) -> (Unit)) {
        callbacks.remove(callback)
    }

    /**
     * Notifies subscribers about changes to the active toasts list
     */
    private fun notifySubscribers() {
        for (callback in this.callbacks) {
            // This looks stupid, but yes we need to create a NEW LIST because if we don't, React won't know how to rerender
            callback.invoke(this.activeToasts.toList())
        }
    }

    class ToastWithAnimationState(
        val toast: Toast,
        val toastId: Long,
        var state: State,
    ) {
        enum class State {
            ADDED,
            DEFAULT,
            REMOVED
        }
    }

    fun setupToastRendering(element: Element) {
        val ToastContainer = FC<Props> {
            var activeToasts by useState<List<ToastWithAnimationState>>(listOf())

            useEffectOnceWithCleanup {
                val callback = subscribe {
                    val currentlyActiveToastsIds = activeToasts.map { it.toastId }
                    val newActiveToastsIds = it.map { it.toastId }

                    for (id in newActiveToastsIds) {
                        if (!currentlyActiveToastsIds.contains(id)) {
                            m.soundEffects.toastNotificationWhoosh.play(
                                0.05,
                                Random.nextDouble(
                                    0.975,
                                    1.025
                                ) // Change the speed/pitch to avoid the sound effect sounding repetitive
                            )
                        }
                    }

                    activeToasts = it
                }

                onCleanup {
                    unsubscribe(callback)
                }
            }

            div {
                this.className = ClassName("toast-list")

                for (toastWithAnimationState in activeToasts) {
                    div {
                        this.key = toastWithAnimationState.toastId.toString()

                        this.id = "toast-${toastWithAnimationState.toastId}-${toastWithAnimationState.state}"

                        val classes = mutableListOf<String>("toast")

                        classes.add(
                            when (toastWithAnimationState.toast.type) {
                                Toast.Type.INFO -> "info"
                                Toast.Type.SUCCESS -> "success"
                                Toast.Type.WARN -> "warn"
                            }
                        )

                        when (toastWithAnimationState.state) {
                            ToastWithAnimationState.State.ADDED -> {
                                classes.add("added")
                                onAnimationEnd = {
                                    println("Finished toast (added) animation!")
                                    toastWithAnimationState.state = ToastWithAnimationState.State.DEFAULT
                                    this@ToastManager.notifySubscribers()
                                }
                            }

                            ToastWithAnimationState.State.DEFAULT -> {
                                // I'm just happy to be here
                            }

                            ToastWithAnimationState.State.REMOVED -> {
                                classes.add("removed")
                                onAnimationEnd = {
                                    println("Finished toast (removed) animation!")
                                    this@ToastManager.activeToasts.remove(toastWithAnimationState)
                                    this@ToastManager.notifySubscribers()
                                }
                            }
                        }

                        this.className = ClassName(classes.joinToString(" "))

                        div {
                            this.className = ClassName("toast-title")
                            +toastWithAnimationState.toast.title
                        }

                        div {
                            this.className = ClassName("toast-description")
                            toastWithAnimationState.toast.body.invoke(this)
                        }
                    }
                }
            }
        }

        createRoot(element.unsafeCast<HTMLDivElement>()).render(ToastContainer.create())
    }
}