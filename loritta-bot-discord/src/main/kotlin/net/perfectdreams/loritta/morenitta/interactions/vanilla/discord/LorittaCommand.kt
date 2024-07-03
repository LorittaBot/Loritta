package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dev.minn.jda.ktx.messages.InlineMessage
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.linkButton
import net.perfectdreams.loritta.morenitta.utils.ClusterOfflineException
import net.perfectdreams.loritta.morenitta.utils.devious.GatewayShardStartupResumeStatus
import org.jetbrains.exposed.sql.select
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

class LorittaCommand : SlashCommandDeclarationWrapper {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loritta
        private val PING_I18N_PREFIX = I18N_PREFIX.Ping
        private val CLUSTERS_I18N_PREFIX = I18N_PREFIX.Clusters
        private val INFO_I18N_PREFIX = I18N_PREFIX.Info
        private val NERD_I18N_PREFIX = I18N_PREFIX.Nerd
    }

    override fun command() = slashCommand(I18nKeysData.Commands.Command.Loritta.Label, TodoFixThisData, CommandCategory.DISCORD) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        subcommand(INFO_I18N_PREFIX.Label, INFO_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("botinfo")
            }

            executor = LorittaInfoExecutor()
        }

        subcommand(PING_I18N_PREFIX.Label, PING_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ping")
            }

            executor = LorittaPingExecutor()
        }

        subcommand(CLUSTERS_I18N_PREFIX.Label, CLUSTERS_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ping clusters")
            }

            executor = LorittaClustersExecutor()
        }

        subcommand(NERD_I18N_PREFIX.Label, NERD_I18N_PREFIX.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("botinfo extended")
            }

            executor = LorittaNerdStatsExecutor()
        }
    }

    inner class LorittaInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val since = Instant.now()
                .minusSeconds(86400)
                .toKotlinInstant()

            val guildCount = context.loritta.lorittaShards.queryGuildCount()
            val executedCommands = context.loritta.transaction {
                val appCommands = ExecutedApplicationCommandsLog.select {
                    ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
                }.count()
                val legacyCommands = ExecutedCommandsLog.select {
                    ExecutedCommandsLog.sentAt greaterEq since.toEpochMilliseconds()
                }.count()

                return@transaction appCommands + legacyCommands
            }

            val uniqueUsersExecutedCommands = context.loritta.transaction {
                val appCommands = ExecutedApplicationCommandsLog.slice(ExecutedApplicationCommandsLog.userId).select {
                    ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
                }.groupBy(ExecutedApplicationCommandsLog.userId).toList()
                    .map { it[ExecutedApplicationCommandsLog.userId] }
                val legacyCommands = ExecutedCommandsLog.slice(ExecutedCommandsLog.userId).select {
                    ExecutedCommandsLog.sentAt greaterEq since.toEpochMilliseconds()
                }.groupBy(ExecutedCommandsLog.userId).toList()
                    .map { it[ExecutedCommandsLog.userId] }

                return@transaction (appCommands + legacyCommands).distinct().size
            }

            context.reply(false) {
                embed {
                    color = LorittaColors.LorittaAqua.rgb

                    author(context.i18nContext.get(I18N_PREFIX.Info.Embed.Title))

                    description = context.i18nContext.get(
                        I18N_PREFIX.Info.Embed.Description(
                            guildCount = guildCount,
                            commandCount = context.loritta.getCommandCount(),
                            executedApplicationCommands = executedCommands,
                            uniqueUsersExecutedApplicationCommands = uniqueUsersExecutedCommands,
                            userMention = context.user.asMention,
                            loriSunglasses = Emotes.LoriSunglasses,
                            loriYay = Emotes.LoriYay,
                            loriKiss = Emotes.LoriKiss,
                            loriHeart = Emotes.LoriHeart
                        )
                    ).joinToString("\n")

                    image = "${context.loritta.config.loritta.website.url}v3/assets/img/sonhos/lori-space.gif"

                    footer(context.i18nContext.get(I18N_PREFIX.Info.Embed.Footer("MrPowerGamerBR#4185", "https://mrpowergamerbr.com")), "https://mrpowergamerbr.com/assets/img/avatar.png")
                }

                actionRow(
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "home-page"
                        ).toString(),
                        context.i18nContext.get(I18N_PREFIX.Info.Website),
                        Emotes.LoriSunglasses
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/dashboard",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "dashboard"
                        ).toString(),
                        context.i18nContext.get(I18N_PREFIX.Info.Dashboard),
                        Emotes.LoriReading
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/commands",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "commands"
                        ).toString(),
                        context.i18nContext.get(I18N_PREFIX.Info.Commands),
                        Emotes.LoriWow
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/support",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "support"
                        ).toString(),
                        context.i18nContext.get(I18N_PREFIX.Info.Support),
                        Emotes.LoriHm
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/donate",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "premium"
                        ).toString(),
                        context.i18nContext.get(I18N_PREFIX.Info.Premium),
                        Emotes.LoriCard
                    )
                )

                actionRow(
                    linkButton(
                        "https://twitter.com/LorittaBot",
                        "Twitter",
                        Emotes.Twitter
                    ),
                    linkButton(
                        "https://instagram.com/lorittabot",
                        "Instagram",
                        Emotes.Instagram
                    ),
                    linkButton(
                        "https://youtube.com/c/Loritta",
                        "YouTube",
                        Emotes.YouTube
                    ),
                    linkButton(
                        "https://www.tiktok.com/@lorittamorenittabot",
                        "TikTok",
                        Emotes.TikTok
                    ),
                    linkButton(
                        "https://github.com/LorittaBot",
                        "GitHub",
                        Emotes.GitHub
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }


    inner class LorittaPingExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val time = System.currentTimeMillis()

            fun buildPingMessage(apiLatency: Long?): InlineMessage<*>.() -> (Unit) = {
                styled(
                    content = "**Pong!** (\uD83D\uDCE1 Shard ${context.jda.shardInfo.shardId}/${context.loritta.config.loritta.discord.maxShards - 1}) (<:loritta:331179879582269451> Loritta Cluster ${context.loritta.lorittaCluster.id} (`${context.loritta.lorittaCluster.name}`))",
                    prefix = ":ping_pong:"
                )
                styled(
                    content = "**Gateway Ping:** `${context.jda.gatewayPing}ms`",
                    prefix = ":stopwatch:"
                )
                if (apiLatency != null)
                    styled(
                        content = "**API Ping:** `${apiLatency}ms`",
                        prefix = ":zap:"
                    )
                else
                    styled(
                        content = "**API Ping:** `...ms`",
                        prefix = ":zap:"
                    )
            }

            val message = context.reply(false) {
                apply(buildPingMessage(null))
            }

            val diff = System.currentTimeMillis() - time
            message.editMessage {
                apply(buildPingMessage(diff))
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    inner class LorittaClustersExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val results = context.loritta.config.loritta.clusters.instances.map {
                GlobalScope.async(context.loritta.coroutineDispatcher) {
                    try {
                        withTimeout(context.loritta.config.loritta.clusterConnectionTimeout.toLong()) {
                            val start = System.currentTimeMillis()
                            val response = context.loritta.http.get("${it.getUrl(context.loritta)}/api/v1/loritta/status") {
                                userAgent(context.loritta.lorittaCluster.getUserAgent(context.loritta))
                                header("Authorization", context.loritta.lorittaInternalApiKey.name)
                            }

                            val body = response.bodyAsText()
                            ClusterQueryResult(
                                System.currentTimeMillis()- start,
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
            val row4 = mutableListOf("Guilds")
            val row5 = mutableListOf("MsgQ")
            val row6 = mutableListOf("R/I/S/U")

            results.forEach {
                try {
                    val (time, json) = it.await()

                    val shardId = json["id"].long
                    val name = json["name"].string
                    val loriBuild = json["build"]["buildNumber"].nullString ?: "Unknown"
                    val pendingMessages = json["pendingMessages"].long

                    val totalGuildCount = json["shards"].array.sumBy { it["guildCount"].int }

                    var jvmUpTime = json["uptime"].long
                    val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
                    jvmUpTime -= java.util.concurrent.TimeUnit.DAYS.toMillis(days)
                    val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
                    jvmUpTime -= java.util.concurrent.TimeUnit.HOURS.toMillis(hours)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
                    jvmUpTime -= java.util.concurrent.TimeUnit.MINUTES.toMillis(minutes)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

                    val pingAverage = json["shards"].array.map { it["ping"].int }.average().toInt() // arredondar

                    val pendingMessagesStatus = when {
                        pendingMessages == 0L -> "^"
                        16 >= pendingMessages -> "*"
                        32 >= pendingMessages -> "-"
                        128 >= pendingMessages -> "~"
                        else -> "!"
                    }

                    val gatewayResumeStatuses = json["shards"].array.map { it["gatewayShardStartupResumeStatus"].string }.map { GatewayShardStartupResumeStatus.valueOf(it) }
                    val resumed = gatewayResumeStatuses.count { it == GatewayShardStartupResumeStatus.SUCCESSFULLY_RESUMED }
                    val invalidated = gatewayResumeStatuses.count { it == GatewayShardStartupResumeStatus.SESSION_INVALIDATED }
                    val scratch = gatewayResumeStatuses.count { it == GatewayShardStartupResumeStatus.LOGGED_IN_FROM_SCRATCH }
                    val unknown = gatewayResumeStatuses.count { it == GatewayShardStartupResumeStatus.UNKNOWN }

                    row0.add("$pendingMessagesStatus Cluster $shardId ($name) [b$loriBuild]")
                    row1.add("~${pingAverage}ms")
                    row2.add("~${time}ms")
                    row3.add("${days}d ${hours}h ${minutes}m ${seconds}s")
                    row4.add("$totalGuildCount")
                    row5.add("$pendingMessages")
                    row6.add("$resumed/$invalidated/$scratch/$unknown")

                    val unstableShards = json["shards"].array.filter {
                        it["status"].string != JDA.Status.CONNECTED.toString() || it["ping"].int == -1 || it["ping"].int >= 250
                    }

                    if (unstableShards.isNotEmpty()) {
                        row0.add("* UNSTABLE SHARDS:")
                        row1.add("---")
                        row2.add("---")
                        row3.add("---")
                        row4.add("---")
                        row5.add("---")
                        row6.add("---")

                        unstableShards.forEach {
                            row0.add("> Shard ${it["id"].long}")
                            row1.add("${it["ping"].int}ms")
                            row2.add("---")
                            row3.add(it["status"].string)
                            row4.add("${it["guildCount"].long}")
                            row5.add("---")
                            row6.add("---")
                        }
                    }
                } catch (e: ClusterOfflineException) {
                    row0.add("X Cluster ${e.id} (${e.name})")
                    row1.add("---")
                    row2.add("---")
                    row3.add("OFFLINE!")
                    row4.add("---")
                    row5.add("---")
                    row6.add("---")
                }
            }

            val maxRow0 = row0.maxByOrNull { it.length }!!.length
            val maxRow1 = row1.maxByOrNull { it.length }!!.length
            val maxRow2 = row2.maxByOrNull { it.length }!!.length
            val maxRow3 = row3.maxByOrNull { it.length }!!.length
            val maxRow4 = row4.maxByOrNull { it.length }!!.length
            val maxRow5 = row5.maxByOrNull { it.length }!!.length

            val lines = mutableListOf<String>()
            for (i in 0 until row0.size) {
                val arg0 = row0.getOrNull(i) ?: "---"
                val arg1 = row1.getOrNull(i) ?: "---"
                val arg2 = row2.getOrNull(i) ?: "---"
                val arg3 = row3.getOrNull(i) ?: "---"
                val arg4 = row4.getOrNull(i) ?: "---"
                val arg5 = row5.getOrNull(i) ?: "---"
                val arg6 = row6.getOrNull(i) ?: "---"

                lines += "${arg0.padEnd(maxRow0, ' ')} | ${arg1.padEnd(maxRow1, ' ')} | ${arg2.padEnd(maxRow2, ' ')} | ${arg3.padEnd(maxRow3, ' ')} | ${arg4.padEnd(maxRow4, ' ')} | ${arg5.padEnd(maxRow4, ' ')} | ${arg6.padEnd(maxRow5, ' ')}"
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
                context.reply(false) {
                    content = "```$str```"
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    inner class LorittaNerdStatsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val locale = context.locale
            val lorittaShards = context.loritta.lorittaShards

            val mb = 1024 * 1024
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
            val freeMemory = runtime.freeMemory() / mb
            val maxMemory = runtime.maxMemory() / mb
            val totalMemory = runtime.totalMemory() / mb

            val buildNumber = System.getenv("BUILD_ID")
            val commitHash = System.getenv("COMMIT_HASH")

            context.reply(false) {
                styled(
                    "**Loritta Cluster ${context.loritta.lorittaCluster.id}** (`${context.loritta.lorittaCluster.name}`) [Shards ${context.loritta.lorittaCluster.minShard}..${context.loritta.lorittaCluster.maxShard}]",
                    "<:loritta:331179879582269451>"
                )
                styled(
                    "**Hostname:** `${HostnameUtils.getHostname()}`",
                    "\uD83C\uDFD7",
                )
                styled(
                    "**${locale["commands.command.botinfo.buildNumber"]}:** #$buildNumber",
                    "\uD83C\uDFD7",
                )
                styled(
                    "**Commit:** $commitHash",
                    "<:github:467329174387032086>",
                )
                styled(
                    "**${locale["commands.command.botinfo.javaVersion"]}:** ${System.getProperty("java.version")}",
                    "<:java:467443707160035329>",
                )
                styled(
                    "**${locale["commands.command.botinfo.kotlinVersion"]}:** ${KotlinVersion.CURRENT}",
                    "<:kotlin:453714186925637642>",
                )

                styled("**${context.i18nContext.get(NERD_I18N_PREFIX.CachedGuilds)}:** ${lorittaShards.shardManager.guildCache.size()}")
                styled("**${context.i18nContext.get(NERD_I18N_PREFIX.CachedUsers)}:** ${lorittaShards.shardManager.userCache.size()}")

                styled(
                    "**${locale["commands.command.botinfo.memoryUsed"]}:** $usedMemory MB",
                    "\uD83D\uDCBB",
                )
                styled(
                    "**${locale["commands.command.botinfo.memoryAvailable"]}:** $freeMemory MB",
                    "\uD83D\uDCBB",
                )
                styled(
                    "**${locale["commands.command.botinfo.memoryAllocated"]}:** $totalMemory MB",
                    "\uD83D\uDCBB",
                )
                styled(
                    "**${locale["commands.command.botinfo.memoryTotal"]}:** $maxMemory MB",
                    "\uD83D\uDCBB",
                )
                styled(
                    "**${locale["commands.command.botinfo.threadCount"]}:** ${ManagementFactory.getThreadMXBean().threadCount}",
                    "\uD83D\uDC4B",
                )
                styled(
                    "**${locale["commands.command.botinfo.environment"]}:** ${context.loritta.config.loritta.environment.name}",
                    "\uD83C\uDF43",
                )
                styled(
                    "**${locale["commands.command.botinfo.love"]}:** ∞",
                    "<:blobheart:467447056374693889>",
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    private data class ClusterQueryResult(
        val time: Long,
        val response: JsonElement
    )
}