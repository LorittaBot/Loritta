package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class McSkinCommand : CommandBase() {
	override fun getLabel(): String {
		return "mcskin"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("MCAVATAR_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MINECRAFT
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getAliases(): List<String> {
		return listOf("skinsteal", "skinstealer")
	}

	override fun getExample(): List<String> {
		return listOf("Monerk")
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val bufferedImage = LorittaUtils.downloadImage("http://skins.minecraft.net/MinecraftSkins/$nickname.png")

			context.sendFile(bufferedImage, "avatar.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

}