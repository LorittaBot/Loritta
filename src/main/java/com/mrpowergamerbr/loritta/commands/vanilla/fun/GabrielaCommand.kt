package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.salomonbrys.kotson.string
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.gabriela.GabrielaAnswer
import com.mrpowergamerbr.loritta.utils.gabriela.GabrielaMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.webhook.DiscordWebhook
import net.dv8tion.jda.core.entities.Message
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.similarity.LevenshteinDistance
import org.bson.types.ObjectId
import java.util.regex.Pattern

class GabrielaCommand : AbstractCommand("gabriela", listOf("gabi"), category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String = locale["FRASETOSCA_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun hasCommandFeedback(): Boolean = false

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val corretores = mapOf(
				"(dima)" to "diamante",
				"(b(e)?l(e)?z(a)?)" to "beleza",
				"(vem ca)" to "vem cá",
				"(n(ã|a)(o|u)(m|n)?)" to "não",
				"\\b(v(o)?c(e|ê)?)\\b" to "você",
				"\\b(v(o)?c(e|ê)?(i)?s)\\b" to "vocês",
				"(v(a)?l(e)?(u|w))" to "valeu",
				"\\b(f(a)?l(o)?(u|w))\\b" to "falou",
				"(cabe(c|ç|ss)a)" to "cabeça",
				"\\b(al(g|q)(u)?(e|é)m)\\b" to "alguém",
				"\\b(al(g|q)m)\\b" to "alguém",
				"\\b(ola)\\b" to "olá",
				"\\b(ta)\\b" to "tá",
				"\\b(n)\\b" to "não",
				"\\b(eh)\\b" to "é",
				"\\b(sever)\\b" to "server",
				"\\b(doq)\\b" to "do quê",
				"\\b(a(qu|k|q)i)\\b" to "aqui",
				"\\b(q)\\b" to "que",
				"\\b(perdo)\\b" to "perto",
				"(come(c|ç|ss)o)" to "começo",
				"(fude(r)?)" to "feliz",
				"\\b(m(e)?sm(o)?)\\b" to "mesmo",
				"\\b(ag(o)?r(a)?)\\b" to "agora",
				"(q(u)?(e)?ro)" to "quero",
				"(t(am)?b(e|é)?(m|n))" to "também",
				"\\b(n(e|é)(h))\\b" to "né",
				"\\b(c(o|ó|õ)(m)?bust(i|í)v(e|é)l)\\b" to "combustível",
				"\\b(perm(((i(ç|ss|s))(a|ã)o))?)\\b" to "permissão",
				"(est(a|ã)o)" to "estão",
				"\\b((c)(e|é)(o|u))\\b" to "céu",
				"\\b(p(a|ã)o)\\b" to "pão",
				"\\b(mds)\\b" to "meo deos",
				"\\b(ne(h)?)\\b" to "né",
				"\\b(gg)\\b" to "GG",
				"\\b(otro)" to "outro",
				"\\b(l(e)?g(a)?l)\\b" to "legal",
				"\\b(ss)\\b" to "sim",
				"\\b(ata)\\b" to "ah tá",
				"\\b((i|e)nt(a|ã)o)\\b" to "então",
				"\\b(sdds)\\b" to "saudades",
				"\\b(aviao)\\b" to "avião",
				"\\b(obg)\\b" to "obrigado",
				"\\b(ja)\\b" to "já",
				"\\b(so)\\b" to "só",
				"\\b(tar)\\b" to "estar",
				"\\b(me( )?d(a|á))\\b" to "me dá",
				"\\b(area)\\b" to "área",
				"\\b(c(o)?m(i)?g(o)?)\\b" to "comigo",
				"\\b(p(o|u)?(r)?( )?q(u)?(e|ê)?)\\b" to "porque",
				"\\b(o( )?q(u)?(e|ê)?)\\b" to "o quê",
				"\\b(gra(n|b)a)\\b" to "grana",
				"\\b(cmo)\\b" to "como",
				"\\b(pd)\\b" to "pode",
				"\\b(flar)\\b" to "falar"
		)

		val wordBlacklist = mutableListOf<String>(
				"calcinha","cueca","buceta","pau","foder","fuder","vadia","crl","puta", "bucetaa","bucetaaa","cu","cú","cuu","cuuu","cuh","whatsapp","endereço","vaca","putaa","gozei","gozar","meter","meti","piranha","cadela","penetro","penetrar","boquete","boqueteira","chupa","chupar","safada","putinha","safadinha","viado","viada","gay","fdp","capeta","demonio","demônio","fudi","fudiii","arrombado","arrombada","prostituta","transa","transar","transei","transou","possuir","seu corpo","estrupar","estrupei","arrombada","piriguete","putona","novinha","novinhas","meter","meteria","comer","comeria","cama","bunda","bundinha","bucetinha","ppk","xoxota","passa o","pauzudo","bucetuda", "camisinha", "cocaína", "fude", "fudee", "viadinho", "xereca","pedofilo", "penis","pênis", "rapariga", "gostosa", "eu chupo","todinha", "sexoo","sexooo", "sex","sexo", "punheta", "siririca", "ponheta", "transaria", "comi ela", "Vo infia tão fundo", "infia", "cuzinho", "cuzao", "cuzão", "bucetinhaa", "bicha","Que tranza","tranza","pica", "pika", "me encontre", "passa","endereço", "vc mora", "você mora", "deu muito", "pika", "bct", "cuu", "gostoso", "putiane", "arrombado", "rolas", "gozo","virgindade","estrupa", "arrombar", "estrupado","estrupada", "estruparei", "chupar", "estrupador", "galinha","estrupar", "penetra","bucetuda","porra","fode", "gozada","nudes","adiciona","cu!","soca","socar","mata","matar","morrer","morre","mora","casa","pelado","pelada", "fudeee", "meteu" ,"chupo", "chupeta"
		)

		if (context.args.isNotEmpty()) {
			val webhook = getOrCreateWebhook(context.event.textChannel!!, locale["FRASETOSCA_GABRIELA"])

			var pergunta = context.strippedArgs.joinToString(" ").toLowerCase().trim() // Já que nós não ligamos se o cara escreve "Nilce" ou "nilce"

			for ((regex, replace) in corretores) {
				pergunta = pergunta.replace(Regex(regex), replace)
			}

			pergunta = pergunta.replace(Regex("\\p{P}"), "")

			pergunta = StringUtils.stripAccents(pergunta)

			val split = pergunta.split(" ")

			val perguntas = mutableSetOf<String>()

			perguntas.addAll(split)

			val discordWebhook = DiscordWebhook(webhook!!.url)

			val documents = loritta.gabrielaMessagesColl.find(
					Filters.`in`("questionWords", perguntas)
			).toMutableList()

			if (documents.isNotEmpty()) {
				var document = documents.first()
				var lastCount = 0
				var sizeMatch = false

				for (aux in documents) {
					if (sizeMatch)
						continue

					val count = aux.questionWords.count { aux.questionWords.contains(it) }

					if (count > lastCount) {
						lastCount = count
						document = aux
						sizeMatch = count == aux.questionWords.size
					}
				}

				val answers = document.answers.filter { raw ->
					wordBlacklist.forEach {
						if (raw.answer.contains(it, true)) {
							return@filter false
						}
					}
					true
				}

				if (answers.isNotEmpty()) {
					val weightedAnswers = mutableListOf<GabrielaAnswer>()
					// Nós iremos guardar qual é a pior nota...
					// Por exemplo, "-7"
					var lowestVotes = 0
					for (answer in answers) {
						if (answer.downvotes.size > lowestVotes) {
							lowestVotes = answer.downvotes.size
						}
					}

					// Agora para aplicar um weighted random, vamos transformar a nota em positiva usando absolute values
					lowestVotes = Math.abs(lowestVotes)
					// Ou seja, agora os downvotes seriam "7"!

					// E agora vamos verificar todas as respostas!
					for (answer in answers) {
						val totalUpvotes = ((answer.upvotes.size - answer.downvotes.size) + lowestVotes)
						val relative = answer.upvotes.size - answer.downvotes.size

						if (-15 > relative) { // Se a resposta possui mais de -15 downvotes totais, ela será completamente ignorada pela Gabriela
							continue
						}

						// Mesmo que seja 0..0 vai adicionar uma vez
						for (i in 0..totalUpvotes) {
							weightedAnswers.add(answer)
						}
					}

					if (weightedAnswers.isNotEmpty()) {
						// E agora... nós selecionamos a resposta (finalmente!)
						val answer = weightedAnswers[RANDOM.nextInt(weightedAnswers.size)]

						discordWebhook.send(
								com.mrpowergamerbr.loritta.utils.webhook.DiscordMessage(
										context.locale["FRASETOSCA_GABRIELA"],
										context.getAsMention(true) + answer.answer.escapeMentions(),
										"${Loritta.config.websiteUrl}assets/img/gabriela_avatar.png"
								),
								true,
								{
									val messageId = it["id"].string
									val functions = loritta.messageInteractionCache.getOrPut(messageId) { MessageInteractionFunctions(context.guild.id, context.userHandle.id) }
									val message = context.message.textChannel.getMessageById(messageId).complete()

									if (message != null) {
										learnGabriela(pergunta, message, context, functions, true, document, answer)
									}
								}
						)
						return
					}
				}
			}
			discordWebhook.send(
					com.mrpowergamerbr.loritta.utils.webhook.DiscordMessage(
							context.locale["FRASETOSCA_GABRIELA"],
							context.getAsMention(true) + locale["FRASETOSCA_DontKnow"],
							"https://loritta.website/assets/img/gabriela_avatar.png"
					),
					true,
					{
						val messageId = it["id"].string
						val functions = loritta.messageInteractionCache.getOrPut(messageId) { MessageInteractionFunctions(context.guild.id, context.userHandle.id) }
						val message = context.message.textChannel.getMessageById(messageId).complete()

						if (message != null) {
							learnGabriela(pergunta, message, context, functions)
						}
					}
			)
		} else {
			context.explain()
		}
		return
	}

	fun learnGabriela(pergunta: String, message: Message, context: CommandContext, functions: MessageInteractionFunctions, allowUpvoteDownvote: Boolean = false, document: GabrielaMessage? = null, answer: GabrielaAnswer? = null) {
		functions.onReactionAdd = {
			// UPVOTE
			if (it.reactionEmote.name == "\uD83D\uDC4D" && document != null && answer != null) {
				val message = loritta.gabrielaMessagesColl.find(
						Filters.eq(
								"_id", document.messageId
						)
				).firstOrNull()

				if (message != null) {
					val answer = message.answers.firstOrNull { answer.answer == it.answer }

					if (answer != null && !answer.upvotes.contains(context.userHandle.id)) {
						answer.downvotes.remove(context.userHandle.id)
						answer.upvotes.add(context.userHandle.id)

						val updateOptions = UpdateOptions().upsert(true)
						loritta.gabrielaMessagesColl.replaceOne(
								Filters.eq("_id", document.messageId),
								message,
								updateOptions
						)
					}
				}
			}
			// DOWNVOTE
			if (it.reactionEmote.name == "\uD83D\uDC4E" && document != null && answer != null) {
				val message = loritta.gabrielaMessagesColl.find(
						Filters.eq(
								"_id", document.messageId
						)
				).firstOrNull()

				if (message != null) {
					val answer = message.answers.firstOrNull { answer.answer == it.answer }

					if (answer != null) {
						answer.downvotes.add(context.userHandle.id)
						answer.upvotes.remove(context.userHandle.id)

						val updateOptions = UpdateOptions().upsert(true)
						loritta.gabrielaMessagesColl.replaceOne(
								Filters.eq("_id", document.messageId),
								message,
								updateOptions
						)
					}
				}
			}
		}

		functions.onReactionAddByAuthor = {
			// ENSINAR
			if (it.reactionEmote.name == "\uD83D\uDCA1") {
				val ask = context.reply(
						LoriReply(
								context.locale["FRASETOSCA_WhenSomeoneAsks", pergunta.stripCodeMarks()],
								"\uD83E\uDD14"
						)
				)

				ask.onResponseByAuthor(context) {
					val pergunta = pergunta.trim()
					ask.delete().queue()
					var deveResponder = it.message.contentStripped

					val split = pergunta.split(" ")

					val perguntas = mutableSetOf<String>()

					perguntas.addAll(split)

					// Nós agora iremos pegar se a Gabriela já aprendeu alguma resposta para esta frase, se não, nós iremos criar uma
					var document = loritta.gabrielaMessagesColl.find(
							Filters.`in`("questionWords", perguntas)
					).firstOrNull()

					if (document == null) {
						val aux = GabrielaMessage(ObjectId(), context.config.localeId)
						val questionWords = mutableSetOf<String>()
						val split = pergunta.split(" ")

						split.forEach {
							if (it.length > 2) {
								questionWords.add(it)
							}
						}

						if (questionWords.isEmpty()) {
							questionWords.addAll(split)
						}

						aux.questionWords = questionWords
						document = aux
					}

					val levensteinDistance = LevenshteinDistance()

					// Vamos verificar se não existe alguma pergunta parecida com a nova, para evitar duplicatas
					document.answers.forEach {
						if (5 > levensteinDistance.apply(deveResponder, it.answer)) {
							context.reply(
									LoriReply(
											context.locale["FRASETOSCA_TooSimilar"],
											Constants.ERROR
									)
							)
							return@onResponseByAuthor
						}
					}

					val linkRemover = "[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,7}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)".toRegex()

					val answer = GabrielaAnswer(deveResponder.replace(linkRemover, ""), it.author.id)

					document.answers.add(answer)

					// upsert = Se já existe, apenas dê replace, se não existe, insira
					val updateOptions = UpdateOptions().upsert(true)
					loritta.gabrielaMessagesColl.replaceOne(
							Filters.eq("_id", document.messageId),
							document,
							updateOptions
					)

					context.reply(
							LoriReply(
									context.locale["FRASETOSCA_ThanksForHelping"],
									"\uD83D\uDCA1"
							)
					)
				}
			}
		}

		message.addReaction("\uD83D\uDCA1").complete()
		if (allowUpvoteDownvote) {
			message.addReaction("\uD83D\uDC4D").complete()
			message.addReaction("\uD83D\uDC4E").complete()
		}
	}
}