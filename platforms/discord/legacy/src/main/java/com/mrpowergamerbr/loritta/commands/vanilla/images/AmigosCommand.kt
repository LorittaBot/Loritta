package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class AmigosCommand : AbstractCommand("friends", listOf("amigos", "meusamigos", "myfriends"), CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "thx.png")) }
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.friends.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.lava.examples")

	override fun getUsage() = arguments {
		repeat(9) {
			argument(ArgumentType.USER) {
				optional = true
			}
		}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val choosen = mutableListOf<Member>()

		var contextImage = context.getImageAt(0, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage2 = context.getImageAt(1, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage3 = context.getImageAt(2, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage4 = context.getImageAt(3, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage5 = context.getImageAt(4, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage6 = context.getImageAt(5, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage7 = context.getImageAt(6, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage8 = context.getImageAt(7, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)
		var contextImage9 = context.getImageAt(8, 0, 128, createTextAsImageIfNotFound = false) ?: getRandomAvatar(context, choosen)

		contextImage = contextImage.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage2 = contextImage2.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage3 = contextImage3.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage4 = contextImage4.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage5 = contextImage5.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage6 = contextImage6.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage7 = contextImage7.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage8 = contextImage8.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()
		contextImage9 = contextImage9.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		val finalImage = BufferedImage(384, 384, BufferedImage.TYPE_INT_ARGB)
		val graphics = finalImage.graphics

		graphics.drawImage(contextImage, 0, 0, null)
		graphics.drawImage(contextImage2, 128, 0, null)
		graphics.drawImage(contextImage3, 256, 0, null)
		graphics.drawImage(contextImage4, 0, 128, null)
		graphics.drawImage(contextImage5, 128, 128, null)
		graphics.drawImage(contextImage6, 256, 128, null)
		graphics.drawImage(contextImage7, 0, 256, null)
		graphics.drawImage(contextImage8, 128, 256, null)
		graphics.drawImage(contextImage9, 256, 256, null)

		graphics.drawImage(TEMPLATE, 0, 0, null)

		context.sendFile(finalImage, "thx.png", context.getAsMention(true))
	}

	fun getRandomAvatar(context: CommandContext, choosen: MutableList<Member>): BufferedImage {
		val list = context.guild.members.toMutableList()
		list.removeAll(choosen)

		var userAvatar: String? = null
		while (userAvatar == null) {
			if (list.isEmpty()) { // omg, lista vazia!
				// Vamos pegar um usuário aleatório e vamos cair fora daqui!
				userAvatar = context.guild.members.random().user.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
				break
			}
			val member = list[Loritta.RANDOM.nextInt(list.size)]
			userAvatar = member.user.avatarUrl
			if (userAvatar == null)
				list.remove(member)
			else
				choosen.add(member)
		}

		return LorittaUtils.downloadImage(userAvatar!!) ?: Constants.IMAGE_FALLBACK
	}
}