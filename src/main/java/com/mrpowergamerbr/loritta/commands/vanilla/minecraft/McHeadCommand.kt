package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class McHeadCommand : CommandBase() {
	override fun getLabel(): String {
		return "mchead"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.MCHEAD_DESCRIPTION.msgFormat()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MINECRAFT
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExample(): List<String> {
		return listOf("Monerk")
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val bufferedImage = LorittaUtils.downloadImage("https://crafatar.com/renders/head/$nickname?size=128&overlay")
			val builder = EmbedBuilder()

			builder.setColor(Color.DARK_GRAY)
			builder.setImage("attachment://avatar.png")

			builder.setTitle("<:grass:330435576392318978> ${context.locale.MCHEAD_HEAD_DE.msgFormat(nickname)}")

			context.sendFile(bufferedImage, "avatar.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

}