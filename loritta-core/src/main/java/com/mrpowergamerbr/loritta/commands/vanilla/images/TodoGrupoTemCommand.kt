package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.User
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class TodoGrupoTemCommand : AbstractCommand("everygrouphas", listOf("todogrupotem"), CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["TODOGRUPOTEM_Description"]
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val bi = ImageIO.read(File(Loritta.ASSETS + context.legacyLocale["TODOGRUPOTEM_File"])) // Primeiro iremos carregar o nosso template

		val base = BufferedImage(366, 266, BufferedImage.TYPE_INT_ARGB) // Iremos criar uma imagem 384x256 (tamanho do template)
		val baseGraph = base.graphics.enableFontAntiAliasing()

		val users = ArrayList<User>()
		val members = context.guild.members.filter { it.onlineStatus != OnlineStatus.OFFLINE && it.user.avatarUrl != null }.toMutableList()

		users.addAll(context.message.mentionedUsers)

		while (6 > users.size) {
			var member = if (members.isEmpty()) {
				// omg
				context.guild.members[Loritta.RANDOM.nextInt(context.guild.members.size)]
			} else {
				members[Loritta.RANDOM.nextInt(members.size)]
			}

			users.add(member.user)
			members.remove(member)
		}

		var x = 0
		var y = 20

		val font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(16f)
		baseGraph.font = font
		ImageUtils.drawCenteredStringOutlined(baseGraph, locale["TODOGRUPOTEM_EveryGroupHas"], Rectangle(0, 0, 366, 22), font)

		for (aux in 5 downTo 0) {
			val member = users[0]

			val avatarImg = LorittaUtils.downloadImage(member.effectiveAvatarUrl)!!.getScaledInstance(122, 122, Image.SCALE_SMOOTH)
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