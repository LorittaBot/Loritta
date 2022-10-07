package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.awt.Color

class CortesFlowCommand(
    m: LorittaBot,
) : DiscordAbstractCommandBase(
    m,
    listOf(
        "cortesflow", "flowcortes"
    ),
    net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES
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
            OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "brmemes cortesflow")

            if (args.isEmpty()) {
                val result = loritta.http.get("https://gabriela.loritta.website/api/v1/images/cortes-flow")
                    .bodyAsText()

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

            val response = loritta.http.post("https://gabriela.loritta.website/api/v1/images/cortes-flow/$type") {
                setBody(
                    buildJsonObject {
                        putJsonArray("strings") {
                            addJsonObject {
                                put("string", string)
                            }
                        }
                    }.toString()
                )
            }

            if (response.status == HttpStatusCode.NotFound)
                fail(locale["commands.command.cortesflow.unknownType", serverConfig.commandPrefix])

            sendFile(response.readBytes(), "cortes_flow.jpg")
        }
    }
}