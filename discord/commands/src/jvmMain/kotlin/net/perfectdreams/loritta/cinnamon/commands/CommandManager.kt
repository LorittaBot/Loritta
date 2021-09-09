package net.perfectdreams.loritta.cinnamon.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.loritta.cinnamon.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.commands.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateHusbandoExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateLoliExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RollExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.ShipExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextClapExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextLowercaseExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextMockExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextUppercaseExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TioDoPaveExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.VieirinhaExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.ShipCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.SummonCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.cinnamon.commands.discord.UserAvatarExecutor
import net.perfectdreams.loritta.cinnamon.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.cinnamon.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.commands.images.ArtExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.MemeMakerExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PetPetExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.SAMExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.TrumpExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.WolverineFrameExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.DrakeCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.GetOverHereCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.InvertColorsCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.MemeMakerCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.NichijouYuukoPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.ToBeContinuedCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.WolverineFrameCommand
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McOfflineUUIDExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McUUIDExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.ECBManager
import net.perfectdreams.loritta.cinnamon.commands.utils.HelpExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.DictionaryCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.FansExplainingExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.FansExplainingCommand
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandRegistry
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
    private val gabrielaImageServerClient = GabrielaImageServerClient(loritta.servicesConfig.gabrielaImageServer.url, loritta.http)
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