package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.minecraft.MCUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class McSkinCommand : AbstractCommand("mcskin", listOf("skinsteal", "skinstealer"), CommandCategory.MINECRAFT) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.mcskin.description")
	override fun getExamplesKey() = LocaleKeyData("commands.category.minecraft.skinPlayerNameExamples")

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val profile = MCUtils.getUserProfileFromName(nickname)

			if (profile == null) {
				context.reply(
                        LorittaReply(
								locale["commands.category.minecraft.unknownPlayer", context.args.getOrNull(0)],
                                Constants.ERROR
                        )
				)
				return
			}

			if (!profile.textures.containsKey("SKIN")) {
				context.reply(
                        LorittaReply(
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