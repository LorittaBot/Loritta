package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.text.TextUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPIParameter
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds.PostMusicalChairsRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds.PutGiveawayRoute
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import java.awt.Color
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object MagicEndpoints {
    private val DEFAULT_CLUSTER_HEADER = "Loritta-Cluster" to "Loritta Cluster 1 (Catalyst)"
    private val DEFAULT_TOKEN_CREATOR_HEADER = "Loritta-Token-Creator" to "123170274651668480"
    private val DEFAULT_TOKEN_USER_HEADER = "Loritta-Token-User" to "123170274651668480"

    fun getEndpointI18nTitle(endpointId: String): StringI18nData {
        return StringI18nData(StringI18nKey("devDocs.endpoint.${TextUtils.snakeToLowerCamelCase(endpointId.lowercase())}.title"), emptyMap())
    }

    class EndpointTesterOptions(
        val pathParameters: List<APIParameter> = listOf(),
        val queryParameters: List<APIParameter> = listOf(),
        val mainRequestBodyClazzName: String? = null,
        val clazzesParameters: Map<String, List<APIParameter>> = mapOf(),
        /**
         * If true, then a file upload request will be made
         */
        val isFullRequestBody: Boolean = false,
        val jsonBodyBuilder: ((ApplicationCall, Parameters) -> (String))? = null,
        val examples: List<EndpointRequestExample> = listOf()
    )

    class EndpointRequestExample(
        val request: Request,
        val response: Response
    ) {
        data class Request(
            val pathParams: Map<String, String> = mapOf(),
            val queryParams: Map<String, String> = mapOf(),
            val requestBody: String? = null
        )

        data class Response(
            val status: HttpStatusCode,
            val headers: Map<String, String>,
            val body: ResponseBody
        ) {
            sealed class ResponseBody

            data class JsonResponseBody(val json: JsonElement) : ResponseBody()
        }
    }

    val endpointTesterOptions = mapOf(
        LoriPublicHttpApiEndpoints.GET_USER_BY_ID to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "userId",
                    I18nKeysData.DevDocs.Generic.UserId,
                    false
                ) {
                    listOf("123170274651668480")
                }
            ),
            examples = listOf(
                EndpointRequestExample(
                    EndpointRequestExample.Request(
                        pathParams = mapOf("userId" to "123170274651668480")
                    ),
                    EndpointRequestExample.Response(
                        HttpStatusCode.OK,
                        mapOf(
                            DEFAULT_CLUSTER_HEADER,
                            DEFAULT_TOKEN_CREATOR_HEADER,
                            DEFAULT_TOKEN_USER_HEADER,
                            "Content-Type" to "application/json"
                        ),
                        EndpointRequestExample.Response.JsonResponseBody(
                            Json.parseToJsonElement("{\"id\":\"123170274651668480\",\"xp\":6283483,\"sonhos\":19613336,\"aboutMe\":\"\\\"She said \\\"rawr x3\\\", so it's true love at first sight\\\" - Aniversário <t:1693234368:R> <:lori_zap:956404868417990776><:ehmole:589518158952398879> <:lori_bonk:956402010712834078>\",\"gender\":\"MALE\",\"emojiFightEmoji\":\"<:lori_sip:1167125644296069160>\"}")
                        )
                    )
                )
            )
        ),

        LoriPublicHttpApiEndpoints.GET_USER_TRANSACTIONS to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "userId",
                    I18nKeysData.DevDocs.Generic.UserId,
                    false
                ) {
                    listOf("123170274651668480")
                }
            ),
            queryParameters = listOf(
                APIParameter(
                    Int::class.createType(),
                    "limit",
                    null,
                    true
                ) {
                    listOf("10")
                },
                APIParameter(
                    Int::class.createType(),
                    "offset",
                    null,
                    true
                ) {
                    listOf("0")
                },
                APIParameter(
                    List::class.createType(arguments = listOf(KTypeProjection.invariant(TransactionType::class.createType()))),
                    "transactionTypes",
                    null,
                    true
                ) {
                    listOf("PAYMENT,DAILY_REWARD")
                },
                APIParameter(
                    Instant::class.createType(),
                    "beforeDate",
                    null,
                    true
                ) {
                    listOf(Clock.System.now().plus(365.days).toString()) // Yes, the toString returns it as ISO8601
                },
                APIParameter(
                    Instant::class.createType(),
                    "afterDate",
                    null,
                    true
                ) {
                    listOf(Clock.System.now().minus(365.days).toString()) // Yes, the toString returns it as ISO8601
                },
            )
        ),

        LoriPublicHttpApiEndpoints.SAVE_LORITTA_MESSAGE to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "guildId",
                    I18nKeysData.DevDocs.Generic.GuildId,
                    false
                ) {
                    listOf("297732013006389252")
                },
                APIParameter(
                    Long::class.createType(),
                    "channelId",
                    I18nKeysData.DevDocs.Generic.ChannelId,
                    false
                ) {
                    listOf("297732013006389252")
                },
                APIParameter(
                    Long::class.createType(),
                    "messageId",
                    I18nKeysData.DevDocs.Generic.MessageId,
                    false,
                ) {
                    listOf("297732013006389252")
                }
            )
        ),

        LoriPublicHttpApiEndpoints.VERIFY_LORITTA_MESSAGE to EndpointTesterOptions(
            isFullRequestBody = true
        ),

        LoriPublicHttpApiEndpoints.CREATE_GUILD_GIVEAWAY to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "guildId",
                    I18nKeysData.DevDocs.Generic.GuildId,
                    false
                ) {
                    listOf("297732013006389252")
                },
            ),
            mainRequestBodyClazzName = PutGiveawayRoute.SpawnGiveawayRequest::class.simpleName!!,
            clazzesParameters = mapOf(
                PutGiveawayRoute.SpawnGiveawayRequest::class.simpleName!! to createAPIParametersFromClazz(
                    PutGiveawayRoute.SpawnGiveawayRequest::class
                ) {
                    when (it) {
                        "channelId" -> {
                            { listOf("297732013006389252") }
                        }
                        "reason" -> {
                            { listOf(it.get(I18nKeysData.Commands.Command.Giveaway.Setup.LetsSetupYourGiveawayStep.GiveawayNamePlaceholder)) }
                        }
                        "description" -> {
                            { listOf(it.get(I18nKeysData.Commands.Command.Giveaway.Setup.LetsSetupYourGiveawayStep.GiveawayDescriptionPlaceholder)) }
                        }
                        "reaction" -> {
                            {
                                listOf("\uD83C\uDF89")
                            }
                        }
                        "endsAt" -> {
                            {
                                listOf(Clock.System.now().plus(5.minutes).toString()) // Yes, the toString returns it as ISO8601
                            }
                        }
                        "numberOfWinners" -> {
                            {
                                listOf("1")
                            }
                        }
                        else -> {
                            { listOf() }
                        }
                    }
                },
                PutGiveawayRoute.SpawnGiveawayRequest.GiveawayRoles::class.simpleName!! to createAPIParametersFromClazz(
                    PutGiveawayRoute.SpawnGiveawayRequest.GiveawayRoles::class
                ) {
                    { listOf() }
                }
            ),
            jsonBodyBuilder = { call, params ->
                val allowedRoleIds = params["jsonparameter:allowedRoles.roleIds"]?.ifBlank { null }
                val allowedRoleIsAndCondition = params["jsonparameter:allowedRoles.isAndCondition"] == "on"
                val deniedRoleIds = params["jsonparameter:deniedRoles.roleIds"]?.ifBlank { null }
                val deniedRoleIsAndCondition = params["jsonparameter:deniedRoles.isAndCondition"] == "on"

                val allowedRoles = if (allowedRoleIds != null) {
                    PutGiveawayRoute.SpawnGiveawayRequest.GiveawayRoles(
                        allowedRoleIds.split(",").map { it.toLong() },
                        allowedRoleIsAndCondition
                    )
                } else null

                val deniedRoles = if (deniedRoleIds != null) {
                    PutGiveawayRoute.SpawnGiveawayRequest.GiveawayRoles(
                        deniedRoleIds.split(",").map { it.toLong() },
                        deniedRoleIsAndCondition
                    )
                } else null

                Json.encodeToString(
                    PutGiveawayRoute.SpawnGiveawayRequest(
                        channelId = params.getOrFail("jsonparameter:channelId").toLong(),
                        reason = params.getOrFail("jsonparameter:reason"),
                        description = params.getOrFail("jsonparameter:description"),
                        imageUrl = params["jsonparameter:imageUrl"]?.ifBlank { null },
                        thumbnailUrl = params["jsonparameter:thumbnailUrl"]?.ifBlank { null },
                        color = params["jsonparameter:color"]?.ifBlank { null }?.let { Color(it.toInt()) },
                        reaction = params.getOrFail("jsonparameter:reaction"),
                        endsAt = kotlinx.datetime.Instant.parse(params.getOrFail("jsonparameter:endsAt")),
                        numberOfWinners = params.getOrFail("jsonparameter:numberOfWinners").toInt(),
                        roleIdsToBeGivenToTheWinners = params["jsonparameter:roleIdsToBeGivenToTheWinners"]
                            ?.ifBlank { null }
                            ?.split(",")
                            ?.map { it.toLong() },
                        allowedRoles = allowedRoles,
                        deniedRoles = deniedRoles
                    )
                )
            },
        ),

        LoriPublicHttpApiEndpoints.REROLL_GUILD_GIVEAWAY to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "guildId",
                    I18nKeysData.DevDocs.Generic.GuildId,
                    false
                ) {
                    listOf("297732013006389252")
                },
                APIParameter(
                    Long::class.createType(),
                    "giveawayId",
                    I18nKeysData.DevDocs.Generic.GiveawayId,
                    false
                ) {
                    listOf("12345")
                }
            )
        ),

        LoriPublicHttpApiEndpoints.END_GUILD_GIVEAWAY to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "guildId",
                    I18nKeysData.DevDocs.Generic.GuildId,
                    false
                ) {
                    listOf("297732013006389252")
                },
                APIParameter(
                    Long::class.createType(),
                    "giveawayId",
                    I18nKeysData.DevDocs.Generic.GiveawayId,
                    false
                ) {
                    listOf("12345")
                }
            )
        ),

        LoriPublicHttpApiEndpoints.CREATE_GUILD_MUSICALCHAIRS to EndpointTesterOptions(
            pathParameters = listOf(
                APIParameter(
                    Long::class.createType(),
                    "guildId",
                    I18nKeysData.DevDocs.Generic.GuildId,
                    false
                ) {
                    listOf("297732013006389252")
                },
            ),
            mainRequestBodyClazzName = PostMusicalChairsRoute.SpawnMusicalChairsRequest::class.simpleName!!,
            clazzesParameters = mapOf(
                PostMusicalChairsRoute.SpawnMusicalChairsRequest::class.simpleName!! to createAPIParametersFromClazz(
                    PostMusicalChairsRoute.SpawnMusicalChairsRequest::class
                ) {
                    when (it) {
                        "channelId" -> {
                            { listOf("297732013006389252") }
                        }
                        else -> {
                            { listOf() }
                        }
                    }
                }
            ),
            jsonBodyBuilder = { call, params ->
                Json.encodeToString(
                    PostMusicalChairsRoute.SpawnMusicalChairsRequest(
                        voiceChannelId = params.getOrFail("jsonparameter:voiceChannelId").toLong(),
                        messageChannelId = params.getOrFail("jsonparameter:messageChannelId").toLong(),
                        songId = params.get("jsonparameter:songId")?.ifBlank { null },
                    )
                )
            },
        ),
    )

    private fun createAPIParametersFromClazz(
        clazz: KClass<*>,
        examples: (String) -> ((I18nContext) -> (List<String>))
    ): List<APIParameter> {
        val firstConstructorParameters = clazz.primaryConstructor!!.parameters
            .filter { it.hasAnnotation<LoriPublicAPIParameter>() }

        val apiParameters = mutableListOf<APIParameter>()

        for (parameter in firstConstructorParameters) {
            apiParameters.add(
                APIParameter(
                    parameter.type,
                    parameter.name!!,
                    null,
                    parameter.isOptional,
                    examples.invoke(parameter.name!!)
                )
            )
        }

        return apiParameters
    }

    data class APIParameter(
        /**
         * The type of this parameter
         */
        val kType: KType,

        /**
         * The parameter name on the files
         */
        val name: String,

        /**
         * The explanation of the parameter
         */
        val explanation: StringI18nData?,

        /**
         * If the parameter is optional
         */
        val isOptional: Boolean,

        /**
         * Examples of how to use this parameter
         */
        val examples: (I18nContext) -> (List<String>)
    )
}