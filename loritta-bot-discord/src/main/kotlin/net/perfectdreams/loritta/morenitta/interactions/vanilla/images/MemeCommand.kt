package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.DrawnMaskAtendenteRequest
import net.perfectdreams.gabrielaimageserver.data.DrawnMaskWordRequest
import net.perfectdreams.gabrielaimageserver.data.ManiaTitleCardRequest
import net.perfectdreams.gabrielaimageserver.data.MemeMakerRequest
import net.perfectdreams.gabrielaimageserver.data.PetPetRequest
import net.perfectdreams.gabrielaimageserver.data.TerminatorAnimeRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.morenitta.utils.UserUtils
import net.perfectdreams.loritta.morenitta.utils.images.userAvatarCollage
import net.perfectdreams.loritta.serializable.UserId
import java.awt.Color
import java.util.*

class MemeCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18nKeysData.Commands.Command.Meme.Label, I18nKeysData.Commands.Command.Meme.Description, CommandCategory.IMAGES, UUID.fromString("13e52bb0-89ee-4567-9924-9b1d9a80d686")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        this.enableLegacyMessageSupport = true

        subcommand(I18nKeysData.Commands.Command.Art.Label, I18nKeysData.Commands.Command.Art.Description, UUID.fromString("f6ef29a6-4b81-42e8-9781-f143e6df8680")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("art")
            }

            this.executor = ArtCommandExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Bobburningpaper.Label, I18nKeysData.Commands.Command.Bobburningpaper.Description, UUID.fromString("44814792-2f61-4459-8e6f-e200dced66c2")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("bobpaperfire")
                add("bobfire")
                add("bobpapelfogo")
                add("bobfogo")
            }

            executor = BobBurningPaperExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Buckshirt.Label, I18nKeysData.Commands.Command.Buckshirt.Description, UUID.fromString("407fc5f6-b1d6-4944-b30c-ec72cb48a6c8")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("buckshirt")
            }

            this.executor = BuckShirtExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Getoverhere.Label, I18nKeysData.Commands.Command.Getoverhere.Description, UUID.fromString("4f23885f-1473-4268-ad87-04e7b4ff69af")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("getoverhere")
            }

            executor = GetOverHereExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Tobecontinued.Label, I18nKeysData.Commands.Command.Tobecontinued.Description, UUID.fromString("e10a9ca5-67c6-4b3e-abfa-ed7f882cb5ce")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("tobecontinued")
            }

            executor = ToBeContinuedExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Passingpaper.Label, I18nKeysData.Commands.Command.Passingpaper.Description,UUID.fromString("b83dbbaf-cc7b-43bc-b052-d2b1fd775f1e")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("passingpaper")
            }

            executor = PassingPaperExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Wolverineframe.Label, I18nKeysData.Commands.Command.Wolverineframe.Description, UUID.fromString("cd92b981-f2ae-4970-857d-d312fe6971bb")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("wolverineframe")
                add("frame")
            }

            executor = WolverineFrameExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Discordping.Label, I18nKeysData.Commands.Command.Discordping.Description, UUID.fromString("7d6d58da-da63-4850-94b4-f28359ba7471")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("discordping")
            }

            executor = NichijouYuukoPaperExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Riptv.Label, I18nKeysData.Commands.Command.Riptv.Description, UUID.fromString("7647e71e-1f1e-4037-a0ca-801587a46688")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("riptv")
            }

            executor = RipTvExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Markmeta.Label, I18nKeysData.Commands.Command.Markmeta.Description, UUID.fromString("8f30907c-70a0-4172-b1b4-9f4af9c80b7f")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("markmeta")
            }

            executor = MarkMetaExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Invertcolors.Label, I18nKeysData.Commands.Command.Invertcolors.Description, UUID.fromString("4bc2aba7-b725-4878-a675-99808ca97a53")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("invertcolors")
                add("invert")
                add("inverter")
            }

            executor = InvertColorsExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Drake.Drake.Label, I18nKeysData.Commands.Command.Drake.Drake.Description, UUID.fromString("2b858d29-10de-4153-9228-1ed4b25072e7")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("drake")
            }

            executor = DrakeCommandExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Drake.Bolsonaro.Label, I18nKeysData.Commands.Command.Drake.Bolsonaro.Description, UUID.fromString("44673256-508f-423b-be2c-5bde7c008105")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("bolsodrake")
            }

            executor = BolsoDrakeCommandExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Drake.Lori.Label, I18nKeysData.Commands.Command.Drake.Lori.Description, UUID.fromString("3ef10dca-f48b-4806-9346-ee46dab2c027")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("loridrake")
            }

            executor = LoriDrakeCommandExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Pepedream.Label, I18nKeysData.Commands.Command.Pepedream.Description, UUID.fromString("938ad45a-4b01-43e5-ba98-d1d718986e3d")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("pepedream")
            }

            executor = PepeDreamExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Trump.Label, I18nKeysData.Commands.Command.Trump.Description, UUID.fromString("fd36c5fb-10d4-47fe-b5d7-c6925ea5f17e")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("trump")
            }

            executor = TrumpExecutor(client)
        }

        subcommandGroup(I18nKeysData.Commands.Command.Drawnmask.Label, I18nKeysData.Commands.Command.Drawnmask.Description) {
            subcommand(I18nKeysData.Commands.Command.Drawnmask.Atendente.Label, I18nKeysData.Commands.Command.Drawnmask.Atendente.Description, UUID.fromString("dc029079-399e-4e60-88bc-f747dbce858d")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("atendente")
                }

                executor = DrawnMaskAtendenteExecutor(client)
            }

            subcommand(I18nKeysData.Commands.Command.Drawnmask.Sign.Label, I18nKeysData.Commands.Command.Drawnmask.Sign.Description, UUID.fromString("f83f6cc0-56d2-47d3-8126-e2255f4bf56c")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("drawnmasksign")
                }

                executor = DrawnMaskSignExecutor(client)
            }

            subcommand(I18nKeysData.Commands.Command.Drawnmask.Word.Label, I18nKeysData.Commands.Command.Drawnmask.Word.Description, UUID.fromString("37ee0692-075e-4275-850b-f7057e820d0e")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("drawnmaskword")
                    add("drawnword")
                }

                executor = DrawnMaskWordExecutor(client)
            }
        }

        subcommandGroup(I18nKeysData.Commands.Command.Sonic.Label, TodoFixThisData) {
            subcommand(I18nKeysData.Commands.Command.Sonic.Knuxthrow.Label, I18nKeysData.Commands.Command.Sonic.Knuxthrow.Description, UUID.fromString("0be0dabf-5acb-4793-9e7b-accd121f312e")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("knuxthrow")
                    add("knucklesthrow")
                    add("throwknux")
                    add("throwknuckles")
                    add("knucklesjogar")
                    add("knuxjogar")
                    add("jogarknuckles")
                    add("jogarknux")
                }

                executor = KnuxThrowExecutor(client)
            }

            subcommand(I18nKeysData.Commands.Command.Sonic.Maniatitlecard.Label, I18nKeysData.Commands.Command.Sonic.Maniatitlecard.Description, UUID.fromString("81d25db7-ad55-4bae-93c3-79bbfe344469")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("maniatitlecard")
                }
                executor = ManiaTitleCardExecutor(client)
            }

            subcommand(I18nKeysData.Commands.Command.Sonic.Studiopolistv.Label, I18nKeysData.Commands.Command.Sonic.Studiopolistv.Description, UUID.fromString("09fb2acd-91d0-4ef4-a4a9-021b0353c511")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("studiopolistv")
                    add("studiopolis")
                }

                executor = StudiopolisTvExecutor(client)
            }
        }

        subcommandGroup(I18nKeysData.Commands.Command.Meme.Loritta.Label, I18nKeysData.Commands.Command.Meme.Loritta.Description) {
            subcommand(I18nKeysData.Commands.Command.Fright.Label, I18nKeysData.Commands.Command.Fright.Description, UUID.fromString("e88665a5-6043-45e0-8312-1098b6f9857a")) {
                enableLegacyMessageSupport = true
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("fright")
                    add("susto")
                    add("lorisusto")
                }

                executor = SustoExecutor(client)
            }

            subcommand(I18nKeysData.Commands.Command.Lorisign.Label, I18nKeysData.Commands.Command.Lorisign.Description, UUID.fromString("418fe58a-6052-41ca-aa6f-4dbd7b78c962")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("lorisign")
                }

                executor = LoriSignExecutor(client)
            }
        }

        subcommand(I18nKeysData.Commands.Command.Terminatoranime.Label, I18nKeysData.Commands.Command.Terminatoranime.Description, UUID.fromString("d29e7262-9626-4802-89d0-c8e3da6abdf4")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("terminatoranime")
                add("terminator")
            }

            executor = TerminatorAnimeExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Mememaker.Label, I18nKeysData.Commands.Command.Mememaker.Description, UUID.fromString("23947584-91f0-4876-b81b-e7057a69a280")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("mememaker")
            }

            executor = MemeMakerExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Petpet.Label, I18nKeysData.Commands.Command.Petpet.Description, UUID.fromString("9de598be-fcca-422e-861b-6a3269bb312b")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("petpet")
            }

            executor = PetPetCommandExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Thanksfriends.Label, I18nKeysData.Commands.Command.Thanksfriends.Description, UUID.fromString("a55edb85-0877-4e8a-8970-79eb73aa71a4")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("thanksfriends")
                add("obrigadoamigos")
            }
            examples = I18nKeysData.Commands.Command.Thanksfriends.Examples

            executor = ThanksFriendsExecutor(client)
        }

        subcommand(I18nKeysData.Commands.Command.Everygrouphas.Label, I18nKeysData.Commands.Command.Everygrouphas.Description, UUID.fromString("e8c0935d-bfba-48af-b2f9-a54c2d5bb96d")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("todogrupotem")
                add("everygrouphas")
            }
            examples = I18nKeysData.Commands.Command.Everygrouphas.Examples

            executor = EveryGroupHasExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Sadreality.Label, I18nKeysData.Commands.Command.Sadreality.Description, UUID.fromString("0342dec6-209d-4435-9186-0e8503b84810")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("tristerealidade")
                add("sadreality")
            }
                  
            executor = SadRealityExecutor()
        }
    }

    class ArtCommandExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.art(it) },
        "art.png"
    )

    class BobBurningPaperExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.bobBurningPaper(it) },
        "bob_burning_paper.png"
    )

    class BuckShirtExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.buckShirt(it) },
        "buck_shirt.png"
    )

    class GetOverHereExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.getOverHere(it) },
        "get_over_here.gif"
    )

    class ToBeContinuedExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.toBeContinued(it) },
        "to_be_continued.png"
    )

    class PassingPaperExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.passingPaper(it) },
        "passing_paper.png"
    )

    class WolverineFrameExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.wolverineFrame(it) },
        "wolverine_frame.png"
    )

    class NichijouYuukoPaperExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.nichijouYuukoPaper(it) },
        "nichijou_yuuko_paper.gif"
    )

    class RipTvExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.ripTv(it) },
        "rip_tv.png"
    )

    class MarkMetaExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.markMeta(it) },
        "mark_meta.png"
    )

    class InvertColorsExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.invertColors(it) },
        "invert.png"
    )

    class DrakeCommandExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.drake(it) },
        "drake.png"
    )

    class BolsoDrakeCommandExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.bolsoDrake(it) },
        "bolsodrake.png"
    )

    class LoriDrakeCommandExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.loriDrake(it) },
        "lori_drake.png"
    )

    class PepeDreamExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.pepeDream(it) },
        "pepe_dream.png"
    )

    class TrumpExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.trump(it) },
        "trump.gif"
    )

    class DrawnMaskAtendenteExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Drawnmask.Atendente.Options.Line1.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]

            val result = client.handleExceptions(context) {
                client.images.drawnMaskAtendente(
                    DrawnMaskAtendenteRequest(text)
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result,
                        "atendente.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }


    class DrawnMaskSignExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.drawnMaskSign(it) },
        "drawn_mask_sign.png"
    )

    class DrawnMaskWordExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Drawnmask.Word.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]

            val result = client.handleExceptions(context) {
                client.images.drawnMaskWord(
                    DrawnMaskWordRequest(text)
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result,
                        "drawn_mask_word.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }

    class KnuxThrowExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.knucklesThrow(it) },
        "knux_throw.gif"
    )

    class ManiaTitleCardExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val line1 = string("line1", I18nKeysData.Commands.Command.Sonic.Maniatitlecard.Options.Line1)
            val line2 = optionalString("line2", I18nKeysData.Commands.Command.Sonic.Maniatitlecard.Options.Line2)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val line1 = args[options.line1]
            val line2 = args[options.line2]

            val result = client.handleExceptions(context) {
                client.images.maniaTitleCard(
                    ManiaTitleCardRequest(
                        line1,
                        line2
                    )
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(result, "mania_title_card.png")
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val input = args.joinToString(" ").split(" | ")
            val line1 = input.getOrNull(0) ?: ""
            val line2 = input.getOrNull(1) ?: ""

            return mapOf(
                options.line1 to line1,
                options.line2 to line2
            )
        }
    }

    class StudiopolisTvExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.studiopolisTv(it) },
        "studiopolis_tv.png"
    )

    class SustoExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.loriScared(it) },
        "loritta_susto.png"
    )

    class LoriSignExecutor(client: GabrielaImageServerClient) : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.loriSign(it) },
        "lori_sign.png"
    )

    class TerminatorAnimeExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val line1 = string("terminator", I18nKeysData.Commands.Command.Terminatoranime.Options.TextTerminator)
            val line2 = string("girl", I18nKeysData.Commands.Command.Terminatoranime.Options.TextGirl)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val line1 = args[options.line1]
            val line2 = args[options.line2]

            val result = client.handleExceptions(context) {
                client.images.terminatorAnime(
                    TerminatorAnimeRequest(line1, line2)
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(result, "terminator_anime.png")
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val input = args.joinToString(" ").split(" | ")
            val line1 = input.getOrNull(0) ?: ""
            val line2 = input.getOrNull(1) ?: ""

            return mapOf(
                options.line1 to line1,
                options.line2 to line2
            )
        }
    }

    class MemeMakerExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val line1 = string("line1", I18nKeysData.Commands.Command.Mememaker.Options.Line1)
            val line2 = optionalString("line2", I18nKeysData.Commands.Command.Mememaker.Options.Line2)

            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false) // Defer message because image manipulation is kinda heavy

            val imageReference = args[options.imageReference].get(context)
            val line1 = args[options.line1]
            val line2 = args[options.line2]

            val result = client.handleExceptions(context) {
                client.images.memeMaker(
                    MemeMakerRequest(
                        URLImageData(imageReference),
                        line1,
                        line2
                    )
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result.inputStream(),
                        "meme_maker.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            val input = args.joinToString(" ").split(" | ")
            val line1 = input.getOrNull(0) ?: ""
            val line2 = input.getOrNull(1) ?: ""

            return mapOf(
                options.imageReference to ImageReferenceOrAttachment(
                    args.getOrNull(3),
                    context.getImage(0)
                ),
                options.line1 to line1,
                options.line2 to line2
            )
        }
    }

    class PetPetCommandExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)

            val squish = optionalString("squish", I18nKeysData.Commands.Command.Petpet.Options.Squish.Text) {
                choice(I18nKeysData.Commands.Command.Petpet.Options.Squish.Choice.Nothing, "0.0")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Squish.Choice.KindaHardButABitSquishy, "0.25")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Squish.Choice.NormalSquishness, "0.875")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Squish.Choice.VerySquishy, "1.5")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Squish.Choice.SoMuchSquishy, "3.0")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Squish.Choice.PatItSoTheyCanFeelIt, "4.0")
            }

            val speed = optionalString("speed", I18nKeysData.Commands.Command.Petpet.Options.Speed.Text) {
                choice(I18nKeysData.Commands.Command.Petpet.Options.Speed.Choice.ILikeToTakeItSlow, "14")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Speed.Choice.ANiceAndSmoothPet, "7")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Speed.Choice.KindaFast, "4")
                choice(I18nKeysData.Commands.Command.Petpet.Options.Speed.Choice.AaaICantHandleItSoCute, "2")
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageReference = args[options.imageReference].get(context)
            val squish = args[options.squish]
            val speed = args[options.speed]

            val result = client.handleExceptions(context) {
                client.images.petPet(
                    PetPetRequest(
                        URLImageData(imageReference),
                        squish?.toDouble() ?: 0.875,
                        speed?.toInt() ?: 7
                    )
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(result.inputStream(), "petpet.gif")
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.imageReference to ImageReferenceOrAttachment(
                    args.getOrNull(0),
                    context.getImage(0)
                ),
                options.squish to args.getOrNull(1),
                options.speed to args.getOrNull(2)
            )
        }
    }

    class ThanksFriendsExecutor(val client: GabrielaImageServerClient) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val user1 = optionalUser("user1", I18nKeysData.Commands.Command.Thanksfriends.Options.User1.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.Thanks))
            val user2 = optionalUser("user2", I18nKeysData.Commands.Command.Thanksfriends.Options.User2.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.For))
            val user3 = optionalUser("user3", I18nKeysData.Commands.Command.Thanksfriends.Options.User3.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.Being))
            val user4 = optionalUser("user4", I18nKeysData.Commands.Command.Thanksfriends.Options.User4.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.The))
            val user5 = optionalUser("user5", I18nKeysData.Commands.Command.Thanksfriends.Options.User5.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.NotYou))
            val user6 = optionalUser("user6", I18nKeysData.Commands.Command.Thanksfriends.Options.User6.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.Best))
            val user7 = optionalUser("user7", I18nKeysData.Commands.Command.Thanksfriends.Options.User7.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.Friends))
            val user8 = optionalUser("user8", I18nKeysData.Commands.Command.Thanksfriends.Options.User8.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.Of))
            val user9 = optionalUser("user9", I18nKeysData.Commands.Command.Thanksfriends.Options.User9.Text(I18nKeysData.Commands.Command.Thanksfriends.Slot.All))
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user1FromArguments = args[options.user1]
            val user2FromArguments = args[options.user2]
            val user3FromArguments = args[options.user3]
            val user4FromArguments = args[options.user4]
            val user5FromArguments = args[options.user5]
            val user6FromArguments = args[options.user6]
            val user7FromArguments = args[options.user7]
            val user8FromArguments = args[options.user8]
            val user9FromArguments = args[options.user9]

            val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
                context,
                listOf(
                    user1FromArguments?.user,
                    user2FromArguments?.user,
                    user3FromArguments?.user,
                    user4FromArguments?.user,
                    user5FromArguments?.user,
                    user6FromArguments?.user,
                    user7FromArguments?.user,
                    user8FromArguments?.user,
                    user9FromArguments?.user
                )
            )

            // Not enough users!
            if (!successfullyFilled) {
                context.fail(false) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.Command.Sadreality.NotEnoughUsers), Emotes.LoriSob)

                    if (noPermissionToQuery) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                    } else if (context.guildOrNull == null) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                    }
                }
            }

            val result = userAvatarCollage(3, 3) {
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[0],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.Thanks
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[1],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.For
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[2],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.Being
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[3],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.The
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[4],
                    Color.RED,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.NotYou
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[5],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.Best
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[6],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.Friends
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[7],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.Of
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[8],
                    Color.WHITE,
                    I18nKeysData.Commands.Command.Thanksfriends.Slot.All
                )
            }.generate(context.loritta)

            context.reply(false) {
                files += AttachedFile.fromData(result.toByteArray(ImageFormatType.PNG).inputStream(), "thanks_friends.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember1 = context.getUserAndMember(0)
            val userAndMember2 = context.getUserAndMember(1)
            val userAndMember3 = context.getUserAndMember(2)
            val userAndMember4 = context.getUserAndMember(3)
            val userAndMember5 = context.getUserAndMember(4)
            val userAndMember6 = context.getUserAndMember(5)

            return mapOf(
                options.user1 to userAndMember1,
                options.user2 to userAndMember2,
                options.user3 to userAndMember3,
                options.user4 to userAndMember4,
                options.user5 to userAndMember5,
                options.user6 to userAndMember6,
            )
        }
    }

    class EveryGroupHasExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val user1 = optionalUser("user1", I18nKeysData.Commands.Command.Everygrouphas.Options.User1.Text(I18nKeysData.Commands.Command.Everygrouphas.Slot.Popular.Male))
            val user2 = optionalUser("user2", I18nKeysData.Commands.Command.Everygrouphas.Options.User1.Text(I18nKeysData.Commands.Command.Everygrouphas.Slot.Quiet.Male))
            val user3 = optionalUser("user3", I18nKeysData.Commands.Command.Everygrouphas.Options.User1.Text(I18nKeysData.Commands.Command.Everygrouphas.Slot.Clown.Male))
            val user4 = optionalUser("user4", I18nKeysData.Commands.Command.Everygrouphas.Options.User1.Text(I18nKeysData.Commands.Command.Everygrouphas.Slot.Nerd.Male))
            val user5 = optionalUser("user5", I18nKeysData.Commands.Command.Everygrouphas.Options.User1.Text(I18nKeysData.Commands.Command.Everygrouphas.Slot.Fanboy.Male))
            val user6 = optionalUser("user6", I18nKeysData.Commands.Command.Everygrouphas.Options.User1.Text(I18nKeysData.Commands.Command.Everygrouphas.Slot.Cranky.Male))
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user1FromArguments = args[options.user1]
            val user2FromArguments = args[options.user2]
            val user3FromArguments = args[options.user3]
            val user4FromArguments = args[options.user4]
            val user5FromArguments = args[options.user5]
            val user6FromArguments = args[options.user6]

            val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
                context,
                listOf(
                    user1FromArguments?.user,
                    user2FromArguments?.user,
                    user3FromArguments?.user,
                    user4FromArguments?.user,
                    user5FromArguments?.user,
                    user6FromArguments?.user
                )
            )

            // Not enough users!
            if (!successfullyFilled) {
                context.fail(false) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.Command.Sadreality.NotEnoughUsers), Emotes.LoriSob)

                    if (noPermissionToQuery) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                    } else if (context.guildOrNull == null) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                    }
                }
            }

            val profileSettings = context.loritta.pudding.users.getProfileSettingsOfUsers(
                listOfUsers.map { UserId(it.idLong) }
            )

            val result = userAvatarCollage(3, 2) {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[0],
                    Color.WHITE,
                    profileSettings,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Popular.Male,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Popular.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[1],
                    Color.WHITE,
                    profileSettings,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Quiet.Male,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Quiet.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[2],
                    Color.WHITE,
                    profileSettings,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Clown.Male,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Clown.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[3],
                    Color.WHITE,
                    profileSettings,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Nerd.Male,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Nerd.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[4],
                    Color.WHITE,
                    profileSettings,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Fanboy.Male,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Fanboy.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[5],
                    Color.WHITE,
                    profileSettings,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Cranky.Male,
                    I18nKeysData.Commands.Command.Everygrouphas.Slot.Cranky.Female
                )
            }.generate(context.loritta)

            context.reply(false) {
                files += AttachedFile.fromData(result.toByteArray(ImageFormatType.PNG).inputStream(), "every_group_has.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember1 = context.getUserAndMember(0)
            val userAndMember2 = context.getUserAndMember(1)
            val userAndMember3 = context.getUserAndMember(2)
            val userAndMember4 = context.getUserAndMember(3)
            val userAndMember5 = context.getUserAndMember(4)
            val userAndMember6 = context.getUserAndMember(5)

            return mapOf(
                options.user1 to userAndMember1,
                options.user2 to userAndMember2,
                options.user3 to userAndMember3,
                options.user4 to userAndMember4,
                options.user5 to userAndMember5,
                options.user6 to userAndMember6,
            )
        }
    }

    class SadRealityExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val user1 = optionalUser("user1", I18nKeysData.Commands.Command.Sadreality.Options.User1.Text(I18nKeysData.Commands.Command.Sadreality.Slot.TheGuyYouLike.Female))
            val user2 = optionalUser("user2", I18nKeysData.Commands.Command.Sadreality.Options.User2.Text(I18nKeysData.Commands.Command.Sadreality.Slot.TheFather.Male.LovedGenderFemale))
            val user3 = optionalUser("user3", I18nKeysData.Commands.Command.Sadreality.Options.User3.Text(I18nKeysData.Commands.Command.Sadreality.Slot.TheBrother.Male.LovedGenderFemale))
            val user4 = optionalUser("user4", I18nKeysData.Commands.Command.Sadreality.Options.User4.Text(I18nKeysData.Commands.Command.Sadreality.Slot.TheFirstLover.Male.LovedGenderFemale))
            val user5 = optionalUser("user5", I18nKeysData.Commands.Command.Sadreality.Options.User5.Text(I18nKeysData.Commands.Command.Sadreality.Slot.TheBestFriend.Male.LovedGenderFemale))
            val user6 = optionalUser("user6", I18nKeysData.Commands.Command.Sadreality.Options.User6.Text(I18nKeysData.Commands.Command.Sadreality.Slot.You.Male))
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user1FromArguments = args[options.user1]
            val user2FromArguments = args[options.user2]
            val user3FromArguments = args[options.user3]
            val user4FromArguments = args[options.user4]
            val user5FromArguments = args[options.user5]
            val user6FromArguments = args[options.user6]

            val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
                context,
                listOf(
                    user1FromArguments?.user,
                    user2FromArguments?.user,
                    user3FromArguments?.user,
                    user4FromArguments?.user,
                    user5FromArguments?.user,
                    user6FromArguments?.user
                )
            )

            // Not enough users!
            if (!successfullyFilled) {
                context.fail(false) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.Command.Sadreality.NotEnoughUsers), Emotes.LoriSob)

                    if (noPermissionToQuery) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                    } else if (context.guildOrNull == null) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                    }
                }
            }

            val profileSettings = context.loritta.pudding.users.getProfileSettingsOfUsers(
                listOfUsers.map { UserId(it.idLong) }
            )

            val lovedGender = profileSettings[listOfUsers[0].id.toLong()]?.gender ?: Gender.FEMALE // The default is FEMALE for the loved gender
            val theFatherGender = profileSettings[listOfUsers[1].id.toLong()]?.gender ?: Gender.UNKNOWN
            val theBrotherGender = profileSettings[listOfUsers[2].id.toLong()]?.gender ?: Gender.UNKNOWN
            val theFirstLoverGender = profileSettings[listOfUsers[3].id.toLong()]?.gender ?: Gender.UNKNOWN
            val theBestFriendGender = profileSettings[listOfUsers[4].id.toLong()]?.gender ?: Gender.UNKNOWN
            val youGender = profileSettings[listOfUsers[5].id.toLong()]?.gender ?: Gender.UNKNOWN

            // This is more complicated than the others userAvatarCollage's uses because it depends on the "lovedGender" value
            val result = userAvatarCollage(3, 2) {
                if (listOfUsers[0].idLong == context.loritta.config.loritta.discord.applicationId.toLong()) {
                    // Easter Egg: Loritta
                    localizedSlot(
                        context.i18nContext,
                        listOfUsers[0],
                        Color.WHITE,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheGuyYouLike.Loritta
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[0],
                        Color.WHITE,
                        lovedGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheGuyYouLike.Male,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheGuyYouLike.Female
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[1],
                        Color.WHITE,
                        theFatherGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFather.Male.LovedGenderFemale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFather.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[1],
                        Color.WHITE,
                        theFatherGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFather.Male.LovedGenderMale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFather.Female.LovedGenderMale
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[2],
                        Color.WHITE,
                        theBrotherGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBrother.Male.LovedGenderFemale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBrother.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[2],
                        Color.WHITE,
                        theBrotherGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBrother.Male.LovedGenderMale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBrother.Female.LovedGenderMale
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[3],
                        Color.WHITE,
                        theFirstLoverGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFirstLover.Male.LovedGenderFemale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFirstLover.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[3],
                        Color.WHITE,
                        theFirstLoverGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFirstLover.Male.LovedGenderMale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheFirstLover.Female.LovedGenderMale
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[4],
                        Color.WHITE,
                        theBestFriendGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBestFriend.Male.LovedGenderFemale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBestFriend.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[4],
                        Color.WHITE,
                        theBestFriendGender,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBestFriend.Male.LovedGenderMale,
                        I18nKeysData.Commands.Command.Sadreality.Slot.TheBestFriend.Female.LovedGenderMale
                    )
                }

                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[5],
                    Color.WHITE,
                    youGender,
                    I18nKeysData.Commands.Command.Sadreality.Slot.You.Male,
                    I18nKeysData.Commands.Command.Sadreality.Slot.You.Female
                )
            }.generate(context.loritta)

            context.reply(false) {
                files += AttachedFile.fromData(result.toByteArray(ImageFormatType.PNG).inputStream(), "sad_reality.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember1 = context.getUserAndMember(0)
            val userAndMember2 = context.getUserAndMember(1)
            val userAndMember3 = context.getUserAndMember(2)
            val userAndMember4 = context.getUserAndMember(3)
            val userAndMember5 = context.getUserAndMember(4)
            val userAndMember6 = context.getUserAndMember(5)

            return mapOf(
                options.user1 to userAndMember1,
                options.user2 to userAndMember2,
                options.user3 to userAndMember3,
                options.user4 to userAndMember4,
                options.user5 to userAndMember5,
                options.user6 to userAndMember6,
            )
        }
    }
}