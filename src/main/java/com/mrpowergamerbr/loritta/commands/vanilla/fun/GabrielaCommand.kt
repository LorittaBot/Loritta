package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.gabriela.Gabriela
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.webhook.DiscordWebhook
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import net.dv8tion.jda.core.entities.Message
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLEncoder

class GabrielaCommand : AbstractCommand("gabriela", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String = locale["FRASETOSCA_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun hasCommandFeedback(): Boolean = false

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

		if (context.args.isNotEmpty()) {
			val webhook = getOrCreateWebhook(context.event.textChannel, locale["FRASETOSCA_GABRIELA"])

			var pergunta = context.strippedArgs.joinToString(" ").toLowerCase().trim() // Já que nós não ligamos se o cara escreve "Nilce" ou "nilce"

			for ((regex, replace) in corretores) {
				pergunta = pergunta.replace(Regex(regex), replace)
			}

			pergunta = pergunta.replace(Regex("\\p{P}"), "")

			pergunta = StringUtils.stripAccents(pergunta)

			val split = pergunta.split(" ")

			val perguntas = mutableSetOf<String>()

			for (n in split.size downTo 1) {
				perguntas.add(split.joinToString(" ", limit = n, truncated = "").trim())
			}

			split.forEach {
				val aux = it

				if (aux.length >= 3) {
					perguntas.add(it)
				}
			}

			val discordWebhook = DiscordWebhook(webhook!!.url)

			val documents = loritta.gabrielaMessagesColl.find(
					Filters.`in`("_id", perguntas)
			).toMutableList()

			if (documents.isNotEmpty()) {
				val levensteinDistance = LevenshteinDistance()
				var distance = 99
				var document = documents.first()

				for (aux in documents) {
					val dist = levensteinDistance.apply(pergunta, aux.question)

					if (distance > dist) {
						distance = dist
						document = aux
					}
				}

				discordWebhook.send(
						com.mrpowergamerbr.loritta.utils.webhook.DiscordMessage(
								context.locale["FRASETOSCA_GABRIELA"],
								context.getAsMention(true) + document.answers[RANDOM.nextInt(document.answers.size)].escapeMentions(),
								"https://loritta.website/assets/img/gabriela_avatar.png"
						),
						true,
						{
							val messageId = it["id"].string
							val functions = loritta.messageInteractionCache.getOrPut(messageId) { MessageInteractionFunctions(context.guild.id, context.userHandle.id) }
							val message = context.message.textChannel.getMessageById(messageId).complete()

							if (message != null) {
								learnGabriela(pergunta, corretores, message, context, functions)
							}
						}
				)
			} else {
				discordWebhook.send(
						com.mrpowergamerbr.loritta.utils.webhook.DiscordMessage(
								context.locale["FRASETOSCA_GABRIELA"],
								"${context.getAsMention(true)}Eu não sei uma resposta para esta pergunta! \uD83D\uDE22 — Se você quer me ensinar, clique no \uD83D\uDCA1!",
								"https://loritta.website/assets/img/gabriela_avatar.png"
						),
						true,
						{
							val messageId = it["id"].string
							val functions = loritta.messageInteractionCache.getOrPut(messageId) { MessageInteractionFunctions(context.guild.id, context.userHandle.id) }
							val message = context.message.textChannel.getMessageById(messageId).complete()

							if (message != null) {
								learnGabriela(pergunta, corretores, message, context, functions)
							}
						}
				)
			}
		} else {
			context.explain()
		}
		return
	}

	fun learnGabriela(pergunta: String, corretores: Map<String, String>, message: Message, context: CommandContext, functions: MessageInteractionFunctions) {
		message.addReaction("\uD83D\uDCA1").complete()

		functions.onReactionAddByAuthor = {
			if (it.reactionEmote.name == "\uD83D\uDCA1") {
				val ask = context.reply(
						LoriReply(
								"Quando alguém perguntar `${pergunta.stripCodeMarks()}`, o que a Gabriela deve responder?",
								"\uD83E\uDD14"
						)
				)

				ask.onResponseByAuthor(context) {
					val pergunta = pergunta.trim()
					ask.delete().queue()
					var deveResponder = it.message.contentStripped

					// Nós agora iremos pegar se a Gabriela já aprendeu alguma resposta para esta frase, se não, nós iremos criar uma
					val document = loritta.gabrielaMessagesColl.find(
							Filters.eq("_id", pergunta)
					).firstOrNull() ?: Gabriela.GabrielaMessage(pergunta, "default")

					document.answers.add(deveResponder)
					// upsert = Se já existe, apenas dê replace, se não existe, insira
					val updateOptions = UpdateOptions().upsert(true)
					loritta.gabrielaMessagesColl.replaceOne(
							Filters.eq("_id", pergunta),
							document,
							updateOptions
					)

					context.reply(
							LoriReply(
									"Agora a Gabriela está mais esperta, obrigada! \uD83E\uDD13",
									"\uD83D\uDCA1"
							)
					)
				}
			}
		}
	}
}