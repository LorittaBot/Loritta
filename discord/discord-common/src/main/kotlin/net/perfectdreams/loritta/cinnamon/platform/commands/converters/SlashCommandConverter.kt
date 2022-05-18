package net.perfectdreams.loritta.cinnamon.platform.commands.converters

import dev.kord.common.Locale
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandGroupDeclaration
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandRegistry
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandOptionsWrapper
import net.perfectdreams.loritta.cinnamon.platform.utils.I18nContextUtils
import kotlin.reflect.KClass

class SlashCommandConverter(
    private val loritta: LorittaCinnamon,
    private val cinnamonCommandRegistry: CommandRegistry,
    private val interaKTionsManager: CommandManager
) {
    companion object {
        private const val MAX_COMMAND_DESCRIPTION_LENGTH = 100
    }

    private val declarationWrappers by cinnamonCommandRegistry::declarationWrappers
    private val executors by cinnamonCommandRegistry::executors

    fun convertCommandsToInteraKTions(defaultLocale: I18nContext) {
        for (declarationWrapper in declarationWrappers) {
            when (declarationWrapper) {
                is SlashCommandDeclarationWrapper -> {
                    val declaration = declarationWrapper.declaration()

                    val (interaKTionsDeclaration, executor) = convertSlashCommandDeclarationToInteraKTions(
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

    private fun convertSlashCommandDeclarationToInteraKTions(
        rootDeclarationClazz: KClass<*>,
        declaration: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclaration,
        declarationExecutor: SlashCommandExecutorDeclaration?,
        defaultLocale: I18nContext
    ): Pair<SlashCommandDeclaration, MutableList<SlashCommandExecutor>> {
        val executors = mutableListOf<SlashCommandExecutor>()

        val declaration = convertSlashCommandDeclarationToSlashCommand(
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

    private fun convertSlashCommandDeclarationToSlashCommand(
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
            convertSlashCommandDeclarationToSlashCommand(
                rootDeclarationClazz,
                it,
                it.executor!!,
                defaultLocale,
                createdExecutors
            )
        }
        val subcommandGroups = declaration.subcommandGroups.map {
            convertSlashSubcommandGroupToSlashCommand(
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

    private fun convertSlashSubcommandGroupToSlashCommand(
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
                convertSlashCommandDeclarationToSlashCommand(
                    rootDeclarationClazz,
                    it,
                    it.executor!!,
                    i18nContext,
                    createdExecutors
                )
            }
        )
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
            executor as UserCommandExecutor
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