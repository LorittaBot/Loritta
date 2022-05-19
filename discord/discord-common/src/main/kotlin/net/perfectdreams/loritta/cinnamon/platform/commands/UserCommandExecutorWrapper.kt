package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOption
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import kotlin.reflect.KClass
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext as CinnamonApplicationCommandContext

/**
 * Bridge between Cinnamon's [UserCommandExecutor] and Discord InteraKTions' [UserCommandExecutor].
 *
 * Used for argument conversion between the two platforms
 */
class UserCommandExecutorWrapper(
    private val loritta: LorittaCinnamon,
    // This is only used for metrics and logs
    private val rootDeclarationClazz: KClass<*>,
    private val declarationExecutor: UserCommandExecutorDeclaration,
    private val executor: UserCommandExecutor
) : net.perfectdreams.discordinteraktions.common.commands.UserCommandExecutor(), CommandExecutorWrapper {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val rootDeclarationClazzName = rootDeclarationClazz.simpleName ?: "UnknownCommand"
    private val executorClazzName = executor::class.simpleName ?: "UnknownExecutor"

    override suspend fun execute(
        context: ApplicationCommandContext,
        targetUser: User,
        targetMember: InteractionMember?
    ) {
        logger.info { "(${context.sender.id.value}) $executor" }

        val timer = Prometheus.EXECUTED_COMMAND_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        // Map Cinnamon Arguments to Discord InteraKTions Arguments
        val cinnamonArgs = mutableMapOf<CommandOption<*>, Any?>()
        val guildId = (context as? GuildApplicationCommandContext)?.guildId

        val result = executeCommand(
            rootDeclarationClazzName,
            executorClazzName,
            context,
            guildId,
            targetUser,
            targetMember
        )

        var stacktrace: String? = null
        if (result is CommandExecutorWrapper.CommandExecutionFailure)
            stacktrace = result.throwable.stackTraceToString()

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $executor - OK! Result: ${result}; Took ${commandLatency * 1000}ms" }

        loritta.services.executedInteractionsLog.insertApplicationCommandLog(
            context.sender.id.value.toLong(),
            guildId?.value?.toLong(),
            context.channelId.value.toLong(),
            Clock.System.now(),
            ApplicationCommandType.USER,
            rootDeclarationClazzName,
            executorClazzName,
            buildJsonWithArguments(cinnamonArgs),
            stacktrace == null,
            commandLatency,
            stacktrace
        )
    }

    private suspend fun executeCommand(
        rootDeclarationClazzName: String,
        executorClazzName: String,
        context: ApplicationCommandContext,
        guildId: Snowflake?,
        targetUser: User,
        targetMember: InteractionMember?
    ): CommandExecutorWrapper.CommandExecutionResult {
        // These variables are used in the catch { ... } block, to make our lives easier
        var i18nContext: I18nContext? = null
        val cinnamonContext: CinnamonApplicationCommandContext?

        try {
            val serverConfig = getGuildServerConfigOrLoadDefaultConfig(loritta, guildId)
            i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

            cinnamonContext = convertInteraKTionsContextToCinnamonContext(loritta, context, i18nContext)

            // Don't let users that are banned from using Loritta
            if (handleIfBanned(loritta, cinnamonContext))
                return CommandExecutorWrapper.CommandExecutionSuccess

            launchUserInfoCacheUpdater(loritta, context, null)

            executor.execute(
                cinnamonContext,
                targetUser,
                targetMember
            )

            launchAdditionalNotificationsCheckerAndSender(loritta, context, i18nContext)

            return CommandExecutorWrapper.CommandExecutionSuccess
        } catch (e: Throwable) {
            return convertThrowableToCommandExecutionResult(
                loritta,
                context,
                i18nContext,
                rootDeclarationClazzName,
                executorClazzName,
                e
            )
        }
    }

    override fun signature() = declarationExecutor::class
}