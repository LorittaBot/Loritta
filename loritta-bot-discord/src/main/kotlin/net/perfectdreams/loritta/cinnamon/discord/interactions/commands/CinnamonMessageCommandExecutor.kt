package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.MessageCommandExecutor
import net.perfectdreams.discordinteraktions.common.entities.messages.Message
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.commands.InteractionContextType
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.time.Duration
import java.time.Instant
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext as CinnamonApplicationCommandContext

/**
 * Discord InteraKTions' [MessageCommandExecutor] wrapper, used to provide Cinnamon-specific features.
 */
abstract class CinnamonMessageCommandExecutor(val loritta: LorittaBot) : MessageCommandExecutor(), CommandExecutorWrapper {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorClazzName = this::class.simpleName ?: "UnknownExecutor"

    val rest = loritta.rest
    val applicationId = loritta.config.loritta.discord.applicationId

    abstract suspend fun execute(context: net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext, targetMessage: Message)

    override suspend fun execute(
        context: ApplicationCommandContext,
        targetMessage: Message
    ) {
        val rootDeclarationClazzName = (context.applicationCommandDeclaration as CinnamonMessageCommandDeclaration)
            .declarationWrapper::class
            .simpleName ?: "UnknownCommand"

        logger.info { "(${context.sender.id.value}) $this" }

        val startedAt = Instant.now()

        val guildId = (context as? GuildApplicationCommandContext)?.guildId

        val result = executeCommand(
            rootDeclarationClazzName,
            executorClazzName,
            context,
            guildId,
            targetMessage
        )

        var stacktrace: String? = null
        if (result is CommandExecutorWrapper.CommandExecutionFailure)
            stacktrace = result.throwable.stackTraceToString()

        val commandLatency = Duration.between(startedAt, Instant.now()).toMillis() / 1000.0

        logger.info { "(${context.sender.id.value}) $this - OK! Result: ${result}; Took ${commandLatency * 1000}ms" }

        loritta.pudding.executedInteractionsLog.insertApplicationCommandLog(
            context.sender.id.value.toLong(),
            guildId?.value?.toLong(),
            context.channelId.value.toLong(),
            Clock.System.now(),
            ApplicationCommandType.USER,
            rootDeclarationClazzName,
            executorClazzName,
            buildJsonWithArguments(emptyMap()),
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
        guildId: Snowflake?,
        targetMessage: Message
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

            launchUserInfoCacheUpdater(loritta, context, null)

            execute(cinnamonContext, targetMessage)

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