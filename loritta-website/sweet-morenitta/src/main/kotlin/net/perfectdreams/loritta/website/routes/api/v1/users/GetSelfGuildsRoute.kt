package net.perfectdreams.loritta.website.routes.api.v1.users

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.website.SampleSession
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.LoriWebCode
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.deserialize
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.website

class GetSelfGuildsRoute : BaseRoute("/api/v1/users/@me/guilds") {
    override suspend fun onRequest(call: ApplicationCall) {
        val session = call.sessions.get<SampleSession>() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        val userGuilds = session.serializedDiscordAuth?.deserialize()?.getUserGuilds() ?: run {
            WebsiteUtils.sendApiError(call, LoriWebCode.UNAUTHORIZED, httpStatusCode = HttpStatusCode.Unauthorized)
            return
        }

        val jsonNode = Constants.JSON_MAPPER.valueToTree<JsonNode>(userGuilds)

        if (call.parameters["check-join"] == "true") {
            val guilds = website.controller.discord.retrieveGuildsById(userGuilds.map { it.id.toLong() })

            jsonNode.map { it as ObjectNode }.forEach { node ->
                node.put(
                    "joined",
                    guilds.any {
                        node["id"].textValue() == it.idAsString
                    }
                )
            }
        }

        call.respondJson(jsonNode)
    }
}