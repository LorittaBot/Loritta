package net.perfectdreams.loritta.morenitta.websiteinternal.loriinternalapi

import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.devious.DeviousConverter
import net.perfectdreams.loritta.morenitta.utils.devious.DeviousConverter.User
import net.perfectdreams.sequins.ktor.BaseRoute
import kotlin.time.measureTime

class GuildJsonBenchmarkRoute(val loritta: LorittaBot) : BaseRoute("/guild-json-benchmark/{guildId}") {
    val json = Json {
        this.explicitNulls = false
    }

    private val tests = listOf(
        BenchmarkTest(
            "Original"
        ) { guild, _ ->
            val g = DeviousConverter.toJson(guild)
            json.encodeToString(g)
        },
        BenchmarkTest(
            "V1"
        ) { guild, _ ->
            val g = DeviousConverter.toSerializableGuildCreateEvent(guild)
            json.encodeToString(g)
        },
        BenchmarkTest(
            "V2"
        ) { guild, _ ->
            val g = DeviousConverter.toSerializableGuildCreateEventV2(guild)
            json.encodeToString(g)
        },
        BenchmarkTest(
            "V3"
        ) { guild, _ ->
            val g = DeviousConverter.toSerializableGuildCreateEventV3(guild)
            json.encodeToString(g)
        },
        BenchmarkTest(
            "V4"
        ) { guild, user ->
            val g = DeviousConverter.toSerializableGuildCreateEventV4(guild, user)
            json.encodeToString(g)
        },
        BenchmarkTest(
            "V4-plus-sorted-hashcode"
        ) { guild, user ->
            val g = DeviousConverter.toSerializableGuildCreateEventV4(guild, user)

            val sortedSerializableGuild = g.copy(
                channels = g.channels.sortedBy {
                    it.id
                }.map { it.copy(permission_overwrites = it.permission_overwrites?.sortedBy { it.id }) } ,
                roles = g.roles.sortedBy {
                    it.id
                },
                emojis = g.emojis.sortedBy {
                    it.id
                },
                stickers = g.stickers.sortedBy {
                    it.id
                }
            )

            sortedSerializableGuild.hashCode()
        }
    )

    override suspend fun onRequest(call: ApplicationCall) {
        try {
            val guildId = call.parameters["guildId"]!!.toLong()
            val repeatCount = call.parameters["repeat"]?.toIntOrNull() ?: 1000
            val benchmarksToBeExecuted = call.parameters["benchmarks"]?.split(",") ?: listOf(tests[tests.size - 2].label, tests.last().label)

            val testsToBeExecuted = tests.filter { it.label in benchmarksToBeExecuted }

            val guild = loritta.lorittaShards.getGuildById(guildId)!!

            val s = buildString {
                val selfUser = loritta.lorittaShards.shardManager.shardCache.first().selfUser

                val user = User(
                    id = selfUser.idLong,
                    username = selfUser.name,
                    global_name = selfUser.globalName,
                    discriminator = selfUser.discriminator,
                    avatar = selfUser.avatarId,
                    public_flags = selfUser.flagsRaw,
                    bot = selfUser.isBot,
                    system = selfUser.isSystem
                )

                for (test in testsToBeExecuted) {
                    val duration = measureTime {
                        repeat(repeatCount) {
                            test.block.invoke(guild, user)
                        }
                    }

                    appendLine("${test.label}: $duration")
                }
            }

            call.respondText(s)
        } catch (e: Exception) {
            call.respondText(e.stackTraceToString())
        }
    }

    private class BenchmarkTest(
        val label: String,
        val block: (Guild, DeviousConverter.User) -> (Unit)
    )
}