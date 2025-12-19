package net.perfectdreams.dora

import kotlinx.serialization.Serializable

@Serializable
data class DoraConfig(
    val websiteUrl: String,
    val cookieDomain: String,
    val llamaCppBaseUrl: String,
    val gitScratchFolder: String,
    val jsPath: String?,
    val cssPath: String?,
    val git: GitConfig,
    val github: GitHubConfig,
    val discord: DiscordConfig,
) {
    @Serializable
    data class DiscordConfig(
        val applicationId: Long,
        val clientSecret: String,
    )

    @Serializable
    data class GitConfig(
        val author: String,
        val email: String
    )

    @Serializable
    data class GitHubConfig(
        val username: String,
        val personalAccessToken: String
    )
}