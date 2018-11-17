package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.register.RegisterConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageChannel

class RegisterCommand : AbstractCommand("register", listOf("registrar"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AFK_Description"]
	}

	suspend fun sendStep(context: CommandContext, channel: MessageChannel, config: RegisterConfig, stepIndex: Int, answers: MutableList<RegisterConfig.RegisterOption>) {
		val step = config.step.getOrNull(stepIndex)

		if (step == null) { // Se for null, quer dizer que o usuário completou todas as perguntas!
			channel.sendMessage("Obrigada por responder nosso questionário de registro marotex! https://i.imgur.com/Rl4198r.png").await()

			for (answer in answers) {
				val guild = context.guild
				val role = guild.getRoleById(answer.roleId)
				guild.controller.addSingleRoleToMember(context.handle, role).queue()
			}
			return
		}

		// Se não...
		val embed = EmbedBuilder().apply {
			setTitle(step.title)
			setDescription(step.description)
			if (step.thumbnail != null)
				setThumbnail(step.thumbnail)
		}

		val message = channel.sendMessage(embed.build()).await()
		for (option in step.options) {
			message.addReaction(option.emote).queue()
		}

		message.onReactionAddByAuthor(context) { event ->
			val answer = step.options.firstOrNull { it.emote == event.reactionEmote.name } ?: return@onReactionAddByAuthor
			message.delete().queue()
			answers.add(answer)
			sendStep(context, channel, config, stepIndex + 1, answers)
		}
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.guild.id != "297732013006389252")
			return

		val privateChannel = context.userHandle.openPrivateChannel().await()

		val registerConfig = RegisterConfig(
				step = listOf(
						RegisterConfig.RegisterStep(
								"are u a novinha or a novinha?",
								"owo nós precisamos saber",
								"https://loritta.website/assets/img/fanarts/Loritta_Dormindo_-_Ayano.png",
								listOf(
									RegisterConfig.RegisterOption(
											"\uD83D\uDD35",
											"513303483659714586"
									),
									RegisterConfig.RegisterOption(
											"\uD83D\uDD34",
											"513303519348916224"
									)
								)
						),
						RegisterConfig.RegisterStep(
								"biscoito ou bolacha?",
								"A resposta certa é bolacha e você sabe disso",
								"https://guiadacozinha.com.br/wp-content/uploads/2016/11/torta-holandesa-facil.jpg",
								listOf(
										RegisterConfig.RegisterOption(
												"\uD83D\uDD35",
												"513303531026120704"
										),
										RegisterConfig.RegisterOption(
												"\uD83D\uDD34",
												"513303543911022593"
										)
								)
						)
				)
		)

		// Vamos começar
		sendStep(context, privateChannel, registerConfig, 0, mutableListOf())
	}
}