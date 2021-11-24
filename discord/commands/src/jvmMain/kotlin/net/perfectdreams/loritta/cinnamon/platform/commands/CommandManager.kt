package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.HungerGamesExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.RateHusbandoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.RateLoliExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.RollExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.ShipExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextClapExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextLowercaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextMockExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextUppercaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TioDoPaveExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.VieirinhaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.HungerGamesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.ShipCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.SummonCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ChannelInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.EmojiInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.InviteInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.RoleInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ServerBannerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ServerIconExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ServerSplashExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.UserAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.EmojiCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookEditJsonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookEditRepostExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookEditSimpleExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookSendJsonExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookSendRepostExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.WebhookSendSimpleExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.InviteCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.ServerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ArtExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MarkMetaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MemeMakerExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.PetPetExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.SAMExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.TrumpExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.WolverineFrameExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.DrakeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.GetOverHereCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.InvertColorsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.MarkMetaCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.MemeMakerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.NichijouYuukoPaperCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.ToBeContinuedCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.WolverineFrameCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.McOfflineUUIDExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.McUUIDExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.ClearExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.declarations.ClearCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AchievementsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.CustomTextBoxExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeCharacterSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeColorPortraitTypeButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeDialogBoxTypeButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ChangeUniverseSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.ConfirmDialogBoxButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.PortraitSelectMenuExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.ECBManager
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.HelpExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.DictionaryCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.FansExplainingExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.FansExplainingCommand
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import kotlin.system.exitProcess

