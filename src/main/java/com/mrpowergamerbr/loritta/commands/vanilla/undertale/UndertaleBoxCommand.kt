package com.mrpowergamerbr.loritta.commands.vanilla.undertale

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class UndertaleBoxCommand : AbstractCommand("utbox", listOf("undertalebox"), CommandCategory.UNDERTALE) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UTBOX_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta Legendary being made of every SOUL in the underground.")
	}

	override fun getUsage(): String {
		return "usuário (caso queira) mensagem"
	}

	override fun needsToUploadFiles(): Boolean {
		return true;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		try {
			if (context.args.size >= 1) {
				var member = context.handle
				if (context.message.mentionedUsers.size == 1) {
					member = context.guild.getMember(context.message.mentionedUsers[0])
				}
				var str = context.args.joinToString(" ") // Primeiro nós juntamos tudo
				// Mas ok, ainda tem uma coisa chamada "nome do usuário mencionado"
				// Sabe o que a gente faz com ele? Gambiarra!
				// TODO: Menos gambiarra
				str = str.replace("@" + member.effectiveName + " ", "")
				val bi = ImageIO.read(File(Loritta.ASSETS + "undertale_dialogbox.png"))
				val graph = bi.graphics

				val determinationMono = Constants.DETERMINATION_MONO
				graph.font = determinationMono.deriveFont(Font.PLAIN, 27f)
				graph.color = Color.WHITE

				// graph.getFontMetrics(determinationMono) tem problemas, a width do char é sempre 1 (bug?)
				ImageUtils.drawTextWrap(str, 180, 56 + determinationMono.size, 578, 0, graph.fontMetrics, graph)

				val avatarImg = LorittaUtils.downloadImage(member.user.effectiveAvatarUrl).getScaledInstance(128, 128, Image.SCALE_SMOOTH)

				val blackWhite = BufferedImage(avatarImg.getWidth(null), avatarImg.getHeight(null), BufferedImage.TYPE_BYTE_GRAY)
				val g2d = blackWhite.createGraphics()
				g2d.drawImage(avatarImg, 0, 0, null)
				blackWhite.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)

				if (true) {
					// TODO: This
					// graph.font = Constants.MINECRAFTIA.deriveFont(Font.PLAIN, 8f)

					val x = 0
					val y = 166
					graph.color = Color.BLACK
					graph.drawString(member.user.name + "#" + member.user.discriminator, x + 1, y + 12)
					graph.drawString(member.user.name + "#" + member.user.discriminator, x + 1, y + 14)
					graph.drawString(member.user.name + "#" + member.user.discriminator, x, y + 13)
					graph.drawString(member.user.name + "#" + member.user.discriminator, x + 2, y + 13)
					graph.color = Color.WHITE
					graph.drawString(member.user.name + "#" + member.user.discriminator, x + 1, y + 13)
				}

				graph.drawImage(blackWhite, 20, 22, null)

				context.sendFile(bi, "undertale_box.png", context.getAsMention(true))
			} else {
				context.explain()
			}
		} catch (e: IOException) {
			// TODO Auto-generated catch block
			e.printStackTrace()
		}

	}
}