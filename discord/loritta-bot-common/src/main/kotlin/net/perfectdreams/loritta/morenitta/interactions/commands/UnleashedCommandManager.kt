package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.kord.common.Locale
import dev.minn.jda.ktx.interactions.commands.*
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandDeclaration
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandGroupDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions.ImageReferenceCommandOption
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions.ImageReferenceOrAttachmentOption
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions.StringListCommandOption
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.customoptions.UserListCommandOption
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.I18nContextUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.options.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.easter2023.EventCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.LorittaCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.WebhookCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.DailyCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.EmojiFightCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.RaffleCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.MusicalChairsCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.RollCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.ShipCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.MinecraftCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.BanInfoCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.GamerSaferCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.ProfileCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.AnagramCommand
import net.perfectdreams.loritta.morenitta.utils.config.EnvironmentType

class UnleashedCommandManager(val loritta: LorittaBot, val languageManager: LanguageManager) {
    val slashCommands = mutableListOf<SlashCommandDeclaration>()
    val userCommands = mutableListOf<UserCommandDeclaration>()
    val messageCommands = mutableListOf<MessageCommandDeclaration>()
    // Application Commands have their label/descriptions in English
    val slashCommandDefaultI18nContext = languageManager.getI18nContextById("en")

    fun register(declaration: SlashCommandDeclarationWrapper) {
        slashCommands += declaration.command().build()
    }

    fun register(declaration: UserCommandDeclarationWrapper) {
        userCommands += declaration.command().build()
    }

    fun register(declaration: MessageCommandDeclarationWrapper) {
        messageCommands += declaration.command().build()
    }

    init {
        // ===[ DISCORD ]===
        register(LorittaCommand())
        register(WebhookCommand(loritta))

        // ===[ MODERATION ]===
        if (loritta.config.loritta.environment == EnvironmentType.CANARY)
            register(GamerSaferCommand(loritta))
        register(BanInfoCommand(loritta))

        // ===[ FUN ]===
        register(EventCommand(loritta))
        register(MusicalChairsCommand(loritta))
        register(ShipCommand(loritta))
        register(RollCommand(loritta))

        // ===[ SOCIAL ]===
        register(ProfileCommand(loritta))

        // ===[ ECONOMY ]===
        register(DailyCommand(loritta))
        register(EmojiFightCommand(loritta))
        register(RaffleCommand(loritta))

        // ===[ DREAMLAND ]===
        if (loritta.config.loritta.environment == EnvironmentType.CANARY)
            register(LoriTuberCommand(loritta))

        // ===[ MINECRAFT ]===
        register(MinecraftCommand(loritta))

        // ===[ UTILS ]===
        register(AnagramCommand())

        // ===[ ROLEPLAY ]===
        register(RoleplayCommand.RoleplaySlashCommand(loritta))
        register(RoleplayCommand.RoleplayUserCommand(loritta))
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: SlashCommandDeclaration): SlashCommandData {
        return Commands.slash(slashCommandDefaultI18nContext.get(declaration.name), buildDescription(slashCommandDefaultI18nContext, declaration.description, declaration.category)).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.isGuildOnly = declaration.isGuildOnly

            forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                setNameLocalization(discordLocale, i18nContext.get(declaration.name))
                setDescriptionLocalization(discordLocale, buildDescription(i18nContext, declaration.description, declaration.category))
            }

