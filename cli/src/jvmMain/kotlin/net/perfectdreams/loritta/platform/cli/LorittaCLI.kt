package net.perfectdreams.loritta.platform.cli

import io.ktor.client.*
import net.perfectdreams.loritta.commands.`fun`.*
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
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
            AvatarTestCommand,
            AvatarTestExecutor(http)
        )

        commandManager.register(
            AtaCommand, MonicaAtaExecutor(http), ChicoAtaExecutor(http), LoriAtaExecutor(http), GessyAtaExecutor(http)
        )

        commandManager.register(JankenponCommand, JankenponExecutor(this.random, this.emotes))
        commandManager.register(MorseCommand, MorseToExecutor(emotes), MorseFromExecutor(emotes))

        commandManager.register(ArtCommand, ArtExecutor(http))
        commandManager.register(BobBurningPaperCommand, BobBurningPaperExecutor(http))
        commandManager.register(BolsonaroCommand, BolsonaroExecutor(http), Bolsonaro2Executor(http), BolsoFrameExecutor(http))
        commandManager.register(BriggsCoverCommand, BriggsCoverExecutor(http))
        commandManager.register(BuckShirtCommand, BuckShirtExecutor(http))
        commandManager.register(CanellaDvdCommand, CanellaDvdExecutor(http))
        commandManager.register(EdnaldoCommand, EdnaldoBandeiraExecutor(http), EdnaldoTvExecutor(http))
        commandManager.register(LoriSignCommand, LoriSignExecutor(http))
        commandManager.register(PassingPaperCommand, PassingPaperExecutor(http))
        commandManager.register(PepeDreamCommand, PepeDreamExecutor(http))
        commandManager.register(PetPetCommand, PetPetExecutor(http))
        commandManager.register(QuadroCommand, QuadroExecutor(http))
        commandManager.register(RipTvCommand, RipTvExecutor(http))
        commandManager.register(RomeroBrittoCommand, RomeroBrittoExecutor(http))
        commandManager.register(StudiopolisTvCommand, StudiopolisTvExecutor(http))
        commandManager.register(SustoCommand, SustoExecutor(http))
        commandManager.register(AttackOnHeartCommand, AttackOnHeartExecutor(http))
        commandManager.register(CarlyAaahCommand, CarlyAaahExecutor(http))
        commandManager.register(CortesFlowCommand, CortesFlowExecutor(http))
        commandManager.register(KnuxThrowCommand, KnuxThrowExecutor(http))
        commandManager.register(CepoDeMadeiraCommand, CepoDeMadeiraExecutor(http))
        commandManager.register(GetOverHereCommand, GetOverHereExecutor(http))
        commandManager.register(NichijouYuukoPaperCommand, NichijouYuukoPaperExecutor(http))
        commandManager.register(TrumpCommand, TrumpExecutor(http))
        commandManager.register(TerminatorAnimeCommand, TerminatorAnimeExecutor(http))
    }

    suspend fun runArgs(args: Array<String>) {
        if (commandManager.matches(args.joinToString(" ")))
            return

        println("No matching command found!")
    }
}