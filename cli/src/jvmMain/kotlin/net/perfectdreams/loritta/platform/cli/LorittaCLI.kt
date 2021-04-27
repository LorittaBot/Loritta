package net.perfectdreams.loritta.platform.cli

import io.ktor.client.*
import net.perfectdreams.loritta.commands.`fun`.*
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.commands.images.*
import net.perfectdreams.loritta.commands.images.declarations.*
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
import net.perfectdreams.loritta.commands.utils.*
import net.perfectdreams.loritta.commands.utils.declarations.*
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.memory.services.MemoryServices
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.common.utils.minecraft.MinecraftMojangAPI
import net.perfectdreams.loritta.platform.cli.commands.CommandManager

class LorittaCLI(config: LorittaConfig): LorittaBot(config) {
    val commandManager = CommandManager(this)
    val localeManager = LocaleManager(
        ConfigUtils.localesFolder
    )

    val mojangApi = MinecraftMojangAPI()

    val http = HttpClient {
        expectSuccess = false
    }

    override val services = MemoryServices()

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
            ManiaTitleCardExecutor(http)
        )

        commandManager.register(
            HelpCommand,
            HelpExecutor(emotes)
        )

        commandManager.register(
            AtaCommand, MonicaAtaExecutor(emotes, http), ChicoAtaExecutor(emotes, http), LoriAtaExecutor(emotes, http), GessyAtaExecutor(emotes, http)
        )

        commandManager.register(JankenponCommand, JankenponExecutor(this.random, this.emotes))
        commandManager.register(MorseCommand, MorseToExecutor(emotes), MorseFromExecutor(emotes))

        commandManager.register(ArtCommand, ArtExecutor(emotes, http))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(emotes, http))
        commandManager.register(BolsonaroCommand, BolsonaroExecutor(emotes, http), Bolsonaro2Executor(emotes, http), BolsoFrameExecutor(emotes, http))
        commandManager.register(BriggsCoverCommand, BriggsCoverExecutor(emotes, http))
        commandManager.register(BuckShirtCommand, BuckShirtExecutor(emotes, http))
        commandManager.register(CanellaDvdCommand, CanellaDvdExecutor(emotes, http))
        commandManager.register(EdnaldoCommand, EdnaldoBandeiraExecutor(emotes, http), EdnaldoTvExecutor(emotes, http))
        commandManager.register(LoriSignCommand, LoriSignExecutor(emotes, http))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(emotes, http))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(emotes, http))
        commandManager.register(PetPetCommand, PetPetExecutor(emotes, http))
        commandManager.register(QuadroCommand, QuadroExecutor(emotes, http))
        commandManager.register(RipTvCommand, RipTvExecutor(emotes, http))
        commandManager.register(RomeroBrittoCommand, RomeroBrittoExecutor(emotes, http))
        commandManager.register(StudiopolisTvCommand, StudiopolisTvExecutor(emotes, http))
        commandManager.register(SustoCommand, SustoExecutor(emotes, http))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(emotes, http))
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(emotes, http))
        commandManager.register(CortesFlowCommand, CortesFlowExecutor(http))
        commandManager.register(KnuxThrowCommand, KnuxThrowExecutor(emotes, http))
        commandManager.register(CepoDeMadeiraCommand, CepoDeMadeiraExecutor(emotes, http))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(emotes, http))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(emotes, http))
        commandManager.register(TrumpCommand, TrumpExecutor(emotes, http))
        commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(http))

        commandManager.register(SonhosCommand, SonhosExecutor(emotes))

    }

    suspend fun runArgs(args: Array<String>) {
        if (commandManager.matches(args.joinToString(" ")))
            return

        println("No matching command found!")
    }
}