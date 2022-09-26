package net.perfectdreams.loritta.morenitta.commands.vanilla.minecraft

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.minecraft.MCUtils
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class McSkinCommand : AbstractCommand("mcskin", listOf("skinsteal", "skinstealer"), net.perfectdreams.loritta.common.commands.CommandCategory.MINECRAFT) {
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