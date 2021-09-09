package net.perfectdreams.loritta.cinnamon.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.loritta.cinnamon.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.commands.`fun`.*
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.*
import net.perfectdreams.loritta.cinnamon.commands.discord.UserAvatarExecutor
import net.perfectdreams.loritta.cinnamon.commands.discord.UserBannerExecutor
import net.perfectdreams.loritta.cinnamon.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.cinnamon.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.commands.images.*
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
import net.perfectdreams.loritta.cinnamon.commands.minecraft.*
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
    private val emotes = loritta.emotes
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
            UserAvatarExecutor(emotes, Snowflake(discordConfig.applicationId)),
            UserBannerExecutor(emotes, rest)
        )

        // ===[ FUN ]===
        commandManager.register(CoinFlipCommand, CoinFlipExecutor(emotes, random))
        commandManager.register(
            RateCommand,
            RateWaifuExecutor(emotes),
            RateHusbandoExecutor(emotes),
            RateLoliExecutor(emotes)
        )

        commandManager.register(
            ShipCommand,
            ShipExecutor(emotes, gabrielaImageServerClient, Snowflake(discordConfig.applicationId))
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

        commandManager.convertToInteraKTions(
            loritta.languageManager.getI18nContextById("en")
        )
    }
}