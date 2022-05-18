package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.CommandRegistry
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.converters.AutocompleteConverter
import net.perfectdreams.loritta.cinnamon.platform.commands.converters.ButtonConverter
import net.perfectdreams.loritta.cinnamon.platform.commands.converters.ModalSubmitConverter
import net.perfectdreams.loritta.cinnamon.platform.commands.converters.SelectMenuConverter
import net.perfectdreams.loritta.cinnamon.platform.commands.converters.SlashCommandConverter
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickBaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuBaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitWithDataExecutor

class CommandRegistry(
    val loritta: LorittaCinnamon,
    val interaKTionsManager: CommandManager,
    val interaKTionsRegistry: CommandRegistry
) {
    companion object {
        private val logger = KotlinLogging.logger {}
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

    private val slashCommandConverter = SlashCommandConverter(loritta, this, interaKTionsManager)
    private val selectMenuConverter = SelectMenuConverter(loritta, this, interaKTionsManager)
    private val buttonConverter = ButtonConverter(loritta, this, interaKTionsManager)
    private val autocompleteConverter = AutocompleteConverter(loritta, this, interaKTionsManager)
    private val modalSubmitConverter = ModalSubmitConverter(loritta, this, interaKTionsManager)

    suspend fun convertToInteraKTions(defaultLocale: I18nContext) {
        slashCommandConverter.convertCommandsToInteraKTions(defaultLocale)
        selectMenuConverter.convertSelectMenusToInteraKTions()
        buttonConverter.convertButtonsToInteraKTions()
        autocompleteConverter.convertAutocompleteToInteraKTions()
        modalSubmitConverter.convertModalSubmitToInteraKTions()

        if (loritta.interactionsConfig.registerGlobally) {
            interaKTionsRegistry.updateAllGlobalCommands()
        } else {
            for (guildId in loritta.interactionsConfig.guildsToBeRegistered) {
                interaKTionsRegistry.updateAllCommandsInGuild(Snowflake(guildId))
            }
        }
    }
}