package net.perfectdreams.loritta.discordchatmessagerendererserver

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes

object DiscordChatMessageRendererServerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        installCoroutinesDebugProbes()
        val server = DiscordChatMessageRendererServer()
        server.start()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installCoroutinesDebugProbes() {
        // Enable coroutine names, they are visible when dumping the coroutines
        System.setProperty("kotlinx.coroutines.debug", "on")

        // Enable coroutines stacktrace recovery
        System.setProperty("kotlinx.coroutines.stacktrace.recovery", "true")

        // It is recommended to set this to false to avoid performance hits with the DebugProbes option!
        DebugProbes.enableCreationStackTraces = false
        DebugProbes.install()
    }
}