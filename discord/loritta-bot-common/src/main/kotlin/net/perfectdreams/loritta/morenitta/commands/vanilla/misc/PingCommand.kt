package net.perfectdreams.loritta.morenitta.commands.vanilla.misc

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.*
import io.ktor.http.userAgent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import net.perfectdreams.loritta.morenitta.utils.ClusterOfflineException
import net.perfectdreams.loritta.morenitta.utils.extensions.build
import java.util.concurrent.TimeUnit
import net.perfectdreams.loritta.morenitta.LorittaBot

class PingCommand(loritta: LorittaBot) :
    AbstractCommand(loritta, "ping", category = net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.ping.description")

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val arg0 = context.args.getOrNull(0)

        if (arg0 == "shards" || arg0 == "clusters") {
            val results = loritta.config.loritta.clusters.instances.map {
                GlobalScope.async(loritta.coroutineDispatcher) {
                    try {
                        withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
                            val start = System.currentTimeMillis()
                            val response = loritta.http.get("https://${it.getUrl(loritta)}/api/v1/loritta/status") {
                                userAgent(loritta.lorittaCluster.getUserAgent(loritta))
                                header("Authorization", loritta.lorittaInternalApiKey.name)
                            }

                            val body = response.bodyAsText()
                            ClusterQueryResult(
                                System.currentTimeMillis() - start,
                                JsonParser.parseString(body)
                            )
                        }
                    } catch (e: Exception) {
                        logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
                        throw ClusterOfflineException(it.id, it.name)
                    }
                }
            }

            val row0 = mutableListOf("Cluster Name")
            val row1 = mutableListOf("WS")
            val row2 = mutableListOf("Lori Web")
            val row3 = mutableListOf("Uptime")
            val row4 = mutableListOf("MsgQ")

            results.forEach {
                try {
                    val (time, json) = it.await()

                    val shardId = json["id"].long
                    val name = json["name"].string
                    val pendingMessages = json["pendingMessages"].long

                    var jvmUpTime = json["uptime"].long
                    val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
                    jvmUpTime -= TimeUnit.DAYS.toMillis(days)
                    val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
                    jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
                    jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

                    val pingAverage = json["shards"].array.map { it["ping"].int }.average().toInt() // arredondar

                    val pendingMessagesStatus = when {
                        pendingMessages == 0L -> "^"
                        16 >= pendingMessages -> "*"
                        32 >= pendingMessages -> "-"
                        128 >= pendingMessages -> "~"
                        else -> "!"
                    }

                    row0.add("$pendingMessagesStatus Cluster $shardId ($name)")
                    row1.add("~${pingAverage}ms")
                    row2.add("~${time}ms")
                    row3.add("${days}d ${hours}h ${minutes}m ${seconds}s")
                    row4.add("$pendingMessages")

                    // TODO - DeviousFun
                    /* val unstableShards = json["shards"].array.filter {
                        it["status"].string != JDA.Status.CONNECTED.toString() || it["ping"].int == -1 || it["ping"].int >= 250
                    }

                    if (unstableShards.isNotEmpty()) {
                        row0.add("* UNSTABLE SHARDS:")
                        row1.add("---")
                        row2.add("---")
                        row3.add("---")
                        row4.add("---")
                        row5.add("---")

                        unstableShards.forEach {
                            row0.add("> Shard ${it["id"].long}")
                            row1.add("${it["ping"].int}ms")
                            row2.add("---")
                            row3.add(it["status"].string)
                            row4.add("${it["guildCount"].long}")
                            row5.add("---")
                        }
                    } */
                } catch (e: ClusterOfflineException) {
                    row0.add("X Cluster ${e.id} (${e.name})")
                    row1.add("---")
                    row2.add("---")
                    row3.add("OFFLINE!")
                    row4.add("---")
                }
            }

            val maxRow0 = row0.maxByOrNull { it.length }!!.length
            val maxRow1 = row1.maxByOrNull { it.length }!!.length
            val maxRow2 = row2.maxByOrNull { it.length }!!.length
            val maxRow3 = row3.maxByOrNull { it.length }!!.length
            val maxRow4 = row4.maxByOrNull { it.length }!!.length

            val lines = mutableListOf<String>()
            for (i in 0 until row0.size) {
                val arg0 = row0.getOrNull(i) ?: "---"
                val arg1 = row1.getOrNull(i) ?: "---"
                val arg2 = row2.getOrNull(i) ?: "---"
                val arg3 = row3.getOrNull(i) ?: "---"
                val arg4 = row4.getOrNull(i) ?: "---"

                lines += "${arg0.padEnd(maxRow0, ' ')} | ${arg1.padEnd(maxRow1, ' ')} | ${
                    arg2.padEnd(
                        maxRow2,
                        ' '
                    )
                } | ${arg3.padEnd(maxRow3, ' ')} | ${arg4.padEnd(maxRow4, ' ')}"
            }

            val asMessage = mutableListOf<String>()

            var buf = ""
            for (aux in lines) {
                if (buf.length + aux.length > 1900) {
                    asMessage.add(buf)
                    buf = ""
                }
                buf += aux + "\n"
            }

            asMessage.add(buf)

            for (str in asMessage) {
                context.sendMessage("```$str```")
            }
        } else {
            val time = System.currentTimeMillis()

            val replies = mutableListOf(
                LorittaReply(
                    message = "**Pong!** (\uD83D\uDCE1 Shard ${context.event.gateway.shardId}/${loritta.config.loritta.discord.maxShards - 1}) (<:loritta:331179879582269451> Loritta Cluster ${loritta.lorittaCluster.id} (`${loritta.lorittaCluster.name}`))",
                    prefix = ":ping_pong:"
                ),
                LorittaReply(
                    message = "**Gateway Ping:** `${context.event.gateway.ping.value}`",
                    prefix = ":stopwatch:",
                    mentionUser = false
                ),
                LorittaReply(
                    message = "**API Ping:** `...ms`",
                    prefix = ":stopwatch:",
                    mentionUser = false
                )
            )

            val message = context.reply(*replies.toTypedArray())

            replies.removeAt(2) // remova o último
            replies.add(
                LorittaReply(
                    message = "**API Ping:** `${System.currentTimeMillis() - time}ms`",
                    prefix = ":zap:",
                    mentionUser = false
                )
            )

            message.editMessage(replies.joinToString(separator = "\n", transform = { it.build(context) }))

            message.onReactionAddByAuthor(context) {
                runCatching { message.editMessage("${context.userHandle.asMention} i luv u <:lori_blobnom:412582340272062464>") }
            }
        }
    }

    private data class ClusterQueryResult(
        val time: Long,
        val response: JsonElement
    )
}