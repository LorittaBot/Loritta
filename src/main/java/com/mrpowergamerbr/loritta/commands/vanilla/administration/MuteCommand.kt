package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
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
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.exceptions.HierarchyException
import java.awt.Color

class MuteCommand : AbstractCommand("mute", listOf("mutar", "silenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MUTE_DESCRIPTION"]
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será silenciado")
	}

	override fun getExample(): List<String> {
		return listOf("@Fulano");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var user = LorittaUtils.getUserFromContext(context, 0)

			if (user == null) {
				context.reply(
						LoriReply(
								message = "Hmmmm, como será que eu posso mutar um usuário que nem está neste servidor?",
								prefix = "<:erro:326509900115083266>"
						)
				)
				return
			}

			if (user.id == Loritta.config.clientId) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("MUTE_CANT_MUTE_ME"))
				return
			}

			val member = context.guild.getMember(user)

			if (member == null) {
				context.reply(
						LoriReply(
								message = "Hmmmm, como será que eu posso mutar um usuário que nem está neste servidor?",
								prefix = "<:erro:326509900115083266>"
						)
				)
				return
			}

			var mutedRole: Role? = context.guild.getRolesByName(locale["MUTE_ROLE_NAME"], false).firstOrNull()

			if (mutedRole != null && member.roles.contains(mutedRole)) {
				val serverConfig = loritta.getServerConfigForGuild(context.guild.id)
				val userData = serverConfig.getUserData(user.id)

				userData.isMuted = false
				userData.temporaryMute = false

				loritta save serverConfig

				context.guild.controller.removeRolesFromMember(member, mutedRole).complete()

				context.reply(LoriReply(
						message = context.locale["MUTE_SUCCESS_OFF", "${user.name}#${user.discriminator}"],
						prefix = "\uD83D\uDC35"
					)
				)
				return
			}
			context.metadata["user"] = user

			if (context.args.size >= 2) {
				val reason = context.args.clone().remove(0).joinToString(" ")
				context.metadata["reason"] = reason
			}

			context.metadata["started"] = true

			val message = context.reply(
					LoriReply(
							message = "Por quanto tempo você deseja silenciar o usuário? (`1 hora`, `5 minutos`, etc) Caso queira que o usuário fique silenciado permanente, reaja com \uD83D\uDD04",
							prefix = "🤔"
					)
			)

			message.addReaction("\uD83D\uDD04").complete()
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

		if (e.reactionEmote.name == "\uD83D\uDD04") {
			muteUser(context, null)
			return
		}

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

		muteUser(context, time)
	}

	fun muteUser(context: CommandContext, time: Long?) {
		context.metadata["cancelled"] = true
		val user = context.metadata["user"] as User

		// Vamos pegar se a nossa role existe
		var mutedRoles = context.guild.getRolesByName(context.locale["MUTE_ROLE_NAME"], false)
		var mutedRole: Role? = null
		if (mutedRoles.isEmpty()) {
			// Se não existe, vamos criar ela!
			mutedRole = context.guild.controller.createRole()
					.setName(context.locale["MUTE_ROLE_NAME"])
					.setColor(Color.BLACK)
					.complete()
		} else {
			// Se existe, vamos carregar a atual
			mutedRole = mutedRoles[0]
		}

		// E agora vamos pegar todos os canais de texto do servidor
		for (textChannel in context.guild.textChannels) {
			var permissionOverride = textChannel.getPermissionOverride(mutedRole)
			if (permissionOverride == null) { // Se é null...
				textChannel.createPermissionOverride(mutedRole)
						.setDeny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
						.complete()
			} else {
				if (permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
					permissionOverride.manager
							.deny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
							.complete()
				}
			}
		}

		// E... finalmente... iremos dar (ou remover) a role para o carinha
		var member = context.guild.getMemberById(user.id)

		if (member == null) {
			context.metadata["cancelled"] = true
			context.reply(
					LoriReply(
							message = "Hmmmm, como será que eu posso mutar um usuário que nem está neste servidor?",
							prefix = "<:erro:326509900115083266>"
					)
			)
			return
		}
		try {
			val addRole = context.guild.controller.addRolesToMember(member, mutedRole)

			if (context.metadata["reason"] != null) {
				addRole.reason(context.metadata["reason"] as String)
			}

			addRole.complete()

			val serverConfig = loritta.getServerConfigForGuild(context.guild.id)
			val userData = serverConfig.getUserData(user.id)

			userData.isMuted = true
			if (time != null) {
				userData.temporaryMute = true
				userData.expiresIn = System.currentTimeMillis() + (time * 1000)
			} else {
				userData.temporaryMute = false
			}

			context.reply(
					LoriReply(
							message = context.locale["MUTE_SUCCESS_ON", "${user.name}#${user.discriminator}"],
							prefix = "\uD83D\uDE4A"
					)
			)

			loritta save serverConfig
		} catch (e: HierarchyException) {
			context.metadata["cancelled"] = true
			context.reply(
					LoriReply(
							message = "Eu não consigo mutar o usuário pois o cargo de `${mutedRole.name}` é acima do meu! Mova ele para baixo do meu e tente novamente!",
							prefix = "<:erro:326509900115083266>"
					)
			)
		}
	}
}
