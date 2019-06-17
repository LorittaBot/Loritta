package net.perfectdreams.loritta.plugin.githubissuesync.listeners

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.plugin.githubissuesync.GitHubConfig
import net.perfectdreams.loritta.plugin.githubissuesync.GitHubIssueSync
import net.perfectdreams.loritta.plugin.githubissuesync.tables.GitHubIssues
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AddReactionListener(val config: GitHubConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val issueMutex = Mutex()
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!config.enabled)
			return

		if (event.channel.idLong in config.channels && (event.reactionEmote.isEmote("\uD83D\uDC4D") || event.reactionEmote.isEmote("\uD83D\uDC4E"))) { // Canal de sugestões
			GlobalScope.launch(loritta.coroutineDispatcher) {
				issueMutex.withLock {
					val alreadySent = transaction(Databases.loritta) {
						GitHubIssues.select { GitHubIssues.messageId eq event.messageIdLong }.count() != 0
					}

					if (alreadySent)
						return@withLock

					val message = event.channel.retrieveMessageById(event.messageId).await()

					if (!GitHubIssueSync.isSuggestionValid(message, config.requiredLikes))
						return@launch

					GitHubIssueSync.sendSuggestionToGitHub(message, config.repositoryUrl)
				}
			}
		}
	}
}