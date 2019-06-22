package net.perfectdreams.loritta.plugin.githubissuesync

import com.fasterxml.jackson.annotation.JsonCreator

class GitHubConfig @JsonCreator constructor(
		val enabled: Boolean,
		val apiKey: String,
		val secretKey: String,
		val channels: List<Long>,
		val repositoryUrl: String,
		val requiredLikes: Int
)
