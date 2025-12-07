package net.perfectdreams.loritta.dashboard.frontend.components

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import net.perfectdreams.bliss.Bliss
import net.perfectdreams.bliss.Bliss.executeAjax
import net.perfectdreams.bliss.BlissComponent
import net.perfectdreams.bliss.HttpMethod
import net.perfectdreams.bliss.HttpStatusCode
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import web.dom.document
import web.events.addEventHandler
import web.html.HTMLDivElement
import web.messaging.MESSAGE
import web.messaging.MessageEvent
import web.window.window

class TwitchCallbackListenerComponent(val m: LorittaDashboardFrontend) : BlissComponent<HTMLDivElement>() {
    override fun onMount() {
        val url = this.mountedElement.getAttribute("twitch-oauth2-url")!!
        val dashboardUrl = this.mountedElement.getAttribute("twitch-dashboard-url")!!

        window.open(url)

        this.registeredEvents += window.addEventHandler(MessageEvent.MESSAGE) {
            val userId = it.data

            // Switch / Twitch!
            // https://youtu.be/bMjA7p2-Ol8
            println("User ID: $userId")

            // This is a bit annoying because we need to do a request to redirect, like we would do with Bliss:tm:
            GlobalScope.launch {
                executeAjax(
                    document.body,
                    HttpMethod.Get,
                    dashboardUrl,
                    emptyMap(),
                    null,
                    null,
                    mapOf(
                        "userId" to JsonPrimitive(userId as String)
                    ),
                    mapOf(),
                    mapOf(),
                    listOf(
                        Bliss.SwapRequest(
                            setOf(HttpStatusCode.OK),
                            "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML)"
                        )
                    ),
                    listOf()
                )
            }

            m.modalManager.closeModal()
        }
    }

    override fun onUnmount() {}
}