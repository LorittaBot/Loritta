package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.RegisterConfig
import com.mrpowergamerbr.loritta.modules.register.RegisterHolder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.RegisterConfigs
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageChannel
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction

class RegisterCommand : AbstractCommand("register", listOf("registrar"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["AFK_Description"]
	}

	suspend fun sendStep(context: CommandContext, channel: MessageChannel, config: RegisterHolder, stepIndex: Int, answers: MutableList<RegisterHolder.RegisterOption>) {
		val step = config.step.getOrNull(stepIndex)

		val registerConfig = transaction(Databases.loritta) {
			RegisterConfig.find { RegisterConfigs.id eq context.guild.idLong }.firstOrNull()
		}

		if (step == null) { // Se for null, quer dizer que o usuário completou todas as perguntas!
			channel.sendMessage("Obrigada por responder nosso questionário de registro marotex! https://i.imgur.com/Rl4198r.png").await()

			for (answer in answers) {
				val guild = context.guild
				val role = guild.getRoleById(answer.roleId) ?: continue
				guild.addRoleToMember(context.handle, role).queue()
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
			// Sim, é necessário pegar a mensagem DE NOVO para pegar os valores das reações atualizados
			val reactedMessage = channel.retrieveMessageById(message.id).await()

			val reactions = reactedMessage.reactions.filter { it.count > 1} // Como é apenas via DM, se as reações forem maiores que 1 == o usuário reagiu!
			val answersMade = step.options.filter {
				reactions.any { storedEmote ->
					it.emote == storedEmote.reactionEmote.name || (storedEmote.reactionEmote.id != null && it.emote.split(":").getOrNull(1) == storedEmote.reactionEmote.id) }
			}

			if (step.maxAnswers > answersMade.size) {
				return@onReactionAddByAuthor
			}

			message.delete().queue()
			answers.addAll(answersMade)
			sendStep(context, channel, config, stepIndex + 1, answers)
		}
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.guild.id != "297732013006389252")
			return

		val privateChannel = context.userHandle.openPrivateChannel().await()

		/*
		val registerConfig = RegisterHolder(
				step = listOf(
						RegisterHolder.RegisterStep(
								"are u a novinha or a novinha?",
								"owo nós precisamos saber",
								"https://loritta.website/assets/img/fanarts/Loritta_Dormindo_-_Ayano.png",
								1,
								listOf(
										RegisterHolder.RegisterOption(
												"\uD83D\uDD35",
												"513303483659714586"
										),
										RegisterHolder.RegisterOption(
												"\uD83D\uDD34",
												"513303519348916224"
										)
								)
						),
						RegisterHolder.RegisterStep(
								"biscoito ou bolacha?",
								"A resposta certa é bolacha e você sabe disso",
								"https://guiadacozinha.com.br/wp-content/uploads/2016/11/torta-holandesa-facil.jpg",
								1,
								listOf(
										RegisterHolder.RegisterOption(
												"\uD83D\uDD35",
												"513303531026120704"
										),
										RegisterHolder.RegisterOption(
												"\uD83D\uDD34",
												"513303543911022593"
										)
								)
						),
						RegisterHolder.RegisterStep(
								"escolhe algo filosófico ai",
								"você pode escolher até DUAS COISAS diferentes, wow!",
								null,
								2,
								listOf(
										RegisterHolder.RegisterOption(
												"krisnite:508811243994480641",
												"513310935511728130"
										),
										RegisterHolder.RegisterOption(
												"ralseinite:508811387175436291",
												"513310965647933443"
										),
										RegisterHolder.RegisterOption(
												"vieirinha:412574915879763982",
												"513310993326014464"
										)
								)
						)
				)
		) */

		val registerConfig = transaction(Databases.loritta) {
			RegisterConfig.find { RegisterConfigs.id eq context.guild.idLong }.firstOrNull()
		}

		if (registerConfig == null) {
			context.sendMessage("derp")
			return
		}

		val registerHolder = registerConfig.holder

		// Retirar todos os cargos existentes do usuário relacionados ao registro
		val flatMap = registerHolder.step.flatMap { it.options }
		val rolesToBeRemoved = context.handle.roles.filter { flatMap.any { option -> it.id == option.roleId }}
		if (rolesToBeRemoved.isNotEmpty()) {
			context.guild.modifyMemberRoles(context.handle, context.handle.roles.toMutableList().apply { this.removeAll(rolesToBeRemoved) }).queue()
		}

		// Vamos começar
		sendStep(context, privateChannel, registerHolder, 0, mutableListOf())
	}
}