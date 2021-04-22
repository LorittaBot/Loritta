package net.perfectdreams.loritta.platform.interaktions

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVemDeZapExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.commands.discord.AvatarExecutor
import net.perfectdreams.loritta.commands.discord.declarations.AvatarCommand
import net.perfectdreams.loritta.commands.images.*
import net.perfectdreams.loritta.commands.images.declarations.*
import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.commands.minecraft.declarations.MinecraftPlayerCommand
import net.perfectdreams.loritta.commands.misc.KkEaeMenExecutor
import net.perfectdreams.loritta.commands.misc.declarations.KkEaeMenCommand
import net.perfectdreams.loritta.commands.utils.*
import net.perfectdreams.loritta.commands.utils.declarations.*
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.LorittaDiscordConfig
import net.perfectdreams.loritta.platform.interaktions.commands.CommandManager
import net.perfectdreams.loritta.platform.interaktions.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.platform.interaktions.webserver.InteractionsServer

class LorittaInteraKTions(
    config: LorittaConfig,
    discordConfig: LorittaDiscordConfig,
    val interactionsConfig: DiscordInteractionsConfig,
    override val emotes: Emotes
): LorittaDiscord(config, discordConfig) {
    val interactions = InteractionsServer(
        applicationId = discordConfig.applicationId,
        publicKey = interactionsConfig.publicKey,
        token = discordConfig.token
    )

    val commandManager = CommandManager(this, interactions.commandManager)

    val localeManager = LocaleManager(
        ConfigUtils.localesFolder
    )

    val http = HttpClient {
        expectSuccess = false
    }

    fun start() {
        localeManager.loadLocales()

        /* commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor(emotes)
        ) */

        // ===[ DISCORD ]===
        commandManager.register(
            AvatarCommand,
            AvatarExecutor()
        )

        // ===[ FUN ]===
        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor(emotes, random)
        )

        commandManager.register(
            RateWaifuCommand,
            RateWaifuExecutor(emotes)
        )

        commandManager.register(
            CancelledCommand,
            CancelledExecutor(emotes)
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
            HelpCommand,
            HelpExecutor(emotes)
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
            JankenponCommand, JankenponExecutor(random, emotes)
        )

        // ===[ IMAGES ]===
        commandManager.register(AtaCommand, MonicaAtaExecutor(emotes, http), ChicoAtaExecutor(emotes, http), LoriAtaExecutor(emotes, http), GessyAtaExecutor(emotes, http))
        commandManager.register(DrakeCommand, DrakeExecutor(emotes, http), BolsoDrakeExecutor(emotes, http), LoriDrakeExecutor(emotes, http))
        commandManager.register(ManiaTitleCardCommand, ManiaTitleCardExecutor(http))

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
        commandManager.register(SAMCommand, SAMExecutor(http))
        commandManager.register(ToBeContinuedCommand, ToBeContinuedExecutor(emotes, http))
        commandManager.register(InvertColorsCommand, InvertColorsExecutor(emotes, http))

        // ===[ UTILS ]===
        commandManager.register(MorseCommand, MorseFromExecutor(emotes), MorseToExecutor(emotes))

        runBlocking {
            commandManager.convertToInteraKTions(
                localeManager.getLocaleById("default")
            )
        }

        interactions.start()
    }
}