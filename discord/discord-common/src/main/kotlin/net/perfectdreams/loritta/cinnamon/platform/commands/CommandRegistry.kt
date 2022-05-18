package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.CommandRegistry
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandGroupDeclaration
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.NumberAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.NumberAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.NumberAutocompleteExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandOptionsWrapper
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickBaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuBaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickWithDataExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuWithDataExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitWithDataExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.modals.components.ModalComponentsWrapper
import net.perfectdreams.loritta.cinnamon.platform.utils.I18nContextUtils
import kotlin.reflect.KClass

class CommandRegistry(
    val loritta: LorittaCinnamon,
    val interaKTionsManager: CommandManager,
    val interaKTionsRegistry: CommandRegistry
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_COMMAND_DESCRIPTION_LENGTH = 100
    }

    val declarationWrappers = mutableListOf<ApplicationCommandDeclarationWrapper>()
    val executors = mutableListOf<ApplicationCommandExecutor>()

    val selectMenusDeclarations = mutableListOf<SelectMenuExecutorDeclaration>()
    val selectMenusExecutors = mutableListOf<SelectMenuBaseExecutor>()

    val buttonsDeclarations = mutableListOf<ButtonClickExecutorDeclaration>()
    val buttonsExecutors = mutableListOf<ButtonClickBaseExecutor>()

    val autocompleteDeclarations = mutableListOf<AutocompleteExecutorDeclaration<*>>()
    val autocompleteExecutors = mutableListOf<AutocompleteExecutor<*>>()

    val modalSubmitDeclarations = mutableListOf<ModalSubmitExecutorDeclaration>()
    val modalSubmitExecutors = mutableListOf<ModalSubmitWithDataExecutor>()

    fun register(declaration: SlashCommandDeclarationWrapper, vararg executors: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor) {
        declarationWrappers.add(declaration)
        this.executors.addAll(executors)
    }

    fun register(declaration: UserCommandDeclarationWrapper, vararg executors: UserCommandExecutor) {
        declarationWrappers.add(declaration)
        this.executors.addAll(executors)
    }

    fun register(declaration: ButtonClickExecutorDeclaration, executor: ButtonClickBaseExecutor) {
        buttonsDeclarations.add(declaration)
        buttonsExecutors.add(executor)
    }

    fun register(declaration: SelectMenuExecutorDeclaration, executor: SelectMenuBaseExecutor) {
        selectMenusDeclarations.add(declaration)
        selectMenusExecutors.add(executor)
    }

    fun register(declaration: AutocompleteExecutorDeclaration<*>, executor: AutocompleteExecutor<*>) {
        autocompleteDeclarations.add(declaration)
        autocompleteExecutors.add(executor)
    }

    fun register(declaration: ModalSubmitExecutorDeclaration, executor: ModalSubmitWithDataExecutor) {
        modalSubmitDeclarations.add(declaration)
        modalSubmitExecutors.add(executor)
    }

    suspend fun convertToInteraKTions(defaultLocale: I18nContext) {
        convertCommandsToInteraKTions(defaultLocale)
        convertSelectMenusToInteraKTions()
        convertButtonsToInteraKTions()
        convertAutocompleteToInteraKTions()
        convertModalSubmitToInteraKTions()

        if (loritta.interactionsConfig.registerGlobally) {
            interaKTionsRegistry.updateAllGlobalCommands()
        } else {
            for (guildId in loritta.interactionsConfig.guildsToBeRegistered) {
                interaKTionsRegistry.updateAllCommandsInGuild(Snowflake(guildId))
            }
        }
    }

    private fun convertCommandsToInteraKTions(defaultLocale: I18nContext) {
        for (declarationWrapper in declarationWrappers) {
            when (declarationWrapper) {
                is SlashCommandDeclarationWrapper -> {
                    val declaration = declarationWrapper.declaration()

                    val (interaKTionsDeclaration, executor) = convertCommandDeclarationToInteraKTions(
                        declarationWrapper::class,
                        declaration,
                        declaration.executor,
                        defaultLocale
                    )

                    interaKTionsManager.register(interaKTionsDeclaration, *executor.toTypedArray())
                }
                is UserCommandDeclarationWrapper -> {
                    val declaration = declarationWrapper.declaration()

                    val (interaKTionsDeclaration, executor) = convertUserCommandDeclarationToInteraKTions(
                        declarationWrapper::class,
                        declaration,
                        declaration.executor
                    )

                    interaKTionsManager.register(interaKTionsDeclaration, executor)
                }
            }
        }
    }

    private fun convertSelectMenusToInteraKTions() {
        for (declaration in selectMenusDeclarations) {
            val executor = selectMenusExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The select menu executor wasn't found! Did you register the select menu executor?")

            if (executor is SelectMenuWithDataExecutor) {
                val interaKTionsExecutor = SelectMenuWithDataExecutorWrapper(
                    loritta,
                    declaration,
                    executor
                )

                val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutorDeclaration(
                    declaration::class,
                    declaration.id.value
                ) {}

                interaKTionsManager.register(
                    interaKTionsExecutorDeclaration,
                    interaKTionsExecutor
                )
            }
        }
    }

    private fun convertButtonsToInteraKTions() {
        for (declaration in buttonsDeclarations) {
            val executor = buttonsExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The button click executor wasn't found! Did you register the button click executor?")

            val interaKTionsExecutor = ButtonClickWithDataExecutorWrapper(
                loritta,
                declaration,
                executor as ButtonClickWithDataExecutor
            )

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration(
                declaration::class,
                declaration.id.value
            ) {}

            interaKTionsManager.register(
                interaKTionsExecutorDeclaration,
                interaKTionsExecutor
            )
        }
    }

    private fun convertAutocompleteToInteraKTions() {
        for (declaration in autocompleteDeclarations) {
            val executor = autocompleteExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The autocomplete executor wasn't found! Did you register the autocomplete executor?")

            // We use a class reference because we need to have a consistent signature, because we also use it on the SlashCommandOptionsWrapper class
            when (executor) {
                is StringAutocompleteExecutor -> {
                    val interaKTionsExecutor = StringAutocompleteExecutorWrapper(
                        loritta,
                        declaration as StringAutocompleteExecutorDeclaration,
                        executor
                    )

                    val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutorDeclaration(
                        declaration::class
                    ) {}

                    interaKTionsManager.register(
                        interaKTionsExecutorDeclaration,
                        interaKTionsExecutor
                    )
                }

                is IntegerAutocompleteExecutor -> {
                    val interaKTionsExecutor = IntegerAutocompleteExecutorWrapper(
                        loritta,
                        declaration as IntegerAutocompleteExecutorDeclaration,
                        executor
                    )

                    val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutorDeclaration(
                        declaration::class
                    ) {}

                    interaKTionsManager.register(
                        interaKTionsExecutorDeclaration,
                        interaKTionsExecutor
                    )
                }
                is NumberAutocompleteExecutor -> {
                    val interaKTionsExecutor = NumberAutocompleteExecutorWrapper(
                        loritta,
                        declaration as NumberAutocompleteExecutorDeclaration,
                        executor
                    )

                    val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutorDeclaration(
                        declaration::class
                    ) {}

                    interaKTionsManager.register(
                        interaKTionsExecutorDeclaration,
                        interaKTionsExecutor
                    )
                }
            }
        }
    }

    private fun convertModalSubmitToInteraKTions() {
        for (declaration in modalSubmitDeclarations) {
            val executor = modalSubmitExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The modal submit executor wasn't found! Did you register the modal submit executor?")

            // We use a class reference because we need to have a consistent signature, because we also use it on the SlashCommandOptionsWrapper class
            val interaKTionsExecutor = ModalSubmitWithDataExecutorWrapper(loritta, declaration, executor)

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutorDeclaration(
                declaration::class,
                declaration.id
            ) {
                override val options = ModalComponentsWrapper(declaration)
            }

            interaKTionsManager.register(
                interaKTionsExecutorDeclaration,
                interaKTionsExecutor
            )
        }
    }

    private fun convertUserCommandDeclarationToInteraKTions(
        rootDeclarationClazz: KClass<*>,
        declaration: UserCommandDeclaration,
        declarationExecutor: UserCommandExecutorDeclaration
    ): Pair<net.perfectdreams.discordinteraktions.common.commands.UserCommandDeclaration, UserCommandExecutorWrapper> {
        val executor = this.executors.firstOrNull { declarationExecutor.parent == it::class }
            ?: throw UnsupportedOperationException("The command executor ${declarationExecutor.parent} wasn't found! Did you register the command executor?")

        val interaKTionsExecutor = UserCommandExecutorWrapper(
            loritta,
            rootDeclarationClazz,
            declarationExecutor,
            executor as net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutor
        )

        val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.commands.UserCommandExecutorDeclaration(declarationExecutor::class) {}

        val interaKTionsDeclaration = net.perfectdreams.discordinteraktions.common.commands.UserCommandDeclaration(
            declaration.name,
            mapOf(),
            interaKTionsExecutorDeclaration
        )

        return Pair(
            interaKTionsDeclaration,
            interaKTionsExecutor
        )
    }

    private fun convertCommandDeclarationToInteraKTions(
        rootDeclarationClazz: KClass<*>,
        declaration: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclaration,
        declarationExecutor: SlashCommandExecutorDeclaration?,
        defaultLocale: I18nContext
    ): Pair<SlashCommandDeclaration, MutableList<SlashCommandExecutor>> {
        val executors = mutableListOf<SlashCommandExecutor>()

        val declaration = convertCommandDeclarationToSlashCommand(
            rootDeclarationClazz,
            declaration,
            declarationExecutor,
            defaultLocale,
            executors
        )

        return Pair(
            declaration,
            executors
        )
    }

    fun convertCommandDeclarationToSlashCommand(
        rootDeclarationClazz: KClass<*>,
        declaration: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclaration,
        declarationExecutor: SlashCommandExecutorDeclaration?,
        defaultLocale: I18nContext,
        createdExecutors: MutableList<SlashCommandExecutor>
    ): SlashCommandDeclaration {
        val label = declaration.name
        val description = buildDescription(defaultLocale, declaration.description, declaration.category)
        val localizedDescriptions = createLocalizedDescriptionMapExcludingDefaultLocale(defaultLocale, declaration.description, declaration.category)
        val subcommands = declaration.subcommands.map {
            convertCommandDeclarationToSlashCommand(
                rootDeclarationClazz,
                it,
                it.executor!!,
                defaultLocale,
                createdExecutors
            )
        }
        val subcommandGroups = declaration.subcommandGroups.map {
            convertSubcommandGroupToSlashCommand(
                rootDeclarationClazz,
                defaultLocale,
                declaration,
                it,
                createdExecutors,
                defaultLocale
            )
        }

        if (declarationExecutor != null) {
            val executor = executors.firstOrNull { declarationExecutor.parent == it::class }
                ?: throw UnsupportedOperationException("The command executor ${declarationExecutor.parent} wasn't found! Did you register the command executor?")

            val interaKTionsExecutor = SlashCommandExecutorWrapper(
                loritta,
                rootDeclarationClazz,
                declarationExecutor,
                executor as net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
            )

            // Register all the command options with Discord InteraKTions
            val interaKTionsOptions = SlashCommandOptionsWrapper(
                declarationExecutor,
                loritta.languageManager,
                defaultLocale
            )

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration(declarationExecutor::class) {
                override val options = interaKTionsOptions
            }

            createdExecutors.add(interaKTionsExecutor)

            return SlashCommandDeclaration(
                label,
                null,
                description,
                localizedDescriptions,
                interaKTionsExecutorDeclaration,
                subcommands,
                subcommandGroups
            )
        } else {
            return SlashCommandDeclaration(
                label,
                null,
                description,
                localizedDescriptions,
                null,
                subcommands,
                subcommandGroups
            )
        }
    }

    private fun convertSubcommandGroupToSlashCommand(
        rootDeclarationClazz: KClass<*>,
        defaultLocale: I18nContext,
        rootDeclaration: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclaration,
        declaration: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandGroupDeclaration,
        createdExecutors: MutableList<SlashCommandExecutor>,
        i18nContext: I18nContext
    ): SlashCommandGroupDeclaration {
        return SlashCommandGroupDeclaration(
            declaration.name,
            null,
            defaultLocale.get(declaration.description).shortenWithEllipsis(MAX_COMMAND_DESCRIPTION_LENGTH),
            createLocalizedDescriptionMapExcludingDefaultLocale(defaultLocale, declaration.description, rootDeclaration.category),
            declaration.subcommands.map {
                convertCommandDeclarationToSlashCommand(
                    rootDeclarationClazz,
                    it,
                    it.executor!!,
                    i18nContext,
                    createdExecutors
                )
            }
        )
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
    }.shortenWithEllipsis(MAX_COMMAND_DESCRIPTION_LENGTH)

    /**
     * Creates a map containing all translated strings of [i18nKey], excluding the [defaultLocale].
     *
     * @param defaultLocale the default locale used when creating the slash commands, this won't be present in the map.
     * @param i18nKey the key
     */
    private fun createLocalizedDescriptionMapExcludingDefaultLocale(defaultLocale: I18nContext, description: StringI18nData, category: CommandCategory) = mutableMapOf<Locale, String>().apply {
        // We will ignore the default i18nContext because that would be redundant
        for ((languageId, i18nContext) in loritta.languageManager.languageContexts.filter { it.value != defaultLocale }) {
            if (i18nContext.language.textBundle.strings.containsKey(description.key.key)) {
                val kordLocale = I18nContextUtils.convertLanguageIdToKordLocale(languageId)
                if (kordLocale != null)
                    this[kordLocale] = buildDescription(i18nContext, description, category)
            }
        }
    }
}