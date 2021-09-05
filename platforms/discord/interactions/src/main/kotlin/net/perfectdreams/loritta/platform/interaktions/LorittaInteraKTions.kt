package net.perfectdreams.loritta.platform.interaktions

import dev.kord.rest.service.RestClient
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.api.entities.Snowflake
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.loritta.commands.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.commands.`fun`.RateHusbandoExecutor
import net.perfectdreams.loritta.commands.`fun`.RateLoliExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.RollExecutor
import net.perfectdreams.loritta.commands.`fun`.ShipExecutor
import net.perfectdreams.loritta.commands.`fun`.TextClapExecutor
import net.perfectdreams.loritta.commands.`fun`.TextLowercaseExecutor
import net.perfectdreams.loritta.commands.`fun`.TextMockExecutor
import net.perfectdreams.loritta.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextUppercaseExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.commands.`fun`.TioDoPaveExecutor
import net.perfectdreams.loritta.commands.`fun`.VieirinhaExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.ShipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.SummonCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.commands.images.ArtExecutor
import net.perfectdreams.loritta.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.commands.images.DrakeExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.commands.images.MemeMakerExecutor
import net.perfectdreams.loritta.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.commands.images.PetPetExecutor
import net.perfectdreams.loritta.commands.images.RipTvExecutor
import net.perfectdreams.loritta.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.commands.images.SAMExecutor
import net.perfectdreams.loritta.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.commands.images.SustoExecutor
import net.perfectdreams.loritta.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.commands.images.TrumpExecutor
import net.perfectdreams.loritta.commands.images.WolverineFrameExecutor
import net.perfectdreams.loritta.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.commands.images.declarations.DrakeCommand
import net.perfectdreams.loritta.commands.images.declarations.GetOverHereCommand
import net.perfectdreams.loritta.commands.images.declarations.InvertColorsCommand
import net.perfectdreams.loritta.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.commands.images.declarations.MemeMakerCommand
import net.perfectdreams.loritta.commands.images.declarations.NichijouYuukoPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.commands.images.declarations.ToBeContinuedCommand
import net.perfectdreams.loritta.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.commands.images.declarations.WolverineFrameCommand
import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McOfflineUUIDExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.commands.minecraft.McUUIDExecutor
import net.perfectdreams.loritta.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.commands.utils.ECBManager
import net.perfectdreams.loritta.commands.utils.HelpExecutor
import net.perfectdreams.loritta.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.commands.utils.declarations.DictionaryCommand
import net.perfectdreams.loritta.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.commands.videos.FansExplainingExecutor
import net.perfectdreams.loritta.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.commands.videos.declarations.FansExplainingCommand
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.services.Services
import net.perfectdreams.loritta.common.utils.config.GabrielaImageServerConfig
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.LorittaDiscordConfig
import net.perfectdreams.loritta.discord.commands.discord.UserAvatarExecutor
import net.perfectdreams.loritta.discord.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.discord.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.platform.interaktions.commands.CommandManager
import net.perfectdreams.loritta.platform.interaktions.commands.ShipDiscordMentionInputConverter
import net.perfectdreams.loritta.platform.interaktions.commands.WaifuDiscordMentionInputConverter
import net.perfectdreams.loritta.platform.interaktions.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.platform.interaktions.webserver.InteractionsServer
import kotlin.system.exitProcess

