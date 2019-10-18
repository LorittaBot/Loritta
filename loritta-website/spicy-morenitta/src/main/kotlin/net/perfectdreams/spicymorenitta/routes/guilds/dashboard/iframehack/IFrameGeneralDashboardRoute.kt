package net.perfectdreams.spicymorenitta.routes.guilds.dashboard.iframehack

import kotlinx.serialization.ImplicitReflectionSerializer
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLIFrameElement
import kotlin.browser.document

class IFrameGeneralDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildId}/dashboard") {
    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        val iframe = document.select<HTMLIFrameElement>("iframe")

        val newDocument = iframe.contentDocument!!
    }
}