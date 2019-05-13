package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object GitHubIssues : LongIdTable() {
    val messageId = long("message").index()
    val githubIssueId = long("github_issue").index()
}