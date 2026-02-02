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
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.IntegrationType
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
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.linkButton
import net.perfectdreams.loritta.morenitta.utils.ClusterOfflineException
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.morenitta.utils.devious.GatewayShardStartupResumeStatus
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils
import org.jetbrains.exposed.sql.selectAll
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class LorittaCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loritta
        private val PING_I18N_PREFIX = I18N_PREFIX.Ping
        private val CLUSTERS_I18N_PREFIX = I18N_PREFIX.Clusters
        private val INFO_I18N_PREFIX = I18N_PREFIX.Info
        private val NERD_I18N_PREFIX = I18N_PREFIX.Nerd
        private val APPEAL_I18N_PREFIX = I18N_PREFIX.Appeal
        private val INVITE_I18N_PREFIX = I18N_PREFIX.Invite
    }

    override fun command() = slashCommand(I18nKeysData.Commands.Command.Loritta.Label, TodoFixThisData, CommandCategory.DISCORD, UUID.fromString("7ff3e80c-5832-48f5-9137-6640ef341863")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(INFO_I18N_PREFIX.Label, INFO_I18N_PREFIX.Description, UUID.fromString("4aae9454-e520-47ac-ba00-1c771456d067")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("botinfo")
            }

            executor = LorittaInfoExecutor(m)
        }

        subcommand(PING_I18N_PREFIX.Label, PING_I18N_PREFIX.Description, UUID.fromString("7dcf8135-ed5c-42e4-87ac-b01fe22fab1b")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ping")
            }

            executor = LorittaPingExecutor(m)
        }

        subcommand(CLUSTERS_I18N_PREFIX.Label, CLUSTERS_I18N_PREFIX.Description, UUID.fromString("a42112fe-3c84-4cae-81dd-bd62dfc71485")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ping clusters")
            }

            executor = LorittaClustersExecutor(m)
        }

        subcommand(NERD_I18N_PREFIX.Label, NERD_I18N_PREFIX.Description, UUID.fromString("5129831a-a9bc-4180-9b87-7e1c466d029a")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("botinfo extended")
            }

            executor = LorittaNerdStatsExecutor(m)
        }

        subcommand(APPEAL_I18N_PREFIX.Label, APPEAL_I18N_PREFIX.Description, UUID.fromString("00d970d5-7b67-433f-bd51-1cb2b67654e7")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("apelo")
                add("appeal")
            }
            this.allowUsageEvenIfLorittaBanned = true

            executor = LorittaAppealExecutor(m)
        }

        subcommand(INVITE_I18N_PREFIX.Label, INVITE_I18N_PREFIX.Description, UUID.fromString("f8a9b2c3-d4e5-6789-abcd-ef0123456789")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("invite")
                add("convidar")
                add("convidarbot")
                add("invitebot")
                add("convite")
            }

            executor = LorittaInviteExecutor(m)
        }
    }

    class LorittaInfoExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val since = Instant.now()
                .minusSeconds(86400)
                .toKotlinInstant()

            val guildCount = context.loritta.lorittaShards.queryGuildCount()
            val executedCommands = context.loritta.transaction {
                val appCommands = ExecutedApplicationCommandsLog.selectAll().where {
                    ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
                }.count()
                val legacyCommands = ExecutedCommandsLog.selectAll().where {
                    ExecutedCommandsLog.sentAt greaterEq since.toEpochMilliseconds()
                }.count()

                return@transaction appCommands + legacyCommands
            }

            val uniqueUsersExecutedCommands = context.loritta.transaction {
                val appCommands = ExecutedApplicationCommandsLog.select(ExecutedApplicationCommandsLog.userId).where {
                    ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
                }.groupBy(ExecutedApplicationCommandsLog.userId).toList()
                    .map { it[ExecutedApplicationCommandsLog.userId] }
                val legacyCommands = ExecutedCommandsLog.select(ExecutedCommandsLog.userId).where {
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
                            m.config.loritta.dashboard.url.removeSuffix("/") + "/${context.i18nContext.get(I18nKeysData.Website.LocalePathId)}/",
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
                        "https://bsky.app/profile/loritta.website",
                        "Bluesky",
                        Emotes.Bluesky
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

    class LorittaPingExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

    class LorittaClustersExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val results = context.loritta.config.loritta.clusters.instances.map {
                GlobalScope.async(context.loritta.coroutineDispatcher) {
                    try {
                        val (internalResponse, internalTime) = withTimeout(context.loritta.config.loritta.clusterConnectionTimeout.toLong()) {
                            val start = System.currentTimeMillis()
                            val response = context.loritta.http.get("${it.getInternalUrl(context.loritta)}/api/v1/loritta/status") {
                                userAgent(context.loritta.lorittaCluster.getUserAgent(context.loritta))
                                header("Authorization", context.loritta.lorittaInternalApiKey.name)
                            }

                            Pair(response.bodyAsText(), System.currentTimeMillis() - start)
                        }

                        // We only care about the public time here, we don't care if it timeouts
                        val publicTime = try {
                            val (_, publicTime) = withTimeout(context.loritta.config.loritta.clusterConnectionTimeout.toLong()) {
                                val start = System.currentTimeMillis()
                                val response =
                                    context.loritta.http.get("${it.getUrl(context.loritta)}/api/v1/loritta/status") {
                                        userAgent(context.loritta.lorittaCluster.getUserAgent(context.loritta))
                                        header("Authorization", context.loritta.lorittaInternalApiKey.name)
                                    }

                                Pair(response.bodyAsText(), System.currentTimeMillis() - start)
                            }
                            publicTime
                        } catch (e: Exception) {
                            null
                        }

                        ClusterQueryResult(
                            internalTime,
                            publicTime,
                            JsonParser.parseString(internalResponse)
                        )
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
                    val (internalTime, publicTime, json) = it.await()

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
                    row1.add("${pingAverage}ms")
                    row2.add("${internalTime}ms/${publicTime ?: "???"}ms")
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
            val maxRow6 = row6.maxByOrNull { it.length }!!.length

            val lines = mutableListOf<String>()
            for (i in 0 until row0.size) {
                val arg0 = row0.getOrNull(i) ?: "---"
                val arg1 = row1.getOrNull(i) ?: "---"
                val arg2 = row2.getOrNull(i) ?: "---"
                val arg3 = row3.getOrNull(i) ?: "---"
                val arg4 = row4.getOrNull(i) ?: "---"
                val arg5 = row5.getOrNull(i) ?: "---"
                val arg6 = row6.getOrNull(i) ?: "---"

                lines += "${arg0.padEnd(maxRow0, ' ')} | ${arg1.padEnd(maxRow1, ' ')} | ${arg2.padEnd(maxRow2, ' ')} | ${arg3.padEnd(maxRow3, ' ')} | ${arg4.padEnd(maxRow4, ' ')} | ${arg5.padEnd(maxRow5, ' ')} | ${arg6.padEnd(maxRow6, ' ')}"
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

    class LorittaNerdStatsExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

    class LorittaAppealExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.Command.Loritta.Appeal.BrokeTheRules),
                    Emotes.LoriBonk
                )

                actionRow(
                    Button.of(
                        ButtonStyle.LINK,
                        GACampaigns.createUrlWithCampaign(
                            m.config.loritta.banAppeals.url.removeSuffix("/") + "/${context.i18nContext.get(I18nKeysData.Website.LocalePathId)}/",
                            "discord",
                            "button",
                            "unban-appeal",
                            "appeal-command"
                        ).toString(),
                        context.i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.SendABanAppeal)
                    ).withEmoji(Emotes.LoriAngel.toJDA())
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    class LorittaInviteExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val sourceTrackingString = context.channelOrNull?.let {
                AuthenticationStateUtils.createDiscordSourceTrackingString(it)
            } ?: "unknown"

            val inviteUrl = LorittaDiscordOAuth2AddBotURL(
                m,
                state = AuthenticationStateUtils.createStateAsBase64(
                    AuthenticationState(
                        source = sourceTrackingString,
                        medium = "inline_link",
                        campaign = null,
                        content = "invite_command"
                    ),
                    m
                )
            ).toString()

            context.reply(false) {
                embed {
                    color = LorittaColors.LorittaAqua.rgb

                    description = context.i18nContext.get(
                        INVITE_I18N_PREFIX.InviteInfo(
                            inviteUrl,
                            m.config.loritta.dashboard.url,
                            "${m.config.loritta.website.url}support"
                        )
                    ).joinToString("\n")

                    thumbnail = "${m.config.loritta.website.url}assets/img/loritta_gabizinha_v1.png"
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    private data class ClusterQueryResult(
        val durationInternalUrl: Long,
        val durationPublicUrl: Long?,
        val response: JsonElement
    )
}