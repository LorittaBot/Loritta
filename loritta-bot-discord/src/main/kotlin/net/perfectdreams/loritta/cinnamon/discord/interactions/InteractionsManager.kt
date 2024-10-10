package net.perfectdreams.loritta.cinnamon.discord.interactions

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.discordinteraktions.common.components.ButtonExecutor
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutor
import net.perfectdreams.discordinteraktions.common.modals.ModalExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonMessageCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonUserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.LorittaBot

class InteractionsManager(
    private val loritta: LorittaBot,
    val interaKTions: DiscordInteraKTions
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val discordConfig = loritta.config.loritta.discord

    val languageManager = loritta.languageManager

    fun register() {
        val publicLorittaCommands = PublicLorittaCommands(languageManager)
        publicLorittaCommands.commands().forEach {
            register(it)
        }

        /* commandManager.register(
            BomDiaECiaCommand,
            BomDiaECiaExecutor()
        ) */

        // ===[ IMAGES ]===

        // ===[ VIDEOS ]===

        // ===[ UTILS ]===

        // ===[ ECONOMY ]===
        // register(ChangeTransactionPageButtonClickExecutor(loritta))
        // register(ChangeTransactionFilterSelectMenuExecutor(loritta))

        // register(StartCoinFlipGlobalBetMatchmakingButtonClickExecutor(loritta))

        // register(TransferSonhosButtonExecutor(loritta))
        // register(CancelSonhosTransferButtonExecutor(loritta))

        // register(ChangeSonhosRankPageButtonExecutor(loritta))

        // ===[ ROBLOX ]===
    }

    private fun register(declarationWrapper: CinnamonSlashCommandDeclarationWrapper) {
        interaKTions.manager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(declarationWrapper: CinnamonUserCommandDeclarationWrapper) {
        interaKTions.manager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(declarationWrapper: CinnamonMessageCommandDeclarationWrapper) {
        interaKTions.manager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(executor: ButtonExecutor) {
        interaKTions.manager.register(executor)
    }

    private fun register(executor: SelectMenuExecutor) {
        interaKTions.manager.register(executor)
    }

    private fun register(executor: ModalExecutor) {
        interaKTions.manager.register(executor)
    }
}