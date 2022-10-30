package net.perfectdreams.loritta.morenitta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.CommunityGuidelinesView

class CommunityGuidelinesRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/guidelines") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
        call.respondHtml(
            CommunityGuidelinesView(
                loritta,
                locale,
                getPathWithoutLocale(call),
            ).generateHtml()
        )
    }
}