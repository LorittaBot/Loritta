package net.perfectdreams.loritta.plugin.akilori.commands

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.doReactions
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionByAuthor
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import io.ktor.client.request.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.akinatorreapi.AkinatorAnswer
import net.perfectdreams.akinatorreapi.AkinatorClient
import net.perfectdreams.akinatorreapi.Region
import net.perfectdreams.akinatorreapi.payload.CharacterGuess
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

object AkinatorCommand {
	private const val LOCALE_PREFIX = "commands.fun.akinator"
	private const val CHARACTER_PROBABILITY = 0.85

	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("akinator"), CommandCategory.FUN) {
		description { it["commands.fun.akinator.description"] }

		botRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

		executesDiscord {
			// TODO: Load correct region
			val gameHtmlPage = loritta.http.get<String>("https://pt.akinator.com/")

			val regex = Regex("arrUrlThemesToPlay', (.*)\\);")
			val result = regex.find(gameHtmlPage)

			if (result != null) {
				val urlWs = JsonParser.parseString(result.groupValues[1])
						.array[0]["urlWs"].nullString

				if (urlWs != null) {
					val akinator = AkinatorClient(
							Region(
									"pt.akinator.com",
									urlWs.removePrefix("https://").removeSuffix("/ws")
							),
							childMode = true
					).apply {
						this.start()
					}

					handleAkinator(this, locale, akinator, null, mutableListOf())
					return@executesDiscord
				}
			}

			reply(
					LorittaReply(
							"Deu ruim!"
					)
			)
		}
	}

	private fun getAkinatorEmbedBase(context: DiscordCommandContext): EmbedBuilder {
		return EmbedBuilder().apply {
			setTitle("<:akinator:383613256939470849> Akinator")
			setFooter(context.user.name, context.user.effectiveAvatarUrl)
			setColor(Color(20, 158, 255))
		}
	}

	suspend fun handleGuess(context: DiscordCommandContext, locale: BaseLocale, aw: AkinatorClient, currentMessage: Message?, guess: CharacterGuess, declinedGuesses: MutableList<Long>) {
		val descriptionBuilder = StringBuilder()
		descriptionBuilder.append("**")
		descriptionBuilder.append(locale["$LOCALE_PREFIX.isThisYourCharacter"])
		descriptionBuilder.append("**")
		descriptionBuilder.append('\n')
		descriptionBuilder.append('\n')
		descriptionBuilder.append(guess.name)
		if (guess.description != null) {
			descriptionBuilder.append('\n')
			descriptionBuilder.append('\n')
			descriptionBuilder.append("*")
			descriptionBuilder.append(guess.description)
			descriptionBuilder.append("*")
		}

		val builder = getAkinatorEmbedBase(context).apply {
			if (guess.absolutePicturePath != null)
				setThumbnail(guess.absolutePicturePath.toString())
			setDescription(descriptionBuilder.toString())
			setColor(Color(20, 158, 255))
		}

		val message = currentMessage?.edit(context.getUserMention(true), builder.build(), clearReactions = false) ?: context.sendMessage(context.getUserMention(true), builder.build())
		message.doReactions(
				"✅",
				"error:412585701054611458"
		)

		message.onReactionByAuthor(context) {
			it.reaction.removeReaction(it.user ?: return@onReactionByAuthor).await()

			when {
				it.reactionEmote.isEmote("✅") -> {
					val builder = getAkinatorEmbedBase(context).apply {
						setDescription(locale["$LOCALE_PREFIX.akinatorWon", Emotes.LORI_HAPPY])
						setColor(Color(20, 158, 255))
					}

					// ganhou
					currentMessage?.edit(context.getUserMention(true), builder.build()) ?: context.sendMessage(context.getUserMention(true), builder.build())
				}
				else -> {
					// continuar
					declinedGuesses.add(guess.id)
					handleAkinator(context, locale, aw, message, declinedGuesses)
				}
			}
		}
	}

	suspend fun handleAkinator(context: DiscordCommandContext, locale: BaseLocale, aw: AkinatorClient, currentMessage: Message?, declinedGuesses: MutableList<Long>) {
		currentMessage?.removeAllFunctions()

		val guesses = aw.retrieveGuesses()
		guesses.filter { it.probability >= CHARACTER_PROBABILITY && !declinedGuesses.contains(it.id) }.forEach { // Existe alguma guess válida, vamos usar!
			handleGuess(context, locale, aw, currentMessage!!, it, declinedGuesses)
			return
		}

		val currentQuestion = aw.currentStep ?: run {
			// Não existe mais perguntas! Vamos mostrar todas as guesses restantes... se o usuário não aceitar nenhuma, quer dizer que o player ganhou...
			val guesses = guesses.filter { declinedGuesses.contains(it.id) }

			if (guesses.isEmpty()) {
				val builder = getAkinatorEmbedBase(context).apply {
					setDescription(locale["$LOCALE_PREFIX.akinatorLost"])
					setColor(Color(20, 158, 255))
				}

				// ganhou
				currentMessage?.edit(context.getUserMention(true), builder.build()) ?: context.sendMessage(context.getUserMention(true), builder.build())
				return
			}

			guesses.forEach {
				handleGuess(context, locale, aw, currentMessage!!, it, declinedGuesses)
			}
			return
		}

		val progression = currentQuestion.progression

		var text = "[`"
		for (i in 0..100 step 10) {
			text += if (progression >= i) {
				"█"
			} else {
				"."
			}
		}
		text += "`]"

		val reactionInfo = """
            👍 ${locale["$LOCALE_PREFIX.answers.yes"]}
            👎 ${locale["$LOCALE_PREFIX.answers.no"]}
            ${Emotes.LORI_SHRUG} ${locale["$LOCALE_PREFIX.answers.dontKnow"]}
            <:lori_sorriso:556525532359950337> ${locale["$LOCALE_PREFIX.answers.probablyYes"]}
            <:lori_tristeliz:556524143281963008> ${locale["$LOCALE_PREFIX.answers.probablyNot"]}
        """.trimIndent()

		val builder = getAkinatorEmbedBase(context).apply {
			setThumbnail("${loritta.instanceConfig.loritta.website.url}assets/img/akinator_embed.png")
			setDescription("**${currentQuestion.question}**" + "\n\n$progression% $text\n\n$reactionInfo")
			setFooter(context.user.name + " • ${locale["$LOCALE_PREFIX.question", currentQuestion.step + 1]}", context.user.effectiveAvatarUrl)
			setColor(Color(20, 158, 255))
		}

		val message = currentMessage?.edit(context.getUserMention(true), builder.build(), clearReactions = false) ?: context.sendMessage(context.getUserMention(true), builder.build())

		message.onReactionByAuthor(context) {
			val answer = when {
				it.reactionEmote.isEmote("\uD83D\uDC4D") -> AkinatorAnswer.YES
				it.reactionEmote.isEmote("\uD83D\uDC4E") -> AkinatorAnswer.NO
				it.reactionEmote.isEmote("556525532359950337") -> AkinatorAnswer.PROBABLY
				it.reactionEmote.isEmote("556524143281963008") -> AkinatorAnswer.PROBABLY_NOT
				it.reactionEmote.isEmote("548639343141715978") -> AkinatorAnswer.DONT_KNOW
				else -> AkinatorAnswer.YES
			}

			aw.answerCurrentQuestion(answer)

			handleAkinator(context, locale, aw, message, declinedGuesses)
		}

		message.doReactions(
				"\uD83D\uDC4D", // Yes
				"\uD83D\uDC4E", // No
				"lori_shrug:548639343141715978", // Don't know
				"lori_sorriso:556525532359950337", // Probably yes
				"lori_tristeliz:556524143281963008" // Probably not
		)
	}
}