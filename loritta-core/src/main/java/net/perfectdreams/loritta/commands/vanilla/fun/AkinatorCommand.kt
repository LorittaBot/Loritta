package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.markozajc.akiwrapper.Akiwrapper
import com.markozajc.akiwrapper.AkiwrapperBuilder
import com.markozajc.akiwrapper.core.entities.Guess
import com.markozajc.akiwrapper.core.entities.Server
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.utils.Emotes
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.doReactions
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import java.awt.Color

class AkinatorCommand : LorittaCommand(arrayOf("akinator"), CommandCategory.FUN) {
    companion object {
        const val LOCALE_PREFIX = "commands.fun.akinator"
        const val CHARACTER_PROBABILITY = 0.85
    }

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.fun.akinator.description"]
    }

    fun getApiEndpoint(localeId: String): Server.Language {
        return when (localeId) {
            "default", "pt-pt", "pt-funk" -> Server.Language.PORTUGUESE
            "tr-tr" -> Server.Language.TURKISH
            "pl-pl" -> Server.Language.POLISH
            "ru-ru" -> Server.Language.RUSSIAN
            "nl-nl" -> Server.Language.DUTCH
            "kr-kr" -> Server.Language.KOREAN
            "ja-jp" -> Server.Language.JAPANESE
            "it-it" -> Server.Language.ITALIAN
            "he-il" -> Server.Language.HEBREW
            "fr-fr" -> Server.Language.FRENCH
            "es-es" -> Server.Language.SPANISH
            "ar-sa" -> Server.Language.ARABIC
            "ch-ch" -> Server.Language.CHINESE
            else -> Server.Language.ENGLISH
        }
    }

    fun getAkinatorEmbedBase(context: DiscordCommandContext): EmbedBuilder {
        return EmbedBuilder().apply {
            setTitle("<:akinator:383613256939470849> Akinator")
            setFooter(context.userHandle.name, context.userHandle.effectiveAvatarUrl)
            setColor(Color(20, 158, 255))
        }
    }

    suspend fun handleGuess(context: DiscordCommandContext, locale: BaseLocale, aw: Akiwrapper, currentMessage: Message?, guess: Guess, declinedGuesses: MutableList<Long>) {
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
            if (guess.image != null)
                setThumbnail(guess.image.toString())
            setDescription(descriptionBuilder.toString())
            setColor(Color(20, 158, 255))
        }

        val message = currentMessage?.edit(context.getAsMention(true), builder.build(), clearReactions = false) ?: context.sendMessage(context.getAsMention(true), builder.build()).handle
        message.doReactions(
                "✅",
                "error:412585701054611458"
        )

        message.onReactionAddByAuthor(context) {
            it.reaction.removeReaction(it.user).await()

            when {
                it.reactionEmote.isEmote("✅") -> {
                    val builder = getAkinatorEmbedBase(context).apply {
                        setDescription(locale["$LOCALE_PREFIX.akinatorWon", Emotes.LORI_HAPPY])
                        setColor(Color(20, 158, 255))
                    }

                    // ganhou
                    currentMessage?.edit(context.getAsMention(true), builder.build()) ?: context.sendMessage(context.getAsMention(true), builder.build()).handle
                }
                else -> {
                    // continuar
                    declinedGuesses.add(guess.idLong)
                    handleAkinator(context, locale, aw, message, declinedGuesses)
                }
            }
        }
    }

    suspend fun handleAkinator(context: DiscordCommandContext, locale: BaseLocale, aw: Akiwrapper, currentMessage: Message?, declinedGuesses: MutableList<Long>) {
        currentMessage?.removeAllFunctions()

        aw.guesses.filter { it.probability >= CHARACTER_PROBABILITY && !declinedGuesses.contains(it.idLong) }.forEach { // Existe alguma guess válida, vamos usar!
            handleGuess(context, locale, aw, currentMessage!!, it, declinedGuesses)
            return
        }

        val currentQuestion = aw.currentQuestion ?: run {
            // Não existe mais perguntas! Vamos mostrar todas as guesses restantes... se o usuário não aceitar nenhuma, quer dizer que o player ganhou...
            val guesses = aw.guesses.filter { declinedGuesses.contains(it.idLong) }

            if (guesses.isEmpty()) {
                val builder = getAkinatorEmbedBase(context).apply {
                    setDescription(locale["$LOCALE_PREFIX.akinatorLost"])
                    setColor(Color(20, 158, 255))
                }

                // ganhou
                currentMessage?.edit(context.getAsMention(true), builder.build()) ?: context.sendMessage(context.getAsMention(true), builder.build()).handle
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
            setFooter(context.userHandle.name + " • ${locale["$LOCALE_PREFIX.question", currentQuestion.step + 1]}", context.userHandle.effectiveAvatarUrl)
            setColor(Color(20, 158, 255))
        }

        val message = currentMessage?.edit(context.getAsMention(true), builder.build(), clearReactions = false) ?: context.sendMessage(context.getAsMention(true), builder.build()).handle

        message.onReactionAddByAuthor(context) {
            it.reaction.removeReaction(it.user).await()

            val answer = when {
                it.reactionEmote.isEmote("\uD83D\uDC4D") -> Akiwrapper.Answer.YES
                it.reactionEmote.isEmote("\uD83D\uDC4E") -> Akiwrapper.Answer.NO
                it.reactionEmote.isEmote("556525532359950337") -> Akiwrapper.Answer.PROBABLY
                it.reactionEmote.isEmote("556524143281963008") -> Akiwrapper.Answer.PROBABLY_NOT
                it.reactionEmote.isEmote("548639343141715978") -> Akiwrapper.Answer.DONT_KNOW
                else -> Akiwrapper.Answer.YES
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

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val aw = AkiwrapperBuilder()
                .setUserAgent(Constants.USER_AGENT)
                .setLocalization(getApiEndpoint(context.config.localeId))
                .setFilterProfanity(true)
                .setName(context.userHandle.name)
                .build()

        handleAkinator(context, locale, aw, null, mutableListOf())
    }
}