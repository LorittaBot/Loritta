package com.mrpowergamerbr.loritta.commands.vanilla.discord

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.jar.Attributes

class BuildInfoTest {
    @Test
    fun `generate Jenkins build URL`() {
        val attribute = Attributes()
        attribute[BUILD_NUMBER] = "123"
        attribute[GITHUB_BUILD_ID] = ""

        val buildInfo = BuildInfo(attribute)

        assertThat( buildInfo.buildUrl()).isEqualTo("https://jenkins.perfectdreams.net/job/Loritta/123/")
    }
    @Test
    fun `generate Github build URL`() {
        val attribute = Attributes()
        attribute[BUILD_NUMBER] = "123"
        attribute[GITHUB_BUILD_ID] = "456"

        val buildInfo = BuildInfo(attribute)

        assertThat( buildInfo.buildUrl()).isEqualTo("https://github.com/LorittaBot/Loritta/actions/runs/456")
    }

    private companion object {
        val BUILD_NUMBER = Attributes.Name("Build-Number")
        val GITHUB_BUILD_ID= Attributes.Name("Github-Build-Id")
    }
}