package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.PetPetRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class PetPetCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Petpet
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("9de598be-fcca-422e-861b-6a3269bb312b")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("petpet")
        }

        executor = PetPetCommandExecutor()
    }

    inner class PetPetCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)

            val squish = optionalString("squish", I18N_PREFIX.Options.Squish.Text) {
                choice(I18N_PREFIX.Options.Squish.Choice.Nothing, "0.0")
                choice(I18N_PREFIX.Options.Squish.Choice.KindaHardButABitSquishy, "0.25")
                choice(I18N_PREFIX.Options.Squish.Choice.NormalSquishness, "0.875")
                choice(I18N_PREFIX.Options.Squish.Choice.VerySquishy, "1.5")
                choice(I18N_PREFIX.Options.Squish.Choice.SoMuchSquishy, "3.0")
                choice(I18N_PREFIX.Options.Squish.Choice.PatItSoTheyCanFeelIt, "4.0")
            }

            val speed = optionalString("speed", I18N_PREFIX.Options.Speed.Text) {
                choice(I18N_PREFIX.Options.Speed.Choice.ILikeToTakeItSlow, "14")
                choice(I18N_PREFIX.Options.Speed.Choice.ANiceAndSmoothPet, "7")
                choice(I18N_PREFIX.Options.Speed.Choice.KindaFast, "4")
                choice(I18N_PREFIX.Options.Speed.Choice.AaaICantHandleItSoCute, "2")
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
}