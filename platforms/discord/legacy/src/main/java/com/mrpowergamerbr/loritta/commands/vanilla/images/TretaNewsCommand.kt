package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.TretaNewsGenerator
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class TretaNewsCommand : AbstractCommand("tretanews", category = CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.tretanews.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.tretanews.examples")

	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val user1 = context.getUserAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val user2 = context.getUserAt(1) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val base = TretaNewsGenerator.generate(context.guild, user1, user2)

		context.reply(base.image, "tretanews.png",
                LorittaReply(
                        message = "VOOOOOOCÊ ESTÁ ASSISTINDO TRETA NEWS E VAMOS DIRETO PARA AS NOTÍCIAAAAAAAAS!",
                        prefix = "<:fluffy:372454445721845761>"
                ),
                LorittaReply(
                        message = "`${base.title}`",
                        mentionUser = false
                ),
                LorittaReply(
                        message = "\uD83D\uDCFA `${base.views}` **${context.locale["commands.command.twitch.views"]}**, \uD83D\uDE0D `${base.likes}` **${context.locale["commands.command.tretanews.likes"]}**, \uD83D\uDE20 `${base.dislikes}` **${context.locale["commands.command.tretanews.dislikes"]}**",
                        prefix = "\uD83D\uDCC8",
                        mentionUser = false
                )
		)
	}
}