package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.PetPetRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class PetPetExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val imageReference = imageReferenceOrAttachment("image", I18nKeysData.Commands.Category.Images.Options.Image)
                .register()

            val squish = optionalNumber("squish", PetPetCommand.I18N_PREFIX.Options.Squish.Text)
                .choice(0.0, PetPetCommand.I18N_PREFIX.Options.Squish.Choice.Nothing)
                .choice(0.25, PetPetCommand.I18N_PREFIX.Options.Squish.Choice.KindaHardButABitSquishy)
                .choice(0.875, PetPetCommand.I18N_PREFIX.Options.Squish.Choice.NormalSquishness)
                .choice(1.5, PetPetCommand.I18N_PREFIX.Options.Squish.Choice.VerySquishy)
                .choice(3.0, PetPetCommand.I18N_PREFIX.Options.Squish.Choice.SoMuchSquishy)
                .choice(4.0, PetPetCommand.I18N_PREFIX.Options.Squish.Choice.PatItSoTheyCanFeelIt)
                .register()

            val speed = optionalInteger("speed", PetPetCommand.I18N_PREFIX.Options.Speed.Text)
                .choice(14, PetPetCommand.I18N_PREFIX.Options.Speed.Choice.ILikeToTakeItSlow)
                .choice(7, PetPetCommand.I18N_PREFIX.Options.Speed.Choice.ANiceAndSmoothPet)
                .choice(4, PetPetCommand.I18N_PREFIX.Options.Speed.Choice.KindaFast)
                .choice(2, PetPetCommand.I18N_PREFIX.Options.Speed.Choice.AaaICantHandleItSoCute)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[Options.imageReference]
        val squish = args[Options.squish]
        val speed = args[Options.speed]

        val result = client.handleExceptions(context) {
            client.images.petPet(
                PetPetRequest(
                    URLImageData(imageReference.url),
                    squish ?: 0.875,
                    speed?.toInt() ?: 7
                )
            )
        }

        context.sendMessage {
            addFile("petpet.gif", result.inputStream())
        }
    }
}