class CommandManager(
    private val loritta: LorittaCinnamon,
    interaKTionsManager: net.perfectdreams.discordinteraktions.common.commands.CommandManager
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val discordConfig = loritta.discordConfig
    private val servicesConfig = loritta.servicesConfig
    private val rest = loritta.rest

    private val gabrielaImageServerClient = net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient(loritta.servicesConfig.gabrielaImageServer.url, loritta.http)
    private val random = loritta.random
    private val http = loritta.http

    private val mojangApi = MinecraftMojangAPI()

    val commandManager = CommandRegistry(
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
        commandManager.register(
            UserCommand,
            UserAvatarExecutor(Snowflake(discordConfig.applicationId)),
            UserBannerExecutor(rest)
        )

        commandManager.register(
            ServerCommand,
            ServerIconExecutor(rest),
            ServerBannerExecutor(rest),
            ServerSplashExecutor(rest),
            RoleInfoExecutor(rest),
            ChannelInfoExecutor(rest)
        )

        commandManager.register(
            InviteCommand,
            InviteInfoExecutor(rest)
        )

        commandManager.register(
            EmojiCommand,
            EmojiInfoExecutor(rest)
        )

        commandManager.register(
            WebhookCommand,
            WebhookSendSimpleExecutor(rest),
            WebhookSendJsonExecutor(rest),
            WebhookSendRepostExecutor(rest),
            WebhookEditSimpleExecutor(rest),
            WebhookEditJsonExecutor(rest),
            WebhookEditRepostExecutor(rest)
        )

        // ===[ FUN ]===
        commandManager.register(CoinFlipCommand, CoinFlipExecutor(random))
        commandManager.register(
            RateCommand,
            RateWaifuExecutor(),
            RateHusbandoExecutor(),
            RateLoliExecutor()
        )

        commandManager.register(
            ShipCommand,
            ShipExecutor(gabrielaImageServerClient, Snowflake(discordConfig.applicationId))
        )

        commandManager.register(CancelledCommand, CancelledExecutor())
        commandManager.register(
            SummonCommand,
            TioDoPaveExecutor(),
            FaustaoExecutor(),
            BemBoladaExecutor()
        )

        commandManager.register(VieirinhaCommand, VieirinhaExecutor())
        commandManager.register(RollCommand, RollExecutor(random))

        commandManager.register(
            HelpCommand,
            HelpExecutor()
        )

        commandManager.register(
            MinecraftCommand,
            McSkinExecutor(mojangApi),
            McAvatarExecutor(mojangApi),
            McHeadExecutor(mojangApi),
            McBodyExecutor(mojangApi),
            McOfflineUUIDExecutor(),
            McUUIDExecutor(mojangApi)
        )

        commandManager.register(
            TextTransformDeclaration,
            TextVaporwaveExecutor(),
            TextQualityExecutor(),
            TextVaporQualityExecutor(),
            TextVemDeZapExecutor(random),
            TextUppercaseExecutor(),
            TextLowercaseExecutor(),
            TextClapExecutor(),
            TextMockExecutor()
        )

        commandManager.register(
            JankenponCommand, JankenponExecutor(random)
        )

        commandManager.register(
            HungerGamesCommand,
            HungerGamesExecutor(rest)
        )

        // ===[ IMAGES ]===
        commandManager.register(DrakeCommand, DrakeExecutor(gabrielaImageServerClient), BolsoDrakeExecutor(gabrielaImageServerClient), LoriDrakeExecutor(gabrielaImageServerClient))
        commandManager.register(
            SonicCommand,
            KnuxThrowExecutor(gabrielaImageServerClient),
            ManiaTitleCardExecutor(gabrielaImageServerClient),
            StudiopolisTvExecutor(gabrielaImageServerClient)
        )
        commandManager.register(ArtCommand, ArtExecutor(gabrielaImageServerClient))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(gabrielaImageServerClient))
        commandManager.register(
            BRMemesCommand,
            BolsonaroExecutor(gabrielaImageServerClient),
            Bolsonaro2Executor(gabrielaImageServerClient),
            MonicaAtaExecutor(gabrielaImageServerClient),
            ChicoAtaExecutor(gabrielaImageServerClient),
            LoriAtaExecutor(gabrielaImageServerClient),
            GessyAtaExecutor(gabrielaImageServerClient),
            EdnaldoBandeiraExecutor(gabrielaImageServerClient),
            EdnaldoTvExecutor(gabrielaImageServerClient),
            BolsoFrameExecutor(gabrielaImageServerClient),
            CanellaDvdExecutor(gabrielaImageServerClient),
            CortesFlowExecutor(gabrielaImageServerClient),
            SAMExecutor(gabrielaImageServerClient),
            CepoDeMadeiraExecutor(gabrielaImageServerClient),
            RomeroBrittoExecutor(gabrielaImageServerClient),
            BriggsCoverExecutor(gabrielaImageServerClient)
        )

        commandManager.register(BuckShirtCommand, BuckShirtExecutor(gabrielaImageServerClient))
        commandManager.register(LoriSignCommand, LoriSignExecutor(gabrielaImageServerClient))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(gabrielaImageServerClient))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(gabrielaImageServerClient))
        commandManager.register(PetPetCommand, PetPetExecutor(gabrielaImageServerClient))
        commandManager.register(WolverineFrameCommand, WolverineFrameExecutor(gabrielaImageServerClient))
        commandManager.register(RipTvCommand, RipTvExecutor(gabrielaImageServerClient))
        commandManager.register(SustoCommand, SustoExecutor(gabrielaImageServerClient))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(gabrielaImageServerClient))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(gabrielaImageServerClient))
        commandManager.register(TrumpCommand, TrumpExecutor(gabrielaImageServerClient))
        commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(gabrielaImageServerClient))
        commandManager.register(ToBeContinuedCommand, ToBeContinuedExecutor(gabrielaImageServerClient))
        commandManager.register(InvertColorsCommand, InvertColorsExecutor(gabrielaImageServerClient))
        commandManager.register(MemeMakerCommand, MemeMakerExecutor(gabrielaImageServerClient))
        commandManager.register(MarkMetaCommand, MarkMetaExecutor(gabrielaImageServerClient))

        // ===[ MODERATION ]===
        commandManager.register(
            ClearCommand,
            ClearExecutor(rest)
        )

        // ===[ VIDEOS ]===
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(gabrielaImageServerClient))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(gabrielaImageServerClient))
        commandManager.register(FansExplainingCommand, FansExplainingExecutor(gabrielaImageServerClient))

        // ===[ UTILS ]===
        commandManager.register(MoneyCommand, MoneyExecutor(ECBManager()))
        commandManager.register(MorseCommand, MorseFromExecutor(), MorseToExecutor())
        commandManager.register(DictionaryCommand, DictionaryExecutor(http), MorseToExecutor())
        commandManager.register(CalculatorCommand, CalculatorExecutor())
        commandManager.register(AnagramCommand, AnagramExecutor())
        commandManager.register(ChooseCommand, ChooseExecutor())

        // ===[ ECONOMY ]===
        commandManager.register(SonhosCommand, SonhosExecutor())

        // ===[ SOCIAL ]===
        commandManager.register(
            AchievementsCommand,
            AchievementsExecutor()
        )

        commandManager.register(
            AchievementsExecutor.ChangeCategoryMenuExecutor,
            AchievementsExecutor.ChangeCategoryMenuExecutor(loritta)
        )

        // ===[ UNDERTALE ]===
        commandManager.register(
            UndertaleCommand,
            CustomTextBoxExecutor(gabrielaImageServerClient),
            TextBoxExecutor(gabrielaImageServerClient),
        )

        commandManager.register(
            PortraitSelectMenuExecutor,
            PortraitSelectMenuExecutor(loritta, gabrielaImageServerClient)
        )

        commandManager.register(
            ChangeUniverseSelectMenuExecutor,
            ChangeUniverseSelectMenuExecutor(loritta, gabrielaImageServerClient)
        )

        commandManager.register(
            ChangeCharacterSelectMenuExecutor,
            ChangeCharacterSelectMenuExecutor(loritta, gabrielaImageServerClient)
        )

        commandManager.register(
            ChangeDialogBoxTypeButtonClickExecutor,
            ChangeDialogBoxTypeButtonClickExecutor(loritta, gabrielaImageServerClient)
        )

        commandManager.register(
            ConfirmDialogBoxButtonClickExecutor,
            ConfirmDialogBoxButtonClickExecutor(loritta, gabrielaImageServerClient)
        )

        commandManager.register(
            ChangeColorPortraitTypeButtonClickExecutor,
            ChangeColorPortraitTypeButtonClickExecutor(loritta, gabrielaImageServerClient)
        )

        // Validate if we don't have more commands than Discord allows
        if (commandManager.declarations.size > 100) {
            logger.error { "Currently there are ${commandManager.declarations.size} root commands registered, however Discord has a 100 root command limit! You need to remove some of the commands!" }
            exitProcess(1)
        }

        logger.info { "Total Root Commands: ${commandManager.declarations.size}/100" }

        commandManager.convertToInteraKTions(
            loritta.languageManager.getI18nContextById("en")
        )
    }
}