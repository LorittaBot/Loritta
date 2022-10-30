package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.requests.InteractionRequestState
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.images.ImageReference
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.discordinteraktions.common.commands.options.OptionReference
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.pudding.data.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext as InteraKTionsApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext as InteraKTionsGuildApplicationCommandContext

@OptIn(DelicateCoroutinesApi::class)
interface CommandExecutorWrapper {
    companion object {
        val NonGuildServerConfigRoot = ServerConfigRoot(
            0u,
            "pt",
            null,
            null,
            null
        )

        private val logger = KotlinLogging.logger {}

        suspend fun handleIfBanned(loritta: LorittaBot, context: InteractionContext): Boolean {
            // Check if the user is banned from using Loritta
            val userBannedState = loritta.pudding.users.getUserBannedState(UserId(context.user.id.value))

            if (userBannedState != null) {
                val banDateInEpochSeconds = userBannedState.bannedAt.epochSeconds
                val expiresDateInEpochSeconds = userBannedState.expiresAt?.epochSeconds

                context.sendEphemeralMessage {
                    val banAppealPageUrl = loritta.config.loritta.website.url + "extras/faq-loritta/loritta-ban-appeal"
                    content = context.i18nContext.get(
                        if (expiresDateInEpochSeconds != null) {
                            I18nKeysData.Commands.YouAreLorittaBannedTemporary(
                                loriHmpf = Emotes.LoriHmpf,
                                reason = userBannedState.reason,
                                banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                                expiresDate = "<t:$expiresDateInEpochSeconds:R> (<t:$expiresDateInEpochSeconds:f>)",
                                banAppealPageUrl = banAppealPageUrl,
                                loriAmeno = Emotes.loriAmeno,
                                loriSob = Emotes.LoriSob
                            )
                        } else {
                            I18nKeysData.Commands.YouAreLorittaBannedPermanent(
                                loriHmpf = Emotes.LoriHmpf,
                                reason = userBannedState.reason,
                                banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                                banAppealPageUrl = banAppealPageUrl,
                                loriAmeno = Emotes.loriAmeno,
                                loriSob = Emotes.LoriSob
                            )
                        }

                    ).joinToString("\n")
                }
                return true
            }
            return false
        }
    }

    suspend fun getGuildServerConfigOrLoadDefaultConfig(loritta: LorittaBot, guildId: Snowflake?) =
        if (guildId != null) {
            // TODO: Fix this workaround, while this does work, it isn't that good
            loritta.pudding.serverConfigs.getServerConfigRoot(guildId.value)?.data ?: NonGuildServerConfigRoot
        } else {
            // TODO: Should this class *really* be named "ServerConfig"? After all, it isn't always used for guilds
            NonGuildServerConfigRoot
        }

    fun convertInteraKTionsContextToCinnamonContext(
        loritta: LorittaBot,
        context: InteraKTionsApplicationCommandContext,
        i18nContext: I18nContext,
        locale: BaseLocale
    ) = if (context is InteraKTionsGuildApplicationCommandContext) {
        GuildApplicationCommandContext(
            loritta,
            i18nContext,
            locale,
            context.sender,
            context,
            context.guildId,
            context.member
        )
    } else {
        ApplicationCommandContext(
            loritta,
            i18nContext,
            locale,
            context.sender,
            context
        )
    }

    // TODO: Don't use GlobalScope!
    fun launchUserInfoCacheUpdater(
        loritta: LorittaBot,
        context: InteraKTionsApplicationCommandContext,
        args: net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments? = null
    ) = GlobalScope.launch {
        // Update the cached Discord users
        // Updating in a separate task to avoid delaying the command processing too much
        val users = mutableSetOf(context.sender)
        if (args != null)
            users.addAll(args.types.values.filterIsInstance<User>())
        val resolvedUsers = context.interactionData.resolved?.users?.values

        if (resolvedUsers != null)
            users.addAll(resolvedUsers)

        val jobs = users
            .distinctBy { it.id } // TODO: Maybe implement proper hash codes in the InteraKTions "User"?
            .map {
                async {
                    loritta.insertOrUpdateCachedUserInfo(it)
                }
            }

        jobs.awaitAll()

        logger.info { "Successfully updated user info cache of ${jobs.size} users!" }
    }