class LorittaInteraKTions(
    config: LorittaConfig,
    discordConfig: LorittaDiscordConfig,
    val interactionsConfig: DiscordInteractionsConfig,
    override val services: Services,
    gabrielaImageServerConfig: GabrielaImageServerConfig,
    override val emotes: Emotes,
    val http: HttpClient
): LorittaDiscord(config, discordConfig) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val rest = RestClient(discordConfig.token)

    val interactions = InteractionsServer(
        rest = rest,
        applicationId = discordConfig.applicationId,
        publicKey = interactionsConfig.publicKey,
    )

    val kordCommandRegistry = KordCommandRegistry(
        Snowflake(discordConfig.applicationId),
        rest,
        interactions.commandManager
    )

    val commandManager = CommandManager(this, interactions.commandManager, kordCommandRegistry)
    val languageManager = LanguageManager(
        LorittaInteraKTions::class,
        "en",
        "/languages/"
    )

    val gabrielaImageServerClient = GabrielaImageServerClient(gabrielaImageServerConfig.url, http)

    fun start() {
        languageManager.loadLanguagesAndContexts()

        // ===[ DISCORD ]===
        commandManager.register(
            UserCommand,
            UserAvatarExecutor(emotes, discordConfig.applicationId),
            UserBannerExecutor(emotes, rest)
        )

        // ===[ FUN ]===
        commandManager.register(CoinFlipCommand, CoinFlipExecutor(emotes, random))
        val converter = WaifuDiscordMentionInputConverter()
        commandManager.register(
            RateCommand,
            RateWaifuExecutor(emotes, converter),
            RateHusbandoExecutor(emotes, converter),
            RateLoliExecutor(emotes)
        )

        commandManager.register(
            ShipCommand,
            ShipExecutor(emotes, ShipDiscordMentionInputConverter(), gabrielaImageServerClient, discordConfig.applicationId)
        )

        commandManager.register(CancelledCommand, CancelledExecutor(emotes))
        commandManager.register(
            SummonCommand,
            TioDoPaveExecutor(emotes),
            FaustaoExecutor(emotes),
            BemBoladaExecutor(emotes)
        )

        commandManager.register(VieirinhaCommand, VieirinhaExecutor(emotes))
        commandManager.register(RollCommand, RollExecutor(emotes, random))

        commandManager.register(
            HelpCommand,
            HelpExecutor(emotes)
        )

        commandManager.register(
            MinecraftCommand,
            McSkinExecutor(emotes, mojangApi),
            McAvatarExecutor(emotes, mojangApi),
            McHeadExecutor(emotes, mojangApi),
            McBodyExecutor(emotes, mojangApi),
            McOfflineUUIDExecutor(emotes),
            McUUIDExecutor(emotes, mojangApi)
        )

        commandManager.register(
            TextTransformDeclaration,
            TextVaporwaveExecutor(emotes),
            TextQualityExecutor(emotes),
            TextVaporQualityExecutor(emotes),
            TextVemDeZapExecutor(emotes, random),
            TextUppercaseExecutor(emotes),
            TextLowercaseExecutor(emotes),
            TextClapExecutor(emotes),
            TextMockExecutor(emotes)
        )

        commandManager.register(
            JankenponCommand, JankenponExecutor(random, emotes)
        )

        // ===[ IMAGES ]===
        commandManager.register(DrakeCommand, DrakeExecutor(emotes, gabrielaImageServerClient), BolsoDrakeExecutor(emotes, gabrielaImageServerClient), LoriDrakeExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(
            SonicCommand,
            KnuxThrowExecutor(emotes, gabrielaImageServerClient),
            ManiaTitleCardExecutor(emotes, gabrielaImageServerClient),
            StudiopolisTvExecutor(emotes, gabrielaImageServerClient)
        )
        commandManager.register(ArtCommand, ArtExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(
            BRMemesCommand,
            BolsonaroExecutor(emotes, gabrielaImageServerClient),
            Bolsonaro2Executor(emotes, gabrielaImageServerClient),
            MonicaAtaExecutor(emotes, gabrielaImageServerClient),
            ChicoAtaExecutor(emotes, gabrielaImageServerClient),
            LoriAtaExecutor(emotes, gabrielaImageServerClient),
            GessyAtaExecutor(emotes, gabrielaImageServerClient),
            EdnaldoBandeiraExecutor(emotes, gabrielaImageServerClient),
            EdnaldoTvExecutor(emotes, gabrielaImageServerClient),
            BolsoFrameExecutor(emotes, gabrielaImageServerClient),
            CanellaDvdExecutor(emotes, gabrielaImageServerClient),
            CortesFlowExecutor(emotes, gabrielaImageServerClient),
            SAMExecutor(emotes, gabrielaImageServerClient),
            CepoDeMadeiraExecutor(emotes, gabrielaImageServerClient),
            RomeroBrittoExecutor(emotes, gabrielaImageServerClient),
            BriggsCoverExecutor(emotes, gabrielaImageServerClient)
        )

        commandManager.register(BuckShirtCommand, BuckShirtExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(LoriSignCommand, LoriSignExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PetPetCommand, PetPetExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(WolverineFrameCommand, WolverineFrameExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(RipTvCommand, RipTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(SustoCommand, SustoExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(TrumpCommand, TrumpExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(ToBeContinuedCommand, ToBeContinuedExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(InvertColorsCommand, InvertColorsExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(MemeMakerCommand, MemeMakerExecutor(emotes, gabrielaImageServerClient))

        // ===[ VIDEOS ]===
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(FansExplainingCommand, FansExplainingExecutor(emotes, gabrielaImageServerClient))

        // ===[ UTILS ]===
        commandManager.register(MoneyCommand, MoneyExecutor(emotes, ECBManager()))
        commandManager.register(MorseCommand, MorseFromExecutor(emotes), MorseToExecutor(emotes))
        commandManager.register(DictionaryCommand, DictionaryExecutor(emotes, http), MorseToExecutor(emotes))
        commandManager.register(CalculatorCommand, CalculatorExecutor(emotes))
        commandManager.register(AnagramCommand, AnagramExecutor(emotes))
        commandManager.register(ChooseCommand, ChooseExecutor(emotes))

        // ===[ ECONOMY ]===
        commandManager.register(SonhosCommand, SonhosExecutor(emotes))

        // Validate if we don't have more commands than Discord allows
        if (commandManager.declarations.size > 100) {
            logger.error { "Currently there are ${commandManager.declarations.size} root commands registered, however Discord has a 100 root command limit! You need to remove some of the commands!" }
            exitProcess(1)
        }

        logger.info { "Total Root Commands: ${commandManager.declarations.size}/100" }

        runBlocking {
            commandManager.convertToInteraKTions(
                languageManager.getI18nContextById("en")
            )
        }

        interactions.start()
    }
}