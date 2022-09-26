package net.perfectdreams.loritta.legacy.commands.vanilla.minecraft

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.LorittaUtils
import net.perfectdreams.loritta.legacy.utils.minecraft.MCUtils
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class McSkinCommand : AbstractCommand("mcskin", listOf("skinsteal", "skinstealer"), CommandCategory.MINECRAFT) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.mcskin.description")
	override fun getExamplesKey() = LocaleKeyData("commands.category.minecraft.skinPlayerNameExamples")

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "minecraft player skin")

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