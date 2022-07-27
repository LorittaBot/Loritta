package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.PetPetRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments

class PetPetExecutor(
    loritta: LorittaCinnamon,
    val client: GabrielaImageServerClient
) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val imageReference = imageReferenceOrAttachment("image")

        val squish = optionalNumber("squish", PetPetCommand.I18N_PREFIX.Options.Squish.Text) {
            choice(PetPetCommand.I18N_PREFIX.Options.Squish.Choice.Nothing, 0.0)
            choice(PetPetCommand.I18N_PREFIX.Options.Squish.Choice.KindaHardButABitSquishy, 0.25)
            choice(PetPetCommand.I18N_PREFIX.Options.Squish.Choice.NormalSquishness, 0.875)
            choice(PetPetCommand.I18N_PREFIX.Options.Squish.Choice.VerySquishy, 1.5)
            choice(PetPetCommand.I18N_PREFIX.Options.Squish.Choice.SoMuchSquishy, 3.0)
            choice(PetPetCommand.I18N_PREFIX.Options.Squish.Choice.PatItSoTheyCanFeelIt, 4.0)
        }

        val speed = optionalInteger("speed", PetPetCommand.I18N_PREFIX.Options.Speed.Text) {
            choice(PetPetCommand.I18N_PREFIX.Options.Speed.Choice.ILikeToTakeItSlow, 14)
            choice(PetPetCommand.I18N_PREFIX.Options.Speed.Choice.ANiceAndSmoothPet, 7)
            choice(PetPetCommand.I18N_PREFIX.Options.Speed.Choice.KindaFast, 4)
            choice(PetPetCommand.I18N_PREFIX.Options.Speed.Choice.AaaICantHandleItSoCute, 2)
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer message because image manipulation is kinda heavy

        val imageReference = args[options.imageReference].get(context)!!
        val squish = args[options.squish]
        val speed = args[options.speed]

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