package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.modals.ModalExecutor
import net.perfectdreams.discordinteraktions.common.modals.components.ModalArguments
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.EphemeralCommandException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.SilentCommandException
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics
import net.perfectdreams.discordinteraktions.common.modals.ModalContext

abstract class CinnamonModalExecutor(
    val loritta: LorittaCinnamon
) : ModalExecutor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val executorClazzName = this::class.simpleName

    abstract suspend fun onSubmit(context: net.perfectdreams.loritta.cinnamon.discord.interactions.modals.ModalContext, args: ModalArguments)

    override suspend fun onSubmit(context: ModalContext, args: ModalArguments) {
        val rootDeclarationClazzName = context.modalExecutorDeclaration::class.simpleName ?: "UnknownDeclaration"

        logger.info { "(${context.sender.id.value}) $this" }

        val timer = InteractionsMetrics.EXECUTED_MODAL_SUBMIT_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        // These variables are used in the catch { ... } block, to make our lives easier
        var i18nContext: I18nContext? = null
        val cinnamonContext: net.perfectdreams.loritta.cinnamon.discord.interactions.modals.ModalContext?
        val guildId = (context as? net.perfectdreams.discordinteraktions.common.modals.GuildModalContext)?.guildId
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

            cinnamonContext = if (context is net.perfectdreams.discordinteraktions.common.modals.GuildModalContext) {
                // TODO: Add Guild ID here
                GuildModalContext(
                    loritta,
                    i18nContext,
                    context.sender,
                    context,
                    context.guildId,
                    context.member
                )
            } else {
                ModalContext(
                    loritta,
                    i18nContext,
                    context.sender,
                    context
                )
            }

            onSubmit(
                cinnamonContext,
                args
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
            i18nContext = i18nContext ?: loritta.languageManager.getI18nContextById(loritta.languageManager.defaultLanguageId)

            // Tell the user that something went *really* wrong
            // While we do have access to the Cinnamon Context, it may be null at this stage, so we will use the Discord InteraKTions context
            val content = "${Emotes.LoriHm} **|** " + i18nContext.get(
                I18nKeysData.Commands.ErrorWhileExecutingCommand(
                    loriRage = Emotes.LoriRage,
                    loriSob = Emotes.LoriSob,
                    // To avoid leaking important things (example: Interaction Webhook URL when a request to Discord timeouts), let's not send errors to everyone
                    stacktrace = if (context.sender.id == Snowflake(123170274651668480)) {// TODO: Stop hardcoding this
                        if (!e.message.isNullOrEmpty())
                            " `${e.message}`" // TODO: Sanitize
                        else
                            " `${e::class.simpleName}`"
                    } else ""
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
        logger.info { "(${context.sender.id.value}) $this - OK! Took ${commandLatency * 1000}ms" }

        // TODO: Interaction Log
        /* loritta.services.executedInteractionsLog.insertComponentLog(
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
        ) */
    }
}