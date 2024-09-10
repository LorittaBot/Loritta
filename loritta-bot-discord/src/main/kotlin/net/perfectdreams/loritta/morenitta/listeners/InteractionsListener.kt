package net.perfectdreams.loritta.morenitta.listeners

import dev.minn.jda.ktx.interactions.commands.updateCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.*
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.ErrorResponse
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.CommandMentions
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordLorittaApplicationCommandHashes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.options.LongDiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.NumberDiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.StringDiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toLoritta
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.postgresql.util.PGobject
import java.time.Duration
import java.time.Instant
import java.util.*

class InteractionsListener(private val loritta: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    val manager = UnleashedCommandManager(loritta, loritta.languageManager)
    private var hasAlreadyGloballyUpdatedTheCommands = false

    override fun onReady(event: ReadyEvent) {
        // Update Slash Commands
        if (loritta.config.loritta.interactions.registerGlobally && !hasAlreadyGloballyUpdatedTheCommands) {
            hasAlreadyGloballyUpdatedTheCommands = true

            GlobalScope.launch {
                val registeredCommands = updateCommands(
                    0
                ) { commands ->
                    event.jda.updateCommands {
                        addCommands(*commands.toTypedArray())
                    }.complete()
                }

                logger.info { "We have ${registeredCommands.size} registered commands, converting it into command mentions..." }

                loritta.commandMentions = CommandMentions(registeredCommands)
            }
        }

        if (!loritta.config.loritta.interactions.registerGlobally) {
            GlobalScope.launch {
                event.jda.guilds.filter { it.idLong in loritta.config.loritta.interactions.guildsToBeRegistered.map { it.toLong() } }
                    .forEach {
                        val registeredCommands = updateCommands(
                            it.idLong
                        ) { commands ->
                            it.updateCommands {
                                addCommands(*commands.toTypedArray())
                            }.complete()
                        }

                        logger.info { "We have ${registeredCommands.size} registered commands, converting it into command mentions..." }

                        loritta.commandMentions = CommandMentions(registeredCommands)
                    }
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        loritta.launchMessageJob(event) {
            var rootDeclaration: SlashCommandDeclaration? = null
            var slashDeclaration: SlashCommandDeclaration? = null

            for (declaration in manager.slashCommands) {
                val rootLabel = event.name
                val subcommandGroupLabel = event.subcommandGroup
                val subcommandLabel = event.subcommandName

                if (rootLabel == manager.slashCommandDefaultI18nContext.get(declaration.name)) {
                    if (subcommandGroupLabel == null && subcommandLabel == null) {
                        // Already found it, yay!
                        slashDeclaration = declaration
                        rootDeclaration = declaration
                    } else {
                        // Check root subcommands
                        if (subcommandLabel != null) {
                            if (subcommandGroupLabel == null) {
                                // "/name subcommand"
                                slashDeclaration =
                                    declaration.subcommands.firstOrNull { manager.slashCommandDefaultI18nContext.get(it.name) == subcommandLabel }
                                rootDeclaration = declaration
                                break
                            } else {
                                // "/name subcommandGroup subcommand"
                                slashDeclaration = declaration.subcommandGroups.firstOrNull {
                                    manager.slashCommandDefaultI18nContext.get(it.name) == subcommandGroupLabel
                                }
                                    ?.subcommands
                                    ?.firstOrNull {
                                        manager.slashCommandDefaultI18nContext.get(it.name) == subcommandLabel
                                    }
                                rootDeclaration = declaration
                                break
                            }
                        }
                    }
                    break
                }
            }

            // We should throw an error here
            // But we won't because we still use Discord InteraKTions
            if (rootDeclaration == null || slashDeclaration == null)
                return@launchMessageJob

            val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

            val rootDeclarationClazzName = rootDeclaration::class.simpleName ?: "UnknownCommand"
            val executorClazzName = executor::class.simpleName ?: "UnknownExecutor"
            val startedAt = Instant.now()

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ApplicationCommandContext? = null
            var stacktrace: String? = null
            // Used for logs
            val slashCommandOptionValuesAsJson = buildJsonObject {
                event.options.forEach {
                    when (it.type) {
                        OptionType.UNKNOWN, OptionType.SUB_COMMAND, OptionType.SUB_COMMAND_GROUP -> {}
                        OptionType.STRING -> put(it.name, it.asString)
                        OptionType.INTEGER -> put(it.name, it.asInt)
                        OptionType.BOOLEAN -> put(it.name, it.asBoolean)
                        OptionType.USER -> put(it.name, it.asUser.idLong)
                        OptionType.CHANNEL -> put(it.name, it.asChannel.idLong)
                        OptionType.ROLE -> put(it.name, it.asRole.idLong)
                        OptionType.MENTIONABLE -> put(it.name, it.asMentionable.idLong)
                        // This is a bit tricky because number can accept double OR longs
                        // So here we get everything as a string
                        OptionType.NUMBER -> put(it.name, it.asString)
                        OptionType.ATTACHMENT -> put(it.name, it.asAttachment.url)
                    }
                }
            }

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }

                val args = SlashCommandArguments(SlashCommandArgumentsSource.SlashCommandArgumentsEventSource(event))
                context = ApplicationCommandContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )

                // Check if user is banned
                if (AccountUtils.checkAndSendMessageIfUserIsBanned(context.loritta, context, context.user))
                    return@launchMessageJob

                // Check if the command is disabled
                val guildId = context.guildId
                if (checkIfCommandIsDisabledOnGuild(event, slashDeclaration, context, i18nContext, guildId))
                    return@launchMessageJob

                loritta.transaction {
                    lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()

                    // Set that the user is in the guild
                    if (guildId != null) {
                        GuildProfiles.update({
                            GuildProfiles.userId eq lorittaUser.profile.userId and (GuildProfiles.guildId eq guildId)
                        }) {
                            it[GuildProfiles.isInGuild] = true
                        }
                    }

                    loritta.lorittaShards.updateCachedUserData(context.user)
                }

                executor.execute(
                    context,
                    args
                )

                // If Loritta has a pending update, let's notify the user that she'll restart soon™
                val pendingUpdate = loritta.pendingUpdate
                if (pendingUpdate != null) {
                    context.reply(true) {
                        styled(
                            i18nContext.get(I18nKeysData.Commands.LorittaPendingUpdate),
                            Emotes.LoriSleeping
                        )
                    }
                }
            } catch (e: CommandException) {
                context?.reply(e.ephemeral, e.builder)
            } catch (e: Exception) {
                val errorId = LorittaUtils.generateErrorId(loritta)
                logger.warn(e) { "Something went wrong while executing command ${executor::class.simpleName}! Option Values: $slashCommandOptionValuesAsJson; Error ID: $errorId" }

                stacktrace = e.stackTraceToString()

                val currentContext = context
                val currentI18nContext = i18nContext
                if (currentContext != null && currentI18nContext != null) {
                    var sendExceptionToUser = true
                    // Don't attempt to send the message to the user if it was a unknown interaction error, because attempting to follow up with a message with surely
                    // cause yet another unknown interaction exception
                    if (e is ErrorResponseException && e.errorResponse == ErrorResponse.UNKNOWN_INTERACTION)
                        sendExceptionToUser = false

                    if (sendExceptionToUser) {
                        try {
                            currentContext.reply(currentContext.wasInitiallyDeferredEphemerally == null || currentContext.wasInitiallyDeferredEphemerally == true) {
                                styled(
                                    currentI18nContext.get(
                                        I18nKeysData.Commands.ErrorWhileExecutingCommandWithErrorId(
                                            loriRage = Emotes.LoriRage,
                                            loriSob = Emotes.LoriSob,
                                            errorId = errorId.toString()
                                        )
                                    ),
                                    Emotes.LoriSob
                                )
                            }
                        } catch (e: Exception) {
                            // wtf
                            logger.warn(e) { "Something went wrong while sending the reason why command ${executor::class.simpleName} was not correctly executed! $errorId" }
                            // At this point just give up bro
                            throw e
                        }
                    }
                }
            }

            loritta.pudding.executedInteractionsLog.insertApplicationCommandLog(
                event.user.idLong,
                event.guild?.idLong,
                event.channel.idLong,
                Clock.System.now(),
                ApplicationCommandType.CHAT_INPUT,
                rootDeclarationClazzName,
                executorClazzName,
                slashCommandOptionValuesAsJson,
                stacktrace == null,
                Duration.between(startedAt, Instant.now()).toMillis() / 1000.0,
                stacktrace,
                event.interaction.context.toLoritta(),
                event.interaction.integrationOwners.guildIntegration,
                event.interaction.integrationOwners.userIntegration?.idLong,
            )
        }
    }

    override fun onUserContextInteraction(event: UserContextInteractionEvent) {
        loritta.launchMessageJob(event) {
            var rootDeclaration: UserCommandDeclaration? = null
            var slashDeclaration: UserCommandDeclaration? = null

            for (declaration in manager.userCommands) {
                val rootLabel = event.name

                if (rootLabel == manager.slashCommandDefaultI18nContext.get(declaration.name)) {
                    slashDeclaration = declaration
                    rootDeclaration = declaration
                    break
                }
            }

            // We should throw an error here
            // But we won't because we still use Discord InteraKTions
            if (rootDeclaration == null || slashDeclaration == null)
                return@launchMessageJob

            val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

            val rootDeclarationClazzName = rootDeclaration::class.simpleName ?: "UnknownCommand"
            val executorClazzName = executor::class.simpleName ?: "UnknownExecutor"
            val startedAt = Instant.now()

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ApplicationCommandContext? = null
            var stacktrace: String? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }

                context = ApplicationCommandContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )

                // Check if user is banned
                if (AccountUtils.checkAndSendMessageIfUserIsBanned(context.loritta, context, context.user))
                    return@launchMessageJob

                // Check if the command is disabled
                val guildId = context.guildId
                if (checkIfCommandIsDisabledOnGuild(event, slashDeclaration, context, i18nContext, guildId))
                    return@launchMessageJob

                executor.execute(
                    context,
                    event.target
                )
            } catch (e: CommandException) {
                context?.reply(e.ephemeral, e.builder)
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()

                stacktrace = e.stackTraceToString()
            }

            loritta.pudding.executedInteractionsLog.insertApplicationCommandLog(
                event.user.idLong,
                event.guild?.idLong,
                event.channel?.idLong!!, // TODO: The channel can be null! This should be fixed
                Clock.System.now(),
                ApplicationCommandType.USER,
                rootDeclarationClazzName,
                executorClazzName,
                buildJsonObject {},
                stacktrace == null,
                Duration.between(startedAt, Instant.now()).toMillis() / 1000.0,
                stacktrace,
                event.interaction.context.toLoritta(),
                event.interaction.integrationOwners.guildIntegration,
                event.interaction.integrationOwners.userIntegration?.idLong,
            )
        }
    }

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        loritta.launchMessageJob(event) {
            var rootDeclaration: MessageCommandDeclaration? = null
            var slashDeclaration: MessageCommandDeclaration? = null

            for (declaration in manager.messageCommands) {
                val rootLabel = event.name

                if (rootLabel == manager.slashCommandDefaultI18nContext.get(declaration.name)) {
                    slashDeclaration = declaration
                    rootDeclaration = declaration
                    break
                }
            }

            // We should throw an error here
            // But we won't because we still use Discord InteraKTions
            if (rootDeclaration == null || slashDeclaration == null)
                return@launchMessageJob

            val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

            val rootDeclarationClazzName = rootDeclaration::class.simpleName ?: "UnknownCommand"
            val executorClazzName = executor::class.simpleName ?: "UnknownExecutor"
            val startedAt = Instant.now()

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ApplicationCommandContext? = null
            var stacktrace: String? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                // We need to check if the guild is detached because we can't get the user roles within a detached instance
                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }

                context = ApplicationCommandContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )

                // Check if user is banned
                if (AccountUtils.checkAndSendMessageIfUserIsBanned(context.loritta, context, context.user))
                    return@launchMessageJob

                // Check if the command is disabled
                val guildId = context.guildId
                if (checkIfCommandIsDisabledOnGuild(event, slashDeclaration, context, i18nContext, guildId))
                    return@launchMessageJob

                executor.execute(
                    context,
                    event.target
                )
            } catch (e: CommandException) {
                context?.reply(e.ephemeral, e.builder)
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()

                stacktrace = e.stackTraceToString()
            }

            loritta.pudding.executedInteractionsLog.insertApplicationCommandLog(
                event.user.idLong,
                event.guild?.idLong,
                event.channel?.idLong!!, // TODO: The channel can be null! This should be fixed
                Clock.System.now(),
                ApplicationCommandType.MESSAGE,
                rootDeclarationClazzName,
                executorClazzName,
                buildJsonObject {},
                stacktrace == null,
                Duration.between(startedAt, Instant.now()).toMillis() / 1000.0,
                stacktrace,
                event.interaction.context.toLoritta(),
                event.interaction.integrationOwners.guildIntegration,
                event.interaction.integrationOwners.userIntegration?.idLong,
            )
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        GlobalScope.launch {
            // Check if it is a InteraKTions Unleashed component
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ComponentContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }
                val callbackId = loritta.interactivityManager.buttonInteractionCallbacks[componentId.uniqueId]
                context = ComponentContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )

                // We don't know about this callback! It probably has expired, so let's tell the user about it
                if (callbackId == null) {
                    context.reply(true) {
                        styled(
                            i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                            Emotes.LoriSleeping
                        )
                    }
                    return@launch
                }

                callbackId.invoke(context)
            } catch (e: Exception) {
                val errorId = LorittaUtils.generateErrorId(loritta)
                logger.warn(e) { "Something went wrong while executing button interaction! Error ID: $errorId" }

                val currentContext = context
                val currentI18nContext = i18nContext
                if (currentContext != null && currentI18nContext != null) {
                    var sendExceptionToUser = true
                    // Don't attempt to send the message to the user if it was a unknown interaction error, because attempting to follow up with a message with surely
                    // cause yet another unknown interaction exception
                    if (e is ErrorResponseException && e.errorResponse == ErrorResponse.UNKNOWN_INTERACTION)
                        sendExceptionToUser = false

                    if (sendExceptionToUser) {
                        try {
                            currentContext.reply(currentContext.wasInitiallyDeferredEphemerally == null || currentContext.wasInitiallyDeferredEphemerally == true) {
                                styled(
                                    currentI18nContext.get(
                                        I18nKeysData.Commands.ErrorWhileExecutingCommandWithErrorId(
                                            loriRage = Emotes.LoriRage,
                                            loriSob = Emotes.LoriSob,
                                            errorId = errorId.toString()
                                        )
                                    ),
                                    Emotes.LoriSob
                                )
                            }
                        } catch (e: Exception) {
                            // wtf
                            logger.warn(e) { "Something went wrong while sending the reason why the button interaction was not correctly executed! Error ID: ${LorittaUtils.generateErrorId(loritta)}" }
                            // At this point just give up bro
                            throw e
                        }
                    }
                }
            }
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        GlobalScope.launch {
            // Check if it is a InteraKTions Unleashed component
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ComponentContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }

                val callback = loritta.interactivityManager.selectMenuInteractionCallbacks[componentId.uniqueId]
                context = ComponentContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )
                // We don't know about this callback! It probably has expired, so let's tell the user about it
                if (callback == null) {
                    context.reply(true) {
                        styled(
                            i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                            Emotes.LoriSleeping
                        )
                    }
                    return@launch
                }

                callback.invoke(context, event.interaction.values)
            } catch (e: Exception) {
                val errorId = LorittaUtils.generateErrorId(loritta)
                logger.warn(e) { "Something went wrong while executing select menu interaction! Error ID: $errorId" }

                val currentContext = context
                val currentI18nContext = i18nContext
                if (currentContext != null && currentI18nContext != null) {
                    var sendExceptionToUser = true
                    // Don't attempt to send the message to the user if it was a unknown interaction error, because attempting to follow up with a message with surely
                    // cause yet another unknown interaction exception
                    if (e is ErrorResponseException && e.errorResponse == ErrorResponse.UNKNOWN_INTERACTION)
                        sendExceptionToUser = false

                    if (sendExceptionToUser) {
                        try {
                            currentContext.reply(currentContext.wasInitiallyDeferredEphemerally == null || currentContext.wasInitiallyDeferredEphemerally == true) {
                                styled(
                                    currentI18nContext.get(
                                        I18nKeysData.Commands.ErrorWhileExecutingCommandWithErrorId(
                                            loriRage = Emotes.LoriRage,
                                            loriSob = Emotes.LoriSob,
                                            errorId = errorId.toString()
                                        )
                                    ),
                                    Emotes.LoriSob
                                )
                            }
                        } catch (e: Exception) {
                            // wtf
                            logger.warn(e) { "Something went wrong while sending the reason why the select menu interaction was not correctly executed! Error ID: ${LorittaUtils.generateErrorId(loritta)}" }
                            // At this point just give up bro
                            throw e
                        }
                    }
                }
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        GlobalScope.launch {
            // Check if it is a InteraKTions Unleashed modal
            val modalId = try {
                UnleashedComponentId(event.modalId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            // These variables are used in the catch { ... } block, to make our lives easier
            var i18nContext: I18nContext? = null
            var context: ModalContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }
                val modalCallback = loritta.interactivityManager.modalCallbacks[modalId.uniqueId]
                context = ModalContext(
                    loritta,
                    serverConfig,
                    lorittaUser,
                    locale,
                    i18nContext,
                    event
                )
                // We don't know about this callback! It probably has expired, so let's tell the user about it
                if (modalCallback == null) {
                    context.reply(true) {
                        styled(
                            i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                            Emotes.LoriSleeping
                        )
                    }
                    return@launch
                }

                modalCallback.invoke(context, ModalArguments(event))
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        loritta.launchMessageJob(event) {
            var rootDeclaration: SlashCommandDeclaration? = null
            var slashDeclaration: SlashCommandDeclaration? = null

            for (declaration in manager.slashCommands) {
                val rootLabel = event.name
                val subcommandGroupLabel = event.subcommandGroup
                val subcommandLabel = event.subcommandName

                if (rootLabel == manager.slashCommandDefaultI18nContext.get(declaration.name)) {
                    if (subcommandGroupLabel == null && subcommandLabel == null) {
                        // Already found it, yay!
                        slashDeclaration = declaration
                        rootDeclaration = declaration
                    } else {
                        // Check root subcommands
                        if (subcommandLabel != null) {
                            if (subcommandGroupLabel == null) {
                                // "/name subcommand"
                                slashDeclaration =
                                    declaration.subcommands.firstOrNull { manager.slashCommandDefaultI18nContext.get(it.name) == subcommandLabel }
                                rootDeclaration = declaration
                                break
                            } else {
                                // "/name subcommandGroup subcommand"
                                slashDeclaration = declaration.subcommandGroups.firstOrNull {
                                    manager.slashCommandDefaultI18nContext.get(it.name) == subcommandGroupLabel
                                }
                                    ?.subcommands
                                    ?.firstOrNull {
                                        manager.slashCommandDefaultI18nContext.get(it.name) == subcommandLabel
                                    }
                                rootDeclaration = declaration
                                break
                            }
                        }
                    }
                    break
                }
            }

            // We should throw an error here
            // But we won't because we still use Discord InteraKTions
            if (rootDeclaration == null || slashDeclaration == null)
                return@launchMessageJob

            // No executor, bail out!
            val executor = slashDeclaration.executor ?: return@launchMessageJob

            val autocompletingOption = executor.options.registeredOptions
                .firstOrNull {
                    it.name == event.focusedOption.name
                } ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the option doesn't exist!")

            try {
                val guild = event.guild
                val member = event.member

                val serverConfigJob = if (guild != null)
                    loritta.getOrCreateServerConfigDeferred(guild.idLong, true)
                else
                    loritta.getOrCreateServerConfigDeferred(-1, true)

                val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.user.idLong)

                val serverConfig = serverConfigJob.await()
                val lorittaProfile = lorittaProfileJob.await()

                val currentLocale = loritta.newSuspendedTransaction {
                    (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                }

                val locale = loritta.localeManager.getLocaleById(currentLocale)
                val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                    // We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
                    val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, guild)
                    val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                        member,
                        rolesLorittaPermissions
                    )
                    GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
                } else {
                    LorittaUser(loritta, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                }

                when (autocompletingOption) {
                    is StringDiscordOptionReference -> {
                        val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<String>
                            ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                        val map = autocompleteCallback.execute(
                            AutocompleteContext(
                                loritta,
                                serverConfig,
                                lorittaUser,
                                locale,
                                i18nContext,
                                event
                            )
                        ).map {
                            Command.Choice(it.key, it.value)
                        }

                        event.replyChoices(map).await()
                    }
                    is LongDiscordOptionReference -> {
                        val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<Long>
                            ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                        val map = autocompleteCallback.execute(
                            AutocompleteContext(
                                loritta,
                                serverConfig,
                                lorittaUser,
                                locale,
                                i18nContext,
                                event
                            )
                        ).map {
                            Command.Choice(it.key, it.value)
                        }

                        event.replyChoices(map).await()
                    }
                    is NumberDiscordOptionReference -> {
                        val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<Double>
                            ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                        val map = autocompleteCallback.execute(
                            AutocompleteContext(
                                loritta,
                                serverConfig,
                                lorittaUser,
                                locale,
                                i18nContext,
                                event
                            )
                        ).map {
                            Command.Choice(it.key, it.value)
                        }

                        event.replyChoices(map).await()
                    }
                    else -> error("Unsupported option reference for autocomplete ${autocompletingOption::class.simpleName}")
                }
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }

    private fun updateCommands(guildId: Long, action: (List<CommandData>) -> (List<Command>)): List<DiscordCommand> {
        logger.info { "Updating slash command on guild $guildId..." }

        val applicationCommands = manager.slashCommands.map { manager.convertDeclarationToJDA(it) } + loritta.interactionsManager.interaKTions.manager.applicationCommandsDeclarations.map { manager.convertInteraKTionsDeclarationToJDA(it) } + manager.userCommands.map { manager.convertDeclarationToJDA(it) } + manager.messageCommands.map { manager.convertDeclarationToJDA(it) }
        logger.info { "Successfully converted all application command declarations to JDA! Total commands: ${applicationCommands.size}" }
        val applicationCommandsHash = applicationCommands.sumOf { it.toData().toString().hashCode() }

        while (true) {
            val lockId = "loritta-cinnamon-application-command-updater-$guildId".hashCode()

            try {
                var registeredCommands: List<DiscordCommand>? = null

                loritta.pudding.hikariDataSource.connection.use { connection ->
                    logger.info { "Locking PostgreSQL advisory lock $lockId" }
                    // First, we will hold a lock to avoid other instances trying to update the app commands at the same time
                    val xactLockStatement = connection.prepareStatement("SELECT pg_advisory_xact_lock(?);")
                    xactLockStatement.setInt(1, lockId)
                    xactLockStatement.execute()
                    logger.info { "Successfully acquired PostgreSQL advisory lock $lockId!" }

                    val pairData =
                        connection.prepareStatement("SELECT hash, data FROM ${DiscordLorittaApplicationCommandHashes.tableName} WHERE id = $guildId;")
                            .executeQuery()
                            .let {
                                if (it.next())
                                    Pair(it.getInt("hash"), it.getString("data"))
                                else
                                    null
                            }

                    if (pairData == null || applicationCommandsHash != pairData.first) {
                        // Needs to be updated!
                        logger.info { "Updating Loritta commands in guild $guildId... Hash: $applicationCommandsHash, lock $lockId" }
                        val updatedCommands = action.invoke(applicationCommands)
                        val updatedCommandsData = updatedCommands.map {
                            DiscordCommand.from(it)
                        }

                        val updateStatement =
                            connection.prepareStatement("INSERT INTO ${DiscordLorittaApplicationCommandHashes.tableName} (id, hash, data) VALUES ($guildId, $applicationCommandsHash, ?) ON CONFLICT (id) DO UPDATE SET hash = $applicationCommandsHash, data = ?;")

                        val pgObject = PGobject()
                        pgObject.type = "jsonb"
                        pgObject.value = Json.encodeToString(updatedCommandsData)
                        updateStatement.setObject(1, pgObject)
                        updateStatement.setObject(2, pgObject)
                        updateStatement.executeUpdate()

                        logger.info { "Successfully updated Loritta's commands in guild $guildId! Hash: $applicationCommandsHash, lock $lockId" }
                        registeredCommands = updatedCommandsData
                    } else {
                        // No need for update, yay :3
                        logger.info { "Stored guild $guildId (lock $lockId) commands hash match our hash $applicationCommandsHash, so we don't need to update, yay! :3" }
                        registeredCommands = Json.decodeFromString(pairData.second)
                    }

                    connection.commit()
                }

                return registeredCommands!!
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while trying to update slash commands! Retrying... Lock: $lockId" }
            }
        }
    }

    private suspend fun checkIfCommandIsDisabledOnGuild(
        event: GenericCommandInteractionEvent,
        slashDeclaration: ExecutableApplicationCommandDeclaration,
        context: ApplicationCommandContext,
        i18nContext: I18nContext,
        guildId: Long?,
    ): Boolean {
        if (guildId == null)
            return false

        val g = loritta.transaction {
            GuildCommandConfigs.selectAll()
                .where {
                    GuildCommandConfigs.guildId eq guildId and (GuildCommandConfigs.commandId eq slashDeclaration.uniqueId)
                }
                .firstOrNull()
                .let { GuildCommandConfigData.fromResultRowOrDefault(it) }
        }

        if (!g.enabled) {
            // Command is NOT enabled!
            // So, how can we check that the user can use it if it is a USER_INSTALL command?
            // 1. Does the "USER_INSTALL" integration type is set?
            // 2. Is the user an integration owner?
            // If both are true, the message will be ephemeral if the user does NOT have the "external apps" permission set
            val canBeUsedAnyway = slashDeclaration.integrationTypes.contains(IntegrationType.USER_INSTALL) && event.interaction.integrationOwners.userIntegration?.idLong == context.user.idLong
            if (!canBeUsedAnyway) {
                // NO, then bail out NOW
                context.reply(true) {
                    styled(
                        i18nContext.get(I18nKeysData.Commands.DisabledCommandOnThisGuild),
                        Emotes.Error
                    )
                }
                return true
            } else {
                // So, it is actually enabled on a user install context!
                // What we'll do instead is force the interaction to ALWAYS be ephemeral
                // If the user has permission, then the message should be PUBLIC, if not, it should be EPHEMERAL
                context.alwaysEphemeral = !context.member.hasPermission(Permission.USE_EXTERNAL_APPLICATIONS)
            }
        }

        return false
    }
}