            if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty()) {
                if (declaration.executor != null)
                    error("Command ${declaration::class.simpleName} has a root executor, but it also has subcommand/subcommand groups!")

                for (subcommand in declaration.subcommands) {
                    subcommand(slashCommandDefaultI18nContext.get(subcommand.name), buildDescription(slashCommandDefaultI18nContext, subcommand.description, subcommand.category)) {
                        val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                        forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                            setNameLocalization(discordLocale, i18nContext.get(subcommand.name))
                            setDescriptionLocalization(discordLocale, buildDescription(i18nContext, subcommand.description, subcommand.category))
                        }

                        for (ref in executor.options.registeredOptions) {
                            addOptions(*createOption(ref).toTypedArray())
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
                                    addOptions(*createOption(ref).toTypedArray())
                                }
                            }
                        }
                    }
                }
            } else {
                val executor = declaration.executor

                if (executor != null) {
                    for (ref in executor.options.registeredOptions) {
                        addOptions(*createOption(ref).toTypedArray())
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

            forEachI18nContextWithValidLocale { discordLocale, i18nContext ->
                setNameLocalization(discordLocale, i18nContext.get(declaration.name))
            }
        }
    }

    /**
     * Converts a Discord InteraKTions [declaration] to JDA
     *
     * This is provided for backwards compatibility!
     */
    fun convertInteraKTionsDeclarationToJDA(declaration: ApplicationCommandDeclaration): CommandData {
        when (declaration) {
            is net.perfectdreams.discordinteraktions.common.commands.UserCommandDeclaration -> {
                return Commands.user(declaration.name).apply {
                    declaration.nameLocalizations?.mapKeysToJDALocales()
                        ?.also { setNameLocalizations(it) }
                    val defPermissions = declaration.defaultMemberPermissions
                    if (defPermissions != null)
                        defaultPermissions = DefaultMemberPermissions.enabledFor(defPermissions.code.value.toLong())
                    val dmPermission = declaration.dmPermission
                    if (dmPermission != null)
                        isGuildOnly = !dmPermission
                }
            }

            is net.perfectdreams.discordinteraktions.common.commands.MessageCommandDeclaration -> {
                return Commands.message(declaration.name).apply {
                    declaration.nameLocalizations?.mapKeysToJDALocales()
                        ?.also { setNameLocalizations(it) }
                    val defPermissions = declaration.defaultMemberPermissions
                    if (defPermissions != null)
                        defaultPermissions = DefaultMemberPermissions.enabledFor(defPermissions.code.value.toLong())
                    val dmPermission = declaration.dmPermission
                    if (dmPermission != null)
                        isGuildOnly = !dmPermission
                }
            }

            is net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclaration -> {
                return Commands.slash(declaration.name, declaration.description).apply {
                    declaration.nameLocalizations?.mapKeysToJDALocales()
                        ?.also { setNameLocalizations(it) }
                    declaration.descriptionLocalizations?.mapKeysToJDALocales()
                        ?.also { setDescriptionLocalizations(it) }
                    val defPermissions = declaration.defaultMemberPermissions
                    if (defPermissions != null)
                        defaultPermissions = DefaultMemberPermissions.enabledFor(defPermissions.code.value.toLong())
                    val dmPermission = declaration.dmPermission
                    if (dmPermission != null)
                        isGuildOnly = !dmPermission

                    // We can only have (subcommands OR subcommand groups) OR arguments
                    if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty()) {
                        for (subcommandDeclaration in declaration.subcommands) {
                            subcommand(subcommandDeclaration.name, subcommandDeclaration.description) {
                                val executor = subcommandDeclaration.executor

                                require(executor != null) { "Subcommand command without a executor!" }

                                subcommandDeclaration.nameLocalizations?.mapKeysToJDALocales()
                                    ?.also { setNameLocalizations(it) }
                                subcommandDeclaration.descriptionLocalizations?.mapKeysToJDALocales()
                                    ?.also { setDescriptionLocalizations(it) }

                                for (option in executor.options.registeredOptions) {
                                    addOptions(*createOption(option).toTypedArray())
                                }
                            }
                        }

                        for (subcommandGroupDeclaration in declaration.subcommandGroups) {
                            group(subcommandGroupDeclaration.name, subcommandGroupDeclaration.description) {
                                subcommandGroupDeclaration.nameLocalizations?.mapKeysToJDALocales()
                                    ?.also { setNameLocalizations(it) }
                                subcommandGroupDeclaration.descriptionLocalizations?.mapKeysToJDALocales()
                                    ?.also { setDescriptionLocalizations(it) }

                                for (subcommandDeclaration in subcommandGroupDeclaration.subcommands) {
                                    subcommand(subcommandDeclaration.name, subcommandDeclaration.description) {
                                        val executor = subcommandDeclaration.executor

                                        require(executor != null) { "Subcommand command without a executor!" }

                                        subcommandDeclaration.nameLocalizations?.mapKeysToJDALocales()
                                            ?.also { setNameLocalizations(it) }
                                        subcommandDeclaration.descriptionLocalizations?.mapKeysToJDALocales()
                                            ?.also { setDescriptionLocalizations(it) }

                                        for (option in executor.options.registeredOptions) {
                                            addOptions(*createOption(option).toTypedArray())
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val executor = declaration.executor

                        require(executor != null) { "Root command without a executor!" }

                        for (option in executor.options.registeredOptions) {
                            addOptions(*createOption(option).toTypedArray())
                        }
                    }
                }
            }
            is SlashCommandGroupDeclaration -> {
                error("This should never be called because the convertInteraKTionsDeclarationToJDA method is only called on a root command!")
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
                                if (interaKTionsOption.requiredRange != null) {
                                    setRequiredRange(interaKTionsOption.requiredRange.first, interaKTionsOption.requiredRange.last)
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

                    is UserDiscordOptionReference -> {
                        return listOf(
                            Option<User>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Converts a Discord InteraKTions [InteraKTionsCommandOption] to JDA
     *
     * This is provided for backwards compatibility!
     */
    private fun createOption(interaKTionsOption: InteraKTionsCommandOption<*>): List<OptionData> {
        when (interaKTionsOption) {
            is StringCommandOption -> {
                return listOf(
                    Option<String>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                        autocomplete = interaKTionsOption.autocompleteExecutor != null,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                        interaKTionsOption.minLength?.let {
                            if (it != 0)
                                setMinLength(it)
                        }
                        interaKTionsOption.maxLength?.let {
                            if (it != 0)
                                setMaxLength(it)
                        }

                        interaKTionsOption.choices?.forEach {
                            this.addChoices(
                                net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value)
                                    .apply {
                                        val localizedOptionNames = it.nameLocalizations?.mapKeysToJDALocales()
                                        if (localizedOptionNames != null)
                                            this.setNameLocalizations(localizedOptionNames)
                                    }
                            )
                        }
                    }
                )
            }
            is IntegerCommandOption -> {
                return listOf(
                    Option<Long>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                        autocomplete = interaKTionsOption.autocompleteExecutor != null,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                        interaKTionsOption.minValue?.let {
                            setMinValue(it)
                        }
                        interaKTionsOption.maxValue?.let {
                            setMaxValue(it)
                        }

                        interaKTionsOption.choices?.forEach {
                            this.addChoices(
                                net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value)
                                    .apply {
                                        val localizedOptionNames = it.nameLocalizations?.mapKeysToJDALocales()
                                        if (localizedOptionNames != null)
                                            this.setNameLocalizations(localizedOptionNames)
                                    }
                            )
                        }
                    }
                )
            }
            is NumberCommandOption -> {
                return listOf(
                    Option<Double>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                        autocomplete = interaKTionsOption.autocompleteExecutor != null,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                        interaKTionsOption.minValue?.let {
                            setMinValue(it)
                        }
                        interaKTionsOption.maxValue?.let {
                            setMaxValue(it)
                        }

                        interaKTionsOption.choices?.forEach {
                            this.addChoices(
                                net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value)
                                    .apply {
                                        val localizedOptionNames = it.nameLocalizations?.mapKeysToJDALocales()
                                        if (localizedOptionNames != null)
                                            this.setNameLocalizations(localizedOptionNames)
                                    }
                            )
                        }
                    }
                )
            }
            is BooleanCommandOption -> {
                return listOf(
                    Option<Boolean>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                )
            }
            is UserCommandOption -> {
                return listOf(
                    Option<User>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                )
            }
            is RoleCommandOption -> {
                return listOf(
                    Option<Role>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                )
            }
            is ChannelCommandOption -> {
                return listOf(
                    Option<Channel>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                )
            }
            is MentionableCommandOption -> {
                return listOf(
                    Option<IMentionable>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                )
            }
            is AttachmentCommandOption -> {
                return listOf(
                    Option<Attachment>(
                        name = interaKTionsOption.name,
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.required,
                    ) {
                        val localizedNames = interaKTionsOption.nameLocalizations?.mapKeysToJDALocales()
                        if (localizedNames != null)
                            this.setNameLocalizations(localizedNames)
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                )
            }
            is UserListCommandOption -> {
                return (1..interaKTionsOption.maximum).map {
                    Option<User>(
                        name = "${interaKTionsOption.name}$it",
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.minimum >= it,
                    ) {
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                }
            }
            is ImageReferenceCommandOption -> {
                return listOf(
                    Option<String>(
                        name = interaKTionsOption.name,
                        description = "User, URL or Emoji",
                        required = interaKTionsOption.required,
                    )
                )
            }
            is ImageReferenceOrAttachmentOption -> {
                return listOf(
                    Option<String>(
                        name = interaKTionsOption.name + "_data",
                        description = "User, URL or Emoji",
                        required = false,
                    ),
                    Option<Attachment>(
                        name = interaKTionsOption.name + "_attachment",
                        description = "Image Attachment",
                        required = false,
                    )
                )
            }
            is StringListCommandOption -> {
                return (1..interaKTionsOption.maximum).map {
                    Option<String>(
                        name = "${interaKTionsOption.name}$it",
                        description = interaKTionsOption.description,
                        required = interaKTionsOption.minimum >= it,
                    ) {
                        val localizedDescriptions = interaKTionsOption.descriptionLocalizations?.mapKeysToJDALocales()
                        if (localizedDescriptions != null)
                            this.setDescriptionLocalizations(localizedDescriptions)
                    }
                }
            }
            else -> error("Unknown Discord InteraKTions option type ${interaKTionsOption::class}")
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