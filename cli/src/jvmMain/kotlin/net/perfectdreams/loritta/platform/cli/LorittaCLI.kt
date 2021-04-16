package net.perfectdreams.loritta.platform.cli

import io.ktor.client.*
import net.perfectdreams.loritta.commands.`fun`.*
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.commands.images.ArtExecutor
import net.perfectdreams.loritta.commands.images.AvatarTestExecutor
import net.perfectdreams.loritta.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.commands.images.PetPetExecutor
import net.perfectdreams.loritta.commands.images.QuadroExecutor
import net.perfectdreams.loritta.commands.images.RipTvExecutor
import net.perfectdreams.loritta.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.commands.images.SustoExecutor
import net.perfectdreams.loritta.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.commands.images.declarations.AtaCommand
import net.perfectdreams.loritta.commands.images.declarations.AvatarTestCommand
import net.perfectdreams.loritta.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.BolsonaroCommand
import net.perfectdreams.loritta.commands.images.declarations.BriggsCoverCommand
import net.perfectdreams.loritta.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.commands.images.declarations.CanellaDvdCommand
import net.perfectdreams.loritta.commands.images.declarations.EdnaldoCommand
import net.perfectdreams.loritta.commands.images.declarations.LoriSignCommand
import net.perfectdreams.loritta.commands.images.declarations.ManiaTitleCardCommand
import net.perfectdreams.loritta.commands.images.declarations.PassingPaperCommand
import net.perfectdreams.loritta.commands.images.declarations.PepeDreamCommand
import net.perfectdreams.loritta.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.commands.images.declarations.QuadroCommand
import net.perfectdreams.loritta.commands.images.declarations.RipTvCommand
import net.perfectdreams.loritta.commands.images.declarations.RomeroBrittoCommand
import net.perfectdreams.loritta.commands.images.declarations.StudiopolisTvCommand
import net.perfectdreams.loritta.commands.images.declarations.SustoCommand
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
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.minecraft.MinecraftMojangAPI
import net.perfectdreams.loritta.platform.cli.commands.CLICommandContext
import net.perfectdreams.loritta.platform.cli.commands.CommandManager
import net.perfectdreams.loritta.platform.cli.entities.CLIMessageChannel
import java.io.File

class LorittaCLI : LorittaBot() {
    val commandManager = CommandManager(this)
    val localeManager = LocaleManager(
        File("L:\\RandomProjects\\LorittaInteractions\\locales")
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
        commandManager.register(MorseCommand, MorseExecutor(emotes))

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
    }

    suspend fun runArgs(args: Array<String>) {
        if (commandManager.matches(args.joinToString(" ")))
            return

        println("No matching command found!")
    }
}