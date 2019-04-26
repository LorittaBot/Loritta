package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.WebhookUtils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.embed.AuthorEmbed
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.*

class QuoteCommand : AbstractCommand("quote", listOf("mencionar", "responder", "r", "reply"), CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("MENCIONAR_DESCRIPTION")
	}

	override fun hasCommandFeedback(): Boolean {
		return false
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("msgId Olá!")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isEmpty()) {
			context.explain()
			return
		}

		if (context.args[0].isValidSnowflake()) {
			var msg: Message? = null
			try {
				msg = context.event.textChannel!!.retrieveMessageById(context.args[0]).await()
			} catch (e: ErrorResponseException) {
			}

			if (msg != null) {
				if (context.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
					context.message.delete().queue() // ok, vamos deletar a msg original
				}

				val args = context.rawArgs.toMutableList()
				args.removeAt(0)

				val content = msg.author.asMention + " " + args.joinToString(" ").escapeMentions()

				val embed = DiscordEmbed
						.builder()
						.description(msg.contentRaw)
						.author(AuthorEmbed(msg.author.name + " ${context.legacyLocale["MENCIONAR_SAID"]}...", null, msg.author.effectiveAvatarUrl, null))
						// .color(msg.member.color.rgb)
						.footer(FooterEmbed("em #" + context.message.textChannel.name + if (context.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) "" else " | Não tenho permissão para deletar mensagens!", null, null))
						.build()

				val dm = DiscordMessage
						.builder()
						.avatarUrl(context.message.author.effectiveAvatarUrl)
						.username(context.message.author.name)
						.content(content)
						.embed(embed)
						.build()


				// dm.embeds = Arrays.asList(embed)

				val temmie = getOrCreateWebhook(context.event.textChannel!!, "Quote Webhook")

				temmie!!.sendMessage(dm)
			} else {
				context.sendMessage(Constants.ERROR + " **|** ${context.getAsMention(true)}" + context.legacyLocale.get("MENCIONAR_UNKNOWN_MESSAGE"))
			}
		} else {
			context.sendMessage(Constants.ERROR + " **|** ${context.getAsMention(true)}" + context.legacyLocale.get("MENCIONAR_NOT_VALID_SNOWFLAKE", context.args[0]))
		}
	}
}