package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.Language
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.CloseModalButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.EtherealGambiImg
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class GlobalState(val m: LorittaDashboardFrontend) {
    private var userInfoResource = mutableStateOf<Resource<GetUserIdentificationResponse>>(Resource.Loading())
    var userInfo by userInfoResource
    var i18nContext by mutableStateOf<Resource<I18nContext>>(Resource.Loading())
    private var spicyInfoResource = mutableStateOf<Resource<LorittaDashboardRPCResponse.GetSpicyInfoResponse.Success>>(Resource.Loading())
    var spicyInfo by spicyInfoResource
    var isSidebarOpenState = mutableStateOf(false)
    var isSidebarOpen by isSidebarOpenState
    var activeModals = mutableStateListOf<Modal>()
    val activeToasts = mutableStateListOf<ToastWithAnimationState>()
    var messageEditorRenderDirection by mutableStateOf(DiscordMessageUtils.RenderDirection.HORIZONTAL)
    var activeSaveBar by mutableStateOf(false)
    var theme by mutableStateOf(ColorTheme.LIGHT)

    private val jobs = mutableListOf<Job>()

    suspend fun updateSelfUserInfo() {
        m.makeApiRequestAndUpdateState(userInfoResource, HttpMethod.Get, "/api/v1/users/@me")
    }

    suspend fun updateSpicyInfo() {
        m.makeRPCRequestAndUpdateStateCheckType<LorittaDashboardRPCResponse.GetSpicyInfoResponse, LorittaDashboardRPCResponse.GetSpicyInfoResponse.Success>(spicyInfoResource, LorittaDashboardRPCRequest.GetSpicyInfoRequest())
    }

    suspend fun updateI18nContext() {
        val i18nContext = retrieveI18nContext()
        this@GlobalState.i18nContext = Resource.Success(i18nContext)
    }

    suspend fun retrieveI18nContext(): I18nContext {
        val languageId = window.location.pathname.split("/")[1]
        val result = m.http.get("${window.location.origin}/api/v1/languages/$languageId").bodyAsText()

        val language = Json.decodeFromString<Language>(result)

        return I18nContext(
            IntlMFFormatter(language.info.formattingLanguageId),
            Json.decodeFromString(result)
        )
    }

    fun openThemeSelectorModal(isFirstTime: Boolean) {
        openModal(
            "Antes de começar...",
            !isFirstTime, // First time users cannot click outside the modal to close it
            true,
            { modal ->
                Div(attrs = {
                    classes("theme-selector")
                }) {
                    Div(attrs = {
                        classes("theme-selector-lori")
                    }) {
                        Div(attrs = {
                            classes("theme-selector-lori-inner")
                        }) {
                            EtherealGambiImg(src = "https://stuff.loritta.website/loritta-matrix-choice-cookiluck.png", sizes = "500px")

                            Div(attrs = {
                                classes("theme-option", "light")
                                onClick {
                                    theme = ColorTheme.LIGHT
                                    localStorage.setItem("dashboard.selectedTheme", theme.name)
                                    modal.close()
                                }
                            }) {
                                Text("Tema Claro")
                            }

                            Div(attrs = {
                                classes("theme-option", "dark")
                                onClick {
                                    theme = ColorTheme.DARK
                                    localStorage.setItem("dashboard.selectedTheme", theme.name)
                                    modal.close()
                                }
                            }) {
                                Text("Tema Escuro")
                            }
                        }
                    }

                    Div(attrs = {
                        classes("theme-selector-buttons")
                    }) {
                        DiscordButton(
                            DiscordButtonType.PRIMARY,
                            attrs = {
                                onClick {
                                    theme = ColorTheme.LIGHT
                                    localStorage.setItem("dashboard.selectedTheme", theme.name)
                                    modal.close()
                                }
                            }
                        ) {
                            Text("Tema Claro")
                        }

                        DiscordButton(
                            DiscordButtonType.PRIMARY,
                            attrs = {
                                onClick {
                                    theme = ColorTheme.DARK
                                    localStorage.setItem("dashboard.selectedTheme", theme.name)
                                    modal.close()
                                }
                            }
                        ) {
                            Text("Tema Escuro")
                        }
                    }
                }
            })
    }

    fun openModal(
        title: StringI18nData,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        clearAllModals: Boolean,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) {
        launch {
            // I think it should always be "Success" here
            val state = i18nContext
            val i18nContext: I18nContext

            // Super hacky! Waits until the state is success and then opens the modal
            while (true) {
                val temp = (state as? Resource.Success)?.value
                if (temp != null) {
                    i18nContext = temp
                    break
                }

                delay(100)
            }

            activeModals.add(
                Modal(
                    this@GlobalState,
                    i18nContext.get(title),
                    canBeClosedByClickingOutsideTheWindow,
                    body,
                    buttons.toMutableList()
                )
            )
        }
    }

    fun openCloseOnlyModal(
        title: StringI18nData,
        clearAllModals: Boolean,
        body: @Composable (Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        clearAllModals,
        body,
        { modal ->
            CloseModalButton(this, modal)
        }
    )

    fun openModal(
        title: String,
        canBeClosedByClickingOutsideTheWindow: Boolean,
        clearAllModals: Boolean,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) {
        if (clearAllModals)
            activeModals.clear()

        activeModals.add(
            Modal(
                this,
                title,
                canBeClosedByClickingOutsideTheWindow,
                body,
                buttons.toMutableList()
            )
        )
    }

    fun openModalWithCloseButton(
        title: StringI18nData,
        clearAllModals: Boolean,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        clearAllModals,
        body,
        { modal ->
            CloseModalButton(this, modal)
        },
        *buttons
    )

    fun openModalWithCloseButton(
        title: String,
        clearAllModals: Boolean,
        body: @Composable (Modal) -> (Unit),
        vararg buttons: @Composable (Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        clearAllModals,
        body,
        { modal ->
            CloseModalButton(this, modal)
        },
        *buttons
    )

    fun openCloseOnlyModal(
        title: String,
        clearAllModals: Boolean,
        body: @Composable (Modal) -> (Unit)
    ) = openModal(
        title,
        true,
        clearAllModals,
        body,
        { modal ->
            CloseModalButton(this, modal)
        }
    )

    fun showToast(toastType: Toast.Type, title: String, body: @Composable () -> (Unit) = {}) {
        val toast = Toast(
            toastType,
            title,
            body
        )
        val toastWithAnimationState = ToastWithAnimationState(toast, Random.nextLong(0, Long.MAX_VALUE), mutableStateOf(ToastWithAnimationState.State.ADDED))

        activeToasts.add(toastWithAnimationState)

        launch {
            delay(7.seconds)
            toastWithAnimationState.state.value = ToastWithAnimationState.State.REMOVED
        }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val job = GlobalScope.launch(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val job = GlobalScope.async(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
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
}