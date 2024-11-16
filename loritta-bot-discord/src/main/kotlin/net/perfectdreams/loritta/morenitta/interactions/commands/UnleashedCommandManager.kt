package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.interactions.commands.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.I18nContextUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.InteractionContextType
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.text.TextTransformCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MinecraftCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.misc.LanguageCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.BanCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.BanInfoCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.DashboardCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.PredefinedReasonsCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.reactionevents.EventCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.color.ColorInfoCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.videos.*
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.extensions.referenceIfPossible
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Duration
import java.time.Instant
import java.util.*

class UnleashedCommandManager(val loritta: LorittaBot, val languageManager: LanguageManager) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val slashCommands = mutableListOf<SlashCommandDeclaration>()
    val userCommands = mutableListOf<UserCommandDeclaration>()
    val messageCommands = mutableListOf<MessageCommandDeclaration>()
    val applicationCommands: List<ExecutableApplicationCommandDeclaration>
        get() = slashCommands + userCommands + messageCommands
    // Application Commands have their label/descriptions in English
    val slashCommandDefaultI18nContext = languageManager.getI18nContextById("en")

    private var commandPathToDeclarations = mutableMapOf<String, SlashCommandDeclaration>()

    fun register(declaration: SlashCommandDeclarationWrapper) {
        val builtDeclaration = declaration.command().build()

        if (builtDeclaration.enableLegacyMessageSupport) {
            // Validate if all executors inherit LorittaLegacyMessageCommandExecutor
            val executors = mutableListOf<Any>()
            if (builtDeclaration.executor != null)
                executors += builtDeclaration.executor

            for (subcommand in builtDeclaration.subcommands) {
                if (subcommand.executor != null)
                    executors += subcommand.executor
            }

            for (subcommandGroup in builtDeclaration.subcommandGroups) {
                for (subcommand in subcommandGroup.subcommands) {
                    if (subcommand.executor != null)
                        executors += subcommand.executor
                }
            }

            for (executor in executors) {
                if (executor !is LorittaLegacyMessageCommandExecutor)
                    error("${executor::class.simpleName} does not inherit LorittaLegacyMessageCommandExecutor, but enable legacy message support is enabled!")
            }
        }

        slashCommands += builtDeclaration
    }

    fun register(declaration: UserCommandDeclarationWrapper) {
        userCommands += declaration.command().build()
    }

    fun register(declaration: MessageCommandDeclarationWrapper) {
        messageCommands += declaration.command().build()
    }

    private fun updateCommandPathToDeclarations() {
        fun isDeclarationExecutable(declaration: SlashCommandDeclaration) = declaration.executor != null

        // No match? Check all executor's absolute paths
        val commandPathToDeclarations = mutableMapOf<String, SlashCommandDeclaration>()

        fun putNormalized(key: String, value: SlashCommandDeclaration) {
            commandPathToDeclarations[key.normalize()] = value
        }

        // Get all executors that have enabled legacy message support enabled and add them to the command path
        for (declaration in slashCommands.filter { it.enableLegacyMessageSupport }) {
            val rootLabels = languageManager.languageContexts.values.map {
                it.get(declaration.name)
            } + declaration.alternativeLegacyLabels

            if (isDeclarationExecutable(declaration)) {
                for (rootLabel in rootLabels) {
                    putNormalized(rootLabel, declaration)
                }

                // And add the absolute commands!
                for (absolutePath in declaration.alternativeLegacyAbsoluteCommandPaths) {
                    putNormalized(absolutePath, declaration)
                }
            }

            declaration.subcommands.forEach { subcommand ->
                if (isDeclarationExecutable(subcommand)) {
                    val subcommandLabels = languageManager.languageContexts.values.map {
                        it.get(subcommand.name)
                    } + subcommand.alternativeLegacyLabels

                    for (rootLabel in rootLabels) {
                        for (subcommandLabel in subcommandLabels) {
                            putNormalized("$rootLabel $subcommandLabel", subcommand)
                        }
                    }

                    // And add the absolute commands!
                    for (absolutePath in subcommand.alternativeLegacyAbsoluteCommandPaths) {
                        putNormalized(absolutePath, subcommand)
                    }
                }
            }

            declaration.subcommandGroups.forEach { group ->
                val subcommandGroupLabels = languageManager.languageContexts.values.map {
                    it.get(group.name)
                } + group.alternativeLegacyLabels

                group.subcommands.forEach { subcommand ->
                    if (isDeclarationExecutable(subcommand)) {
                        val subcommandLabels = languageManager.languageContexts.values.map {
                            it.get(subcommand.name)
                        } + subcommand.alternativeLegacyLabels

                        for (rootLabel in rootLabels) {
                            for (subcommandGroupLabel in subcommandGroupLabels) {
                                for (subcommandLabel in subcommandLabels) {
                                    putNormalized("$rootLabel $subcommandGroupLabel $subcommandLabel", subcommand)
                                }
                            }
                        }

                        // And add the absolute commands!
                        for (absolutePath in subcommand.alternativeLegacyAbsoluteCommandPaths) {
                            putNormalized(absolutePath.normalize(), subcommand)
                        }
                    }
                }
            }
        }

        this.commandPathToDeclarations = commandPathToDeclarations
    }

    init {
        // ===[ DISCORD ]===
        register(LorittaCommand())
        register(WebhookCommand(loritta))
        register(UserCommand(loritta))
        register(MessageStickerCommand())
        register(GuildCommand()) // TODO: Merge with ServerCommand
        register(ServerCommand(loritta))
        register(EmojiCommand())
        register(InviteCommand())
        register(UserAvatarUserCommand())
        register(UserInfoUserCommand())
        register(SaveMessageCommand.SaveMessagePublicCommand(loritta))
        register(SaveMessageCommand.SaveMessagePrivateCommand(loritta))
        register(VerifyMessageCommand(loritta))

        // ===[ MODERATION ]===
        register(BanInfoCommand(loritta))
        register(DashboardCommand(loritta))
        register(PredefinedReasonsCommand())

        // ===[ FUN ]===
        register(EventCommand(loritta))
        register(MusicalChairsCommand(loritta))
        register(ShipCommand(loritta))
        register(RollCommand(loritta))
        register(CoinFlipCommand())
        register(VieirinhaCommand())
        register(CancelledCommand())
        register(HungerGamesCommand(loritta))
        register(SummonCommand(loritta))
        register(JankenponCommand(loritta))
        register(RateCommand(loritta))
        register(TextTransformCommand())
        register(SoundboxCommand(loritta))
        register(BanCommand(loritta))

        // ===[ IMAGES ]==
        register(ArtCommand(loritta.gabrielaImageServerClient))
        register(BobBurningPaperCommand(loritta.gabrielaImageServerClient))
        register(DrakeCommand(loritta.gabrielaImageServerClient))
        register(BRMemesCommand(loritta.gabrielaImageServerClient))
        register(BuckShirtCommand(loritta.gabrielaImageServerClient))
        register(LoriSignCommand(loritta.gabrielaImageServerClient))
        register(PassingPaperCommand(loritta.gabrielaImageServerClient))
        register(PepeDreamCommand(loritta.gabrielaImageServerClient))
        register(PetPetCommand(loritta.gabrielaImageServerClient))
        register(WolverineFrameCommand(loritta.gabrielaImageServerClient))
        register(RipTvCommand(loritta.gabrielaImageServerClient))
        register(SustoCommand(loritta.gabrielaImageServerClient))
        register(GetOverHereCommand(loritta.gabrielaImageServerClient))
        register(NichijouYuukoPaperCommand(loritta.gabrielaImageServerClient))
        register(TrumpCommand(loritta.gabrielaImageServerClient))
        register(TerminatorAnimeCommand(loritta.gabrielaImageServerClient))
        register(ToBeContinuedCommand(loritta.gabrielaImageServerClient))
        register(InvertColorsCommand(loritta.gabrielaImageServerClient))
        register(MemeMakerCommand(loritta.gabrielaImageServerClient))
        register(MarkMetaCommand(loritta.gabrielaImageServerClient))
        register(SonicCommand(loritta.gabrielaImageServerClient))
        register(DrawnMaskCommand(loritta.gabrielaImageServerClient))
        register(SadRealityCommand())
        register(EveryGroupHasCommand())
        register(ThanksFriendsCommand())

        // ===[ VIDEOS ]===
        register(AttackOnHeartCommand(loritta.gabrielaImageServerClient))
        register(CarlyAaahCommand(loritta.gabrielaImageServerClient))
        register(ChavesCommand(loritta.gabrielaImageServerClient))
        register(FansExplainingCommand(loritta.gabrielaImageServerClient))
        register(GigaChadCommand(loritta.gabrielaImageServerClient))

        // ===[ SOCIAL ]===
        register(AfkCommand())
        register(GenderCommand())
        register(ProfileCommand(loritta))
        register(RepCommand(loritta))
        register(XpCommand(loritta))
        register(AchievementsCommand(loritta))

        // ===[ ECONOMY ]===
        register(DailyCommand(loritta))
        register(CoinFlipBetCommand(loritta))
        register(EmojiFightCommand(loritta))
        register(RaffleCommand(loritta))
        register(BrokerCommand(loritta))
        register(CoinFlipBetGlobalCommand())
        // April Fools
        // register(CoinFlipBetBugCommand(loritta))
        register(LoriCoolCardsCommand(loritta))
        register(SonhosCommand(loritta))

        // ===[ DREAMLAND ]===
        if (loritta.config.loritta.environment == EnvironmentType.CANARY)
            register(LoriTuberCommand(loritta))

        // ===[ MINECRAFT ]===
        register(MinecraftCommand(loritta))

        // ===[ MISCELLANEOUS ]===
        register(LanguageCommand(loritta))

        // ===[ UTILS ]===
        register(AnagramCommand())
        register(ColorInfoCommand(loritta))
        register(HelpCommand())
        register(CalculatorCommand())
        register(OCRSlashCommand(loritta))
        register(OCRMessageCommand(loritta))
        register(DictionaryCommand(loritta))
        register(MoneyCommand(loritta, loritta.ecbManager))
        register(MorseCommand())
        register(NotificationsCommand())
        register(ChooseCommand())
        register(TranslateCommand(loritta))

        // ===[ ROLEPLAY ]===
        register(RoleplayCommand.RoleplaySlashCommand(loritta))
        register(RoleplayCommand.RoleplayUserCommand(loritta))

        // ===[ UNDERTALE ]===
        register(UndertaleCommand(loritta, loritta.gabrielaImageServerClient))

        // Check if there is any duplicated IDs
        val allCommands = slashCommands.flatMap {
            listOf(it) + it.subcommands + it.subcommandGroups.flatMap { it.subcommands }
        } + messageCommands + userCommands
        val uniqueIds = mutableListOf<UUID>()
        allCommands.forEach {
            val sameIdCommand = allCommands.firstOrNull { cmd ->
                cmd != it && cmd.uniqueId == it.uniqueId
            }

            if (sameIdCommand != null)
                error("Command ${it.uniqueId} has the same ID as ${sameIdCommand.uniqueId}")

            uniqueIds.add(it.uniqueId)
        }

        // After registering everything, the command path must be updated!
        updateCommandPathToDeclarations()
    }

    /**
     * Checks if the command should be handled (if all conditions are valid, like labels, etc)
     *
     * This is used if a command has [enableLegacyMessageSupport]
     *
     * @param event          the event wrapped in a LorittaMessageEvent
     * @param legacyServerConfig        the server configuration
     * @param legacyLocale      the language of the server
     * @param lorittaUser the user that is executing this command
     * @return            if the command was handled or not
     */
    suspend fun matches(event: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
        var rootDeclaration: SlashCommandDeclaration? = null
        var slashDeclaration: SlashCommandDeclaration? = null

        var argumentsToBeDropped = 0

        var bestMatch: SlashCommandDeclaration? = null
        var absolutePathSize = 0

        commandDeclarationsLoop@for ((commandPath, declaration) in commandPathToDeclarations) {
            argumentsToBeDropped = 0

            val absolutePathSplit = commandPath.split(" ")

            if (absolutePathSize > absolutePathSplit.size)
                continue // Too smol, the current command is a better match

            for ((index, pathSection) in absolutePathSplit.withIndex()) {
                val rawArgument = rawArguments.getOrNull(index)?.lowercase()?.normalize() ?: continue@commandDeclarationsLoop

                if (pathSection.normalize() == rawArgument) {
                    argumentsToBeDropped++
                } else {
                    continue@commandDeclarationsLoop
                }
            }

            bestMatch = declaration
            absolutePathSize = argumentsToBeDropped
        }

        if (bestMatch != null) {
            rootDeclaration = bestMatch
            slashDeclaration = bestMatch
            argumentsToBeDropped = absolutePathSize
        }

        // No match, bail out!
        if (rootDeclaration == null || slashDeclaration == null)
            return false

        val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")
        if (executor !is LorittaLegacyMessageCommandExecutor)
            error("$executor doesn't inherit LorittaLegacyMessageCommandExecutor!")

        val rootDeclarationClazzName = rootDeclaration::class.simpleName ?: "UnknownCommand"
        val executorClazzName = executor::class.simpleName ?: "UnknownExecutor"
        val startedAt = Instant.now()

        // These variables are used in the catch { ... } block, to make our lives easier
        var context: UnleashedContext? = null
        var stacktrace: String? = null

        try {
            val rawArgumentsAfterDrop = rawArguments.drop(argumentsToBeDropped)

            context = LegacyMessageCommandContext(
                loritta,
                serverConfig,
                lorittaUser,
                locale,
                i18nContext,
                event,
                rawArgumentsAfterDrop,
                rootDeclaration,
                slashDeclaration
            )
            val guild = context.guildOrNull

            if (serverConfig.blacklistedChannels.contains(event.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
                if (serverConfig.warnIfBlacklisted) {
                    if (serverConfig.blacklistedWarning?.isNotEmpty() == true && event.guild != null && event.member != null && event.textChannel != null) {
                        val generatedMessage = MessageUtils.generateMessageOrFallbackIfInvalid(
                            i18nContext,
                            serverConfig.blacklistedWarning ?: "???",
                            listOf(event.member, event.textChannel, event.guild),
                            event.guild,
                            emptyMap(),
                            generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.CommandDenylist
                        )

                        event.textChannel.sendMessage(generatedMessage)
                            .referenceIfPossible(event.message, serverConfig, true)
                            .await()
                    }
                }
                // Channel is blocked so let's bail out
                return true
            }

            // Check if user is banned
            if (AccountUtils.checkAndSendMessageIfUserIsBanned(context.loritta, context, context.user))
                return true

            // Check if the command is disabled
            val guildId = context.guildId
            if (guildId != null) {
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
                    // Message commands should be fully disabled, no way around it! So let's bail out!
                    context.reply(true) {
                        styled(
                            i18nContext.get(I18nKeysData.Commands.DisabledCommandOnThisGuild),
                            Emotes.Error
                        )
                    }
                    return true
                }
            }

            if (rawArgumentsAfterDrop.getOrNull(0) == "🤷") { // Show the command's help embed if 🤷 has been used
                context.explain()
                return true
            }

            if (rootDeclaration.isGuildOnly && guild == null) {
                // Matched, but it is guild only
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds),
                        Emotes.Error
                    )
                }
                return true
            }

            // Are we in a guild?
            if (guild != null) {
                // Check if guild is banned
                if (LorittaUtils.isGuildOwnerBanned(loritta, lorittaUser._profile, guild) || LorittaUtils.isGuildBanned(loritta, guild))
                    return true

                // Get the permissions
                // To mimick how slash commands work, we only check the root declaration permissions
                val requiredPermissionsRaw = rootDeclaration.defaultMemberPermissions?.permissionsRaw
                if (requiredPermissionsRaw != null) {
                    val requiredPermissions = Permission.getPermissions(requiredPermissionsRaw)

                    val missingPermissions = requiredPermissions.filter { !context.member.hasPermission(context.channel as GuildChannel, it) }

                    if (missingPermissions.isNotEmpty()) {
                        val missingPermissionsAsString = missingPermissions.joinToString(
                            ", ",
                            transform = { "`" + it.getLocalizedName(i18nContext) + "`" }
                        )
                        context.reply(true) {
                            styled(
                                locale["commands.userDoesntHavePermissionDiscord", missingPermissionsAsString],
                                Constants.ERROR
                            )
                        }
                        return true
                    }
                }

                if (!loritta.discordSlashCommandScopeWorkaround.checkIfSlashCommandScopeIsEnabled(guild, context.member)) {
                    context.reply(false, loritta.discordSlashCommandScopeWorkaround.unauthMessage(context.guild, context.member))
                }
            }

            loritta.transaction {
                lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()

                // Set that the user is in the guild
                val guildId = context.guildId
                if (guildId != null) {
                    GuildProfiles.update({
                        GuildProfiles.userId eq lorittaUser.profile.userId and (GuildProfiles.guildId eq guildId)
                    }) {
                        it[GuildProfiles.isInGuild] = true
                    }
                }

                loritta.lorittaShards.updateCachedUserData(context.user)
            }

            val argMap = executor.convertToInteractionsArguments(context, rawArgumentsAfterDrop)

            // If the argument map is null, bail out!
            if (argMap != null) {
                val args = SlashCommandArguments(SlashCommandArgumentsSource.SlashCommandArgumentsMapSource(argMap))

                executor.execute(
                    context,
                    args
                )
            }
        } catch (e: CommandException) {
            context?.reply(e.ephemeral, e.builder)
        } catch (e: Exception) {
            // TODO: Proper catch and throw
            e.printStackTrace()

            stacktrace = e.stackTraceToString()
        }

        loritta.pudding.executedInteractionsLog.insertApplicationCommandLog(
            event.author.idLong,
            event.guild?.idLong,
            event.channel.idLong,
            Clock.System.now(),
            ApplicationCommandType.LEGACY_CHAT_MESSAGE_INPUT,
            rootDeclarationClazzName,
            executorClazzName,
            // Because this is via legacy message, we don't actually have "options" per se, so let's store the raw message content
            buildJsonObject {
                put("raw_message", event.message.contentRaw)
            },
            stacktrace == null,
            Duration.between(startedAt, Instant.now()).toMillis() / 1000.0,
            stacktrace,
            // Once again we don't have these attributes because this was invoked via messages, so we will roll our own!
            // Messages can only be sent in the bot DM's
            if (event.channel.type == ChannelType.PRIVATE) {
                InteractionContextType.BOT_DM
            } else InteractionContextType.GUILD, // If else, then it is a guild
            // JDA always returns 0 when the guild is the bot DM's
            if (event.channel.type == ChannelType.PRIVATE) {
                0L
            } else event.guild?.idLong, // If else, then it is a guild
            // And finally, messages cannot be installed into the user's account
            null
        )

        return true
    }

    /**
     * Gets the declaration path (command -> group -> subcommand, and anything in between)
     */
    fun findDeclarationPath(endDeclaration: SlashCommandDeclaration): List<Any> {
        for (declaration in slashCommands) {
            if (declaration == endDeclaration) {
                return listOf(declaration)
            }

            for (subcommandDeclaration in declaration.subcommands) {
                if (subcommandDeclaration == endDeclaration)
                    return listOf(declaration, subcommandDeclaration)
            }

            for (group in declaration.subcommandGroups) {
                for (subcommandDeclaration in group.subcommands) {
                    if (subcommandDeclaration == endDeclaration)
                        return listOf(declaration, group, subcommandDeclaration)
                }
            }
        }

        error("Declaration path is null for $endDeclaration! This should never happen! Are you trying to find a declaration that isn't registered in InteraKTions Unleashed?")
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: SlashCommandDeclaration): SlashCommandData {
        return Commands.slash(slashCommandDefaultI18nContext.get(declaration.name), buildDescription(slashCommandDefaultI18nContext, declaration.description, declaration.category)).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.isGuildOnly = declaration.isGuildOnly
            this.setContexts(declaration.interactionContexts[0], *declaration.interactionContexts.toTypedArray())
            this.setIntegrationTypes(declaration.integrationTypes[0], *declaration.integrationTypes.toTypedArray())

            forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                setNameLocalization(discordLocale, i18nContext.get(declaration.name))
                setDescriptionLocalization(discordLocale, buildDescription(i18nContext, declaration.description, declaration.category))
            }

            if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty()) {
                // If legacy message support is enabled, then the executor *can* actually be used via message command, so we will skip this check
                if (declaration.executor != null && !declaration.enableLegacyMessageSupport)
                    error("Command ${declaration::class.simpleName} has a root executor, but it also has subcommand/subcommand groups!")

                for (subcommand in declaration.subcommands) {
                    subcommand(slashCommandDefaultI18nContext.get(subcommand.name), buildDescription(slashCommandDefaultI18nContext, subcommand.description, subcommand.category)) {
                        val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                        forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                            setNameLocalization(discordLocale, i18nContext.get(subcommand.name))
                            setDescriptionLocalization(discordLocale, buildDescription(i18nContext, subcommand.description, subcommand.category))
                        }

                        for (ref in executor.options.registeredOptions) {
                            try {
                                addOptions(*createOption(ref).toTypedArray())
                            } catch (e: Exception) {
                                logger.error(e) { "Something went wrong while trying to add options of $executor" }
                            }
                        }
                    }
                }

                for (group in declaration.subcommandGroups) {
                    group(slashCommandDefaultI18nContext.get(group.name), buildDescription(slashCommandDefaultI18nContext, group.description, group.category)) {
                        forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                            setNameLocalization(discordLocale, i18nContext.get(group.name))
                            setDescriptionLocalization(discordLocale, buildDescription(i18nContext, group.description, group.category))
                        }

                        for (subcommand in group.subcommands) {
                            subcommand(slashCommandDefaultI18nContext.get(subcommand.name), buildDescription(slashCommandDefaultI18nContext, subcommand.description, subcommand.category)) {
                                val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                                forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                                    setNameLocalization(discordLocale, i18nContext.get(subcommand.name))
                                    setDescriptionLocalization(discordLocale, buildDescription(i18nContext, subcommand.description, subcommand.category))
                                }

                                for (ref in executor.options.registeredOptions) {
                                    try {
                                        addOptions(*createOption(ref).toTypedArray())
                                    } catch (e: Exception) {
                                        logger.error(e) { "Something went wrong while trying to add options of $executor" }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val executor = declaration.executor

                if (executor != null) {
                    for (ref in executor.options.registeredOptions) {
                        try {
                            addOptions(*createOption(ref).toTypedArray())
                        } catch (e: Exception) {
                            logger.error(e) { "Something went wrong while trying to add options of $executor" }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: UserCommandDeclaration): CommandData {
        return Commands.user(slashCommandDefaultI18nContext.get(declaration.name)).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.isGuildOnly = declaration.isGuildOnly
            this.setContexts(declaration.interactionContexts[0], *declaration.interactionContexts.toTypedArray())
            this.setIntegrationTypes(declaration.integrationTypes[0], *declaration.integrationTypes.toTypedArray())

            forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                setNameLocalization(discordLocale, i18nContext.get(declaration.name))
            }
        }
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: MessageCommandDeclaration): CommandData {
        return Commands.message(slashCommandDefaultI18nContext.get(declaration.name)).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.isGuildOnly = declaration.isGuildOnly
            this.setContexts(declaration.interactionContexts[0], *declaration.interactionContexts.toTypedArray())
            this.setIntegrationTypes(declaration.integrationTypes[0], *declaration.integrationTypes.toTypedArray())

            forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                setNameLocalization(discordLocale, i18nContext.get(declaration.name))
            }
        }
    }

    private fun createOption(interaKTionsOption: OptionReference<*>): List<OptionData> {
        when (interaKTionsOption) {
            is DiscordOptionReference -> {
                val description = slashCommandDefaultI18nContext
                    .get(interaKTionsOption.description)
                    .shortenWithEllipsis(100)

                when (interaKTionsOption) {
                    is LongDiscordOptionReference -> {
                        return listOf(
                            Option<Long>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                if (interaKTionsOption.requiredRange != null) {
                                    setRequiredRange(interaKTionsOption.requiredRange.first, interaKTionsOption.requiredRange.last)
                                }
                            }
                        )
                    }

                    is NumberDiscordOptionReference -> {
                        return listOf(
                            Option<Double>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                if (interaKTionsOption.requiredRange != null) {
                                    setRequiredRange(interaKTionsOption.requiredRange.start, interaKTionsOption.requiredRange.endInclusive)
                                }
                            }
                        )
                    }

                    is StringDiscordOptionReference -> {
                        return listOf(
                            Option<String>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                for (choice in interaKTionsOption.choices) {
                                    when (choice) {
                                        is StringDiscordOptionReference.Choice.LocalizedChoice -> {
                                            addChoices(
                                                Command.Choice(
                                                    slashCommandDefaultI18nContext.get(choice.name), choice.value
                                                ).apply {
                                                    forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                                                        setNameLocalization(discordLocale, i18nContext.get(choice.name))
                                                    }
                                                }
                                            )
                                        }
                                        is StringDiscordOptionReference.Choice.RawChoice -> choice(choice.name, choice.value)
                                    }
                                }
                            }
                        )
                    }

                    is ChannelDiscordOptionReference -> {
                        return listOf(
                            Option<GuildChannel>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is UserDiscordOptionReference -> {
                        return listOf(
                            Option<User>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is RoleDiscordOptionReference -> {
                        return listOf(
                            Option<Role>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is AttachmentDiscordOptionReference -> {
                        return listOf(
                            Option<Attachment>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is BooleanDiscordOptionReference -> {
                        return listOf(
                            Option<Boolean>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }
                }
            }

            is ImageReferenceDiscordOptionReference -> {
                return listOf(
                    Option<String>(
                        interaKTionsOption.name,
                        "User, URL or Emoji",
                        true
                    )
                )
            }

            is ImageReferenceOrAttachmentDiscordOptionReference -> {
                return listOf(
                    Option<String>(
                        interaKTionsOption.name + "_data",
                        "User, URL or Emoji",
                        false
                    ),
                    Option<Attachment>(
                        interaKTionsOption.name + "_attachment",
                        "Image Attachment",
                        false
                    )
                )
            }
        }
    }

    private fun buildDescription(i18nContext: I18nContext, description: StringI18nData, category: CommandCategory) = buildString {
        // It looks like this
        // "「Emoji Category」 Description"
        append("「")
        // Before we had unicode emojis reflecting each category, but the emojis look super ugly on Windows 10
        // https://cdn.discordapp.com/attachments/297732013006389252/973613713456250910/unknown.png
        // So we removed it ;)
        append(category.getLocalizedName(i18nContext))
        append("」")
        // Looks better without this whitespace
        // append(" ")
        append(i18nContext.get(description))
    }.shortenWithEllipsis(DiscordResourceLimits.Command.Description.Length)

    /**
     * For each loop on every [I18nContext] that has a valid [DiscordLocale]
     *
     * @see [I18nContextUtils.convertLanguageIdToJDALocale]
     */
    private fun forEachI18nContextWithValidLocale(action: (DiscordLocale, I18nContext) -> (Unit)) {
        for ((languageId, i18nContext) in languageManager.languageContexts) {
            val discordLocale = I18nContextUtils.convertLanguageIdToJDALocale(languageId)
            if (discordLocale != null)
                action.invoke(discordLocale, i18nContext)
        }
    }

    private fun Map<Locale, String>.mapKeysToJDALocales() = this.mapKeys {
        val language = it.key.language
        val country = it.key.country
        var locale = language
        if (country != null)
            locale += "-$country"

        DiscordLocale.from(locale)
    }
}