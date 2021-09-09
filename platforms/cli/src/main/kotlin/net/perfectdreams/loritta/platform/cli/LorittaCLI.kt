package net.perfectdreams.loritta.cinnamon.platform.cli

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.cinnamon.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.commands.images.ArtExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.PetPetExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.QuadroExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.TrumpExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.AtaCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BolsonaroCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BriggsCoverCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.CanellaDvdCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.CepoDeMadeiraCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.CortesFlowCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.EdnaldoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.GetOverHereCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.KnuxThrowCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.ManiaTitleCardCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.NichijouYuukoPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.QuadroCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.RomeroBrittoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.StudiopolisTvCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.cinnamon.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.cinnamon.commands.minecraft.declarations.MinecraftPlayerCommand
import net.perfectdreams.loritta.cinnamon.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.cinnamon.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.cinnamon.commands.misc.PingExecutor
import net.perfectdreams.loritta.cinnamon.commands.misc.declarations.KkEaeMenCommand
import net.perfectdreams.loritta.cinnamon.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.CalculatorExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.ECBManager
import net.perfectdreams.loritta.cinnamon.commands.utils.HelpExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.MoneyExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.cinnamon.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.cinnamon.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.cinnamon.common.LorittaBot
import net.perfectdreams.loritta.cinnamon.common.locale.LocaleManager
import net.perfectdreams.loritta.cinnamon.common.memory.services.MemoryServices
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.common.utils.config.GabrielaImageServerConfig
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.perfectdreams.loritta.cinnamon.platform.cli.commands.CommandManager

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