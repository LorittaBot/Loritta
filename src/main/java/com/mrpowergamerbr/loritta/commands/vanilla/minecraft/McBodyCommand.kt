package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.minecraft.MCUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class McBodyCommand : AbstractCommand("mcbody", listOf("mcstatue"), CommandCategory.MINECRAFT) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("MCBODY_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExample(): List<String> {
		return listOf("Monerk")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		context.handle
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val uuid = MCUtils.getUniqueId(nickname)

			if (uuid == null) {
				context.reply(
						LoriReply(
								locale["MCSKIN_UnknownPlayer", context.args.getOrNull(0)],
								Constants.ERROR
						)
				)
				return
			}

			val bufferedImage = LorittaUtils.downloadImage("https://crafatar.com/renders/body/$uuid?size=128&overlay")
			val builder = EmbedBuilder()

			builder.setColor(Color.DARK_GRAY)
			builder.setImage("attachment://avatar.png")

			builder.setTitle("<:minecraft_logo:412575161041289217> ${context.locale.get("MCBODY_BODY_DE", nickname)}")

			context.sendFile(bufferedImage, "avatar.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

}