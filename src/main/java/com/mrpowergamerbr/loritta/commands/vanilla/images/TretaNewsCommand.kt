package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.TretaNewsGenerator
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.User
import java.util.*

class TretaNewsCommand : CommandBase() {
	override fun getLabel(): String {
		return "tretanews"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.TRETANEWS_DESCRIPTION.f()
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

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun run(context: CommandContext) {
		var user1: User? = null
		var user2: User? = null

		if (context.message.mentionedUsers.size >= 1) {
			user1 = context.message.mentionedUsers[0]
		}

		if (context.message.mentionedUsers.size >= 2) {
			user2 = context.message.mentionedUsers[1]
		}

		if (user1 == null) {
			var member1 = context.guild.members[SplittableRandom().nextInt(context.guild.members.size)]

			while (member1.onlineStatus == OnlineStatus.OFFLINE) {
				member1 = context.guild.members[SplittableRandom().nextInt(context.guild.members.size)]
			}

			user1 = member1.user
		}

		if (user2 == null) {
			var member2 = context.guild.members[SplittableRandom().nextInt(context.guild.members.size)]

			while (member2.onlineStatus == OnlineStatus.OFFLINE) {
				member2 = context.guild.members[SplittableRandom().nextInt(context.guild.members.size)]
			}

			user2 = member2.user
		}

		val base = TretaNewsGenerator.generate(context.guild, context.guild.getMember(user1), context.guild.getMember(user2))

		val builder = MessageBuilder()
		builder.append(context.getAsMention(true) + "VOOOOOOCÊ ESTÁ ASSISTINDO TRETA NEWS E VAMOS DIRETO PARA AS NOTÍCIAAAAAAAAS!")

		if (false) {
			builder.append(" ")
			builder.append(user1!!)
			builder.append(" ")
			builder.append(user2!!)
		} else {

		}

		context.sendFile(base, "tretanews.png", builder.build())
	}
}