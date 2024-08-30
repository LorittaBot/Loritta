package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.httpapidocs.LoriEndpointDevelopersDocsView.Companion.renderTestEndpointParameter
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints

class PostCreateObjectTemplateLoriDevelopersDocsRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/developers/docs/create-object-template") {
    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext
    ) {
        val session = call.lorittaSession
        val discordAuth = session.getDiscordAuth(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)
        val userIdentification = session.getUserIdentification(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)

        val postParams = call.receiveParameters()
        val endpointId = postParams.getOrFail("endpointId")
        val prefix = postParams.getOrFail("prefix")
        val clazzName = postParams.getOrFail("clazzName")

        val endpoint = LoriPublicHttpApiEndpoints.getEndpointById(endpointId)
        val apiParameters = MagicEndpoints.endpointTesterOptions[endpoint]!!.clazzesParameters[clazzName]!!

        call.respondHtml(
            createHTML()
                .body {
                    div(classes = "created-endpoint-object-template") {
                        div {
                            style = "border: 1px solid var(--soft-border-color);\n" +
                                    "  border-radius: var(--first-level-border-radius);\n" +
                                    "  padding: 1em;"

                            for (option in apiParameters) {
                                renderTestEndpointParameter(
                                    i18nContext,
                                    endpointId,
                                    ParameterKind.JSON_BODY,
                                    option,
                                    "$prefix."
                                )
                            }
                        }

                        button(type = ButtonType.button, classes = "discord-button danger") {
                            style = "margin-top: 0.5em;"

                            attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs/delete-object-template"
                            attributes["hx-target"] = "closest .created-endpoint-object-template"
                            attributes["hx-disabled-elt"] = "this"
                            attributes["hx-swap"] = "outerHTML"
                            attributes["hx-vals"] = buildJsonObject {
                                put("endpointId", endpointId)
                                put("clazzName", clazzName)
                                put("prefix", prefix)
                            }.toString()

                            text("Deletar")
                        }
                    }
                }
        )
    }
}