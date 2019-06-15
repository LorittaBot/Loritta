package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.extensions.obj
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.website

class GetFanArtsRoute : BaseRoute("/api/v1/loritta/fan-arts") {
    fun filterEntries(node: ObjectNode, vararg fields: String): JsonNode {
        val toBeRemoved = mutableSetOf<String>()

        node.fieldNames().forEach {
            if (!fields.contains(it))
                toBeRemoved.add(it)
        }

        toBeRemoved.forEach { node.remove(it) }

        return node
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val fanArtists = Constants.JSON_MAPPER.valueToTree<JsonNode>(LorittaWebsite.INSTANCE.fanArtArtists)

        if (call.parameters["query"] == "all") {
            val discordIds = LorittaWebsite.INSTANCE.fanArtArtists
                .mapNotNull {
                    it.socialNetworks?.asSequence()?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
                        ?.firstOrNull()?.let { discordInfo ->
                        discordInfo.id.toLong()
                    }
                }

            val users = website.controller.discord.retrieveUsersById(discordIds)

            fanArtists.map {
                val discordInfo = it["networks"]?.firstOrNull { it["type"].textValue() == "discord" }

                if (discordInfo != null) {
                    val id = discordInfo["id"].textValue()
                    val user = users.firstOrNull { it.idAsString == id }

                    if (user != null) {
                        it.obj["user"] = filterEntries(Constants.JSON_MAPPER.valueToTree(user), "id", "name", "effectiveAvatarUrl")
                    }
                }
            }
        }

        call.respondJson(fanArtists)
    }
}