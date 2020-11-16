package net.perfectdreams.loritta.plugin.rosbife.commands

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin

class CortesFlowCommand(
        m: RosbifePlugin,
) : DiscordAbstractCommandBase(
        m.loritta as LorittaDiscord,
        listOf(
                "cortesflow"
        ),
        CommandCategory.IMAGES
) {
    override fun command() = create {
        localizedDescription("commands.images.cortesflow.description")

        executesDiscord {
            if (args.isEmpty()) {
                val result = loritta.http.get<String>("https://gabriela-canary.loritta.website/api/v1/images/cortes-flow")

                val elements = Json.parseToJsonElement(result)
                        .jsonArray

                val availableGroupedBy = elements.groupBy { it.jsonObject["participant"]!!.jsonPrimitive.content }
                        .entries
                        .sortedByDescending { it.value.size }
                val embed = EmbedBuilder()

                for ((key, value) in availableGroupedBy) {
                    embed.addField(
                            key,
                            value.joinToString {
                                "`${it.jsonObject["path"]!!.jsonPrimitive.content.removePrefix("/api/v1/images/cortes-flow/")}` \\[[fonte](${it.jsonObject["source"]!!.jsonPrimitive.content})\\]"
                            },
                            true
                    )
                }

                sendMessage(
                        embed.build()
                )
                return@executesDiscord
            }

            val type = args.getOrNull(0)
            val string = args
                    .drop(1)
                    .joinToString(" ")

            val response = loritta.http.post<HttpResponse>("https://gabriela-canary.loritta.website/api/v1/images/cortes-flow/$type") {
                body = buildJsonObject {
                    putJsonArray("strings") {
                        addJsonObject {
                            put("string", string)
                        }
                    }
                }.toString()
            }

            sendFile(response.receive<ByteArray>(), "cortes_flow.jpg")
        }
    }
}