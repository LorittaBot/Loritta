package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.embed.AuthorEmbed
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import net.dv8tion.jda.core.Permission
import java.util.*

class QuoteCommand : CommandBase() {
	override fun getLabel(): String {
		return "mencionar"
	}

	override fun getDescription(): String {
		return "Menciona uma mensagem, dica: para copiar o ID da mensagem, ative o modo de desenvolvedor do Discord e clique com botão direito em uma mensagem!"
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun getExample(): List<String> {
		return Arrays.asList("msgId Olá!")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 1) {
			val temmie = Loritta.getOrCreateWebhook(context.event.textChannel, "Quote Webhook")

			val msg = context.event.textChannel.getMessageById(context.args[0]).complete() ?: return

			if (context.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
				context.message.delete().complete() // ok, vamos deletar a msg original
			}

			var content = msg.author.asMention + " " + context.event.message.rawContent.replace(context.config.commandPrefix + "mencionar " + context.args[0], "").trim { it <= ' ' }
			content = content.replace("@here", "")
			content = content.replace("@everyone", "")

			val embed = DiscordEmbed
					.builder()
					.author(AuthorEmbed(msg.author.name + " disse...", null, msg.author.effectiveAvatarUrl, null))
					.color(123)
					.description(msg.rawContent)
					// .title("Wow!")
					.footer(FooterEmbed("em #" + context.message.textChannel.name + if (context.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) "" else " | Não tenho permissão para deletar mensagens!", null, null))
					.build()

			val dm = DiscordMessage
					.builder()
					.avatarUrl(context.message.author.effectiveAvatarUrl)
					.username(context.message.author.name)
					.content(content)
					.embed(embed)
					.build()


			dm.embeds = Arrays.asList(embed)

			temmie!!.sendMessage(dm)

			println("Enviado!")
		} else {
			context.explain()
		}
	}
}