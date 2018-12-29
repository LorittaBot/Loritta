package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.minecraft.MCUtils

class McSkinCommand : AbstractCommand("mcskin", listOf("skinsteal", "skinstealer"), CommandCategory.MINECRAFT) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("MCSKIN_Description")
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExamples(): List<String> {
		return listOf("Monerk")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val profile = MCUtils.getUserProfileFromName(nickname)

			if (profile == null) {
				context.reply(
						LoriReply(
								locale["MCSKIN_UnknownPlayer", context.args.getOrNull(0)],
								Constants.ERROR
						)
				)
				return
			}

			if (!profile.textures.containsKey("SKIN")) {
				context.reply(
						LoriReply(
								"Player não possui skin!",
								Constants.ERROR
						)
				)
				return
			}

			val bufferedImage = LorittaUtils.downloadImage(profile.textures["SKIN"]!!.url)

			context.sendFile(bufferedImage!!, "${nickname}.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}
}