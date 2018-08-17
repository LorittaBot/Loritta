package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.TretaNewsGenerator
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.User
import java.util.*

class TretaNewsCommand : AbstractCommand("tretanews", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["TRETANEWS_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "[usuário1] [usuário2]"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("", "@Loritta @MrPowerGamerBR")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("usuário1" to "*(Opcional)* \"YouTuber\" sortudo que apareceu no Treta News",
				"usuário2" to "*(Opcional)* \"YouTuber\" sortudo que apareceu no Treta News")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var user1: User? = context.getUserAt(0)
		var user2: User? =  context.getUserAt(1)

		if (user1 == null) {
			var member1 = context.guild.members[RANDOM.nextInt(context.guild.members.size)]

			while (member1.onlineStatus == OnlineStatus.OFFLINE) {
				member1 = context.guild.members[RANDOM.nextInt(context.guild.members.size)]
			}

			user1 = member1.user
		}

		if (user2 == null) {
			var member2 = context.guild.members[RANDOM.nextInt(context.guild.members.size)]

			while (member2.onlineStatus == OnlineStatus.OFFLINE) {
				member2 = context.guild.members[RANDOM.nextInt(context.guild.members.size)]
			}

			user2 = member2.user
		}

		val base = TretaNewsGenerator.generate(context.guild, context.guild.getMember(user1), context.guild.getMember(user2))

		context.reply(base.image, "tretanews.png",
				LoriReply(
						message = "VOOOOOOCÊ ESTÁ ASSISTINDO TRETA NEWS E VAMOS DIRETO PARA AS NOTÍCIAAAAAAAAS!",
						prefix = "<:fluffy:372454445721845761>"
				),
				LoriReply(
						message = "`${base.title}`",
						mentionUser = false
				),
				LoriReply(
						message = "\uD83D\uDCFA `${base.views}` **${context.locale["MUSICINFO_VIEWS"]}**, \uD83D\uDE0D `${base.likes}` **${context.locale["MUSICINFO_LIKES"]}**, \uD83D\uDE20 `${base.dislikes}` **${context.locale["MUSICINFO_DISLIKES"]}**",
						prefix = "\uD83D\uDCC8",
						mentionUser = false
				)
		)
	}
}