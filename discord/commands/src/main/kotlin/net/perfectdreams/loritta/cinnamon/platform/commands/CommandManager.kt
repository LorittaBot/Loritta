package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.components.ButtonExecutor
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutor
import net.perfectdreams.discordinteraktions.common.modals.ModalExecutor
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.SwitchToGlobalAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.SwitchToGuildProfileAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.ShowGuildMemberPermissionsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.bet.StartCoinFlipGlobalBetMatchmakingButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.ChangeTransactionFilterSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.transactions.ChangeTransactionPageButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.PlayAudioClipButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.roblox.declarations.RobloxCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations.RoleplayCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.*
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.source.SourcePictureExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AchievementsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.*
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker.*
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.interactions.inviteblocker.ActivateInviteBlockerBypassButtonClickExecutor
import kotlin.system.exitProcess

class CommandManager(
    private val loritta: LorittaCinnamon,
    val interaKTionsManager: net.perfectdreams.discordinteraktions.common.commands.CommandManager
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val discordConfig = loritta.discordConfig
    private val servicesConfig = loritta.servicesConfig
    private val rest = loritta.rest

    private val random = loritta.random
    private val http = loritta.http

    val commandRegistry = CommandRegistry(
        loritta,
        interaKTionsManager,
        KordCommandRegistry(
            Snowflake(discordConfig.applicationId),
            rest,
            interaKTionsManager
        )
    )

    val languageManager = loritta.languageManager

    suspend fun register() {
        val publicLorittaCommands = PublicLorittaCommands(languageManager)
        publicLorittaCommands.commands().forEach {
            register(it)
        }

        // ===[ DISCORD ]===
        register(UserAvatarUserCommand(languageManager))
        register(SwitchToGuildProfileAvatarExecutor(loritta, Snowflake(discordConfig.applicationId)))
        register(SwitchToGlobalAvatarExecutor(loritta, Snowflake(discordConfig.applicationId)))

        register(UserInfoUserCommand(languageManager))
        register(ShowGuildMemberPermissionsExecutor(loritta))

        // ===[ FUN ]===
        register(PlayAudioClipButtonExecutor(loritta))

        /* commandManager.register(
            BomDiaECiaCommand,
            BomDiaECiaExecutor()
        ) */

        // ===[ IMAGES ]===

        // ===[ VIDEOS ]===

        // ===[ UTILS ]===
        register(FollowPackageButtonClickExecutor(loritta, loritta.correiosClient))
        register(UnfollowPackageButtonClickExecutor(loritta, loritta.correiosClient))
        register(SelectPackageSelectMenuExecutor(loritta))

        register(GoBackToPackageListButtonClickExecutor(loritta, loritta.correiosClient))
        register(TrackPackageButtonClickExecutor(loritta, loritta.correiosClient))

        // ===[ ECONOMY ]===
        register(ChangeTransactionPageButtonClickExecutor(loritta))
        register(ChangeTransactionFilterSelectMenuExecutor(loritta))

        register(StartCoinFlipGlobalBetMatchmakingButtonClickExecutor(loritta))

        // ===[ SOCIAL ]===
        register(AchievementsExecutor.ChangeCategoryMenuExecutor(loritta))

        // ===[ UNDERTALE ]===
        register(PortraitSelectMenuExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ChangeUniverseSelectMenuExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ChangeCharacterSelectMenuExecutor(loritta, loritta.gabrielaImageServerClient))

        register(ChangeDialogBoxTypeButtonClickExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ConfirmDialogBoxButtonClickExecutor(loritta, loritta.gabrielaImageServerClient))
        register(ChangeColorPortraitTypeButtonClickExecutor(loritta, loritta.gabrielaImageServerClient))

        // ===[ ROLEPLAY ]===
        register(RetributeHugButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(RetributeHeadPatButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(RetributeHighFiveButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(RetributeSlapButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(RetributeAttackButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(RetributeDanceButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(RetributeKissButtonExecutor(loritta, loritta.randomRoleplayPicturesClient))
        register(SourcePictureExecutor(loritta))

        // ===[ ROBLOX ]===

        // ===[ OTHER STUFF ]===
        register(ActivateInviteBlockerBypassButtonClickExecutor(loritta))

        // Validate if we don't have more commands than Discord allows
        if (commandRegistry.interaKTionsManager.applicationCommandsDeclarations.size > 100) {
            logger.error { "Currently there are ${commandRegistry.interaKTionsManager.applicationCommandsDeclarations.size} root commands registered, however Discord has a 100 root command limit! You need to remove some of the commands!" }
            exitProcess(1)
        }

        logger.info { "Total Root Commands: ${commandRegistry.interaKTionsManager.applicationCommandsDeclarations.size}/100" }

        commandRegistry.updateAllCommands()
    }

    private fun register(declarationWrapper: CinnamonSlashCommandDeclarationWrapper) {
        interaKTionsManager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(declarationWrapper: CinnamonUserCommandDeclarationWrapper) {
        interaKTionsManager.register(declarationWrapper.declaration().build(loritta))
    }

    private fun register(executor: ButtonExecutor) {
        interaKTionsManager.register(executor)
    }

    private fun register(executor: SelectMenuExecutor) {
        interaKTionsManager.register(executor)
    }

    private fun register(executor: ModalExecutor) {
        interaKTionsManager.register(executor)
    }
}