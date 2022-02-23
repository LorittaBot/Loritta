package net.perfectdreams.showtime.backend

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationBuilder
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
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MonicaAtaExecutor
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

object PublicApplicationCommands {
    val cinnamonDeclarations = listOf(
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
    ).map { it.declaration() }

    val dataDeclarations = cinnamonDeclarations.map { convertToData(it) }

    val flattenedDataDeclarations = dataDeclarations.flatMap { flattenData(it) }

    val additionalCommandsInfo = commandsInfo {
        command(MonicaAtaExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/ata.png")
        }
    }

    private fun commandsInfo(builder: AdditionalCommandsInfoBuilder.() -> (Unit)) = AdditionalCommandsInfoBuilder().apply(builder).build()

    class AdditionalCommandsInfoBuilder {
        val commands = mutableMapOf<KClass<*>, AdditionalCommandInfoBuilder>()

        fun command(kClass: KClass<*>, builder: AdditionalCommandInfoBuilder.() -> (Unit)) {
            commands[kClass] = AdditionalCommandInfoBuilder().apply(builder)
        }

        fun build() = commands.map { it.key.simpleName to it.value.build() }.toMap()
    }

    class AdditionalCommandInfoBuilder {
        private var imageUrls: MutableList<ImageUrl>? = null
        private var videoUrls: MutableList<VideoUrl>? = null

        operator fun ImageUrl.unaryPlus() {
            imageUrls = (imageUrls ?: mutableListOf()).also { it.add(this) }
        }

        operator fun VideoUrl.unaryPlus() {
            videoUrls = (videoUrls ?: mutableListOf()).also { it.add(this) }
        }

        fun build() = AdditionalApplicationCommandInfo(imageUrls?.map { it.url }, videoUrls?.map { it.url })
    }

    @JvmInline
    value class ImageUrl(val url: String)

    @JvmInline
    value class VideoUrl(val url: String)

    /**
     * Converts a [SlashCommandDeclarationBuilder] to [InteractionCommand]
     */
    private fun convertToData(declaration: SlashCommandDeclarationBuilder): InteractionCommand {
        return InteractionCommand(
            declaration.labels.first(),
            declaration.description,
            declaration.category,
            (declaration.executor?.parent as KClass<*>?)?.simpleName,
            declaration.subcommandGroups.map {
                InteractionCommandGroup(
                    it.labels.first(),
                    it.subcommands.map {
                        InteractionCommand(
                            it.labels.first(),
                            it.description,
                            it.category,
                            (it.executor?.parent as KClass<*>?)?.simpleName,
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

    /**
     * Flattens the [InteractionCommand] subcommands and subcommand groups into a list of [InteractionCommand].
     *
     * Example: A command "/loritta" with group "is" with subcommands "cute" and "awesome" is flattened to:
     * "/loritta is cute"
     * "/loritta is awesome"
     */
    private fun flattenData(rootCommand: InteractionCommand): List<InteractionCommand> {
        val flattenedData = mutableListOf<InteractionCommand>()

        if (rootCommand.executor != null)
            flattenedData.add(rootCommand)

        for (command in rootCommand.subcommands) {
            flattenedData.add(command.copy(label = "${rootCommand.label} ${command.label}"))
        }

        for (group in rootCommand.groups) {
            for (command in group.subcommands) {
                flattenedData.add(command.copy(label = "${rootCommand.label} ${group.label} ${command.label}"))
            }
        }

        return flattenedData
    }

    @Serializable
    data class InteractionCommand(
        val label: String,
        val description: StringI18nData,
        val category: CommandCategory,
        val executor: String?,
        val groups: List<InteractionCommandGroup>,
        val subcommands: List<InteractionCommand>
    )

    @Serializable
    data class InteractionCommandGroup(
        val label: String,
        val subcommands: List<InteractionCommand>
    )

    data class AdditionalApplicationCommandInfo(
        val imageUrls: List<String>? = null,
        val videoUrls: List<String>? = null
    )
}