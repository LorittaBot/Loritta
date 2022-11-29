package net.perfectdreams.spicymorenitta.routes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.coroutines.*
import net.perfectdreams.loritta.serializable.requests.GetDailyRewardRequest
import net.perfectdreams.loritta.serializable.requests.GetDailyRewardStatusRequest
import net.perfectdreams.loritta.serializable.responses.*
import net.perfectdreams.spicymorenitta.utils.State
import net.perfectdreams.spicymorenitta.utils.loriUrl
import org.w3c.dom.Audio
import org.w3c.dom.url.URLSearchParams

sealed class DailyScreen(internal val route: DailyRoute) {
    internal val m = route.m
    private var rejectNewJobs = false
    private val jobs = mutableListOf<Job>()

    open fun onLoad() {}

    fun dispose() {
        rejectNewJobs = true
        println("Disposing ${jobs.size} jobs...")
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        if (rejectNewJobs)
            error("All new jobs are being rejected!")

        val job = GlobalScope.launch(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        if (rejectNewJobs)
            error("All new jobs are being rejected!")

        val job = GlobalScope.async(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    class GetDailyRewardScreen(route: DailyRoute) : DailyScreen(route) {
        var responseState by mutableStateOf<State<GetDailyRewardStatusResponse>>(State.Loading())
        val ts1Promotion2 = Audio("${loriUrl}assets/snd/ts1_promotion2.mp3")
        var executingRequest by mutableStateOf<Boolean>(false)
        var captchaToken by mutableStateOf<String?>(null)

        override fun onLoad() {
            launch {
                val response = m.sendRPCRequest<GetDailyRewardStatusResponse>(GetDailyRewardStatusRequest)
                responseState = State.Success(response)
            }
        }

        suspend fun sendDailyRewardRequest(captchaToken: String, questionId: String, questionResponse: Boolean) {
            executingRequest = true
            val searchParams = URLSearchParams(window.location.search)
            val guild = searchParams.get("guild")

            val response = m.sendRPCRequest<GetDailyRewardResponse>(
                GetDailyRewardRequest(
                    captchaToken,
                    questionId,
                    questionResponse,
                    guild?.toLongOrNull()
                )
            )

            when (response) {
                is DiscordAccountError.InvalidDiscordAuthorization -> {
                    window.alert("Você não está logado na sua conta!")
                }
                is DiscordAccountError.UserIsLorittaBanned -> {
                    window.alert("Banido de usar a Loritta!")
                }
                is DailyPayoutError.AlreadyGotTheDailyRewardSameAccount -> TODO()
                is DailyPayoutError.AlreadyGotTheDailyRewardSameIp -> TODO()
                is DailyPayoutError.AlreadyGotTheDailyRewardSameIpRequiresMFA -> TODO()
                is UserVerificationError.BlockedEmail -> TODO()
                is UserVerificationError.BlockedIp -> TODO()
                is UserVerificationError.DiscordAccountNotVerified -> TODO()
                is GetDailyRewardResponse.InvalidCaptchaToken -> TODO()
                is GetDailyRewardResponse.Success -> {
                    // Switch out
                    while (route.opacity > 0.0) {
                        route.opacity -= 0.10
                        delay(25)
                    }

                    route.opacity = 0.0

                    ts1Promotion2.play()

                    route.screen = GotDailyRewardScreen(
                        route,
                        response
                    )

                    while (1.0 > route.opacity) {
                        route.opacity += 0.10
                        delay(25)
                    }

                    route.opacity = 1.0
                }
            }
        }
    }

    class GotDailyRewardScreen(route: DailyRoute, val response: GetDailyRewardResponse.Success) : DailyScreen(route) {
        val cash = Audio("${loriUrl}assets/snd/css1_cash.wav")

        override fun onLoad() {}
    }
}