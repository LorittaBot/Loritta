package net.perfectdreams.loritta.website.backend

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.commands.CommandCategory

class PublicApplicationCommands(languageManager: LanguageManager) {
    val flattenedDataDeclarations = listOf<InteractionCommand>()

    /* val cinnamonCommands = PublicLorittaCommands(languageManager).commands()
    val dataDeclarations = cinnamonCommands.map { convertToData(it.declaration()) }

    val flattenedDataDeclarations = dataDeclarations.flatMap { flattenData(it) }

    val additionalCommandsInfo = commandsInfo {
        command(MonicaAtaExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/ata.png")
        }

        command(AttackOnHeartExecutor::class) {
            +VideoUrl("/v3/assets/img/commands/attack_on_heart.mp4")
        }

        command(ArtExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/art.png")
        }

        command(DrawnMaskAtendenteExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/atendente.png")
        }

        command(BobBurningPaperExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/bobfire.png")
        }

        command(BolsoDrakeExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/bolsodrake.png")
        }

        command(BolsoFrameExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/bolsoframe.png")
        }

        command(BolsonaroExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/bolsonaro_tv.png")
        }

        command(Bolsonaro2Executor::class) {
            +ImageUrl("/v3/assets/img/commands/bolsonaro_tv2.png")
        }

        command(BriggsCoverExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/briggs_capa.png")
        }

        command(BuckShirtExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/buck_shirt.png")
        }

        command(CanellaDvdExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/canella_dvd.png")
        }

        command(CepoDeMadeiraExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/cepo.gif")
        }

        command(CarlyAaahExecutor::class) {
            +VideoUrl("/v3/assets/img/commands/carly_aaah.mp4")
        }

        command(ChicoAtaExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/chico_ata.png")
        }

        command(CortesFlowExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/cortes_flow.jpg")
        }

        command(NichijouYuukoPaperExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/discordia.gif")
        }

        command(DrakeExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/drake.png")
        }

        command(DrawnMaskWordExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/drawn_word.png")
        }

        command(DrawnMaskSignExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/drawn_mask_placa.png")
        }

        command(EdnaldoBandeiraExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/ednaldo_bandeira.png")
        }

        command(EdnaldoTvExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/ednaldo_tv.png")
        }

        command(GetOverHereExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/getoverhere.gif")
        }

        command(GessyAtaExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/gessy_ata.png")
        }

        command(InvertColorsExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/invertido.png")
        }

        command(KnuxThrowExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/knuxthrow.gif")
        }

        command(LoriAtaExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/lori_ata.png")
        }

        command(LoriSignExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/lori_sign.png")
        }

        command(ManiaTitleCardExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/mania_title_card.png")
        }

        command(PassingPaperExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/passing_paper.png")
        }

        command(PepeDreamExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/pepe_dream.png")
        }

        command(PetPetExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/petpet.gif")
        }

        command(WolverineFrameExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/wolverine_frame.png")
        }

        command(RipTvExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/rip_tv.png")
        }

        command(RomeroBrittoExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/romero_britto.png")
        }

        command(SAMExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/south_america_memes.png")
        }

        command(SustoExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/loritta_susto.png")
        }

        command(StudiopolisTvExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/studiopolis_tv.png")
        }

        command(TerminatorAnimeExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/terminator_anime.png")
        }

        command(TrumpExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/trump.gif")
        }

        command(ToBeContinuedExecutor::class) {
            +ImageUrl("/v3/assets/img/commands/to_be_continued.png")
        }

        command(GigaChadExecutor::class) {
            +VideoUrl("/v3/assets/img/commands/gigachad.mp4")
        }

        command(ChavesCocieloExecutor::class) {
            +VideoUrl("/v3/assets/img/commands/cocielo_chaves.mp4")
        }

        command(ChavesOpeningExecutor::class) {
            +VideoUrl("https://stuff.loritta.website/commands/chaves-opening.mp4")
        }
    }

    private fun commandsInfo(builder: AdditionalCommandsInfoBuilder.() -> (Unit)) = AdditionalCommandsInfoBuilder()
        .apply(builder).build()

    class AdditionalCommandsInfoBuilder {
        val commands = mutableMapOf<KClass<out SlashCommandExecutor>, AdditionalCommandInfoBuilder>()

        fun command(kClass: KClass<out SlashCommandExecutor>, builder: AdditionalCommandInfoBuilder.() -> (Unit)) {
            commands[kClass] = AdditionalCommandInfoBuilder()
                .apply(builder)
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

        fun build() =
            AdditionalApplicationCommandInfo(
                imageUrls?.map { it.url },
                videoUrls?.map { it.url })
    }

    @JvmInline
    value class ImageUrl(val url: String)

    @JvmInline
    value class VideoUrl(val url: String)

    /**
     * Converts a [LorittaCommandDeclarationBuilder] to [InteractionCommand]
     */
    private fun convertToData(declaration: CinnamonSlashCommandDeclarationBuilder): InteractionCommand {
        return InteractionCommand(
            listOf(declaration.name),
            declaration.description,
            declaration.category,
            if (declaration.executor != null) "Workaround" else null, // (declaration.executor?.parent as KClass<*>?)?.simpleName,
            declaration.subcommandGroups.map {
                InteractionCommandGroup(
                    it.name,
                    it.subcommands.map {
                        InteractionCommand(
                            listOf(it.name),
                            it.description,
                            it.category,
                            if (declaration.executor != null) "Workaround" else null, // (it.executor?.parent as KClass<*>?)?.simpleName,
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

        // TODO: Improve this label work around by having "proper" subcommands and command groups
        for (command in rootCommand.subcommands) {
            flattenedData.add(
                command.copy(
                    label = rootCommand.label.toMutableList()
                        .apply {
                            addAll(command.label)
                        }
                )
            )
        }

        for (group in rootCommand.groups) {
            for (command in group.subcommands) {
                flattenedData.add(
                    command.copy(
                        label = rootCommand.label.toMutableList()
                            .apply {
                                add(group.label)
                                addAll(command.label)
                            }
                    )
                )
            }
        }

        return flattenedData
    }  */

    @Serializable
    data class InteractionCommand(
        val label: List<StringI18nData>,
        val description: StringI18nData,
        val category: CommandCategory,
        val executor: String?,
        val groups: List<InteractionCommandGroup>,
        val subcommands: List<InteractionCommand>
    )

    @Serializable
    data class InteractionCommandGroup(
        val label: StringI18nData,
        val subcommands: List<InteractionCommand>
    )

    data class AdditionalApplicationCommandInfo(
        val imageUrls: List<String>? = null,
        val videoUrls: List<String>? = null
    )
}