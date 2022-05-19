package net.perfectdreams.loritta.cinnamon.platform.components.buttons

import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.components.GuildComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.components.ComponentType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandException
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.EphemeralCommandException
import net.perfectdreams.loritta.cinnamon.platform.commands.SilentCommandException
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext as CinnamonComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.GuildComponentContext as CinnamonGuildComponentContext

class ButtonClickWithDataExecutorWrapper(
    private val loritta: LorittaCinnamon,
    // This is only used for metrics and logs
    private val executorDeclaration: ButtonClickExecutorDeclaration,
    private val executor: ButtonClickWithDataExecutor
) : net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val rootDeclarationClazzName = executorDeclaration::class.simpleName
        val executorClazzName = executor::class.simpleName

        logger.info { "(${context.sender.id.value}) $executor" }

        val timer = Prometheus.EXECUTED_BUTTON_CLICK_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        // These variables are used in the catch { ... } block, to make our lives easier
        var i18nContext: I18nContext? = null
        var cinnamonContext: CinnamonComponentContext? = null
        val guildId = (context as? GuildComponentContext)?.guildId
        var stacktrace: String? = null

        try {
            val serverConfig = if (guildId != null) {
                // TODO: Fix this workaround, while this does work, it isn't that good
                loritta.services.serverConfigs.getServerConfigRoot(guildId.value)?.data ?: CommandExecutorWrapper.NonGuildServerConfigRoot
            } else {
                // TODO: Should this class *really* be named "ServerConfig"? After all, it isn't always used for guilds
                CommandExecutorWrapper.NonGuildServerConfigRoot
            }

            i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

            // val channel = loritta.interactions.rest.channel.getChannel(context.request.channelId)
            cinnamonContext = if (guildId != null) {
                // TODO: Add Guild ID here
                CinnamonGuildComponentContext(
                    loritta,
                    i18nContext,
                    context.sender,
                    context,
                    context.guildId,
                    context.member
                )
            } else {
                CinnamonComponentContext(
                    loritta,
                    i18nContext,
                    context.sender,
                    context
                )
            }

            executor.onClick(
                user,
                cinnamonContext,
                data
            )
        } catch (e: Throwable) {
            if (e is SilentCommandException)
                return // SilentCommandExceptions should be ignored

            if (e is CommandException) {
                context.sendPublicMessage(e.builder)
                return
            }

            if (e is EphemeralCommandException) {
                context.sendEphemeralMessage(e.builder)
                return
            }

            logger.warn(e) { "Something went wrong while executing this executor!" } // TODO: Better logs

            // If the i18nContext is not present, we will default to the default language provided
            i18nContext = i18nContext ?: loritta.languageManager.getI18nContextByLegacyLocaleId(loritta.languageManager.defaultLanguageId)

            // Tell the user that something went *really* wrong
            // While we do have access to the Cinnamon Context, it may be null at this stage, so we will use the Discord InteraKTions context
            val content = "${Emotes.LoriHm} **|** " + i18nContext.get(
                I18nKeysData.Commands.ErrorWhileExecutingCommand(
                    loriRage = Emotes.LoriRage,
                    loriSob = Emotes.LoriSob,
                    stacktrace = if (!e.message.isNullOrEmpty())
                        " `${e.message}`" // TODO: Sanitize
                    else
                        ""
                )
            )

            // If the context was already deferred, but it isn't ephemeral, then we will send a non-ephemeral message
            val isEphemeral = if (context.isDeferred)
                context.wasInitiallyDeferredEphemerally
            else true

            if (isEphemeral)
                context.sendEphemeralMessage {
                    this.content = content
                }
            else
                context.sendMessage {
                    this.content = content
                }

            stacktrace = e.stackTraceToString()
        }

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $executor - OK! Took ${commandLatency * 1000}ms" }

        loritta.services.executedInteractionsLog.insertComponentLog(
            context.sender.id.value.toLong(),
            guildId?.value?.toLong(),
            context.channelId.value.toLong(),
            Clock.System.now(),
            ComponentType.BUTTON,
            rootDeclarationClazzName!!,
            executorClazzName!!,
            stacktrace == null,
            commandLatency,
            stacktrace
        )
    }

    override fun signature() = executorDeclaration::class
}