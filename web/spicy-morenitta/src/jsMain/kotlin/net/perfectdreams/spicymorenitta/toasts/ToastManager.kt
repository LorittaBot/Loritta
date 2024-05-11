package net.perfectdreams.spicymorenitta.toasts

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.components.HtmlText
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ToastManager(private val m: SpicyMorenitta) {
    private val activeToasts = mutableStateListOf<ToastWithAnimationState>()

    fun showToast(embeddedSpicyToast: EmbeddedSpicyToast) {
        showToast(
            Toast.Type.valueOf(embeddedSpicyToast.type.name),
            embeddedSpicyToast.title
        ) {
            val descriptionHtml = embeddedSpicyToast.descriptionHtml
            if (descriptionHtml != null)
                HtmlText(descriptionHtml)
        }
    }

    fun showToast(toastType: Toast.Type, title: String, body: @Composable () -> (Unit) = {}) {
        val toast = Toast(
            toastType,
            title,
            body
        )
        val toastWithAnimationState = ToastWithAnimationState(toast, Random.nextLong(0, Long.MAX_VALUE), mutableStateOf(ToastWithAnimationState.State.ADDED))

        activeToasts.add(toastWithAnimationState)

        // TODO - htmx-adventures: Don't use GlobalScope
        GlobalScope.launch {
            delay(7.seconds)
            toastWithAnimationState.state.value = ToastWithAnimationState.State.REMOVED
        }
    }

    class ToastWithAnimationState(
        val toast: Toast,
        val toastId: Long,
        val state: MutableState<State>,
    ) {
        enum class State {
            ADDED,
            DEFAULT,
            REMOVED
        }
    }

    fun setupToastRendering(element: Element) {
        renderComposable(element) {
            Div(attrs = { classes("toast-list") }) {
                // TODO: How can we enable the "save-bar-active" class?
                for (toastWithAnimationState in activeToasts) {
                    // We need to key it based on the ID to avoid Compose recomposing the toast notification during an animation
                    // https://kotlinlang.slack.com/archives/C01F2HV7868/p1694583087487209
                    key(toastWithAnimationState.toastId) {
                        LaunchedEffect(Unit) {
                            m.soundEffects.toastNotificationWhoosh.play(
                                0.05,
                                Random.nextDouble(
                                    0.975,
                                    1.025
                                ) // Change the speed/pitch to avoid the sound effect sounding repetitive
                            )
                        }

                        Div(attrs = {
                            id("toast-${toastWithAnimationState.toastId}")
                            classes(
                                "toast",
                                when (toastWithAnimationState.toast.type) {
                                    Toast.Type.INFO -> "info"
                                    Toast.Type.SUCCESS -> "success"
                                    Toast.Type.WARN -> "warn"
                                }
                            )

                            when (toastWithAnimationState.state.value) {
                                ToastManager.ToastWithAnimationState.State.ADDED -> {
                                    classes("added")
                                    onAnimationEnd {
                                        println("Finished toast (added) animation!")
                                        toastWithAnimationState.state.value =
                                            ToastManager.ToastWithAnimationState.State.DEFAULT
                                    }
                                }

                                ToastManager.ToastWithAnimationState.State.DEFAULT -> {
                                    // I'm just happy to be here
                                }

                                ToastManager.ToastWithAnimationState.State.REMOVED -> {
                                    classes("removed")
                                    onAnimationEnd {
                                        println("Finished toast (removed) animation!")
                                        activeToasts.remove(toastWithAnimationState)
                                    }
                                }
                            }
                        }) {
                            Div(attrs = {
                                classes("toast-title")
                            }) {
                                Text(toastWithAnimationState.toast.title)
                            }

                            Div(attrs = {
                                classes("toast-description")
                            }) {
                                toastWithAnimationState.toast.body.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}