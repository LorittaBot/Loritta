package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.formatters.IntlMFFormatter
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSpicyInfoResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend

class GlobalState(val m: LorittaDashboardFrontend) {
    private var userInfoState = mutableStateOf<State<GetUserIdentificationResponse>>(State.Loading())
    var userInfo by userInfoState
    var i18nContext by mutableStateOf<State<I18nContext>>(State.Loading())
    private var spicyInfoState = mutableStateOf<State<GetSpicyInfoResponse>>(State.Loading())
    var spicyInfo by spicyInfoState
    var isSidebarOpenState = mutableStateOf(false)
    var isSidebarOpen by isSidebarOpenState
    var activeModal by mutableStateOf<Modal?>(null)

    private val jobs = mutableListOf<Job>()

    suspend fun updateSelfUserInfo() {
        m.makeApiRequestAndUpdateState(userInfoState, HttpMethod.Get, "/api/v1/users/@me")
    }

    suspend fun updateSpicyInfo() {
        m.makeApiRequestAndUpdateState(spicyInfoState, HttpMethod.Get, "/api/v1/spicy")
    }

    suspend fun updateI18nContext() {
        val languageId = window.location.pathname.split("/")[1]
        val result = m.http.get("${window.location.origin}/api/v1/languages/$languageId").bodyAsText()

        val i18nContext = I18nContext(
            IntlMFFormatter(),
            Json.decodeFromString(result)
        )

        this@GlobalState.i18nContext = State.Success(i18nContext)
    }

    fun openModal(
        title: StringI18nData,
        body: @Composable () -> (Unit),
        vararg buttons: @Composable () -> (Unit)
    ) {
        launch {
            // I think it should always be "Success" here
            val state = i18nContext
            val i18nContext: I18nContext

            // Super hacky! Waits until the state is success and then opens the modal
            while (true) {
                val temp = (state as? State.Success)?.value
                if (temp != null) {
                    i18nContext = temp
                    break
                }

                delay(100)
            }

            activeModal = Modal(
                i18nContext.get(title),
                body,
                buttons.toMutableList()
            )
        }
    }

    fun openModal(
        title: String,
        body: @Composable () -> (Unit),
        vararg buttons: @Composable () -> (Unit)
    ) {
        activeModal = Modal(
            title,
            body,
            buttons.toMutableList()
        )
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
}