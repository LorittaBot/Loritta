package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.CommandOptions
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class TristeRealidadeCommand : AbstractCommand("sadreality", listOf("tristerealidade"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["TRISTEREALIDADE_DESCRIPTION"]
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val cmdOpti = context.config.getCommandOptionsFor(this) as TristeRealidadeCommandOptions

		val bi = ImageIO.read(File(Loritta.ASSETS + context.locale["TRISTEREALIDADE_FILE"])) // Primeiro iremos carregar o nosso template
		var x = 0
		var y = 0

		val base = BufferedImage(384, 256, BufferedImage.TYPE_INT_ARGB) // Iremos criar uma imagem 384x256 (tamanho do template)
		val baseGraph = base.graphics

		baseGraph.font = Constants.MINECRAFTIA.deriveFont(Font.PLAIN, 8f)

		val users = ArrayList<User>()
		var members = context.guild.members.filter { it.onlineStatus != OnlineStatus.OFFLINE && it.user.avatarUrl != null }.toMutableList()

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

		var aux = 0
		while (6 > aux) {
			val member = users[0]

			val avatarImg = LorittaUtils.downloadImage(member.effectiveAvatarUrl).getScaledInstance(128, 128, Image.SCALE_SMOOTH)
			baseGraph.drawImage(avatarImg, x, y, null)

			if (!cmdOpti.hideDiscordTags) {
				baseGraph.color = Color.BLACK
				baseGraph.drawString(member.name + "#" + member.discriminator, x + 1, y + 12)
				baseGraph.drawString(member.name + "#" + member.discriminator, x + 1, y + 14)
				baseGraph.drawString(member.name + "#" + member.discriminator, x, y + 13)
				baseGraph.drawString(member.name + "#" + member.discriminator, x + 2, y + 13)
				baseGraph.color = Color.WHITE
				baseGraph.drawString(member.name + "#" + member.discriminator, x + 1, y + 13)
			}

			x += 128
			if (x > 256) {
				x = 0
				y = 128
			}
			aux++
			users.removeAt(0)
		}

		baseGraph.drawImage(bi, 0, 0, null)

		context.sendFile(base, "triste_realidade.png", context.getAsMention(true))
	}

	class TristeRealidadeCommandOptions : CommandOptions() {
		var mentionEveryone = false // Caso esteja ativado, todos que aparecerem serão mencionados
		var hideDiscordTags = false // Caso esteja ativado, todas as discord tags não irão aparecer na imagem
	}
}