    /**
     * Additional messages that must be sent after the command sends at least one message
     */
    // TODO: Don't use GlobalScope!
    fun launchAdditionalNotificationsCheckerAndSender(
        loritta: LorittaBot,
        context: InteraKTionsApplicationCommandContext,
        i18nContext: I18nContext
    ) = GlobalScope.launch {
        var state = context.bridge.state.value

        try {
            withTimeout(15_000) {
                while (state != InteractionRequestState.ALREADY_REPLIED)
                    state = context.bridge.state.awaitChange() // The ".awaitChange()" is cancellable
            }
        } catch (e: TimeoutCancellationException) {
            logger.warn(e) { "Timed out while waiting for InteractionRequestState, we won't send any additional messages then..." }
            return@launch
        }

        // At this point, state should be "ALREADY_REPLIED"
        val userId = UserId(context.sender.id.value)

        // Website Update Message
        val patchNotesNotifications =
            loritta.pudding.patchNotesNotifications.getUnreadPatchNotesNotificationsAndMarkAsRead(
                UserId(context.sender.id.value),
                Clock.System.now()
            )

        for (patchNote in patchNotesNotifications) {
            context.sendEphemeralMessage {
                styled(
                    i18nContext.get(
                        I18nKeysData.Commands.CheckOutNews(
                            GACampaigns.patchNotesUrl(
                                loritta.config.loritta.website.url,
                                i18nContext.get(I18nKeysData.Website.LocalePathId),
                                patchNote.path,
                                "discord",
                                "slash-commands",
                                "lori-news",
                                "patch-notes-notification"
                            )
                        )
                    ),
                    Emotes.LoriSunglasses
                )
            }
        }
    }

    suspend fun convertThrowableToCommandExecutionResult(
        loritta: LorittaBot,
        context: InteraKTionsApplicationCommandContext,
        i18nContext: I18nContext?,
        rootDeclarationClazzName: String,
        executorClazzName: String,
        e: Throwable
    ): CommandExecutionResult {
        if (e is SilentCommandException)
            return CommandExecutionSuccess // SilentCommandExceptions should be ignored

        if (e is CommandException) {
            context.sendPublicMessage(e.builder)
            return CommandExecutionSuccess
        }

        if (e is EphemeralCommandException) {
            context.sendEphemeralMessage(e.builder)
            return CommandExecutionSuccess
        }

        logger.warn(e) { "Something went wrong while executing $rootDeclarationClazzName $executorClazzName" }

        // If the i18nContext is not present, we will default to the default language provided
        val i18nContext =
            i18nContext ?: loritta.languageManager.getI18nContextById(loritta.languageManager.defaultLanguageId)

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

        return CommandExecutionFailure(e)
    }

    /**
     * Stringifies the arguments in the [types] map to its name
     *
     * Useful for debug logging
     *
     * @param types the arguments
     * @return a map with argument name -> argument value
     */
    fun stringifyArgumentNames(types: Map<OptionReference<*>, Any?>) =
        types.map { it.key.name to it.value }
            .toMap()

    fun buildJsonWithArguments(types: Map<OptionReference<*>, Any?>) = buildJsonObject {
        for ((option, value) in types) {
            when (value) {
                is net.perfectdreams.loritta.common.images.ImageReference -> put(option.name, value.url)
                is Number -> put(option.name, value)
                else -> put(option.name, value.toString())
            }
        }
    }

    sealed class CommandExecutionResult
    object CommandExecutionSuccess : CommandExecutionResult()
    class CommandExecutionFailure(val throwable: Throwable) : CommandExecutionResult()
}