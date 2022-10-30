package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.PetPetRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.PetPetCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.morenitta.LorittaBot

class PetPetExecutor(
    loritta: LorittaBot,
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