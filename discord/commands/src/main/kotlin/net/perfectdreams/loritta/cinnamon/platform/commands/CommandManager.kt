package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import io.ktor.client.*
import io.ktor.client.plugins.*
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.components.ButtonExecutor
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutor
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
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
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.FalatronModelsManager
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
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.ECBManager
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker.*
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.*
import net.perfectdreams.loritta.cinnamon.platform.interactions.inviteblocker.ActivateInviteBlockerBypassButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
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

    private val gabrielaImageServerClient = GabrielaImageServerClient(
        loritta.servicesConfig.gabrielaImageServer.url,
        HttpClient {
            // Increase the default timeout for image generation, because some video generations may take too long to be generated
            install(HttpTimeout) {
                this.socketTimeoutMillis = 60_000
                this.requestTimeoutMillis = 60_000
                this.connectTimeoutMillis = 60_000
            }
        }
    )

    private val random = loritta.random
    private val http = loritta.http

    private val mojangApi = MinecraftMojangAPI()
    private val correiosClient = CorreiosClient()
    private val randomRoleplayPicturesClient = RandomRoleplayPicturesClient(loritta.servicesConfig.randomRoleplayPictures.url)
    private val falatronModelsManager = FalatronModelsManager().also {
        it.startUpdater()
    }
    private val ecbManager = ECBManager()

    val commandRegistry = CommandRegistry(
        loritta,
        interaKTionsManager,
        KordCommandRegistry(
            Snowflake(discordConfig.applicationId),
            rest,
            interaKTionsManager
        )
    )

    suspend fun register() {
        // ===[ DISCORD ]===
        register(UserCommand(loritta))

        register(UserAvatarUserCommand(loritta))
        register(SwitchToGuildProfileAvatarExecutor(loritta, Snowflake(discordConfig.applicationId)))
        register(SwitchToGlobalAvatarExecutor(loritta, Snowflake(discordConfig.applicationId)))

        register(UserInfoUserCommand(loritta, http))
        register(ShowGuildMemberPermissionsExecutor(loritta))

        register(ServerCommand(loritta))
        register(InviteCommand(loritta))
        register(EmojiCommand(loritta))
        register(WebhookCommand(loritta))
        register(LorittaCommand(loritta))

        // ===[ FUN ]===
        register(CoinFlipCommand(loritta))
        register(RateCommand(loritta))
        register(ShipCommand(loritta, gabrielaImageServerClient))
        register(CancelledCommand(loritta))
        register(SummonCommand(loritta))
        register(VieirinhaCommand(loritta))
        register(RollCommand(loritta))
        register(MinecraftCommand(loritta, mojangApi, gabrielaImageServerClient))
        register(TextTransformCommand(loritta))
        register(JankenponCommand(loritta))
        register(HungerGamesCommand(loritta))

        register(SoundboxCommand(loritta, falatronModelsManager))
        register(PlayAudioClipButtonExecutor(loritta))

        /* commandManager.register(
            BomDiaECiaCommand,
            BomDiaECiaExecutor()
        ) */

        // ===[ IMAGES ]===
        register(DrakeCommand(loritta, gabrielaImageServerClient))
        register(SonicCommand(loritta, gabrielaImageServerClient))
        register(ArtCommand(loritta, gabrielaImageServerClient))
        register(BobBurningPaperCommand(loritta, gabrielaImageServerClient))
        register(BRMemesCommand(loritta, gabrielaImageServerClient))
        register(BuckShirtCommand(loritta, gabrielaImageServerClient))
        register(LoriSignCommand(loritta, gabrielaImageServerClient))
        register(PassingPaperCommand(loritta, gabrielaImageServerClient))
        register(PepeDreamCommand(loritta, gabrielaImageServerClient))
        register(PetPetCommand(loritta, gabrielaImageServerClient))
        register(WolverineFrameCommand(loritta, gabrielaImageServerClient))
        register(RipTvCommand(loritta, gabrielaImageServerClient))
        register(SustoCommand(loritta, gabrielaImageServerClient))
        register(GetOverHereCommand(loritta, gabrielaImageServerClient))
        register(NichijouYuukoPaperCommand(loritta, gabrielaImageServerClient))
        register(TrumpCommand(loritta, gabrielaImageServerClient))
        register(TerminatorAnimeCommand(loritta, gabrielaImageServerClient))
        register(ToBeContinuedCommand(loritta, gabrielaImageServerClient))
        register(InvertColorsCommand(loritta, gabrielaImageServerClient))
        register(MemeMakerCommand(loritta, gabrielaImageServerClient))
        register(MarkMetaCommand(loritta, gabrielaImageServerClient))
        register(DrawnMaskCommand(loritta, gabrielaImageServerClient))
        register(SadRealityCommand(loritta, gabrielaImageServerClient))

        // ===[ VIDEOS ]===
        register(CarlyAaahCommand(loritta, gabrielaImageServerClient))
        register(AttackOnHeartCommand(loritta, gabrielaImageServerClient))
        register(FansExplainingCommand(loritta, gabrielaImageServerClient))
        register(GigaChadCommand(loritta, gabrielaImageServerClient))
        register(ChavesCommand(loritta, gabrielaImageServerClient))

        // ===[ UTILS ]===
        register(HelpCommand(loritta))
        register(MoneyCommand(loritta, ecbManager))
        register(MorseCommand(loritta))
        register(DictionaryCommand(loritta))
        register(CalculatorCommand(loritta))
        register(AnagramCommand(loritta))
        register(ChooseCommand(loritta))

        register(PackageCommand(loritta, correiosClient))
        register(FollowPackageButtonClickExecutor(loritta, correiosClient))
        register(UnfollowPackageButtonClickExecutor(loritta, correiosClient))
        register(SelectPackageSelectMenuExecutor(loritta))

        register(GoBackToPackageListButtonClickExecutor(loritta, correiosClient))
        register(TrackPackageButtonClickExecutor(loritta, correiosClient))

        register(ColorInfoCommand(loritta, gabrielaImageServerClient))
        register(NotificationsCommand(loritta))

        // ===[ ECONOMY ]===
        register(SonhosCommand(loritta))
        register(DailyCommand(loritta))
        register(BrokerCommand(loritta))

        register(TransactionsCommand(loritta))
        register(ChangeTransactionPageButtonClickExecutor(loritta))
        register(ChangeTransactionFilterSelectMenuExecutor(loritta))

        register(BetCommand(loritta))
        register(StartCoinFlipGlobalBetMatchmakingButtonClickExecutor(loritta))

        // ===[ SOCIAL ]===
        register(AchievementsCommand(loritta))
        register(AchievementsExecutor.ChangeCategoryMenuExecutor(loritta))

        register(AfkCommand(loritta))
        register(GenderCommand(loritta))

        // ===[ UNDERTALE ]===
        register(UndertaleCommand(loritta, gabrielaImageServerClient))
        register(PortraitSelectMenuExecutor(loritta, gabrielaImageServerClient))
        register(ChangeUniverseSelectMenuExecutor(loritta, gabrielaImageServerClient))
        register(ChangeCharacterSelectMenuExecutor(loritta, gabrielaImageServerClient))

        register(ChangeDialogBoxTypeButtonClickExecutor(loritta, gabrielaImageServerClient))
        register(ConfirmDialogBoxButtonClickExecutor(loritta, gabrielaImageServerClient))
        register(ChangeColorPortraitTypeButtonClickExecutor(loritta, gabrielaImageServerClient))

        // ===[ ROLEPLAY ]===
        register(RoleplayCommand(loritta, randomRoleplayPicturesClient))
        register(RetributeHugButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(RetributeHeadPatButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(RetributeHighFiveButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(RetributeSlapButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(RetributeAttackButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(RetributeDanceButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(RetributeKissButtonExecutor(loritta, randomRoleplayPicturesClient))
        register(SourcePictureExecutor(loritta))

        // ===[ ROBLOX ]===
        register(RobloxCommand(loritta))

        // ===[ OTHER STUFF ]===
        commandRegistry.register(
            ActivateInviteBlockerBypassButtonClickExecutor,
            ActivateInviteBlockerBypassButtonClickExecutor(loritta)
        )

        // Validate if we don't have more commands than Discord allows
        if (commandRegistry.interaKTionsManager.applicationCommandsDeclarations.size > 100) {
            logger.error { "Currently there are ${commandRegistry.interaKTionsManager.applicationCommandsDeclarations.size} root commands registered, however Discord has a 100 root command limit! You need to remove some of the commands!" }
            exitProcess(1)
        }

        logger.info { "Total Root Commands: ${commandRegistry.interaKTionsManager.applicationCommandsDeclarations.size}/100" }

        commandRegistry.updateAllCommands()
    }

    private fun register(declarationWrapper: CinnamonSlashCommandDeclarationWrapper) {
        interaKTionsManager.register(declarationWrapper.declaration().build())
    }

    private fun register(declarationWrapper: CinnamonUserCommandDeclarationWrapper) {
        interaKTionsManager.register(declarationWrapper.declaration().build())
    }

    private fun register(executor: ButtonExecutor) {
        interaKTionsManager.register(executor)
    }

    private fun register(executor: SelectMenuExecutor) {
        interaKTionsManager.register(executor)
    }
}