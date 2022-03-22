package net.perfectdreams.showtime.backend

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
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
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.LorittaCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.ServerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.DailyCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ArtExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskAtendenteExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskSignExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskWordExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.PetPetExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.SAMExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.TerminatorAnimeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.TrumpExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.WolverineFrameExecutor
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
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.CarlyAaahExecutor
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
        LorittaCommand,

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

        command(AttackOnHeartExecutor::class) {
            + VideoUrl("/v3/assets/img/commands/attack_on_heart.mp4")
        }

        command(ArtExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/art.png")
        }

        command(DrawnMaskAtendenteExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/atendente.png")
        }

        command(BobBurningPaperExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/bobfire.png")
        }

        command(BolsoDrakeExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/bolsodrake.png")
        }

        command(BolsoFrameExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/bolsoframe.png")
        }

        command(BolsonaroExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/bolsonaro_tv.png")
        }

        command(Bolsonaro2Executor::class) {
            + ImageUrl("/v3/assets/img/commands/bolsonaro_tv2.png")
        }

        command(BriggsCoverExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/briggs_capa.png")
        }

        command(BuckShirtExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/buck_shirt.png")
        }

        command(CanellaDvdExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/canella_dvd.png")
        }

        command(CepoDeMadeiraExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/cepo.gif")
        }

        command(CarlyAaahExecutor::class) {
            + VideoUrl("/v3/assets/img/commands/carly_aaah.mp4")
        }

        command(ChicoAtaExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/chico_ata.png")
        }

        command(CortesFlowExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/cortes_flow.jpg")
        }

        command(NichijouYuukoPaperExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/discordia.gif")
        }

        command(DrakeExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/drake.png")
        }

        command(DrawnMaskWordExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/drawn_word.png")
        }

        command(DrawnMaskSignExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/drawn_mask_placa.png")
        }

        command(EdnaldoBandeiraExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/ednaldo_bandeira.png")
        }

        command(EdnaldoTvExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/ednaldo_tv.png")
        }

        command(GetOverHereExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/getoverhere.gif")
        }

        command(GessyAtaExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/gessy_ata.png")
        }

        command(InvertColorsExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/invertido.png")
        }

        command(KnuxThrowExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/knuxthrow.gif")
        }

        command(LoriAtaExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/lori_ata.png")
        }

        command(LoriSignExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/lori_sign.png")
        }

        command(ManiaTitleCardExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/mania_title_card.png")
        }

        command(PassingPaperExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/passing_paper.png")
        }

        command(PepeDreamExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/pepe_dream.png")
        }

        command(PetPetExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/petpet.gif")
        }

        command(WolverineFrameExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/wolverine_frame.png")
        }

        command(RipTvExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/rip_tv.png")
        }

        command(RomeroBrittoExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/romero_britto.png")
        }

        command(SAMExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/south_america_memes.png")
        }

        command(SustoExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/loritta_susto.png")
        }

        command(StudiopolisTvExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/studiopolis_tv.png")
        }

        command(TerminatorAnimeExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/terminator_anime.png")
        }

        command(TrumpExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/trump.gif")
        }

        command(ToBeContinuedExecutor::class) {
            + ImageUrl("/v3/assets/img/commands/to_be_continued.png")
        }
    }

    private fun commandsInfo(builder: AdditionalCommandsInfoBuilder.() -> (Unit)) = AdditionalCommandsInfoBuilder().apply(builder).build()

    class AdditionalCommandsInfoBuilder {
        val commands = mutableMapOf<KClass<out SlashCommandExecutor>, AdditionalCommandInfoBuilder>()

        fun command(kClass: KClass<out SlashCommandExecutor>, builder: AdditionalCommandInfoBuilder.() -> (Unit)) {
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