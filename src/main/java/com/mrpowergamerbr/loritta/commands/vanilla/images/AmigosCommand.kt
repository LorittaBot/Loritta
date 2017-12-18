package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.dv8tion.jda.core.entities.Member
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class AmigosCommand : AbstractCommand("amigos", listOf("friends", "meusamigos", "myfriends")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.AMIGOS_DESCRIPTION.f();
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val choosen = mutableListOf<Member>()

		var contextImage = LorittaUtils.getImageFromContext(context, 0, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage2 = LorittaUtils.getImageFromContext(context, 1, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage3 = LorittaUtils.getImageFromContext(context, 2, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage4 = LorittaUtils.getImageFromContext(context, 3, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage5 = LorittaUtils.getImageFromContext(context, 4, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage6 = LorittaUtils.getImageFromContext(context, 5, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage7 = LorittaUtils.getImageFromContext(context, 6, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage8 = LorittaUtils.getImageFromContext(context, 7, 0, 128) ?: getRandomAvatar(context, choosen)
		var contextImage9 = LorittaUtils.getImageFromContext(context, 8, 0, 128) ?: getRandomAvatar(context, choosen)

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
		val graphics = finalImage.graphics;

		graphics.drawImage(contextImage, 0, 0, null)
		graphics.drawImage(contextImage2, 128, 0, null)
		graphics.drawImage(contextImage3, 256, 0, null)
		graphics.drawImage(contextImage4, 0, 128, null)
		graphics.drawImage(contextImage5, 128, 128, null)
		graphics.drawImage(contextImage6, 256, 128, null)
		graphics.drawImage(contextImage7, 0, 256, null)
		graphics.drawImage(contextImage8, 128, 256, null)
		graphics.drawImage(contextImage9, 256, 256, null)

		val template = ImageIO.read(File(Loritta.ASSETS + "thx.png"))
		graphics.drawImage(template, 0, 0, null)

		context.sendFile(finalImage, "thx.png", context.getAsMention(true));
	}

	fun getRandomAvatar(context: CommandContext, choosen: MutableList<Member>): BufferedImage {
		var list = context.guild.members.toMutableList()
		list.removeAll(choosen)

		var userAvatar: String? = null;
		while (userAvatar == null) {
			if (list.isEmpty()) { // omg, lista vazia!
				// Vamos pegar um usuário aleatório e vamos cair fora daqui!
				userAvatar = context.guild.members[Loritta.RANDOM.nextInt(context.guild.members.size)].user.effectiveAvatarUrl
				break
			}
			val member = list[Loritta.RANDOM.nextInt(context.guild.members.size)]
			userAvatar = member.user.avatarUrl
			if (userAvatar == null)
				list.remove(member)
			else
				choosen.add(member)
		}

		val newImage = LorittaUtils.downloadImage(userAvatar);
		if (newImage != null) {
			return newImage;
		} else {
			throw RuntimeException("Image is null")
		}
	}
}