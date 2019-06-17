package net.perfectdreams.loritta.plugin.githubissuesync

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.plugin.githubissuesync.listeners.AddReactionListener
import net.perfectdreams.loritta.plugin.githubissuesync.tables.GitHubIssues
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class GitHubIssueSync : DiscordPlugin() {
    override fun onEnable() {
        val config = Constants.HOCON_MAPPER.readValue<GitHubConfig>(File(dataFolder, "config.conf"))

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					GitHubIssues
			)
		}

        registerEventListeners(
				AddReactionListener(config)
        )
    }

	companion object {
		suspend fun isSuggestionValid(message: Message, requiredCount: Int): Boolean {
			// Pegar o número de likes - dislikes
			val reactionCount = (message.reactions.firstOrNull { it.reactionEmote.isEmote("\uD83D\uDC4D") }?.retrieveUsers()?.await()?.filter { !it.isBot }?.size ?: 0) - (message.reactions.firstOrNull { it.reactionEmote.isEmote("\uD83D\uDC4E") }?.retrieveUsers()?.await()?.filter { !it.isBot }?.size ?: 0)
			return reactionCount >= requiredCount
		}

		fun sendSuggestionToGitHub(message: Message, repositoryUrl: String) {
			var issueTitle = message.contentStripped

			while (issueTitle.length > 50) {
				if (issueTitle.contains(":")) {
					issueTitle = issueTitle.split(":").first()
					continue
				}
				if (issueTitle.contains("\n")) {
					issueTitle = issueTitle.split("\n").first()
					continue
				}

				issueTitle = issueTitle.substringIfNeeded(0 until 77)
				break
			}

			val labels = mutableListOf<String>()

			if (message.contentRaw.contains("bug", true) || message.contentRaw.contains("problema", true)) {
				labels.add("\uD83D\uDC1E bug")
			}

			if (message.contentRaw.contains("adicionar", true) || message.contentRaw.contains("colocar", true) || message.contentRaw.contains("fazer", true)) {
				labels.add("✨ enhancement")
			}

			var suggestionBody = message.contentRaw

			message.emotes.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "<img src=\"${it.imageUrl}\" width=\"16\">")
			}
			message.mentionedUsers.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "`@${it.name}#${it.discriminator}` (`${it.id}`)")
			}
			message.mentionedChannels.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "`#${it.name}` (`${it.id}`)")
			}
			message.mentionedRoles.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "`@${it.name}` (`${it.id}`)")
			}
			// Encontrar links na sugestão
			val regex = Constants.HTTP_URL_PATTERN.toRegex()
			val regexMatch = regex.findAll(suggestionBody)

			// Agora vamos fazer com que imagens do imgur (e imagens do discord em forma de link) funcionem!
			// Se não for um link do imgur e imagem do discord em forma de link, então o link vai ficar em markdown
			regexMatch.forEach {
				if (it.value.contains("i.imgur")) {
					// Precisamos substituir o sufixo do link, caso seja um gif, para a imagem seja valída
					if (!it.value.endsWith(".gifv"))
						suggestionBody = suggestionBody.replace(it.value, "![${it.value}](${it.value})")
					else {
						val link = it.value.replace(".gifv", ".gif")
						suggestionBody = suggestionBody.replace(link, "![$link]($link)")
					}
				}
				else if (it.value.contains("cdn.discordapp.com") && (!it.value.contains("/emojis/")))
					suggestionBody = suggestionBody.replace(it.value, "![${it.value}](${it.value})")
				else
					if (!it.value.contains("cdn.discordapp.com/emojis/"))
						suggestionBody = suggestionBody.replace(it.value, "`${it.value}`")
			}

			val body = """<img width="64" align="left" src="${message.author.effectiveAvatarUrl}">
    |
    |**Sugestão de `${message.author.name}#${message.author.discriminator}` (`${message.author.id}`)**
    |**ID da Mensagem: `${message.channel.id}-${message.idLong}`**
    |
    |<hr>
    |
    |$suggestionBody
    |
    |${message.attachments.filter { !it.isImage }.joinToString("\n", transform = { it.url })}
    |${message.attachments.filter { it.isImage }.joinToString("\n", transform = { "![${it.url}](${it.url})" })}
""".trimMargin()

			val request = HttpRequest.post("$repositoryUrl/issues")
					.header("Authorization", "token ${loritta.config.github.apiKey}")
					.accept("application/vnd.github.symmetra-preview+json")
					.send(
							gson.toJson(
									jsonObject(
											"title" to issueTitle,
											"body" to body,
											"labels" to labels.toJsonArray()
									)
							)
					)

			val json = jsonParser.parse(request.body())

			val issueId = json["number"].long
			transaction(Databases.loritta) {
				GitHubIssues.insert {
					it[messageId] = message.idLong
					it[githubIssueId] = issueId
				}
			}
		}
	}
}