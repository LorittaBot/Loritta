package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import java.util.*

class HesayCommand : CommandBase() {
	override fun getLabel(): String {
		return "hesay";
	}
	
	override fun getDescription(locale: BaseLocale): String {
		return "Faça alguem dizer algo"
	}
	override fun getExample(): List<String> {
		return Arrays.asList("@NotSoBaleia Eu sou legal")
	}
	
	override fun getUsage(): String {
        return "mencão mensagem"
    }

	
	override fun getCategory(): CommandCategory {
		return CommandCategory.MISC;
	}
	
	
		override fun run(context: CommandContext) {

			if (context.args.size >= 2) {
			
					context.message.delete().complete()
					
			val user = context.message.mentionedUsers[0]
			
				var message: String? = null;
					message = context.args.toList().subList(1, context.args.size).joinToString(separator = " ");
					message = message.escapeMentions()
			
				val webhook = getOrCreateWebhook(context.event.textChannel, "hesay")
				context.sendMessage(webhook, DiscordMessage.builder()
								.username(user.name)
								.content(message)
								.avatarUrl(user.getEffectiveAvatarUrl())
								.build())
				return
			} else {
					context.explain()
		}
	}
}
