package net.perfectdreams.loritta.plugin.rosbife.commands

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

class CortesFlowCommand(
        m: RosbifePlugin,
) : DiscordAbstractCommandBase(
        m.loritta as LorittaDiscord,
        listOf(
                "cortesflow", "flowcortes"
        ),
        CommandCategory.IMAGES
) {
    override fun command() = create {
        localizedDescription("commands.command.cortesflow.description")
        localizedExamples("commands.command.cortesflow.examples")

        needsToUploadFiles = true

        arguments {
            argument(ArgumentType.TEXT) {}
            argument(ArgumentType.TEXT) {}
        }

        executesDiscord {
            if (args.isEmpty()) {
                val result = loritta.http.get<String>("https://gabriela.loritta.website/api/v1/images/cortes-flow")

                val elements = Json.parseToJsonElement(result)
                        .jsonArray

                val availableGroupedBy = elements.groupBy { it.jsonObject["participant"]!!.jsonPrimitive.content }
                        .entries
                        .sortedByDescending { it.value.size }
                val embed = EmbedBuilder()
                        .setTitle("${Emotes.FLOW_PODCAST} ${locale["commands.command.cortesflow.embedTitle"]}")
                        .setDescription(
                                locale.getList(
                                        "commands.command.cortesflow.embedDescription",
                                        locale["commands.command.cortesflow.howToUseExample", serverConfig.commandPrefix],
                                        locale["commands.command.cortesflow.commandExample", serverConfig.commandPrefix]
                                ).joinToString("\n")
                        )
                        .setFooter(locale["commands.command.cortesflow.findOutThumbnailSource"], "https://yt3.ggpht.com/a/AATXAJwhhX5JXoYvdDwDI56fQfTDinfs21vzivC-DBW6=s88-c-k-c0x00ffffff-no-rj")
                        .setColor(Color.BLACK)

                for ((_, value) in availableGroupedBy) {
                    embed.addField(
                            value.first().jsonObject["participantDisplayName"]!!.jsonPrimitive.content,
                            value.joinToString {
                                locale[
                                        "commands.command.cortesflow.thumbnailSelection",
                                        it.jsonObject["path"]!!.jsonPrimitive.content.removePrefix("/api/v1/images/cortes-flow/"),
                                        it.jsonObject["source"]!!.jsonPrimitive.content
                                ]
                             },
                            true
                    )
                }

                sendMessage(
                        embed.build()
                )
                return@executesDiscord
            }

            if (args.size == 1)
                fail(locale["commands.command.cortesflow.youNeedToAddText"])

            val type = args.getOrNull(0)
            val string = args
                    .drop(1)
                    .joinToString(" ")

            val response = loritta.http.post<HttpResponse>("https://gabriela.loritta.website/api/v1/images/cortes-flow/$type") {
                body = buildJsonObject {
                    putJsonArray("strings") {
                        addJsonObject {
                            put("string", string)
                        }
                    }
                }.toString()
            }

            if (response.status == HttpStatusCode.NotFound)
                fail(locale["commands.command.cortesflow.unknownType", serverConfig.commandPrefix])

            sendFile(response.receive<ByteArray>(), "cortes_flow.jpg")
        }
    }
}