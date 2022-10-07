package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Font
import java.awt.Image
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

class TodoGrupoTemCommand(loritta: LorittaBot) : AbstractCommand(loritta, "everygrouphas", listOf("todogrupotem"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.everygrouphas.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.everygrouphas.examples")

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val bi = readImage(File(LorittaBot.ASSETS + context.locale["commands.command.everygrouphas.file"])) // Primeiro iremos carregar o nosso template

		val base = BufferedImage(366, 266, BufferedImage.TYPE_INT_ARGB) // Iremos criar uma imagem 384x256 (tamanho do template)
		val baseGraph = base.graphics.enableFontAntiAliasing()

		val users = ArrayList<User>()
		val allMembers = context.guild.retrieveMembers()
		val members = allMembers.filter { it.user.avatarUrl != null && !it.user.isBot }.toMutableList()

		users.addAll(context.message.mentionedUsers)

		while (6 > users.size) {
			val member = if (members.isEmpty()) {
				// omg
				allMembers[LorittaBot.RANDOM.nextInt(allMembers.size)]
			} else {
				members[LorittaBot.RANDOM.nextInt(members.size)]
			}

			users.add(member.user)
			members.remove(member)
		}

		var x = 0
		var y = 20

		val font = Font.createFont(0, File(LorittaBot.ASSETS + "mavenpro-bold.ttf")).deriveFont(16f)
		baseGraph.font = font
		ImageUtils.drawCenteredStringOutlined(baseGraph, locale["commands.command.everygrouphas.everygrouphas"], Rectangle(0, 0, 366, 22), font)

		for (aux in 5 downTo 0) {
			val member = users[0]

			val avatarImg = LorittaUtils.downloadImage(loritta, member.getEffectiveAvatarUrl(ImageFormat.PNG, 128))!!.getScaledInstance(122, 122, Image.SCALE_SMOOTH)
			baseGraph.drawImage(avatarImg, x, y, null)

			x += 122
			if (x >= 366) {
				y += 122
				x = 0
			}

			users.removeAt(0)
		}

		baseGraph.drawImage(bi, 0, 20, null)

		context.sendFile(base, "todo_grupo_tem.png", context.getAsMention(true))
	}
}