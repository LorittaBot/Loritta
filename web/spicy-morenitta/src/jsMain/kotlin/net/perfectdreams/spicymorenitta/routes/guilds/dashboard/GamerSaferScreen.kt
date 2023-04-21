package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.requests.GetGamerSaferVerifyConfigRequest
import net.perfectdreams.loritta.serializable.responses.*
import net.perfectdreams.spicymorenitta.utils.State

sealed class GamerSaferScreen(internal val route: GamerSaferVerifyRoute) {
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

    class GamerSaferVerifyScreen(route: GamerSaferVerifyRoute, val guildId: Long) : GamerSaferScreen(route) {
        var responseState by mutableStateOf<State<GetGamerSaferVerifyConfigResponse>>(State.Loading())
        var gamerSaferVerificationRoles = mutableStateListOf<GamerSaferVerificationUserAndRole>()

        override fun onLoad() {
            launch {
                val response = m.sendRPCRequest<GetGamerSaferVerifyConfigResponse>(GetGamerSaferVerifyConfigRequest(guildId))
                responseState = State.Success(response)
                if (response is GetGamerSaferVerifyConfigResponse.Success) {
                    gamerSaferVerificationRoles.addAll(response.verificationRoles)
                }
            }
        }
    }
}