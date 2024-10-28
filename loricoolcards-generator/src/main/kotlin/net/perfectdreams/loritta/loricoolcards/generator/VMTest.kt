package net.perfectdreams.loritta.loricoolcards.generator

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.stats.LorittaDiscordShardStats
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.sql.Connection

suspend fun main() {
    val http = HttpClient {}

    val configurationFile = File(System.getProperty("conf") ?: "./loricoolcards-production-stickers-generator.conf")

    if (!configurationFile.exists()) {
        println("Missing configuration file!")
        System.exit(1)
        return
    }

    val config = readConfigurationFromFile<LoriCoolCardsGeneratorProductionStickersConfig>(configurationFile)

    val pudding = Pudding.createPostgreSQLPudding(
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )



    pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
        val list = LorittaDiscordShardStats.selectAll()
            .limit(1_000_000)
            .orderBy(LorittaDiscordShardStats.timestamp, SortOrder.DESC)
            .toList()

        println(list.first()[LorittaDiscordShardStats.timestamp])
        println(list.last()[LorittaDiscordShardStats.timestamp])

        var i = 0
        list.chunked(256)
            .forEach {
                val jobs = mutableListOf<Job>()

                it.forEach {
                    val timestamp = it[LorittaDiscordShardStats.timestamp].toEpochMilli()
                    val clusterId = it[LorittaDiscordShardStats.clusterId].toString()
                    val shardId = it[LorittaDiscordShardStats.shardId].toString()

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_gateway_ping")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.gatewayPing])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_response_total")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.responseTotal])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_status")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.status])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    if (it[LorittaDiscordShardStats.gatewayStartupResumeStatus] != null) {
                        jobs.add(
                            GlobalScope.launch(Dispatchers.IO) {
                                http.post("http://127.0.0.1:8428/api/v1/import") {
                                    setBody(
                                        buildJsonObject {
                                            putJsonObject("metric") {
                                                put("__name__", "jda_gateway_startup_resume_status")
                                                put("loritta_cluster_id", clusterId)
                                                put("jda_shard_id", shardId)
                                            }

                                            putJsonArray("values") {
                                                add(it[LorittaDiscordShardStats.gatewayStartupResumeStatus])
                                            }

                                            putJsonArray("timestamps") {
                                                add(timestamp)
                                            }
                                        }.toString()
                                    )
                                }
                            }
                        )
                    }

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_cached_users")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.cachedUsersCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_guilds")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.guildsCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_unavailable_guilds")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.unavailableGuildsCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_cached_roles")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.cachedRolesCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_cached_channels")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.cachedChannelsCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_cached_scheduledevents")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.cachedScheduledEventsCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_cached_emojis")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.cachedEmojisCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )

                    jobs.add(
                        GlobalScope.launch(Dispatchers.IO) {
                            http.post("http://127.0.0.1:8428/api/v1/import") {
                                setBody(
                                    buildJsonObject {
                                        putJsonObject("metric") {
                                            put("__name__", "jda_cached_audiomanagers")
                                            put("loritta_cluster_id", clusterId)
                                            put("jda_shard_id", shardId)
                                        }

                                        putJsonArray("values") {
                                            add(it[LorittaDiscordShardStats.cachedAudioManagerCount])
                                        }

                                        putJsonArray("timestamps") {
                                            add(timestamp)
                                        }
                                    }.toString()
                                )
                            }
                        }
                    )
                }

                jobs.joinAll()

                println("Progress: ${i}/${list.size}")
                i += it.size
            }

        println(list.first()[LorittaDiscordShardStats.timestamp])
        println(list.last()[LorittaDiscordShardStats.timestamp])

    }
}