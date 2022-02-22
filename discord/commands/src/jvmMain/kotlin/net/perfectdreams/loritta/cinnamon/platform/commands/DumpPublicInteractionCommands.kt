package net.perfectdreams.loritta.cinnamon.platform.commands

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.HungerGamesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.JankenponCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RateCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.RollCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.ShipCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.SummonCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.VieirinhaCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.EmojiCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.InviteCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.ServerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.DailyCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.ArtCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BRMemesCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BobBurningPaperCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.BuckShirtCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.DrakeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.DrawnMaskCommand
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
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SadRealityCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SonicCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.SustoCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.TerminatorAnimeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.ToBeContinuedCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.TrumpCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.WolverineFrameCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.minecraft.declarations.MinecraftCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.CalculatorCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ChooseCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.DictionaryCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.AttackOnHeartCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.CarlyAaahCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations.FansExplainingCommand
import kotlin.reflect.KClass

object DumpPublicInteractionCommands {
    val declarations = listOf(
        // ===[ DISCORD ]===
        UserCommand,
        ServerCommand,
        InviteCommand,
        EmojiCommand,
        WebhookCommand,

        // ===[ FUN ]===
        CoinFlipCommand,
        RateCommand,
        ShipCommand,
        SummonCommand,
        VieirinhaCommand,
        RollCommand,
        HelpCommand,
        TextTransformDeclaration,
        JankenponCommand,
        HungerGamesCommand,

        // ===[ MINECRAFT ]===
        MinecraftCommand,

        // ===[ IMAGES ]===
        DrakeCommand,
        SonicCommand,
        ArtCommand,
        BobBurningPaperCommand,
        BRMemesCommand,
        BuckShirtCommand,
        LoriSignCommand,
        PassingPaperCommand,
        PepeDreamCommand,
        PetPetCommand,
        WolverineFrameCommand,
        RipTvCommand,
        SustoCommand,
        GetOverHereCommand,
        NichijouYuukoPaperCommand,
        TrumpCommand,
        TerminatorAnimeCommand,
        ToBeContinuedCommand,
        InvertColorsCommand,
        MemeMakerCommand,
        MarkMetaCommand,
        DrawnMaskCommand,
        SadRealityCommand,

        // ===[ VIDEOS ]===
        CarlyAaahCommand,
        AttackOnHeartCommand,
        FansExplainingCommand,

        // ===[ UTILS ]===
        HelpCommand,
        MoneyCommand,
        MorseCommand,
        DictionaryCommand,
        CalculatorCommand,
        AnagramCommand,
        ChooseCommand,

        // ===[ ECONOMY ]===
        SonhosCommand,
        DailyCommand,
        BrokerCommand,
        TransactionsCommand,
        BetCommand,
        AchievementsCommand,
        AfkCommand,
        GenderCommand,
        UndertaleCommand
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val commands = declarations.map {
            convertToData(it.declaration())
        }

        println(Json.encodeToString(commands))
    }

    private fun convertToData(declaration: SlashCommandDeclarationBuilder): InteractionCommand {
        return InteractionCommand(
            declaration.labels,
            declaration.description,
            declaration.category,
            (declaration.executor?.parent as KClass<*>?)?.simpleName,
            declaration.subcommandGroups.map {
                InteractionCommandGroup(
                    it.labels,
                    it.subcommands.map {
                        InteractionCommand(
                            it.labels,
                            it.description,
                            it.category,
                            (declaration.executor?.parent as KClass<*>?)?.simpleName,
                            listOf(),
                            listOf()
                        )
                    }
                )
            },
            declaration.subcommands.map {
                convertToData(it)
            }
        )
    }
}