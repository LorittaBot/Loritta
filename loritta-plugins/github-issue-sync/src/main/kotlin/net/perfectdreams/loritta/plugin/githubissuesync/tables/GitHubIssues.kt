package net.perfectdreams.loritta.plugin.githubissuesync.tables

import org.jetbrains.exposed.dao.LongIdTable

object GitHubIssues : LongIdTable() {
    val channelId = long("channel").index()
    val messageId = long("message").index()
    val githubIssueId = long("github_issue").index()
}