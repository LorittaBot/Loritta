package net.perfectdreams.loritta.platform.cli

import io.ktor.client.*
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.commands.images.ArtExecutor
import net.perfectdreams.loritta.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.commands.images.PetPetExecutor
import net.perfectdreams.loritta.commands.images.QuadroExecutor
import net.perfectdreams.loritta.commands.images.RipTvExecutor
import net.perfectdreams.loritta.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.commands.images.SustoExecutor
import net.perfectdreams.loritta.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.commands.images.TrumpExecutor
import net.perfectdreams.loritta.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.commands.images.declarations.AtaCommand
import net.perfectdreams.loritta.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.BolsonaroCommand
import net.perfectdreams.loritta.commands.images.declarations.BriggsCoverCommand
import net.perfectdreams.loritta.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.commands.images.declarations.CanellaDvdCommand
import net.perfectdreams.loritta.commands.images.declarations.CepoDeMadeiraCommand
import net.perfectdreams.loritta.commands.images.declarations.CortesFlowCommand
import net.perfectdreams.loritta.commands.images.declarations.EdnaldoCommand
import net.perfectdreams.loritta.commands.images.declarations.GetOverHereCommand
import net.perfectdreams.loritta.commands.images.declarations.KnuxThrowCommand
import net.perfectdreams.loritta.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.commands.images.declarations.ManiaTitleCardCommand
import net.perfectdreams.loritta.commands.images.declarations.NichijouYuukoPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.commands.images.declarations.QuadroCommand
import net.perfectdreams.loritta.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.commands.images.declarations.RomeroBrittoCommand
import net.perfectdreams.loritta.commands.images.declarations.StudiopolisTvCommand
import net.perfectdreams.loritta.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.commands.minecraft.declarations.MinecraftPlayerCommand
import net.perfectdreams.loritta.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.commands.misc.declarations.KkEaeMenCommand
import net.perfectdreams.loritta.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.commands.utils.ECBManager
import net.perfectdreams.loritta.commands.utils.HelpExecutor
import net.perfectdreams.loritta.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.memory.services.MemoryServices
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.common.utils.config.GabrielaImageServerConfig
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.common.utils.minecraft.MinecraftMojangAPI
import net.perfectdreams.loritta.platform.cli.commands.CommandManager

class LorittaCLI(
    config: LorittaConfig,
    gabrielaImageServerConfig: GabrielaImageServerConfig
): LorittaBot(config) {
    val commandManager = CommandManager(this)
    val localeManager = LocaleManager(
        ConfigUtils.localesFolder
    )

    val mojangApi = MinecraftMojangAPI()

    val http = HttpClient {
        expectSuccess = false
    }

    override val services = MemoryServices()
    
    val gabrielaImageServerClient = GabrielaImageServerClient(gabrielaImageServerConfig.url, http)

    fun start() {
        localeManager.loadLocales()

        commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor(emotes)
        )

        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor(emotes, random)
        )

        commandManager.register(
            RateWaifuCommand,
            RateWaifuExecutor(emotes)
        )

        commandManager.register(
            CalculatorCommand,
            CalculatorExecutor(emotes)
        )

        commandManager.register(
            AnagramCommand,
            AnagramExecutor(emotes)
        )

        commandManager.register(
            MoneyCommand,
            MoneyExecutor(emotes, ECBManager())
        )

        commandManager.register(
            ChooseCommand,
            ChooseExecutor(emotes)
        )

        commandManager.register(
            MinecraftPlayerCommand,
            McSkinExecutor(emotes, mojangApi),
            McAvatarExecutor(emotes, mojangApi),
            McHeadExecutor(emotes, mojangApi),
            McBodyExecutor(emotes, mojangApi)
        )

        commandManager.register(
            TextTransformDeclaration,
            TextVaporwaveExecutor(emotes),
            TextQualityExecutor(emotes),
            TextVaporQualityExecutor(emotes),
            TextVemDeZapExecutor(emotes, random)
        )

        commandManager.register(
            KkEaeMenCommand,
            KkEaeMenExecutor(emotes)
        )

        commandManager.register(
            ManiaTitleCardCommand,
            ManiaTitleCardExecutor(emotes, gabrielaImageServerClient)
        )

        commandManager.register(
            HelpCommand,
            HelpExecutor(emotes)
        )

        commandManager.register(
            AtaCommand, MonicaAtaExecutor(emotes, gabrielaImageServerClient), ChicoAtaExecutor(emotes, gabrielaImageServerClient), LoriAtaExecutor(emotes, gabrielaImageServerClient), GessyAtaExecutor(emotes, gabrielaImageServerClient)
        )

        commandManager.register(JankenponCommand, JankenponExecutor(this.random, this.emotes))
        commandManager.register(MorseCommand, MorseToExecutor(emotes), MorseFromExecutor(emotes))

        commandManager.register(ArtCommand, ArtExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BolsonaroCommand, BolsonaroExecutor(emotes, gabrielaImageServerClient), Bolsonaro2Executor(emotes, gabrielaImageServerClient), BolsoFrameExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BriggsCoverCommand, BriggsCoverExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(BuckShirtCommand, BuckShirtExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(CanellaDvdCommand, CanellaDvdExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(EdnaldoCommand, EdnaldoBandeiraExecutor(emotes, gabrielaImageServerClient), EdnaldoTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(LoriSignCommand, LoriSignExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(PetPetCommand, PetPetExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(QuadroCommand, QuadroExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(RipTvCommand, RipTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(RomeroBrittoCommand, RomeroBrittoExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(StudiopolisTvCommand, StudiopolisTvExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(SustoCommand, SustoExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(CortesFlowCommand, CortesFlowExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(KnuxThrowCommand, KnuxThrowExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(CepoDeMadeiraCommand, CepoDeMadeiraExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(TrumpCommand, TrumpExecutor(emotes, gabrielaImageServerClient))
        commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(emotes, gabrielaImageServerClient))

        commandManager.register(SonhosCommand, SonhosExecutor(emotes))

    }

    suspend fun runArgs(args: Array<String>) {
        if (commandManager.matches(args.joinToString(" ")))
            return

        println("No matching command found!")
    }
}