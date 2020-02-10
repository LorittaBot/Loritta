package net.perfectdreams.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import java.io.File
import javax.imageio.ImageIO

class DrakeCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(
			loritta,
			"DrakeCommand",
			listOf("drake2")
	) {
		description { it["commands.images.drake.description"] }
		examples { listOf("@MrPowerGamerBR @Loritta") }

		usage {
			argument(ArgumentType.IMAGE) {}
			argument(ArgumentType.IMAGE) {}
		}

		userRequiredPermissions = listOf(Permission.MANAGE_ROLES)
		needsToUploadFiles = true

		executes {
			val bi = JVMImage(ImageIO.read(File(Loritta.ASSETS + "drake.png"))) // Primeiro iremos carregar o nosso template
			val graph = bi.createGraphics()

			run {
				val avatarImg = validate(image(0))
				val image = avatarImg.getScaledInstance(150, 150, Image.ScaleType.SMOOTH)
				graph.drawImage(image, 150, 0)
			}

			run {
				val avatarImg = validate(image(1))
				val image = avatarImg.getScaledInstance(150, 150, Image.ScaleType.SMOOTH)
				graph.drawImage(image, 150, 150)
			}

			sendImage(bi, "drake.png")
		}
	}
}