package com.mrpowergamerbr.loritta.commands.vanilla.discord

import java.util.jar.Attributes

class BuildInfo(val attribute: Attributes) {
    val githubBuildId = attribute[GITHUB_BUILD_ID] as String
    fun buildUrl(): String {
        return if (isJenkinsBuild())
            createJenkinsBuildURL()
        else
            createGithubBuildURL()
    }

    private fun createGithubBuildURL() = "$GITHUB_BUILD_BASE_URL$githubBuildId"
    private fun createJenkinsBuildURL(): String {
        val buildNumber = attribute[BUILD_NUMBER] as String
        return "$JENKINS_BUILD_BASE_URL$buildNumber/"
    }

    private fun isJenkinsBuild() = (githubBuildId as String) == ""

    private companion object {
        val BUILD_NUMBER = Attributes.Name("Build-Number")
        val GITHUB_BUILD_ID = Attributes.Name("Github-Build-Id")
        const val GITHUB_BUILD_BASE_URL = "https://github.com/LorittaBot/Loritta/actions/runs/"
        const val JENKINS_BUILD_BASE_URL = "https://jenkins.perfectdreams.net/job/Loritta/"
    }
}