package net.perfectdreams.loritta.morenitta.website.views.httpapidocs

import io.ktor.http.*
import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DiscordLikeToggles.discordToggle
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.CurrentSong
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.LoriDevelopersDocsRoute.Companion.createObjectTemplateButton
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.MagicEndpoints
import net.perfectdreams.loritta.morenitta.website.routes.httpapidocs.ParameterKind
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds.PutGiveawayRoute
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoint
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.withNullability

class LoriEndpointDevelopersDocsView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification?,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    sidebarCategories: List<LoriDevelopersDocsView.SidebarCategory>,
    private val endpointId: String,
    private val endpoint: LoriPublicHttpApiEndpoint,
    private val endpointTesterOptions: MagicEndpoints.EndpointTesterOptions,
    private val guildCount: Int,
    private val executedCommands: Int,
    private val uniqueUsersExecutedCommands: Int,
    private val currentSong: CurrentSong
) : LoriDevelopersDocsDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    sidebarCategories
) {
    override fun DIV.generateRightSidebarContents() {
        div(classes = "developer-docs") {
            h1 {
                text(i18nContext.get(MagicEndpoints.getEndpointI18nTitle(endpointId)))
            }

            div(classes = "endpoint-reference http-method-${endpoint.method.value.lowercase()}") {
                span(classes = "http-method-pill") {
                    text(endpoint.method.value)
                }

                text(" ")

                span(classes = "endpoint-path") {
                    text("/v1")
                    text(endpoint.path)
                }
            }

            val isFullRequestBody = endpointTesterOptions.isFullRequestBody

            h2 {
                text("Parâmetros de Requisição")
            }
            if (endpointTesterOptions.pathParameters.isNotEmpty()) {
                h3 {
                    text("Path")
                }

                renderParametersExplanations(endpointTesterOptions.pathParameters)
            }

            if (endpointTesterOptions.queryParameters.isNotEmpty()) {
                h3 {
                    text("Query")
                }

                renderParametersExplanations(endpointTesterOptions.queryParameters)
            }

            val clazzesParameters = endpointTesterOptions.clazzesParameters
            if (clazzesParameters.isNotEmpty()) {
                // Should NEVER be null
                val mainJsonBodyParams = endpointTesterOptions.clazzesParameters[endpointTesterOptions.mainRequestBodyClazzName]!!
                val additionalParams = endpointTesterOptions.clazzesParameters.toMutableMap()
                    .apply {
                        remove(endpointTesterOptions.mainRequestBodyClazzName)
                    }

                h3 {
                    text("JSON Body")
                }

                renderParametersExplanations(mainJsonBodyParams)

                for (apiParameter in additionalParams) {
                    h4 {
                        text(apiParameter.key)
                    }
                    renderParametersExplanations(apiParameter.value)
                }
            }

            style {
                unsafe {
                    raw(""".parameter-info p { margin: 0; }""")
                }
            }

            val examples = endpointTesterOptions.examples
            if (examples.isNotEmpty()) {
                h2 {
                    text("Exemplos")
                }

                for (example in examples) {
                    var pathToBeUsed = endpoint.path
                    for (pathParameter in endpointTesterOptions.pathParameters) {
                        pathToBeUsed = pathToBeUsed.replace("{${pathParameter.name}}", example.request.pathParams[pathParameter.name]!!)
                    }

                    val requestUrl = URLBuilder("https://api.loritta.website/v1$pathToBeUsed")
                        .apply {
                            for (queryParameter in endpointTesterOptions.queryParameters) {
                                val value = example.request.queryParams[queryParameter.name] ?: continue

                                parameters[queryParameter.name] = value
                            }
                        }.buildString()

                    val curlRequestCLI = buildString {
                        append("curl \"$requestUrl\" ")
                        if (endpoint.method != HttpMethod.Get)
                            append("-X ${endpoint.method.value} ")
                        if (example.request.requestBody != null)
                            append("--data '${example.request.requestBody}' ")
                        // when (requestBody) {
                        //     is String -> append("--data '$requestBody' ")
                        //     is ByteArray -> append("--data-binary @image.png ")
                        // }
                        append("--header \"Authorization: lorixp_xxx\" -i")
                    }

                    mainframeTerminal(
                        "Requisição",
                    ) {
                        div {
                            span(classes = "term-green") {
                                text("${userIdentification?.username ?: "wumpus"}@loritta:~# ")
                            }

                            text(curlRequestCLI)
                        }

                        div(classes = "term-pink") {
                            text("HTTP ${example.response.status.value} (${example.response.status.description})")
                        }

                        for (header in example.response.headers) {
                            div {
                                span(classes = "term-blue") {
                                    text("${header.key}: ")
                                }

                                text(header.value)
                            }
                        }
                        div(classes = "term-orange") {
                            style = "white-space: pre-wrap;"

                            when (example.response.body) {
                                is MagicEndpoints.EndpointRequestExample.Response.JsonResponseBody -> {
                                    text(
                                        Json {
                                            prettyPrint = true
                                        }.encodeToString(example.response.body.json)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            h2 {
                text("Testar Endpoint")
            }

            div {
                style = "display: flex; gap: 1em; flex-direction: column;"

                div {
                    style = ""
                    form {
                        // If it is full request body, we'll use a different encoding, because they'll probably use file uploads
                        if (isFullRequestBody) {
                            attributes["hx-encoding"] = "multipart/form-data"
                        }

                        input(InputType.hidden) {
                            name = "endpointId"
                            value = endpointId
                        }

                        div {
                            text("Token de Autenticação")
                        }

                        input(InputType.password) {
                            name = "authToken"
                        }

                        for (option in endpointTesterOptions.pathParameters) {
                            renderTestEndpointParameter(
                                i18nContext,
                                endpointId,
                                ParameterKind.PATH,
                                option,
                                ""
                            )
                        }

                        for (option in endpointTesterOptions.queryParameters) {
                            renderTestEndpointParameter(
                                i18nContext,
                                endpointId,
                                ParameterKind.QUERY,
                                option,
                                ""
                            )
                        }

                        val mainRequestBodyClazzName = endpointTesterOptions.mainRequestBodyClazzName
                        if (mainRequestBodyClazzName != null) {
                            val parameters = endpointTesterOptions.clazzesParameters[mainRequestBodyClazzName]
                            if (parameters != null) {
                                for (option in parameters) {
                                    renderTestEndpointParameter(
                                        i18nContext,
                                        endpointId,
                                        ParameterKind.JSON_BODY,
                                        option,
                                        ""
                                    )
                                }
                            }
                        }

                        // If it is full request body, we'll use a different encoding, because they'll probably use file uploads
                        if (isFullRequestBody) {
                            renderTestEndpointParameter(
                                i18nContext,
                                endpointId,
                                ParameterKind.FULL_BODY,
                                MagicEndpoints.APIParameter(
                                    ByteArray::class.createType(),
                                    "body",
                                    null,
                                    false
                                ) { listOf() },
                                ""
                            )
                        }

                        div {
                            style = "margin-top: 0.5em; display: flex; gap: 0.5em;"
                            button(classes = "discord-button primary", type = ButtonType.submit) {
                                attributes["hx-vals"] = buildJsonObject {
                                    put("executeRequest", true)
                                }.toString()
                                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs/endpoint-tester"
                                attributes["hx-target"] = "#result-stuff"
                                attributes["hx-disabled-elt"] = "this"

                                htmxDiscordLikeLoadingButtonSetup(i18nContext) {
                                    text("Testar")
                                }
                            }

                            button(classes = "discord-button secondary", type = ButtonType.submit) {
                                attributes["hx-vals"] = buildJsonObject {
                                    put("executeRequest", false)
                                }.toString()
                                attributes["hx-post"] =
                                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/developers/docs/endpoint-tester"
                                attributes["hx-target"] = "#result-stuff"
                                attributes["hx-disabled-elt"] = "this"

                                htmxDiscordLikeLoadingButtonSetup(i18nContext) {
                                    text("Gerar comando cURL")
                                }
                            }
                        }
                    }
                }

                mainframeTerminalLorifetch(
                    lorittaWebsite.loritta,
                    i18nContext,
                    userIdentification,
                    "Requisição",
                    "result-stuff",
                    guildCount,
                    executedCommands,
                    uniqueUsersExecutedCommands,
                    currentSong
                )
            }
        }
    }

    private fun DIV.renderParametersExplanations(apiParameters: List<MagicEndpoints.APIParameter>) {
        for ((index, queryParameter) in apiParameters.withIndex()) {
            val isFirst = index == 0
            val isLast = index == apiParameters.size - 1
            val isOnlyOne = isFirst && isLast

            div(classes = "parameter-info") {
                if (isOnlyOne) {
                    style = "border: 1px solid var(--soft-border-color);\n" +
                            "  padding: 1em;\n" +
                            "  border-radius: 3px 3px 3px 3px; display: flex; gap: 0.5em; flex-direction: column;"
                } else if (isLast) {
                    style = "border: 1px solid var(--soft-border-color);\n" +
                            "  padding: 1em;\n" +
                            "  border-radius: 0px 0px 3px 3px; display: flex; gap: 0.5em; flex-direction: column;"
                } else if (isFirst) {
                    style = "border: 1px solid var(--soft-border-color);\n" +
                            "  padding: 1em;\n" +
                            "  border-radius: 3px 3px 0px 0px; border-bottom: none; display: flex; gap: 0.5em; flex-direction: column;"
                } else {
                    style = "border: 1px solid var(--soft-border-color); border-bottom: none;\n" +
                            "  padding: 1em; display: flex; gap: 0.5em; flex-direction: column;"
                }

                div {
                    style = "font-size: 1.25em;"

                    code {
                        style = "font-weight: bold;"
                        text(queryParameter.name)
                    }

                    val parameterType = if (queryParameter.name == "color")
                        Int::class.createType(nullable = true)
                    else
                        queryParameter.kType

                    val parameterTypeAsString = parameterType.toString()

                    text(
                        ": " + parameterTypeAsString.replace(
                            Regex("((?:[A-z0-9]+\\.)*)([A-z0-9]+)"),
                            { it.groupValues[2] })
                    ) // hack!!!

                    if (queryParameter.isOptional)
                        text(" (opcional)")
                }

                if (queryParameter.explanation != null) {
                    p {
                        text(i18nContext.get(queryParameter.explanation))
                    }
                }
                /* val pathParameterInHtml = it.select("api-parameter")
                .firstOrNull { it.attr("name") == queryParameter.name }

            if (pathParameterInHtml != null) {
                unsafe {
                    raw(pathParameterInHtml.html())
                }
            } */

                val examples = queryParameter.examples.invoke(i18nContext)

                if (examples.isNotEmpty()) {
                    div {
                        if (examples.size == 1) {
                            div {
                                text("Exemplo:")
                            }
                        } else {
                            div {
                                text("Exemplos:")
                            }
                        }

                        for (example in examples) {
                            code {
                                text(example)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getTitle() = i18nContext.get(MagicEndpoints.getEndpointI18nTitle(endpointId))

    companion object {
        fun FlowContent.renderTestEndpointParameter(
            i18nContext: I18nContext,
            endpointId: String,
            parameterKind: ParameterKind,
            option: MagicEndpoints.APIParameter,
            optionNamePrefix: String
        ) {
            div {
                div {
                    text(option.name)
                    text(" ")
                    span(classes = "test-parameter-type") {
                        classes += "${parameterKind.clazzPrefix}-parameter"
                        text(i18nContext.get(parameterKind.title))
                    }
                }

                if (option.kType.withNullability(false) == ByteArray::class.createType()) {
                    input(InputType.file) {
                        name = "uploadedFile"
                    }
                } else if (option.kType.isSubtypeOf(
                        List::class.createType(
                            arguments = listOf(KTypeProjection.invariant(TransactionType::class.createType())),
                            nullable = true
                        )
                    )
                ) {
                    select {
                        name = "${parameterKind.postParameterPrefix}:$optionNamePrefix${option.name}"
                        multiple = true
                        attributes["data-component-mounter"] = "loritta-select-menu"

                        for (transactionType in TransactionType.entries) {
                            option {
                                this.value = transactionType.name
                                text(transactionType.name)
                            }
                        }
                    }
                } else if (option.kType.withNullability(false) == PutGiveawayRoute.SpawnGiveawayRequest.GiveawayRoles::class.createType()) {
                    // We can't use a form here because nested forms are not allowed!
                    createObjectTemplateButton(
                        i18nContext,
                        endpointId,
                        option,
                        PutGiveawayRoute.SpawnGiveawayRequest.GiveawayRoles::class.simpleName!!,
                        "Criar GiveawayRoles"
                    )
                } else if (option.kType.withNullability(false) == Boolean::class.createType()) {
                    discordToggle(
                        "${parameterKind.postParameterPrefix}:$optionNamePrefix${option.name}",
                        option.name,
                        null,
                        false
                    ) {

                    }
                    /* input(InputType.checkBox) {
                    name = "${parameterKind.postParameterPrefix}:$optionNamePrefix${option.name}"
                    // val defaultValue = option.examples.invoke(i18nContext).firstOrNull()
                    // if (defaultValue != null)
                    //     value = defaultValue
                } */
                } else {
                    input(InputType.text) {
                        name = "${parameterKind.postParameterPrefix}:$optionNamePrefix${option.name}"
                        val defaultValue = option.examples.invoke(i18nContext).firstOrNull()
                        if (defaultValue != null)
                            value = defaultValue
                    }
                }
            }
        }
    }
}