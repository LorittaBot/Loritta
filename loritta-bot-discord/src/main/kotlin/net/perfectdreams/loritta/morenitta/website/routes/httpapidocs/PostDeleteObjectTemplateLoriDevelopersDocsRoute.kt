package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.LoriDevelopersDocsRoute.Companion.createObjectTemplateButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds.PutGiveawayRoute
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints

class PostDeleteObjectTemplateLoriDevelopersDocsRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/developers/docs/delete-object-template") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext
    ) {
        val postParams = call.receiveParameters()
        val endpointId = postParams.getOrFail("endpointId")
        val clazzName = postParams.getOrFail("clazzName")

        val apiParameters = MagicEndpoints.endpointTesterOptions[LoriPublicHttpApiEndpoints.CREATE_GUILD_GIVEAWAY]!!
        val option = apiParameters.clazzesParameters[PutGiveawayRoute.SpawnGiveawayRequest::class.simpleName!!]!!.first { it.name == "allowedRoles" }

        call.response.headerHXTrigger {
            playSoundEffect = "recycle-bin"
        }

        call.respondHtml(
            createHTML()
                .body {
                    // We can't use a form here because nested forms are not allowed!
                    createObjectTemplateButton(
                        i18nContext,
                        endpointId,
                        option,
                        clazzName,
                        "Criar GiveawayRoles"
                    )
                }
        )
    }
}