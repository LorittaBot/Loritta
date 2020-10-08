package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.TretaNewsGenerator
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.*

class TretaNewsCommand : AbstractCommand("tretanews", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["TRETANEWS_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "[usuário1] [usuário2]"
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("", "@Loritta @MrPowerGamerBR")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("usuário1" to "*(Opcional)* \"YouTuber\" sortudo que apareceu no Treta News",
				"usuário2" to "*(Opcional)* \"YouTuber\" sortudo que apareceu no Treta News")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val user1 = context.getUserAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val user2 = context.getUserAt(1) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val base = TretaNewsGenerator.generate(context.guild, context.guild.getMember(user1)!!, context.guild.getMember(user2)!!)

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
                        message = "\uD83D\uDCFA `${base.views}` **${context.legacyLocale["MUSICINFO_VIEWS"]}**, \uD83D\uDE0D `${base.likes}` **${context.legacyLocale["MUSICINFO_LIKES"]}**, \uD83D\uDE20 `${base.dislikes}` **${context.legacyLocale["MUSICINFO_DISLIKES"]}**",
                        prefix = "\uD83D\uDCC8",
                        mentionUser = false
                )
		)
	}
}