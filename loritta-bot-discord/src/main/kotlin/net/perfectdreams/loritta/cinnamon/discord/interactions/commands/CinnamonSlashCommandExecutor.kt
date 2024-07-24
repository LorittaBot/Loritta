package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.commands.InteractionContextType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext as CinnamonApplicationCommandContext

/**
 * Discord InteraKTions' [SlashCommandExecutor] wrapper, used to provide Cinnamon-specific features.
 */
abstract class CinnamonSlashCommandExecutor(val loritta: LorittaBot) : SlashCommandExecutor(), CommandExecutorWrapper {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorClazzName: String = this::class.simpleName ?: "UnknownExecutor"

    val rest = loritta.rest
    val applicationId = loritta.config.loritta.discord.applicationId

    abstract suspend fun execute(
        context: CinnamonApplicationCommandContext,
        args: SlashCommandArguments
    )

    override suspend fun execute(
        context: ApplicationCommandContext,
        args: SlashCommandArguments
    ) {
        val rootDeclarationClazzName = (context.applicationCommandDeclaration as CinnamonSlashCommandDeclaration).declarationWrapper::class
            .simpleName ?: "UnknownCommand"

        val stringifiedArgumentNames = stringifyArgumentNames(args.types)

        logger.info { "(${context.sender.id.value}) $this $stringifiedArgumentNames" }

        val timer = InteractionsMetrics.EXECUTED_COMMAND_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        val guildId = (context as? GuildApplicationCommandContext)?.guildId

        val result = executeCommand(
            rootDeclarationClazzName,
            executorClazzName,
            context,
            args,
            guildId
        )

        var stacktrace: String? = null
        if (result is CommandExecutorWrapper.CommandExecutionFailure)
            stacktrace = result.throwable.stackTraceToString()

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $this $stringifiedArgumentNames - OK! Result: ${result}; Took ${commandLatency * 1000}ms" }

        loritta.pudding.executedInteractionsLog.insertApplicationCommandLog(
            context.sender.id.value.toLong(),
            guildId?.value?.toLong(),
            context.channelId.value.toLong(),
            Clock.System.now(),
            ApplicationCommandType.CHAT_INPUT,
            rootDeclarationClazzName,
            executorClazzName,
            buildJsonWithArguments(args.types),
            stacktrace == null,
            commandLatency,
            stacktrace,
            // Technically this is an interaction, but we are using Kord and I really do not want to update my fork, so we will just fall back to UNKNOWN
            InteractionContextType.UNKNOWN,
            null,
            null
        )
    }

    private suspend fun executeCommand(
        rootDeclarationClazzName: String,
        executorClazzName: String,
        context: ApplicationCommandContext,
        args: SlashCommandArguments,
        guildId: Snowflake?
    ): CommandExecutorWrapper.CommandExecutionResult {
        // These variables are used in the catch { ... } block, to make our lives easier
        var i18nContext: I18nContext? = null
        val cinnamonContext: CinnamonApplicationCommandContext?

        try {
            val serverConfig = getGuildServerConfigOrLoadDefaultConfig(loritta, guildId)
            val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
            i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

            cinnamonContext = convertInteraKTionsContextToCinnamonContext(loritta, context, i18nContext, locale)

            // Don't let users that are banned from using Loritta
            if (CommandExecutorWrapper.handleIfBanned(loritta, cinnamonContext))
                return CommandExecutorWrapper.CommandExecutionSuccess

            launchUserInfoCacheUpdater(loritta, context, args)

            execute(
                cinnamonContext,
                args
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
}