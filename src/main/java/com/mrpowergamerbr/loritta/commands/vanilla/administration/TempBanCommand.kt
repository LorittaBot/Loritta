package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.convertToSpan
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.remove
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent

class TempBanCommand : CommandBase("tempban") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["TEMPBAN_Description"]
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será temporariamente banido")
	}

	override fun getExample(): List<String> {
		return listOf("@Fulano");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN;
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var user = LorittaUtils.getUserFromContext(context, 0)

			if (user.id == Loritta.config.clientId) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("TEMPBAN_CantBanMe"))
				return
			}

			if (context.args.size >= 2) {
				val reason = context.args.clone().remove(0).joinToString(" ")
				context.metadata["reason"] = reason
			}

			context.metadata["started"] = true

			val message = context.reply(
					LoriReply(
							message = "Por quanto tempo você deseja banir o usuário? (`1 hora`, `5 minutos`, etc)",
							prefix = "🤔"
					)
			)

			context.metadata["user"] = user

			// message.addReaction("\uD83D\uDD04").complete()
			message.addReaction("\uD83D\uDE45").complete()
		} else {
			this.explain(context);
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		super.onCommandReactionFeedback(context, e, msg)
		if (context.metadata.containsKey("cancelled"))
			return

		if (e.member.user.id != context.userHandle.id)
			return

		if (!context.metadata.containsKey("started"))
			return

		if (e.reactionEmote.name == "\uD83D\uDE45") {
			context.metadata["cancelled"] = true
			context.reply(
					LoriReply(
							message = "Decidiu ter pena do usuário?",
							prefix = "<:LorittaThinking3:346810804613414912>"
					)
			)
			return
		}
	}

	override fun onCommandMessageReceivedFeedback(context: CommandContext, e: MessageReceivedEvent, msg: Message) {
		super.onCommandMessageReceivedFeedback(context, e, msg)
		if (context.metadata.containsKey("cancelled"))
			return

		if (e.member.user.id != context.userHandle.id)
			return

		if (!context.metadata.containsKey("started"))
			return

		var time = e.message.content.convertToSpan()

		if (time == 0L) {
			context.metadata["cancelled"] = true
			context.reply(
					LoriReply(
							message = "`${e.message.content}` não é um tempo válido!",
							prefix = "<:erro:326509900115083266>"
					)
			)
			return
		}

		banUser(context, time)
	}

	fun banUser(context: CommandContext, time: Long) {
		context.metadata["cancelled"] = true
		val user = context.metadata["user"] as User

		val config = loritta.getServerConfigForGuild(context.guild.id)

		config.temporaryBans[user.id] = System.currentTimeMillis() + time

		try {
			context.guild.controller.ban(user, 0).complete()

			context.reply(
					LoriReply(
							"Usuário temporariamente banido!",
							"<:banhammer:380424348382658561>"
					)
			)
			loritta save config
		} catch (e: Exception) {
			context.metadata["cancelled"] = true
			context.reply(
					LoriReply(
							message = "Eu não consigo banir o usuário pois ele possui um cargo que é acima do meu! Mova ele para baixo do meu e tente novamente!",
							prefix = "<:erro:326509900115083266>"
					)
			)
		}
	}
}